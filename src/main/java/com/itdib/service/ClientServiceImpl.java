package com.itdib.service;

import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.itdib.dto.Dsource;
import com.itdib.dto.OrgDTO;
import com.itdib.multitenancy.MultiTenancyJpaConfiguration;
import com.itdib.multitenancy.TenantContextHolder;
import com.zaxxer.hikari.HikariDataSource;

@Service
public class ClientServiceImpl implements ClientService {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private Map<String, DataSource> dataSourcesMtApp;

	@Override
	public String register(OrgDTO orgDTO) {
		boolean flag = false;
		HttpHeaders headers = new HttpHeaders();
		ResponseEntity<Dsource[]> response = restTemplate.exchange("http://localhost:2021/base/getAll", HttpMethod.GET,
				new HttpEntity<Object>(headers), Dsource[].class);

		if (response.getBody() != null) {
			Dsource[] dsList = response.getBody();
			if (dsList != null && dsList.length > 0) {
				for (Dsource d : dsList) {
					if (d.getTenantId().equalsIgnoreCase(orgDTO.getInstanceName())) {
						flag = true;
					}
				}
			}
		}

		if (flag) {
			return "tenant already exists!";
		}

		String url = "http://localhost:2021/base/addsource/" + orgDTO.getInstanceName();
		try {
			restTemplate.postForEntity(url, orgDTO, String.class);
		} catch (Exception e) {
		}

		DataSourceBuilder<?> factory = DataSourceBuilder.create(MultiTenancyJpaConfiguration.class.getClassLoader())
				.url("jdbc:mysql://localhost:3306/" + orgDTO.getInstanceName() + "?useSSL=false").username("root")
				.password("WitchMYSQL").driverClassName("com.mysql.cj.jdbc.Driver");
		HikariDataSource ds = (HikariDataSource) factory.build();
		ds.setKeepaliveTime(40000);
		ds.setMinimumIdle(1);
		ds.setMaxLifetime(45000);
		ds.setIdleTimeout(35000);
		dataSourcesMtApp.put(orgDTO.getInstanceName(), ds);
		TenantContextHolder.setTenantId(orgDTO.getInstanceName());
		return "tenant registered successfully!";
	}

}

package com.itdib.multitenancy;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.hibernate.MultiTenancyStrategy;
import org.hibernate.cfg.Environment;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

import com.itdib.dto.Dsource;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableTransactionManagement
@ComponentScan("com.itdib")
@Lazy
public class MultiTenancyJpaConfiguration {

	@Autowired
	private JpaProperties jpaProperties;

	@Autowired
	private RestTemplate rs;

	@Primary
	@Bean(name = "dataSourcesMtApp")
	public Map<String, DataSource> dataSourcesMtApp() {

		HttpHeaders headers = new HttpHeaders();

		ResponseEntity<Dsource[]> response =
				rs.exchange(
						"http://localhost:2021/base/getAll", HttpMethod.GET, new HttpEntity<Object>(headers),
						Dsource[].class);

		Dsource[] dsList = response.getBody();
		
		Map<String, DataSource> result = new HashMap<>();
		for (Dsource source :dsList) {
			DataSourceBuilder<?> factory = DataSourceBuilder.create().url(source.getUrl())
					.username(source.getUsername()).password(source.getPassword())
					.driverClassName(source.getDriverClassName());
			
			HikariDataSource ds = (HikariDataSource)factory.build();
	        ds.setKeepaliveTime(40000);
	        ds.setMinimumIdle(1);
	        ds.setMaxLifetime(45000);
	        ds.setIdleTimeout(35000);
			result.put(source.getTenantId(), ds);
		}
		
		return result;
	}

	@Bean
	public MultiTenantConnectionProvider multiTenantConnectionProvider() {
		return new DataSourceBasedMultiTenantConnectionProviderImpl();
	}

	@Bean
	public CurrentTenantIdentifierResolver currentTenantIdentifierResolver() {
		return new CurrentTenantIdentifierResolverImpl();
	}

	@Bean(name="entityManagerFactoryBean")
	public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean(
			AbstractDataSourceBasedMultiTenantConnectionProviderImpl multiTenantConnectionProvider,
			CurrentTenantIdentifierResolver currentTenantIdentifierResolver) {

		Map<String, Object> hibernateProps = new LinkedHashMap<>();
		hibernateProps.putAll(this.jpaProperties.getProperties());
		hibernateProps.put(Environment.MULTI_TENANT, MultiTenancyStrategy.DATABASE);
		hibernateProps.put(Environment.MULTI_TENANT_CONNECTION_PROVIDER, multiTenantConnectionProvider);
		hibernateProps.put(Environment.MULTI_TENANT_IDENTIFIER_RESOLVER, currentTenantIdentifierResolver);
		hibernateProps.put("hibernate.hbm2ddl.auto", "update");
		hibernateProps.put("hibernate.dialact", "org.hibernate.dialect.MySQL8Dialect");
		LocalContainerEntityManagerFactoryBean result = new LocalContainerEntityManagerFactoryBean();
		result.setPackagesToScan("com.itdib");
		result.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
		result.setJpaPropertyMap(hibernateProps);

		return result;
	}

	@Bean
	@Primary
	public EntityManagerFactory entityManagerFactory(LocalContainerEntityManagerFactoryBean entityManagerFactoryBean) {
		return entityManagerFactoryBean.getObject();
	}

	@Bean
	public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
		return new JpaTransactionManager(entityManagerFactory);
	}
}
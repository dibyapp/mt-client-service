package com.itdib.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.itdib.dto.OrgDTO;
import com.itdib.entity.Product;
import com.itdib.multitenancy.TenantContextHolder;
import com.itdib.repository.ProductRepository;
import com.itdib.service.ClientService;


@RestController
public class ClientController {
	
	@Autowired
	private ClientService clientService;
	
	@Autowired
	private ProductRepository productRepository;
	
	@PostMapping("/registerOrg")
	public String registerOrg(@RequestBody OrgDTO orgDTO)
	{
		return clientService.register(orgDTO);
	}
	
	@PostMapping("/getProducts")
	public List<Product> getProducts(HttpServletRequest request){
		TenantContextHolder.setTenantId(obtainTenantFromSubdomain(request));
		return productRepository.findAll();
	}
	
	
    private String obtainTenantFromSubdomain(HttpServletRequest request) {
        return request.getServerName().split("\\.")[0];
    }

}

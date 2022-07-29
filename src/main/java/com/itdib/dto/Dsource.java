package com.itdib.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Dsource {
	
	int id;
	
	String tenantId;
	
	String url;
	
	String username;
	
	String password;
	
	String driverClassName;

}

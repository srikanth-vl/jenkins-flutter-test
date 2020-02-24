package com.vassarlabs.proj.uniapp.api.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetails {
	
	@JsonProperty("name")
	String userName;
	
//	@JsonProperty("department")
//	String department;
	
	@JsonProperty("designation")
	String designation;
	
	@JsonProperty("email")
	String emailId;
	
	@JsonProperty("mobile")
	long mobileNumber;
	
	@JsonProperty("zone")
	String zone;
}

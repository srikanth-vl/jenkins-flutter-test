package com.vassarlabs.proj.uniapp.api.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class LoginRequestDetails 
	extends AppRequestObject {
	
	String userId;
	String password;
}

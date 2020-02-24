package com.vassarlabs.proj.uniapp.api.pojo;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ApiRequestObject 
	extends AppRequestObject {
	
	@JsonProperty("user_id")
	String userId;
	
	@JsonProperty("token")
	UUID tokenId;
}

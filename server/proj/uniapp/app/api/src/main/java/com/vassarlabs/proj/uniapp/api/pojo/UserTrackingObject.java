package com.vassarlabs.proj.uniapp.api.pojo;

import java.util.List;
import java.util.UUID;

import com.vassarlabs.proj.uniapp.enums.APITypes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class UserTrackingObject {
	
	UUID superAppId;
	UUID appId;
	String userId;
	UUID tokenId;
	APITypes apiType;
	String api;
	String requestObj;
	List<String> errorsList;
	boolean isRequestSuccessful;
	long insertTimeStamp;
}

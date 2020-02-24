package com.vassarlabs.proj.uniapp.dashboard.api.pojo;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SuperAppAnalyticsData {
	@JsonProperty("super_app_id")
	UUID superAppId;
	
	@JsonProperty("super_app_name")
	String superAppName;
	
	@JsonProperty("image_url")
	String imageUrl;
	
	@JsonProperty("app_analytics")
	List<AppAnalyticsData> appAnalyticsData;
	 
	@JsonProperty("registered_users")
	int registeredUsers;
	
	@JsonProperty("users_logged_in")
	int usersLoggedIn;
	
	@JsonProperty("users_never_logged_in")
	int usersNeverLoggedIn;
	
	@JsonProperty("installations")
	String installations;
	
	//TODO: Add user data if necessary, else take from firebase analytics
}

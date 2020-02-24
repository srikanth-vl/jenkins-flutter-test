package com.vassarlabs.proj.uniapp.api.pojo;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class BboxContainerObject {
	
	@JsonProperty("super_app")
	UUID superAppId;
	@JsonProperty("app_id")
	UUID appId;
	@JsonProperty("user_id")
	String userId;
	@JsonProperty("north")
	Double north;
	@JsonProperty("south")
	Double south;
	@JsonProperty("east")
	Double east;
	@JsonProperty("west")
	Double west;
}

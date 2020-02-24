package com.vassarlabs.proj.uniapp.upload.pojo;

import java.util.List;
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
public class UserMetaDataRequest {
	
	@JsonProperty("super_app")
	UUID superAppId;
	
	@JsonProperty("user_data_list")
	List<UserMetaDataList> userMetaData;
}

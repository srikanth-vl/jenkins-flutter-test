package com.vassarlabs.proj.uniapp.api.pojo;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MediaSubmissionLogRequestObject
		extends AppRequestObject {

	
	@JsonProperty("app_id")
	UUID appid;
	
	@JsonProperty("start_ts")
	long startTs;
	
	@JsonProperty("end_ts")
	long endTs;
	
}

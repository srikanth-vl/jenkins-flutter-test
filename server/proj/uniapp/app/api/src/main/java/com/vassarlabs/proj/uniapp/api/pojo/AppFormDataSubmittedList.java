package com.vassarlabs.proj.uniapp.api.pojo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString
public class AppFormDataSubmittedList 
	extends AppFormRequestObject {
	
	@JsonProperty("submit_data")
	List<AppFormData> appFormDataList;
	
	@JsonProperty("relay_retries")
	Integer relayRetries;
	
	@JsonIgnore
	public boolean isEmpty() {
		if (appFormDataList == null
				|| appFormDataList.isEmpty()) {
			return true;
		}
		return false;
	}
}

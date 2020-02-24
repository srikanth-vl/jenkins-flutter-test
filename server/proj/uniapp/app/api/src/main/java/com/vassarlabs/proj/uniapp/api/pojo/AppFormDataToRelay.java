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
public class AppFormDataToRelay 
	extends AppFormRequestObject {
	
	@JsonProperty("submit_data")
	List<AppFormData> appFormDataList;
	
	@JsonIgnore
	public boolean isEmpty() {
		if (appFormDataList == null
				|| appFormDataList.isEmpty()) {
			return true;
		}
		return false;
	}
}

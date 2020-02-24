package com.vassarlabs.proj.uniapp.fileupload.object;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class KeyFields 
	extends KeyObject {
	
	@JsonProperty("masterDataName")
	String masterDataName;
}

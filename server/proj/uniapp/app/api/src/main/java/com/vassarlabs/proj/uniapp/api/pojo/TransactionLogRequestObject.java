package com.vassarlabs.proj.uniapp.api.pojo;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TransactionLogRequestObject 
	extends AppFormRequestObject {
	
	@JsonProperty("fields")
	List<String> fields;
	
	@JsonProperty("limit")
	int limit;
	
	@JsonIgnore
	@JsonProperty("token_id")
	UUID tokenId;
	
	@JsonProperty("page_no")
	int pageNo;
}

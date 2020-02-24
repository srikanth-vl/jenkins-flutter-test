package com.vassarlabs.proj.uniapp.api.pojo;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EntityGroup {

	@JsonProperty("entity_group_type")
	String type;
	
	@JsonProperty("entities")
	List<Entity> entities;
	
}

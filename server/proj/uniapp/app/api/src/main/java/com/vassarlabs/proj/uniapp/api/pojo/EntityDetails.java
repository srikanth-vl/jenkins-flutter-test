package com.vassarlabs.proj.uniapp.api.pojo;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntityDetails {
	UUID entityId;
	String entityName;
}

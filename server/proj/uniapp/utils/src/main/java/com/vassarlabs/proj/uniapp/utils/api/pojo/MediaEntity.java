package com.vassarlabs.proj.uniapp.utils.api.pojo;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MediaEntity {

public UUID mediaUUID;
public double latitude;
public double longitude;
		
}

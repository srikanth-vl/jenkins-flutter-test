package com.vassarlabs.proj.uniapp.api.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationCoordinateObject implements Comparable<Object>{
	
	@JsonProperty("latitude")
	Double lat;
	@JsonProperty("longitude")
	Double lon;
	
	@Override 
    public int compareTo(Object o) {
		LocationCoordinateObject f = (LocationCoordinateObject) o; 
		Double distanceNew = (this.lat* this.lat+ this.lon* this.lon) ;
		Double current =  this.lat* this.lat+ this.lon* this.lon;
		if (distanceNew >current) return 1;
		if (distanceNew  == current) return 0;
		if (distanceNew < current) return -1 ;
		return 0;
    }
	
}

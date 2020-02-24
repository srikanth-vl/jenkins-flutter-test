package com.vassarlabs.prod.common.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class NumberUtils {

	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
	/**
	 * @returns decimal point value of a decimal number example 0.5 for value 32.5 
	 * 
	 * */
	public static double getDecimalPoints(double value) {
	    
	    return value - Math.floor(value);
	}
	
	/**
	 * This method convert a decimal number into its near by floor or ceiling or in 0.5
	 * 
	 * for example if your no is 34.1/34.2 then its near by no is 34 which is floor(34.1/34.2)
	 * And if you have 34.3/34.4 then near by range is 34.5 - in case we have range = 0.5
	 * if we have 34.7 which is more then range 0.5 then near by no will be ceiling of number i.e. 35
	 * 
	 * */
	public static double convertIntoNearByRange(double value) {
		
		double returnValue = 0.0;

		if (0.5 == getDecimalPoints(value)) {
			returnValue = value;	
		} else if (0.3 >= getDecimalPoints(value)) {
			returnValue = Math.floor(value) + 0.5;
		} else if (0.5 > getDecimalPoints(value)) {
			returnValue = Math.ceil(value);
		} else {
			returnValue = Math.floor(value);
		}

		return returnValue;
	}
}


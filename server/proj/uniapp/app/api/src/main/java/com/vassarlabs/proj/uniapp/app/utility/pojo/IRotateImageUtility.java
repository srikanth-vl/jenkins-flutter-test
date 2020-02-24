package com.vassarlabs.proj.uniapp.app.utility.pojo;

import java.io.IOException;

import org.imgscalr.Scalr.Rotation;

import com.vassarlabs.proj.uniapp.api.pojo.FormMediaValue;

import javaxt.io.Image;


public interface IRotateImageUtility {
	
	/**
	 * Rotate Image if Orientation property exist and has rotation and upload image with correct orientation to AWSS3 Bucket 
	 * 
	 * @param mediaValue
	 * @throws IOException
	 */
	public void rotateImage(FormMediaValue mediaValue) throws IOException;
	
	/**
	 * Return true if Orientation property exist and has rotation to  be rectify otherwise false
	 * 
	 * @param image
	 */
	public boolean isRotated(Image image);
	
	/**
	 * Return Scalr.Rotation enum if Orientation property exist and has rotation to  be rectify otherwise null object
	 * 
	 * @param image
	 */
	public Rotation getOrientation(Image image);
	public void rotateImageForPilot(FormMediaValue mediaValue, Integer orientationdegree) throws IOException ;
	
}

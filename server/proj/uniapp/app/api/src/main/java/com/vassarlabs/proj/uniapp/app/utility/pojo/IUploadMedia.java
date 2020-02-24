package com.vassarlabs.proj.uniapp.app.utility.pojo;

import java.io.IOException;

import com.vassarlabs.proj.uniapp.api.pojo.FormMediaValue;


public interface IUploadMedia {
	
	/**
	 * Upload Media 
	 * 
	 * @param mediaValue
	 * @throws IOException
	 */
	public boolean upload(FormMediaValue mediaValue) throws InterruptedException;
	
}

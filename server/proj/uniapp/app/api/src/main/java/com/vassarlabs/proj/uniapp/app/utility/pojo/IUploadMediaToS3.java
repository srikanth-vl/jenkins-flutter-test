package com.vassarlabs.proj.uniapp.app.utility.pojo;

import java.io.IOException;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.vassarlabs.proj.uniapp.api.pojo.FormMediaValue;


public interface IUploadMediaToS3 {
	
	/**
	 * upload Media to S3 Bucket
	 * 
	 * @param mediaValue
	 * @param mediaData
	 * @param metadata
	 * @throws IOException
	 */
	public boolean uploadToS3(String fileName, FormMediaValue mediaData, ObjectMetadata metadata) throws IOException;
	

	
}

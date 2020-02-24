package com.vassarlabs.proj.uniapp.app.utility.pojo;

import java.io.IOException;

import com.vassarlabs.proj.uniapp.api.pojo.FormMediaValue;

public interface IDownloadMediaFromS3 {
	
	/**
	 * IOException Media from S3 Bucket
	 * 
	 * @param mediaValue	
	 * throws IOException
	 */
	public byte[] downloadFromS3(FormMediaValue mediaValue) throws IOException ;
	

	
}

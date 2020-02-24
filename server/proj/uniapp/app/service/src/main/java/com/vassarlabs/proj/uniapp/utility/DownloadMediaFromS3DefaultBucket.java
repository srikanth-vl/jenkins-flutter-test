package com.vassarlabs.proj.uniapp.utility;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vassarlabs.awss3.service.api.IAWSS3Service;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.proj.uniapp.api.pojo.FormMediaValue;
import com.vassarlabs.proj.uniapp.app.utility.pojo.IDownloadMediaFromS3;

@Component("DownloadMediaFromS3DefaultBucket")
public class DownloadMediaFromS3DefaultBucket implements IDownloadMediaFromS3 {
	@Autowired private IVLLogService logFactory;
	private IVLLogger logger;

	@PostConstruct
	public void init() {
		logger = logFactory.getLogger(getClass());
	}

	@Autowired 
	private IAWSS3Service awsS3Service;

	@Override
	public byte[] downloadFromS3(FormMediaValue mediaValue) throws IOException{
		logger.info("Downloading image from s3 bucket uniapp-test:: ImagePath - " + mediaValue.getMediaPath());
		byte[] mediaByteArray = awsS3Service.downloadS3Object(mediaValue.getMediaPath());
		return mediaByteArray;
	}
}

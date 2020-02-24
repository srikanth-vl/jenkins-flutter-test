package com.vassarlabs.proj.uniapp.utility;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.proj.uniapp.api.pojo.FormMediaValue;
import com.vassarlabs.proj.uniapp.app.projectform.insert.service.AWSPropsService;
import com.vassarlabs.proj.uniapp.app.utility.pojo.IUploadMedia;
import com.vassarlabs.proj.uniapp.app.utility.pojo.IUploadMediaToS3;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;

@Component
public class UploadMedia implements IUploadMedia {

	@Autowired private IVLLogService logFactory;
	@Autowired private AWSPropsService awsPropsService;
// for Default bucket
	@Qualifier("UploadMediaToS3DefaultBucket")
// for specific bucket (production deployment)
//	@Qualifier("UploadMediaToS3ProductionBucket")
	@Autowired private IUploadMediaToS3 uploadMediaToS3Utility;

	private IVLLogger logger;
	
	@PostConstruct
	public void init() {
		logger = logFactory.getLogger(getClass());
	}
	
	@Override
	public boolean upload(FormMediaValue mediaData) throws InterruptedException {
		int retryCount = 0;
		
		while(true) {
			retryCount++;
			try {
				String fileName = generateKey(mediaData);
				ObjectMetadata metadata = awsPropsService.getObjectMetaData(mediaData);
				if(fileName == null) {
					logger.error("Cannot upload to AWS S3 - Name of the file is NULL :: for super App ID- " + mediaData.getSuperAppId() + " App ID - " + mediaData.getAppId() + " Project ID -" + mediaData.getProjectId()
					+ " Media UUID - " + mediaData.getMediaUUID() + " Media Type - " + mediaData.getMediaType() + " Media Size- " + mediaData.getMediaContent().length);
					return false;
				}
				if(!uploadMediaToS3Utility.uploadToS3(fileName, mediaData, metadata)) {
					return false;
				}
				// Set keyName of object uploaded
				mediaData.setMediaPath(fileName);
				return true;
			} catch(IOException e) {
				if(retryCount >= CommonConstants.MAX_RETRIES) {
					logger.error("Problem uploading media to AWS S3 Server :: for super App ID- " + mediaData.getSuperAppId() + " App ID - " + mediaData.getAppId() + " Project ID -" + mediaData.getProjectId()
					+ " Media UUID - " + mediaData.getMediaUUID() + " Media Type - " + mediaData.getMediaType() + " Media Size- " + mediaData.getMediaContent().length);
					return false;
				}
				logger.error("Problem uploading media to AWS S3 Server :: for super App ID- " + mediaData.getSuperAppId() + " App ID - " + mediaData.getAppId() + " Project ID -" + mediaData.getProjectId()
				+ " Media UUID - " + mediaData.getMediaUUID() + " Media Type - " + mediaData.getMediaType() + " Media Size- " + mediaData.getMediaContent().length + " retrying for " + retryCount + " ....");
			}
			if(retryCount >= CommonConstants.MAX_RETRIES) {
				logger.error("Uncaught exception while uploading to S3: retrying for " + retryCount + " ...");
				return false;
			}
			Thread.sleep(CommonConstants.THREAD_SLEEP_TIME);
		}
	}

	private String generateKey(FormMediaValue formData) {
		return formData.getAppId()
				+ CommonConstants.KEY_DELIMITER + formData.getProjectId()
				+ CommonConstants.KEY_DELIMITER + formData.getMediaUUID();
	}
}

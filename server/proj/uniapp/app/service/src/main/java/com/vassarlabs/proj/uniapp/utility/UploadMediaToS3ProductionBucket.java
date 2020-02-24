package com.vassarlabs.proj.uniapp.utility;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.awss3.props.AWSS3Props;
import com.vassarlabs.awss3.service.AWSS3ClientService;
import com.vassarlabs.awss3.service.api.IAWSS3Service;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.proj.uniapp.api.pojo.FormMediaValue;
import com.vassarlabs.proj.uniapp.app.utility.pojo.IUploadMediaToS3;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.SuperApplicationData;
import com.vassarlabs.proj.uniapp.crud.service.SuperAppDataCrudService;

@Component("UploadMediaToS3ProductionBucket")
public class UploadMediaToS3ProductionBucket implements IUploadMediaToS3 {

	@Autowired private IVLLogService logFactory;
	@Autowired 
	private IAWSS3Service awsService;

	@Autowired 
	private SuperAppDataCrudService superAppDataCrudService;
	
	private IVLLogger logger;
	
	@Autowired
	private AWSS3Props awss3Props;

	@Autowired
	private AWSS3ClientService awss3ClientService;

	@PostConstruct
	public void init() {
		logger = logFactory.getLogger(getClass());
	}

	private ObjectMapper objectMapper = new ObjectMapper();
	
	public boolean uploadToS3(String fileName, FormMediaValue mediaData, ObjectMetadata metadata) throws IOException {
		SuperApplicationData superAppData = superAppDataCrudService.findLatestVersion(mediaData.getSuperAppId());
		String awsProperties = superAppData.getAwsProperties();
		if(awsProperties == null || awsProperties.isEmpty()) {
			logger.error("No AWS properties set, cannot load object to S3");
			return false;
		}
		logger.info("AWS properties found");
		awss3Props = objectMapper.readValue(awsProperties, AWSS3Props.class);
		AmazonS3 s3Client = awss3ClientService.getAWSS3Client(awss3Props);
		logger.info("AWS client created");
		awsService.setS3Client(s3Client);
		logger.info("AWS S3 Properties set-" + awss3Props);
		awsService.uploadS3Object(fileName, awss3Props.getBucketName(), mediaData.getMediaContent(), metadata);
		return true;
	}

	
}

package com.vassarlabs.proj.uniapp.utility;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.awss3.props.AWSS3Props;
import com.vassarlabs.awss3.service.AWSS3ClientService;
import com.vassarlabs.awss3.service.api.IAWSS3Service;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.MapFileData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.SuperApplicationData;
import com.vassarlabs.proj.uniapp.constants.MapConstants;
import com.vassarlabs.proj.uniapp.constants.SuperAppAndAppIdsConstants;
import com.vassarlabs.proj.uniapp.crud.service.SuperAppDataCrudService;

@Component
public class UploadMapZipToS3ProductionBucket {

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
	
	public boolean uploadToS3(MapFileData mapData, UUID superApp, String outputFilePath) throws IOException {
		SuperApplicationData superAppData = superAppDataCrudService.findLatestVersion(superApp);
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
		s3Client.putObject(new PutObjectRequest(awss3Props.getBucketName(), "mapfiles/"+mapData.getMapFileName(), 
				new File(outputFilePath))
				.withCannedAcl(CannedAccessControlList.PublicRead));
		logger.info("AWS S3 Properties set-" + awss3Props);
		return true;
	}
	public Long ifExists(String name, UUID superApp) throws  IOException {
		SuperApplicationData superAppData = superAppDataCrudService.findLatestVersion(superApp);
		String awsProperties = superAppData.getAwsProperties();
		if(awsProperties == null || awsProperties.isEmpty()) {
			logger.error("No AWS properties set, cannot load object to S3");
			return null;
		}
		logger.info("AWS properties found");
		awss3Props = objectMapper.readValue(awsProperties, AWSS3Props.class);
		AmazonS3 s3Client = awss3ClientService.getAWSS3Client(awss3Props);
		logger.info("AWS client created");
		awsService.setS3Client(s3Client);
	    try {
	    	ObjectMetadata obj = s3Client.getObjectMetadata(awss3Props.getBucketName(), "mapfiles/"+ name); 
	    	logger.info("File :: " +  "mapfiles/"+ name + " Found in S3") ;
	    	return obj.getContentLength();
	    } catch(AmazonServiceException e) {
	    	logger.info("File :: " +  "mapfiles/"+ name + " Not Found in S3") ;
	        return null;
	    }
	}

	
}

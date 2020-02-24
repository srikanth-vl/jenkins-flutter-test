package com.vassarlabs.proj.uniapp.app.projectform.insert.service;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.proj.uniapp.api.pojo.FormMediaValue;
import com.vassarlabs.proj.uniapp.enums.MediaTypes;

@Service
public class AWSPropsService {
	
	@Autowired private IVLLogService logFactory;

	private IVLLogger logger;

	@PostConstruct
	public void init() {
		logger = logFactory.getLogger(getClass());
	}

	public ObjectMetadata getObjectMetaData(FormMediaValue mediaValue) {
		
		ObjectMetadata metadata = new ObjectMetadata();
		if(mediaValue.getMediaType().equalsIgnoreCase(MediaTypes.IMAGE.getValue())) {
			setImageContentTypes(mediaValue.getMediaFileExtension(), metadata);
		} else if(mediaValue.getMediaType().equalsIgnoreCase(MediaTypes.VIDEO.getValue())) {
			setVideoContentTypes(mediaValue.getMediaFileExtension(), metadata);
		} else {
			logger.debug("Invalid media Type obtained - " + mediaValue.getMediaType());
		}
		metadata.setContentLength(mediaValue.getMediaContent().length);
		metadata.setContentDisposition("inline");
		return metadata;
	}

	private void setVideoContentTypes(String mediaFileExtension, ObjectMetadata metadata) {
		switch(mediaFileExtension) {
		case "wm":
			metadata.setContentType("video/x-ms-wm");
			break;
		case "wmv":
			metadata.setContentType("video/x-ms-wmv");
			break;
		case "wmx":
			metadata.setContentType("video/x-ms-wmv");
			break;
		case "mp4v":
		case "mp4":
			metadata.setContentType("video/mp4");
			break;
		case "mpa" :
		case "mpe":
		case "mpeg":
		case "mpg" :
		case "mpv2":
			metadata.setContentType("video/mpeg");
			break;
		default :
			logger.error("Unsupported media Extension to upload to S3 :: " + mediaFileExtension);	
		}
		
	}

	private void setImageContentTypes(String mediaFileExtension, ObjectMetadata metadata) {
		switch(mediaFileExtension) {
		case "png" :
			metadata.setContentType("image/png");
			break;
		case "jpeg" :
		case "jpe" :
		case "jpg" :
			metadata.setContentType("image/jpeg");
			break;
		case "svg" :
			metadata.setContentType("image/svg+xml");
			break;
		case "tif" :
		case "tiff" :
			metadata.setContentType("image/tiff");
			break;
		default:
			logger.error("Unsupported media Extension to upload to S3 :: " + mediaFileExtension);
		}
	}

}

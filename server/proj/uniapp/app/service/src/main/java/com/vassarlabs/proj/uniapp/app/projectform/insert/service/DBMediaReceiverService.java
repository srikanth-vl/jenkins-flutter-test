package com.vassarlabs.proj.uniapp.app.projectform.insert.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Rotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.CassandraWriteTimeoutException;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.exceptions.WriteFailureException;
import com.datastax.driver.core.exceptions.WriteTimeoutException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.utils.err.DataNotFoundException;
import com.vassarlabs.common.utils.err.TokenExpiredException;
import com.vassarlabs.common.utils.err.TokenNotFoundException;
import com.vassarlabs.common.utils.err.ValidationException;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.proj.uniapp.api.pojo.FormMediaValue;
import com.vassarlabs.proj.uniapp.api.pojo.MediaMetaData;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;
import com.vassarlabs.proj.uniapp.api.pojo.UserTrackingObject;
import com.vassarlabs.proj.uniapp.app.insert.service.IMediaReceiverService;
import com.vassarlabs.proj.uniapp.app.utility.pojo.IRotateImageUtility;
import com.vassarlabs.proj.uniapp.app.utility.pojo.IUploadMedia;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ImageGeotagData;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.crud.service.ImageGeotagDataCrudService;
import com.vassarlabs.proj.uniapp.enums.APITypes;
import com.vassarlabs.proj.uniapp.enums.MediaTypes;

import javaxt.io.Image;

/**
 * 1. Uploads media to S3
 * 2. Stores media data in DB
 * 
 * @author nidhi
 *
 */
@Component("DBMediaReceiverService")
public class DBMediaReceiverService 
	implements IMediaReceiverService {

	@Autowired 
	private ImageGeotagDataCrudService insertCrudService;
	
	@Autowired private IVLLogService logFactory;
	

	@Autowired private IRotateImageUtility rotateImageUtility;
	private IVLLogger logger;
	
	@Autowired private IUploadMedia uploadMediaUtility;
	@PostConstruct
	public void init() {
		logger = logFactory.getLogger(getClass());
	}

	private ObjectMapper objectMapper = new ObjectMapper();
	
	
	public ServiceOutputObject execute(FormMediaValue formData)
			throws TokenNotFoundException, TokenExpiredException, DataNotFoundException, ValidationException, JsonProcessingException, InterruptedException {

		ServiceOutputObject outputObject = new ServiceOutputObject();

		int retryCount = 0;
		while(true) {
			try {
				
					//rotate image before uploading to s3
				Long startTs = System.currentTimeMillis();
				Rotation orientation = null;
				Map<String, String> otherProperties = formData.getOtherParams();
				if(formData!= null && (formData.getMediaType().equalsIgnoreCase(MediaTypes.IMAGE.getValue()) || formData.getMediaFileExtension().equalsIgnoreCase("jpeg")|| formData.getMediaFileExtension().equalsIgnoreCase("jpg")|| formData.getMediaFileExtension().equalsIgnoreCase("png"))) {
					if( otherProperties!= null && !otherProperties.isEmpty() && otherProperties.get(CommonConstants.IMAGE_ORIENTATION_KEY)!=null) {
						orientation = getOrientation(otherProperties.get(CommonConstants.IMAGE_ORIENTATION_KEY));
					} else {
						Image img = new Image(formData.getMediaContent());
						orientation = rotateImageUtility.getOrientation(img);
					}
				}
				if(orientation !=  null)
				{  
					try {
						InputStream in = new ByteArrayInputStream(formData.getMediaContent());
						BufferedImage bImageFromConvert = ImageIO.read(in);
						BufferedImage rotatedImage = Scalr.rotate(bImageFromConvert, orientation);
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						ImageIO.write(rotatedImage, formData.getMediaFileExtension(), bos );
						formData.setMediaContent(bos.toByteArray()); 

					} catch (IOException e) {
						logger.info("Image rotaion failed");
						e.printStackTrace();
					}
					logger.info("Time Taken to rectify orientaion of Image :: " + formData.getMediaUUID() + " :: " + (System.currentTimeMillis()-startTs) ) ;
				}
				
				boolean isUploadSuccessful = uploadMediaUtility.upload(formData);
				if(!isUploadSuccessful) {
					outputObject.setSuccessful(false);
					outputObject.setOutputMap(null);
					List<String> errorList = new ArrayList<>();
					errorList.add("Problem uploading media to S3");
					UserTrackingObject trackingObject = new UserTrackingObject(formData.getSuperAppId(), formData.getAppId(), formData.getUserId(), formData.getTokenId(),
							APITypes.SUBMIT, "Media data submission", new ObjectMapper().writeValueAsString(formData.getMediaUUID()), errorList, false, formData.getSyncTimeStamp());
					outputObject.setTrackingObject(trackingObject);
					outputObject.setSuccessful(false);
					return outputObject;
				}
				
				ImageGeotagData data = createDataForInsertion(formData);
				insertCrudService.insertImageGeotagData(data);
				UserTrackingObject trackingObject = new UserTrackingObject(formData.getSuperAppId()
						, formData.getAppId(), formData.getUserId(), formData.getTokenId(),
						APITypes.FORM_MEDIA_RELAY, "Form Data Submission"
						, new ObjectMapper().writeValueAsString(formData.getMediaUUID()), null, true, formData.getSyncTimeStamp());
				outputObject.setTrackingObject(trackingObject);
				outputObject.setSuccessful(true);
				break;
			} catch(WriteFailureException | WriteTimeoutException | CassandraWriteTimeoutException e) {
				if(retryCount++ < 5) {
					logger.debug("Retrying for image after getting Write Timeout Exception in cassandra for media data for" + formData.getSuperAppId()
					+ " app Id - " + formData.getAppId() + " Project ID-" + formData.getProjectId() + " Image Size- " + formData.getMediaContent().length);
					Thread.sleep(CommonConstants.THREAD_SLEEP_TIME);
					continue;
				} else {
					List<String> errorList = new ArrayList<>();
					errorList.add("Write timeout exception");
					UserTrackingObject trackingObject = new UserTrackingObject(formData.getSuperAppId(), formData.getAppId(), formData.getUserId(), formData.getTokenId(),
							APITypes.FORM_MEDIA_RELAY, "Form Data Submission", new ObjectMapper().writeValueAsString(formData.getMediaUUID()), errorList, false, formData.getSyncTimeStamp());
					outputObject.setTrackingObject(trackingObject);
					outputObject.setSuccessful(false);
					break;
				}
			}
		}
		return outputObject;
	}

	private ImageGeotagData createDataForInsertion(FormMediaValue formData) throws JsonProcessingException {
		
		// Add other fields
		ImageGeotagData dataToInsert = new ImageGeotagData();
		dataToInsert.setSuperAppId(formData.getSuperAppId());
		dataToInsert.setAppId(formData.getAppId());
		dataToInsert.setProjectId(formData.getProjectId());
		dataToInsert.setFieldId(formData.getMediaUUID());
		//data.setImageData(ByteBuffer.wrap(formData.getMediaContent()));
		dataToInsert.setLatitude(formData.getLatitude());
		dataToInsert.setLongitude(formData.getLongitude());
		dataToInsert.setSyncTs(formData.getSyncTimeStamp());
		dataToInsert.setGpsAccuracy(formData.getGpsAccuracy());
		dataToInsert.setTimestampOverlay(formData.getTimestampOverlay());
		dataToInsert.setMediaPath(formData.getMediaPath());
		dataToInsert.setInsertTs(formData.getInsTimeStamp());
		MediaMetaData mediaMetaData = new MediaMetaData();
		mediaMetaData.setExtension(formData.getMediaFileExtension());
		mediaMetaData.setSubType(formData.getMediaSubtype());
		dataToInsert.setMediaType(formData.getMediaType());
		dataToInsert.setMediaMetaData(objectMapper.writeValueAsString(mediaMetaData));
		dataToInsert.setAdditionalProperties(objectMapper.writeValueAsString(formData.getOtherParams()));
		return dataToInsert;
	}
	public Rotation getOrientation(String exifOrientationCode) {
		if(exifOrientationCode == null || exifOrientationCode.isEmpty()) {
			return null;
		}
		Rotation rotation = null;
		try{
			int orientation = Integer.parseInt(exifOrientationCode);
			String desc = "";

			switch (orientation) {
			case 1: desc = "Top, left side (Horizontal / normal)"; break;
			case 2: desc = "Top, right side (Mirror horizontal)"; break;
			case 3: desc = "Bottom, right side (Rotate 180)"; break;
			case 4: desc = "Bottom, left side (Mirror vertical)"; break;
			case 5: desc = "Left side, top (Mirror horizontal and rotate 270 CW)";
			rotation = Rotation.CW_270;
			break;
			case 6: desc = "Right side, top (Rotate 90 CW)"; 
			rotation = Rotation.CW_90;
			break;
			case 7: desc = "Right side, bottom (Mirror horizontal and rotate 90 CW)";
			rotation = Rotation.CW_90;
			break;
			case 8: desc = "Left side, bottom (Rotate 270 CW)"; 
			rotation = Rotation.CW_270;
			break;
			}
			logger.info("Orientation: " + orientation + " -- " + desc);
		}
		catch(Exception e){
			logger.info("Could Not Found  any Orientation properties for image");
			e.printStackTrace();
		}
		return rotation;
	}

}

package com.vassarlabs.proj.uniapp.media.download;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.imgscalr.Scalr.Mode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.awss3.service.api.IAWSS3Service;
import com.vassarlabs.proj.uniapp.api.pojo.FormMediaValue;
import com.vassarlabs.proj.uniapp.api.pojo.MediaDownloadRequestParams;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;
import com.vassarlabs.proj.uniapp.api.pojo.UserTrackingObject;
import com.vassarlabs.proj.uniapp.app.api.IMediaDownloadService;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ImageGeotagData;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.constants.ServiceNamesConstants;
import com.vassarlabs.proj.uniapp.crud.service.ImageGeotagDataCrudService;
import com.vassarlabs.proj.uniapp.enums.APITypes;
import com.vassarlabs.proj.uniapp.enums.MediaTypes;

@Component
public class MediaDownloadService 
	implements IMediaDownloadService {
	
	@Autowired private ImageGeotagDataCrudService mediaDataCrudService;
	
	@Autowired private IAWSS3Service awsS3Service;
	
	private ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public ServiceOutputObject downloadMedia(MediaDownloadRequestParams mediaDownloadRequestParams) throws IOException {
		
		FormMediaValue mediaValue = generateMediaValue(mediaDownloadRequestParams);
		ImageGeotagData mediaDBData = mediaDataCrudService.findImageGeotagDataByPrimaryKey(mediaDownloadRequestParams.getSuperAppId(), mediaDownloadRequestParams.getAppId(),
				mediaDownloadRequestParams.getProjectId(), mediaDownloadRequestParams.getMediaUUID());
		mediaValue.setGpsAccuracy(mediaDBData.getGpsAccuracy());
		mediaValue.setLatitude(mediaDBData.getLatitude());
		mediaValue.setLongitude(mediaDBData.getLongitude());
		mediaValue.setTimestampOverlay(mediaDBData.getTimestampOverlay());
		mediaValue.setTokenId(mediaDownloadRequestParams.getTokenId());
		byte[] mediaByteArray = null;
		if(mediaDBData.getMediaPath() != null) {
			mediaByteArray = awsS3Service.downloadS3Object(mediaDBData.getMediaPath(), null);
		} else {
			mediaByteArray = mediaDBData.getImageData().array();
		}
		// Set media content as requested
		String mediaType = mediaDownloadRequestParams.getMediaType();
		if(mediaType.contains(MediaTypes.THUMBNAIL.getValue())
				&& mediaType.contains(MediaTypes.IMAGE.getValue())) {
		    BufferedImage image = ImageIO.read(new ByteArrayInputStream(mediaByteArray));
			BufferedImage thumbnailImage = Scalr.resize(image, Method.QUALITY, Mode.AUTOMATIC, 50, 50, Scalr.OP_ANTIALIAS);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ImageIO.write(thumbnailImage, "png", os);
			mediaByteArray = os.toByteArray();
		} 
		writeByte(mediaByteArray, String.valueOf(mediaValue.getMediaUUID()));
		mediaValue.setMediaContent(mediaByteArray);
		
		Map<String, Object> outputMap = new HashMap<>();
		outputMap.put(CommonConstants.MEDIA, mediaValue);
		ServiceOutputObject outputObject = new ServiceOutputObject();
		UserTrackingObject trackingObject = new UserTrackingObject(mediaDownloadRequestParams.getSuperAppId(), mediaDownloadRequestParams.getAppId(), mediaDownloadRequestParams.getUserId(), mediaDownloadRequestParams.getTokenId(),
				APITypes.MEDIA_DOWNLOAD, ServiceNamesConstants.MEDIA_DOWNLOAD, objectMapper.writeValueAsString(mediaDownloadRequestParams), null, true, System.currentTimeMillis());
		outputObject.setTrackingObject(trackingObject);
		outputObject.setSuccessful(true);
		outputObject.setOutputMap(outputMap);
		return outputObject;
	}

	private FormMediaValue generateMediaValue(MediaDownloadRequestParams mediaDownloadRequestParams) {
		FormMediaValue mediaValue = new FormMediaValue();
		mediaValue.setSuperAppId(mediaDownloadRequestParams.getSuperAppId());
		mediaValue.setAppId(mediaDownloadRequestParams.getAppId());
		mediaValue.setProjectId(mediaDownloadRequestParams.getProjectId());
		mediaValue.setMediaUUID(mediaDownloadRequestParams.getMediaUUID());
		return mediaValue;
	}

	public void writeByte(byte[] bytes, String name) { 
    	// Path of a file 
        String fp = "/home/nidhi/images/" + name; 
        File file = new File(fp); 
        try { 
            // Initialize a pointer 
            // in file using OutputStream 
            OutputStream os  = new FileOutputStream(file); 
  
            // Starts writing the bytes in it 
            os.write(bytes); 
            System.out.println("Successfully"
                               + " byte inserted"); 
  
            os.close(); 
        } 
        catch (Exception e) { 
            System.out.println("Exception: " + e); 
        } 
    }
}

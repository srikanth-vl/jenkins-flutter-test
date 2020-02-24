package services.com.vassarlabs.play.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.proj.uniapp.api.pojo.FormMediaValue;

import controllers.com.vassarlabs.play.constants.ResponseConstants;
import models.com.vassarlabs.play.models.ApiResponse;
import play.Logger;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;

@Service 
public class MediaDataGenerationUtils 
	extends CommonUtils {

	public FormMediaValue getMediaObject(MultipartFormData body, ApiResponse response) throws IOException {

		Map<String, String[]> requestMap = body.asFormUrlEncoded();
		if(requestMap == null) {
			Logger.error("In submitImageGeotagData() - RequestMap is NULL");
			response.setResult(false, ResponseConstants.REQUEST_MAP_NULL_ERROR_CODE, null, ResponseConstants.REQUEST_MAP_NULL_ERROR_MESSAGE);
			return null;
		}
	
		UUID superAppId = getUUIDValue(requestMap, response, "superapp");
		UUID appId = getUUIDValue(requestMap, response, "appid");
		UUID projectId = getUUIDValue(requestMap, response, "projectid");
		UUID imageId = getUUIDValue(requestMap, response, "imageid");
		UUID token = getUUIDValue(requestMap, response, "token");
		Long syncTs = getLongValue(requestMap, response, "syncts");
		String userId = getStringValue(requestMap, response, "userid");
		double latitude = getDoubleValue(requestMap, response, "lat");
		double longitude = getDoubleValue(requestMap, response, "lon");
		String mediaType = getStringValue(requestMap, response, "mediatype") ;
		String gpsAccuracy = getStringValue(requestMap, response, "gps_accuracy");
		String mediaExtension = getStringValue(requestMap, response, "media_ext");
		String mediaSubType = getStringValue(requestMap, response, "media_subtype");
		Long insert_ts = getLongValue(requestMap, response, "insert_ts");
		String additionalProperties = getStringValue(requestMap, response, "additional_props");
		
		if(superAppId == null || appId == null || projectId == null || imageId == null 
				|| token == null || syncTs == null || userId == null) {
			Logger.error("In getMediaObject() - Found NULL parameters : superAppId : " + superAppId + " appId : " + appId + " projectId : "
					+ projectId + " imageId : " + imageId + " token : " + token + " sync TS : " + syncTs + " userID : " + userId);
			response.setResult(false, ResponseConstants.FAILURE, null, ResponseConstants.UNSUCCESSFUL_OPERATION);
			return null;
		}
		FormMediaValue mediaObject = createMediaObject(body, superAppId, appId, projectId, imageId, syncTs, token, userId, latitude, longitude, mediaType, gpsAccuracy, mediaExtension, 
				mediaSubType, insert_ts, response, additionalProperties);
		
		return mediaObject;
	}

	private FormMediaValue createMediaObject(MultipartFormData body, UUID superAppId, UUID appId, UUID projectId, UUID imageId, Long syncTs, UUID token, String userId, double latitude, double longitude,
			String mediaType, String gpsAccuracy, String mediaExtension, String mediaSubType, Long insert_ts, ApiResponse response, String additionalProperties) throws IOException {
		
		FormMediaValue mediaObject = new FormMediaValue();
		List<FilePart> files = body.getFiles();
		if(files == null || files.isEmpty()) {
			Logger.error("In getMediaObject() - Did not receive any media");
			response.setResult(false, ResponseConstants.FAILURE, null, ResponseConstants.UNSUCCESSFUL_OPERATION);
			return null;
		}
		for (Iterator<FilePart> iterator = files.iterator(); iterator.hasNext();) {
			FilePart filePart = (FilePart) iterator.next();
			File file = filePart.getFile();
			mediaObject.setAppId(appId);
			mediaObject.setSuperAppId(superAppId);
			mediaObject.setProjectId(projectId);
			mediaObject.setMediaUUID(imageId);
			mediaObject.setSyncTimeStamp(syncTs);
			mediaObject.setTokenId(token);
			mediaObject.setUserId(userId);
			mediaObject.setLatitude(latitude);
			mediaObject.setLongitude(longitude);
			mediaObject.setMediaType(mediaType);
			mediaObject.setTimestampOverlay(String.valueOf(syncTs));
			mediaObject.setGpsAccuracy(gpsAccuracy);
			mediaObject.setInsTimeStamp(insert_ts);
			mediaObject.setMediaFileExtension(mediaExtension);
			mediaObject.setMediaSubtype(mediaSubType);
			if(additionalProperties != null && !additionalProperties.isEmpty()) {
				Map<String, String> otherParams = new ObjectMapper().readValue(additionalProperties, new TypeReference<Map<String, String>>(){});
				mediaObject.setOtherParams(otherParams);
			}
			byte[] imageContent = new byte[(int) file.length()];
			Logger.debug("Size of image recieved from app-" + file.length() + " Absolute Path - " + file.getAbsolutePath());
			imageContent = Files.readAllBytes(file.toPath());
			mediaObject.setMediaContent(imageContent);
		}
		
		return mediaObject;
	}
}
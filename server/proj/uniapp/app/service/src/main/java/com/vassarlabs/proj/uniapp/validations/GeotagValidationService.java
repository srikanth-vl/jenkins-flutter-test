package com.vassarlabs.proj.uniapp.validations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vassarlabs.common.utils.err.DataNotFoundException;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.data.retrieve.DataRetrievalService;
import com.vassarlabs.proj.uniapp.enums.ProjectStates;
import com.vassarlabs.proj.uniapp.validations.api.pojo.ValidationResult;

@Component
public class GeotagValidationService {
	
	@Autowired DataRetrievalService dataRetrievalService;
	
	@Autowired private IVLLogService logFactory;
	private IVLLogger logger;

	@PostConstruct
	public void init() {
		logger = logFactory.getLogger(getClass());
	}
	
	public ValidationResult validate(UUID superAppId, UUID appId, UUID projectId, double latitude, double longitude) throws DataNotFoundException {
		
		ValidationResult validationResult = new ValidationResult();
		validationResult.setValid(true);
		if(latitude == 0 && longitude == 0) {
			return validationResult;
		}
		Map<String, String> keyToValueMap = dataRetrievalService.getValueForAProject(superAppId, appId, projectId, ProjectStates.ALL);
		if(keyToValueMap == null || keyToValueMap.isEmpty()) {
			logger.error("No data found for super app ID -" + superAppId + " app ID - " + appId + " project ID - "  + projectId);
			return validationResult;
		}
		
		Map<String, List<String>> keyToErrorMessage = new HashMap<>();
		List<String> errorMessages = new ArrayList<>();
		// If center-radius values are given for validation
		if(keyToValueMap.containsKey(CommonConstants.DATATYPE_CENTER_RADIUS_ENVELOPE)) {
			String value = keyToValueMap.get(CommonConstants.DATATYPE_CENTER_RADIUS_ENVELOPE);
			String[] geotaggedCoordinates = value.split(",");
			if(geotaggedCoordinates.length != 3) {
				logger.error("Geotagged validation cannot happen, not enough Coordinates obtained");
				errorMessages.add("Geotagged validation cannot happen, not enough Coordinates obtained");
				keyToErrorMessage.put(CommonConstants.GPS_VALIDATION_KEY, errorMessages);
				return ValidationResult.fail(keyToErrorMessage);
			}
			Double centerx = Double.parseDouble(geotaggedCoordinates[0]);
			Double centery = Double.parseDouble(geotaggedCoordinates[1]);
			Double radius = Double.parseDouble(geotaggedCoordinates[2]);
			double result = Math.pow((latitude - centerx), 2) + Math.pow((longitude - centery), 2) * 111.324; //Multiplying by 111.324 will convert it into kms 
			if(result > Math.pow(radius, 2)) {
				errorMessages.add("Geotagged validation failed - Point (" + latitude + ", " + longitude + ") does not lie within center (" + centerx + ", " + centery + ") and radius - " + radius);
				keyToErrorMessage.put(CommonConstants.GPS_VALIDATION_KEY, errorMessages);
				return ValidationResult.fail(keyToErrorMessage);
			}
		}
		// If BBox values are given for validation
		if(keyToValueMap.containsKey(CommonConstants.DATATYPE_BBOX)) {
			String value = keyToValueMap.get(CommonConstants.DATATYPE_BBOX);
			String[] geotaggedCoordinates = value.split(",");
			if(geotaggedCoordinates.length != 4) {
				logger.error("Geotagged validation cannot happen, not enough BBox Coordinates obtained");
				errorMessages.add("Geotagged validation cannot happen, not enough BBox Coordinates obtained");
				keyToErrorMessage.put(CommonConstants.GPS_VALIDATION_KEY, errorMessages);
				return ValidationResult.fail(keyToErrorMessage);
			}
			Double minx = Double.parseDouble(geotaggedCoordinates[0]);
			Double miny = Double.parseDouble(geotaggedCoordinates[1]);
			Double maxx = Double.parseDouble(geotaggedCoordinates[2]);
			Double maxy = Double.parseDouble(geotaggedCoordinates[3]);
			if(!(minx <= latitude && latitude <= maxx
					&& miny <= longitude && longitude <= maxy)) {
				
				errorMessages.add("Geotagged validation failed - Point (" + latitude + ", " + longitude + ") does not lie within BBox (" + minx + ", " + miny + ") and (" + maxx + ", " + maxy + ")");
				keyToErrorMessage.put(CommonConstants.GPS_VALIDATION_KEY, errorMessages);
				return ValidationResult.fail(keyToErrorMessage);
			}
		}
		return validationResult;
	}
	
}

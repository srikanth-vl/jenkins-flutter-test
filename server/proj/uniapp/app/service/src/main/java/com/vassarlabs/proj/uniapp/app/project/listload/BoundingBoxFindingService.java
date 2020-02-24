package com.vassarlabs.proj.uniapp.app.project.listload;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vassarlabs.proj.uniapp.api.pojo.BboxContainerObject;
import com.vassarlabs.proj.uniapp.api.pojo.LocationCoordinateObject;
import com.vassarlabs.proj.uniapp.app.kafkaqueue.service.SendToQueueBboxDataService;
import com.vassarlabs.proj.uniapp.constants.MapConstants;
import com.vassarlabs.proj.uniapp.crud.service.MapConfigDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.UserMetaDataCrudService;

@Component
public class BoundingBoxFindingService {
	
	@Autowired SendToQueueBboxDataService sendToQueueBboxData;
	@Autowired UserMetaDataCrudService userDataCrudService;
	@Autowired MapConfigDataCrudService mapConfigDataCrudService;
	public void bBoxForListOfCoordinates(UUID superAppId, UUID appId, String userId, List<LocationCoordinateObject> locPoints, Map<String, Set<String>> userIdtoMapFiles, String mapBaseUrl) throws JsonProcessingException {
		
		DecimalFormat df = new DecimalFormat("#.##");
		df.setRoundingMode(RoundingMode.CEILING);
		
//		BBox Format N S E W --->  maxLon minLon maxLat minLat
		Double minLat = Double.MAX_VALUE;
		Double maxLat = Double.MIN_VALUE;
		Double minLon = Double.MAX_VALUE;
		Double maxLon = Double.MIN_VALUE;
		
		for (LocationCoordinateObject point : locPoints) {
			
			if (point.getLat() < minLat) {
				minLat = point.getLat();
			}
			if (point.getLat() > maxLat) {
				maxLat = point.getLat();
			}
			if (point.getLon() < minLon) {
				minLon = point.getLon();
			}
			if (point.getLon() > maxLon) {
				maxLon = point.getLon();
			}
		}
		
		minLat = Double.valueOf(df.format(minLat - (MapConstants.extraRadiusInKm/100.0)));
		maxLat = Double.valueOf(df.format(maxLat + (MapConstants.extraRadiusInKm/100.0)));
		minLon = Double.valueOf(df.format(minLon - (MapConstants.extraRadiusInKm/100.0)));
		maxLon = Double.valueOf(df.format(maxLon + (MapConstants.extraRadiusInKm/100.0)));
		
//		minLat = minLat - newLatDegreeFromKm(MapConstants.extraRadiusInKm);
//		maxLat = maxLat + newLatDegreeFromKm(MapConstants.extraRadiusInKm);
//		minLon = minLon - newLonDegreeFromkm(minLat, MapConstants.extraRadiusInKm);
//		maxLon = maxLon + newLonDegreeFromkm(maxLat, MapConstants.extraRadiusInKm);
		BboxContainerObject bboxObject = new BboxContainerObject(superAppId, appId, userId, maxLat, minLat, maxLon, minLon);
		updateUserMetadata(bboxObject, mapBaseUrl, userIdtoMapFiles);
		System.out.println("BBOX "+bboxObject);
		sendToQueueBboxData.sendMessage(bboxObject);
		
	}
	
	public Double newLatDegreeFromKm (Double distance) {
		Double latDiff = (distance/((Double)110.574));
		return latDiff;
	}
	
	public Double newLonDegreeFromkm (Double latitude, Double distance) {
		
		Double oneDegree = (111.320)*(Math.cos(Math.toRadians(latitude)));
		Double lonDiff = (distance/oneDegree);

		return lonDiff;
	}
	public void updateUserMetadata(BboxContainerObject bboxObject, String mapBaseUrl, Map<String, Set<String>> userIdtoMapFiles) {
		Double north = bboxObject.getNorth();
		Double south = bboxObject.getSouth();
		Double east = bboxObject.getEast();
		Double west = bboxObject.getWest();
		String url = mapBaseUrl + north + "_" + south + "_" + east + "_" + west + ".zip";
		addToUserIdToFileMap(userIdtoMapFiles, bboxObject.getUserId(), url);
	}
	public void addToUserIdToFileMap(Map<String, Set<String>> userIdtoMapFiles, String userId, String mapFileUrl) {
		if(userId == null || userId.isEmpty() || mapFileUrl ==  null || mapFileUrl.isEmpty() ) {
			return;
		}
		Set<String> files =userIdtoMapFiles.get(userId);
		if(files == null ) {
			files =  new HashSet<>();
		}
		files.add(mapFileUrl);
		userIdtoMapFiles.put(userId, files);
	}
	
}

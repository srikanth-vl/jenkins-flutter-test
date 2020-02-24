package com.vassarlabs.proj.uniapp.app.mapconfiguration.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.CassandraConnectionFailureException;
import org.springframework.data.cassandra.CassandraInternalException;
import org.springframework.data.cassandra.CassandraInvalidQueryException;
import org.springframework.data.cassandra.CassandraQuerySyntaxException;
import org.springframework.data.cassandra.CassandraReadTimeoutException;
import org.springframework.data.cassandra.CassandraTypeMismatchException;
import org.springframework.data.cassandra.CassandraWriteTimeoutException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.utils.err.AppMetaDataNotFoundException;
import com.vassarlabs.common.utils.err.TokenExpiredException;
import com.vassarlabs.common.utils.err.TokenNotFoundException;
import com.vassarlabs.common.utils.err.UserNotFoundException;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.prod.common.utils.StringUtils;
import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.proj.uniapp.api.pojo.ApiRequestObject;
import com.vassarlabs.proj.uniapp.api.pojo.MapConfig;
import com.vassarlabs.proj.uniapp.api.pojo.MapMarkersInfo;
import com.vassarlabs.proj.uniapp.api.pojo.OffLineMapFile;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;
import com.vassarlabs.proj.uniapp.api.pojo.UserTrackingObject;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.MapConfigData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.MapFileData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserDBMetaData;
import com.vassarlabs.proj.uniapp.constants.BackendPropertiesConstants;
import com.vassarlabs.proj.uniapp.constants.MapConstants;
import com.vassarlabs.proj.uniapp.constants.RootConfigurationConstants;
import com.vassarlabs.proj.uniapp.constants.ServiceNamesConstants;
import com.vassarlabs.proj.uniapp.crud.service.MapConfigDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.MapFileDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.UserMetaDataCrudService;
import com.vassarlabs.proj.uniapp.enums.APITypes;
import com.vassarlabs.proj.uniapp.enums.UserStates;

@Component
public class MapConfigurationService {

	@Autowired
	private IVLLogService logFactory;

	private IVLLogger logger;

	private ObjectMapper objectMapper = new ObjectMapper();
	@Autowired UserMetaDataCrudService userMetaDataCrudService;
	@Autowired MapFileDataCrudService mapDataCrudService;

	@PostConstruct
	public void init() {
		logger = logFactory.getLogger(getClass());
	}


	@Autowired MapConfigDataCrudService mapConfigDataCrudService;
	public ServiceOutputObject getMapConfiguration(ApiRequestObject apiRequestObject) 
			throws IOException, TokenNotFoundException, TokenExpiredException, 
			AppMetaDataNotFoundException ,CassandraConnectionFailureException, 
			CassandraReadTimeoutException, CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, 
			CassandraQuerySyntaxException, CassandraTypeMismatchException, UserNotFoundException {

		Map<String, Object> mapConfigMap = new HashMap<>();
		if (apiRequestObject == null) {
			logger.error("ApiRequestObject is NULL");
			return null;
		}

		UUID superAppId = apiRequestObject.getSuperAppId();
		String userId = apiRequestObject.getUserId();
		UUID token = apiRequestObject.getTokenId();
		int maxVersion = -1;
		String configMapStr =  mapConfigDataCrudService.getMapConfiguration(superAppId, UUIDUtils.getDefaultUUID());
		List<MapConfigData> mapConfigData =  mapConfigDataCrudService.getAllLatestMapConfigData(superAppId);
		List<String> mapFileNames = new ArrayList<String>();
		MapConfig config  =  new MapConfig();
		config.setMaxZoom(Integer.parseInt(MapConstants.zmax));
		config.setMinZoom(Integer.parseInt(MapConstants.zmin));
		config.setMapSourceName(MapConstants.tempFolder);
		if(configMapStr != null && !configMapStr.isEmpty()) {
			config = objectMapper.readValue(configMapStr, MapConfig.class); 
		}
		UserDBMetaData userDBMetaData =  userMetaDataCrudService.findUserDataByUserIdKey(superAppId, userId, UserStates.ACTIVE);

		Map<UUID, String> mapFileUrls = userDBMetaData.getMapFileUrls() != null ? userDBMetaData.getMapFileUrls() : new HashMap<>();
		List<OffLineMapFile> mapFiles = new ArrayList<>();
		for (UUID appId : mapFileUrls.keySet()) {
			String mapUrlsString = mapFileUrls.get(appId);
			String[] mapUrls = mapUrlsString.split(",");

			for (int i = 0; i < mapUrls.length; i++) {
				OffLineMapFile mapFileDetail  = new OffLineMapFile();
				String fileUrl = mapUrls[i]; 
				List<String> fileUrlStr = StringUtils.getStringListFromDelimitter("/", fileUrl);
				String fileName = "";
				if(fileUrlStr.get(fileUrlStr.size()-1).contains("."))  {
					fileName = fileUrlStr.get(fileUrlStr.size()-1);
				}
				mapFileNames.add(fileName);
				MapFileData mapData = mapDataCrudService.findDataByPartitionKey(fileName);
				String fileSize = "";
				if (mapData != null) {
					fileSize = mapData.getFileSize();

					mapFileDetail.setFileName(fileName);
					mapFileDetail.setFileUrl(fileUrl);
					mapFileDetail.setFileStoragePath("/osmdroid");
					mapFileDetail.setFileSize(fileSize);
					if(mapData.getUploaded() != null && mapData.getUploaded()) {
						mapFiles.add(mapFileDetail);
					}
				}
			}

		}
		addGeogsonFile(mapFiles, userId);
		Map<UUID, Object> mapMarkerIconsInfoMap = new HashMap<>(); 
		for (MapConfigData data : mapConfigData) {
			if(data.getMarkersInfo() !=  null && !data.getMarkersInfo().isEmpty()) {
				List<MapMarkersInfo> markers  = objectMapper.readValue(data.getMarkersInfo(), new TypeReference<List<MapMarkersInfo>>() {});
				mapMarkerIconsInfoMap.put(data.getAppId(), markers);
			}
		}
		mapConfigMap.put(MapConstants.mapMarkers, mapMarkerIconsInfoMap);
		mapConfigMap.put(MapConstants.minZoom, config.getMinZoom());
		mapConfigMap.put(MapConstants.maxZoom, config.getMaxZoom());
		mapConfigMap.put(RootConfigurationConstants.VERSION, maxVersion);
		mapConfigMap.put(MapConstants.offlineMapSourceName,config.getMapSourceName());
		long trackingTS = System.currentTimeMillis();
		mapConfigMap.put(MapConstants.offlineMapFiles, mapFiles);
		mapConfigMap.put(BackendPropertiesConstants.CURRENT_SERVER_TIME, trackingTS);
		String boudingBox = bboxOfUser(mapFileNames);
		mapConfigMap.put(MapConstants.boundingBox, boudingBox);
		UserTrackingObject trackingObject = new UserTrackingObject(superAppId, superAppId, userId, token, APITypes.MAP_CONFIG,
				ServiceNamesConstants.MAP_CONFIG_NAME, objectMapper.writeValueAsString(apiRequestObject), null, true, trackingTS);

		ServiceOutputObject output = new ServiceOutputObject(mapConfigMap, trackingObject, true);
		return output;
	}
	public String bboxOfUser(List<String> mapFileNames) {

		if (mapFileNames == null || mapFileNames.size() == 0) {
			return "";
		}

		//			BBox Format N S E W --->  maxLat minLat maxLon minLon
		Double minLat = Double.MAX_VALUE;
		Double maxLat = Double.MIN_VALUE;
		Double minLon = Double.MAX_VALUE;
		Double maxLon = Double.MIN_VALUE;

		for (String fileName : mapFileNames) {
			fileName = fileName.replace(".zip", "");
			String[] bbox = fileName.split("_");

			Double north = Double.parseDouble(bbox[0]);
			Double south = Double.parseDouble(bbox[1]);
			Double east = Double.parseDouble(bbox[2]);
			Double west = Double.parseDouble(bbox[3]);

			if (north > maxLat) {
				maxLat = north;
			}
			if (south < minLat) {
				minLat = south;
			}
			if (east > maxLon) {
				maxLon = east;
			}
			if (west < minLon) {
				minLon = west;
			}
		}
		minLat = minLat - MapConstants.extraBbox;
		maxLat = maxLat + MapConstants.extraBbox;
		minLon = minLon - MapConstants.extraBbox;
		maxLon = maxLon + MapConstants.extraBbox;
		return maxLat + "_" + minLat + "_" + maxLon + "_" + minLon;
	}
	public void addGeogsonFile(List<OffLineMapFile> mapFiles, String userId) {
		if(userId.equalsIgnoreCase("1234567890")) {
			OffLineMapFile mapFileDetail  = new OffLineMapFile();
			mapFileDetail.setFileUrl("https://prrd.s3-us-west-2.amazonaws.com/map/padamatiyerulu_drainage.geojson");
			mapFileDetail.setFileName("padamatiyerulu_drainage.geojson");
			mapFileDetail.setFileStoragePath("/osmdroid/"+ userId +"/geojson");
			mapFileDetail.setFileSize("6500");
			Map<String, String> additionalInfo = new HashMap<>();
			additionalInfo.put("index", "2");
			additionalInfo.put("label", "drain");
			additionalInfo.put("icon_url", "https://prrd.s3-us-west-2.amazonaws.com/icons/map_marker_icon.png");
			mapFileDetail.setFileAdditionalInfo(additionalInfo);
			mapFiles.add(mapFileDetail);

			OffLineMapFile mapFileDetail1  = new OffLineMapFile();
			mapFileDetail1.setFileUrl("https://prrd.s3-us-west-2.amazonaws.com/map/cadastral_padamatiyerulu.geojson");
			mapFileDetail1.setFileName("cadastral_padamatiyerulu.geojson");
			mapFileDetail1.setFileStoragePath("/osmdroid/"+ userId +"/geojson");
			mapFileDetail1.setFileSize("596.9");
			Map<String, String> additionalInfo2 = new HashMap<>();
			additionalInfo2.put("index", "1");
			additionalInfo2.put("label", "cadastral");
			additionalInfo2.put("icon_url", "https://prrd.s3-us-west-2.amazonaws.com/icons/map_marker_icon.png");
			mapFileDetail1.setFileAdditionalInfo(additionalInfo2);
			mapFiles.add(mapFileDetail1);
			OffLineMapFile mapFileDetail2  = new OffLineMapFile();
			mapFileDetail2.setFileUrl("https://prrd.s3-us-west-2.amazonaws.com/map/padamati_good_bad_zone.geojson");
			mapFileDetail2.setFileName("padamati_good_bad_zone.geojson");
			mapFileDetail2.setFileStoragePath("/osmdroid/"+ userId +"/geojson");
			mapFileDetail2.setFileSize("38.6");
			Map<String, String> additionalInfo3 = new HashMap<>();
			additionalInfo3.put("index", "3");
			additionalInfo3.put("label", "goodbad");
			additionalInfo3.put("icon_url", "https://prrd.s3-us-west-2.amazonaws.com/icons/map_marker_icon.png");
			mapFileDetail2.setFileAdditionalInfo(additionalInfo3);
			mapFiles.add(mapFileDetail2);

		}	
		if(userId.equalsIgnoreCase("1111111111")) {
			OffLineMapFile mapFileDetail  = new OffLineMapFile();
			mapFileDetail.setFileUrl("https://prrd.s3-us-west-2.amazonaws.com/map/remidi_good_bad_zone.geojson");
			mapFileDetail.setFileName("remidi_good_bad_zone.geojson");
			mapFileDetail.setFileStoragePath("/osmdroid/"+ userId +"/geojson");
			mapFileDetail.setFileSize("105.9");
			Map<String, String> additionalInfo = new HashMap<>();
			additionalInfo.put("icon_url", "https://prrd.s3-us-west-2.amazonaws.com/icons/map_marker_icon.png");
			additionalInfo.put("index", "3");
			additionalInfo.put("label", "goodbad");
			mapFileDetail.setFileAdditionalInfo(additionalInfo);
			mapFiles.add(mapFileDetail);

			OffLineMapFile mapFileDetail1  = new OffLineMapFile();
			mapFileDetail1.setFileUrl("https://prrd.s3-us-west-2.amazonaws.com/map/remidi_drain.geojson");
			mapFileDetail1.setFileName("remidi_drain.geojson");
			mapFileDetail1.setFileStoragePath("/osmdroid/"+ userId +"/geojson");
			mapFileDetail1.setFileSize("70.0");
			Map<String, String> additionalInfo2 = new HashMap<>();
			additionalInfo2.put("index", "2");
			additionalInfo2.put("label", "drain");
			additionalInfo2.put("icon_url", "https://prrd.s3-us-west-2.amazonaws.com/icons/map_marker_icon.png");
			mapFileDetail1.setFileAdditionalInfo(additionalInfo2);
			mapFiles.add(mapFileDetail1);

			OffLineMapFile mapFileDetail2  = new OffLineMapFile();
			mapFileDetail2.setFileUrl("https://prrd.s3-us-west-2.amazonaws.com/map/cadastral_remidicherla.geojson");
			mapFileDetail2.setFileName("cadastral_remidicherla.geojson");
			mapFileDetail2.setFileStoragePath("/osmdroid/"+ userId +"/geojson");
			mapFileDetail2.setFileSize("509.0");
			Map<String, String> additionalInfo3 = new HashMap<>();
			additionalInfo3.put("index", "1");
			additionalInfo3.put("icon_url", "https://prrd.s3-us-west-2.amazonaws.com/icons/map_marker_icon.png");
			additionalInfo3.put("label", "cadastral");
			mapFileDetail2.setFileAdditionalInfo(additionalInfo3);
			mapFiles.add(mapFileDetail2);
		}
	}
}

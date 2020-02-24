package com.vassarlabs.proj.uniapp.map.osmdata;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;

import com.vassarlabs.proj.uniapp.api.pojo.BboxContainerObject;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.MapFileData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserDBMetaData;
import com.vassarlabs.proj.uniapp.constants.MapConstants;
import com.vassarlabs.proj.uniapp.constants.SuperAppAndAppIdsConstants;
import com.vassarlabs.proj.uniapp.crud.service.MapFileDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.UserMetaDataCrudService;
import com.vassarlabs.proj.uniapp.enums.UserStates;
import com.vassarlabs.proj.uniapp.utility.UploadMapZipToS3ProductionBucket;

@Component
public class TileProviderUtil {
	
	@Autowired
	UploadMapZipToS3ProductionBucket uploadZipToS3Util;
	
	@Autowired
	MapFileDataCrudService mapfileDataCrudService;
	
	@Autowired 
	private IVLLogService logFactory;
	private IVLLogger logger;
	@PostConstruct
	public void init() {
		logger = logFactory.getLogger(getClass());
	}
//	-u http://tile.openstreetmap.org/%d/%d/%d.png -t map -d at_mapnik_13.zip -zmax 15 -n 80.489273 -s 80.399323 -e 16.360662 -w 16.268732".split("\\s+"));
    
	public String getTilesForABbox(BboxContainerObject bboxObject, String maxZoom, String minZoom, String mapBaseUrl, String partition) throws IOException {
		
		Double north = bboxObject.getNorth();
		Double south = bboxObject.getSouth();
		Double east = bboxObject.getEast();
		Double west = bboxObject.getWest();
		
//		TODO if db has already this file then return
		String outputFile = north + "_" + south + "_" + east + "_" + west + ".zip";
		
		MapFileData mapFileData = mapfileDataCrudService.findDataByPartitionKey(outputFile);
		if (mapFileData != null && mapFileData.getUploaded()) {
			logger.info("In TileProviderUtil :: getTilesForABbox() - " +outputFile+ " file already exist");
			return mapFileData.getMapFileName();
		} 
		Long size= uploadZipToS3Util.ifExists(outputFile, bboxObject.getSuperAppId());
		if( size != null ) {
			logger.info("In TileProviderUtil :: getTilesForABbox() - " +outputFile+ " file already exist is S3");
			if(mapFileData == null || mapFileData.getFileSize() == null) {
				mapFileData =  new MapFileData();
				mapFileData.setFileSize(getStringSizeLengthFile(size));
			}
			
			updateMapFileMetadata(bboxObject,  maxZoom, minZoom, mapBaseUrl, mapFileData);
			return outputFile;
		}
		String pathOfMapFolder = MapConstants.jarLocation  + partition +"/";
		Long startTs=  System.currentTimeMillis();
		File dir = new File( pathOfMapFolder +  MapConstants.tempFolder);
		if(dir.exists()) {
			deleteDirectory(dir);
		}
		logger.info("In TileProviderUtil :: getTilesForABbox() - downloading file " + outputFile);
		String command = "";
		command = command + "java -jar " + MapConstants.jarLocation + MapConstants.jarFile;
		command = command + " -u " + MapConstants.getServerUrl();
		command = command + " -t " + pathOfMapFolder + MapConstants.tempFolder;
		command = command + " -d " + pathOfMapFolder + outputFile;
		command = command + " -zmin " + minZoom;
		command = command + " -zmax " + maxZoom;
		command = command + " -n " + north;
		command = command + " -s " + south;
		command = command + " -e " + east;
		command = command + " -w " + west;
		
		try {           
	        Process proc = Runtime.getRuntime().exec(command.split("\\s+"));
	        proc.waitFor();
	        InputStream in = proc.getInputStream();
	        InputStream err = proc.getErrorStream();

	        byte b[]=new byte[in.available()];
	        in.read(b,0,b.length);
	        System.out.println(new String(b));

	        byte c[]=new byte[err.available()];
	        err.read(c,0,c.length);
	        System.out.println(new String(c));
	        
	        String outputFilePath = pathOfMapFolder + outputFile;
	        File tempFile = new File(outputFilePath);
	        boolean fileExists = tempFile.exists();
	        
	        if (fileExists) {
	        	logger.info("In TileProviderUtil :: getTilesForABbox() - downloaded file " + outputFile);
//	    		upload to s3 and generate s3 link return the s3 link
	        	MapFileData mapData = new MapFileData();
	        	mapData.setMapFileName(outputFile);
	        	mapData.setUploaded(false);
	        	mapData.setFileSize(getStringSizeLengthFile(tempFile.length()));
	        	mapData.setInsertTs(System.currentTimeMillis());
	        	mapfileDataCrudService.insertMapData(mapData);
	        	
	    		boolean isUploaded = uploadZipToS3Util.uploadToS3(mapData, bboxObject.getSuperAppId(), outputFilePath);
	    		proc.destroy();
	    		if (isUploaded) {
	    			tempFile.delete();
//	    			UserDBMetaData userData = userDataCrudService.findUserDataByUserIdKey(bboxObject.getSuperAppId(), bboxObject.getUserId(), UserStates.ACTIVE);
//	    			Map<UUID, String> appUrls = new HashMap<UUID, String>();
//	    			if (userData.getMapFileUrls() != null) {
//	    				appUrls = userData.getMapFileUrls();
//	    			}
//	    			
//	    			String url = mapBaseUrl + north + "_" + south + "_" + east + "_" + west + ".zip";
////	    			Map<UUID, String> mapfiles = userData.getMapFileUrls() != null ? userData.getMapFileUrls() :new HashMap<>();
////	    			if (mapfiles.get(bboxObject.getAppId()) != null &&
//	    					mapfiles.get(bboxObject.getAppId())  != "") {
//	    				url = userData.getMapFileUrls().get(bboxObject.getAppId()) + "," + url;
//					} 
//	    			appUrls.put(bboxObject.getAppId(), url);
//	    			userData.setMapFileUrls(appUrls);
//	    			userDataCrudService.insertUserMetaData(userData);
	    			
					mapData.setUploaded(true);
		        	mapfileDataCrudService.insertMapData(mapData);
				}
			}  else {
				logger.info("In TileProviderUtil :: getTilesForABbox() - " + outputFile + " file could not be downloaded.");
			}
	       long endTs =  System.currentTimeMillis();
	       logger.info("In TileProviderUtil :: getTilesForABbox() - " + "time taken to process BBOX " + north + "_" + south + "_" + east + "_" + west +" :: " + (endTs - startTs));
        } catch (IOException e) {
        	logger.info("In TileProviderUtil :: getTilesForABbox() - " + outputFile + " file could not be downloaded, Some exception occured.");
			e.printStackTrace();
		} catch (InterruptedException e) {
			logger.info("In TileProviderUtil :: getTilesForABbox() - " + outputFile + " file could not be downloaded, Some exception occured.");
			e.printStackTrace();
		}
		File dir1 = new File( pathOfMapFolder.substring(0,pathOfMapFolder.length()-1));
		if(dir1.exists()) {
			deleteDirectory(dir1);
		}
		return outputFile; 
	}
	
	public boolean deleteDirectory(File dir) { 
		if (dir.isDirectory()) { 
			File[] children = dir.listFiles(); 
			for (int i = 0; i < children.length; i++)  { 
				boolean success = deleteDirectory(children[i]); 
				if (!success) { 
					return false; 
				}
			}
		} 
//		System.out.println("removing file or directory : " + dir.getName());
		return dir.delete();
	}

	
	public String getStringSizeLengthFile(long size) {

	    DecimalFormat df = new DecimalFormat("0.00");

	    float sizeKb = 1024.0f;
//	    float sizeMb = sizeKb * sizeKb;
//	    float sizeGb = sizeMb * sizeKb;
//	    float sizeTerra = sizeGb * sizeKb;

	    return df.format(size / sizeKb);
//	    if(size < sizeMb)
//	        return df.format(size / sizeKb)+ " Kb";
//	    else if(size < sizeGb)
//	        return df.format(size / sizeMb) + " Mb";
//	    else if(size < sizeTerra)
//	        return df.format(size / sizeGb) + " Gb";

//	    return "";
	}
	public void updateMapFileMetadata(BboxContainerObject bboxObject, String maxZoom, String minZoom, String mapBaseUrl, MapFileData mapFileData) {
		Double north = bboxObject.getNorth();
		Double south = bboxObject.getSouth();
		Double east = bboxObject.getEast();
		Double west = bboxObject.getWest();
		String outputFile = north + "_" + south + "_" + east + "_" + west + ".zip";
		MapFileData mapData = new MapFileData();
    	mapData.setMapFileName(outputFile);
    	mapData.setFileSize((mapFileData != null && mapFileData.getFileSize() != null && !mapFileData.getFileSize().isEmpty())  ? mapFileData.getFileSize() : "0");
    	mapData.setInsertTs(System.currentTimeMillis());
    	mapData.setUploaded(true);
    	mapfileDataCrudService.insertMapData(mapData);
	}
	public void getFilesize(String outputFile) {
		MapFileData mapFileData = mapfileDataCrudService.findDataByPartitionKey(outputFile);
		if (mapFileData != null && mapFileData.getUploaded()) {
			logger.info("In TileProviderUtil :: getTilesForABbox() - " +outputFile+ " file already exist");
		} 
		Long size;
		try {
			size = uploadZipToS3Util.ifExists(outputFile, SuperAppAndAppIdsConstants.HAIMS);
		
		if( size != null ) {
			logger.info("In TileProviderUtil :: getTilesForABbox() - " +outputFile+ " file already exist is S3");
			if(mapFileData == null || mapFileData.getFileSize() == null) {
				mapFileData =  new MapFileData();
				mapFileData.setFileSize(getStringSizeLengthFile(size));
			}
			
			MapFileData mapData = new MapFileData();
	    	mapData.setMapFileName(outputFile);
	    	mapData.setFileSize((mapFileData != null && mapFileData.getFileSize() != null && !mapFileData.getFileSize().isEmpty())  ? mapFileData.getFileSize() : "0");
	    	mapData.setInsertTs(System.currentTimeMillis());
	    	mapData.setUploaded(true);
	    	mapfileDataCrudService.insertMapData(mapData);
		}} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

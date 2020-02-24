package com.vassarlabs.proj.uniapp.constants;

import java.util.ArrayList;
import java.util.Random;

public class MapConstants {
	
	public static final String jarFile = "OSMMapTilePackager-6.1.1-SNAPSHOT.jar";
//	public static final String jarLocation = "/home/vassar/offlinemap_project_directory/map_files/";
//	public static final String jarLocation = "production jar location";
	//staging jar-location
	public static final String jarLocation = "/home/vassar/offline_maps/";
	
	public static final Double extraRadiusInKm = 2.0;
	public static final Double bboxRadius = 0.2;
	public static final Double extraBbox = 1.0;
	
	public static final String mapBaseUrlS3 = "https://prrd.s3-us-west-2.amazonaws.com/mapfiles/";
	
	public static final String mapMarkers = "map_markers";
	public static final String minZoom = "min_zoom";
	public static final String maxZoom = "max_zoom";
	public static final String offlineMapSourceName = "offline_map_source_name";
	public static final String offlineMapFiles = "offline_map_files";
	public static final String boundingBox = "bounding_box";
	
	public static String tempFolder = "vassarMaps";
	public static String zmin = "0";
	public static String zmax = "16";

	@SuppressWarnings("serial")
	public static ArrayList<String> serverUrls = new ArrayList<String>() {{ 
		add("http://tile.openstreetmap.org/%d/%d/%d.png");
	    add("http://a.tile.openstreetmap.org/%d/%d/%d.png");
	    add("http://b.tile.openstreetmap.org/%d/%d/%d.png");
	    add("http://c.tile.openstreetmap.org/%d/%d/%d.png");
	    add("http://d.tile.openstreetmap.org/%d/%d/%d.png");
	}};
	
	public static String getServerUrl() { 
        Random rand = new Random(); 
        return serverUrls.get(rand.nextInt(serverUrls.size())); 
    }
	
	

}

package com.peritus.peritusofflinemap;

import android.graphics.drawable.Drawable;
import android.location.Location;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public interface MapInterface {


    //To download map
    public void mapDownload(String baseurl, String fileurl, String filepath);

    //Set map to MapView
    public void setMap(MapView map, String filePath, String fileName);

    //Set map to MapView With custom location
    public void setMap(MapView map, String filePath, String fileName, GeoPoint geoPoint, float zoomLevel);

    //Set KML
    public void setKMLFromAssets(MapView map, String filename);

    //Set GeoJSON
    public void setGeojson(MapView map, String filename);

    //Set Marker
    public void setItem(MapView map, double lat, double longg, String name, String icon, String packageName);

    //Set Multiple Markers
    public void setMultipleItems(MapView map, ArrayList<ArrayList<String>> mlocations, String packageName);

    //find Surrounding Structures
    public int alertUser(int distance, ArrayList<GeoPoint> locationList);

    //find Bearing
    public float bearing(double startLatitude, double startLongitude, double endLatitude, double endLongitude);

    //Set Destination
    public void setDestination(GeoPoint destinationGeoPoint, String destinationName);

    //Download Map
    public void downloadMap(String file_url, String path);

    //Set Marker Items
    public void setMarkerItems(String markerName, String markerIcon, String packageName);

    public Location getCurrentLocation();

    //Set item with Custom Layout
    public void setItem(Marker mMarker, MapView map, double lat, double longg, String name, String icon, String packageName);


    //Set multiple items with Custom Layout
    public void setMultipleItems(MapView map, ArrayList<Marker> markers, ArrayList<ArrayList<String>> mlocations, String packageName);

    //Show GeoJSON
    public void showGeoJsonObjects(MapView map, String str, String mapType,String packageName);

    public ArrayList<String> getPercolationZones();
    public ArrayList<String> getStorageZones();
    public ArrayList<String> getNotSuitableZones();
    public ArrayList<String> getOtherZones();
    public ArrayList<String> getPercolationStorageZones();
    public ArrayList<String> getZonesList();

    public void showLegends(ArrayList<String> legendList,ArrayList<String> legendNamesList,LinearLayout  linearLayout);
    public void hideOrShowLayer(MapView map,int index,boolean flag);
    public void showGeoJsonObjects(MapView map, ArrayList<String> map_layers, ArrayList<String> map_types, String packageName);
    public void downloadMapList(String file_url, List<String> file_name, String path);
    public void hideOrShowLayer(ProgressBar pb, MapView map, int index, boolean flag);
    public void downloadMap(String file_url,String file_name, String path);

    public void setMultipleMap(MapView map, String filePath, List<String> fileName);
    public void setMultipleMap(MapView map, String filePath, List<String> fileName, GeoPoint geoPoint, float zoomLevel);
    public void showLegendsItems(ArrayList<String> legendList, ArrayList<String> legendNamesList, LinearLayout linearLayout);

    public List<CheckBox> getLegendItemCheckboxes();
    public List<LinearLayout> getLegendItemLayouts();


    //New Implementations
    public void setMultipleItemsCIP(MapView map, ArrayList<Marker> markers, ArrayList<ArrayList<String>> mlocations, String packageName,int typeOfPath, ArrayList<String> layerIndex);
    public void setItemCIP(MapView map, double lat, double longg, String name, String icon, String packageName, int typeOfPath,String layerIndex);
    public void setItemCIP(Marker mMarker, MapView map, double lat, double longg, String name, String icon, String packageName, int typeOfPath,String layerIndex);
    public void setMultipleItemsCIP(MapView map, ArrayList<ArrayList<String>> mlocations, String packageName, int typeOfPath, ArrayList<String> layerIndex);
    public void showLegendsItemsCIP(ArrayList<String> legendList, ArrayList<String> legendNamesList, LinearLayout linearLayout, int typeOfPath);


    //New 2
    public void showGeoJsonObjectsCIP(MapView map, ArrayList<String> map_layers, ArrayList<String> map_types, String packageName,int fileFrom,String filePath, ArrayList<String> layerIndex);
    public void addClickListener(MapView map, MapEventsOverlay mapEventsOverlay, String id);
    public void drawCircle(MapView map,GeoPoint geoPoint,Float radius,String fillColor,String strokeColor,int strokeWidth,String title,String layerIndex);
    public List<String> getDownloadIDs();
    public void CheckDownloadStatus(List<String>  download_ids,String path);
    public void downloadMapListWithStatus(String file_url, List<String> file_name, String path);
}

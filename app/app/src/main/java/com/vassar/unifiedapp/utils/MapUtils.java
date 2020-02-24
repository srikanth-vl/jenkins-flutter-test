package com.vassar.unifiedapp.utils;

import android.content.Context;
import android.os.Environment;

import com.google.android.gms.common.util.IOUtils;
import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.api.NewImageDownloaderService;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.model.IncomingImage;
import com.vassar.unifiedapp.model.MapConfigurationV1;
import com.vassar.unifiedapp.model.MapInfo;
import com.vassar.unifiedapp.model.OfflineMapFile;
import com.vassar.unifiedapp.model.Project;
import com.vassar.unifiedapp.model.ProjectList;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.MapView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapUtils {

    private static MapUtils mInstance = null;

    private MapUtils() { }

    public static MapUtils getInstance() {
        if (mInstance == null)
            mInstance = new MapUtils();
        return mInstance;
    }

    public void downloadMapMarkers(List<MapInfo> mapMarkers) {
        for (MapInfo marker : mapMarkers) {
            String markerIconUrl = marker.getIconUrl();
            if (markerIconUrl != null && !markerIconUrl.isEmpty()) {
                String path = getMapElementIcon(markerIconUrl);
            }
        }
    }

    public void downloadMapLayers(List<OfflineMapFile> mapLayers) {
        for (OfflineMapFile layer : mapLayers) {
            if (layer.fileAdditionalInfo != null && !layer.fileAdditionalInfo.isEmpty()
                    && layer.fileAdditionalInfo.containsKey(Constants.PROJECT_LIST_MAP_ELEMENT_ICON_URL)) {
                String iconUrl = layer.fileAdditionalInfo.get(Constants.PROJECT_LIST_MAP_ELEMENT_ICON_URL);
                if (iconUrl != null && !iconUrl.isEmpty()) {
                    String iconPath = getMapElementIcon(iconUrl);
                }
            }
        }
    }

    public String saveMapGeojsonToStorage(Context context, InputStream inputStream, String fileUrl) {

        File root = android.os.Environment.getExternalStorageDirectory();

        File storageDir = new File(root.getAbsolutePath() + "/"+ "Android" + "/" + "data" + "/" +
                context.getPackageName() + "/" + UAAppContext.getInstance().getUserID() + "/" + "Offlinemap" + "/" + "Geojson");

        if (!storageDir.exists()) {
            boolean makeDir = storageDir.mkdirs();
        }
        File f = new File(fileUrl);

        FileOutputStream fileOutputStream = null;
        File geojson = new File(storageDir, f.getName());

        try {
            fileOutputStream = new FileOutputStream(geojson);
            IOUtils.copyStream(inputStream, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return geojson.getAbsolutePath();
    }

    public String getGeojsonFilePath(String geojsonUrl) {
        String geojsonFilePath = "";

        File urlFile = new File(geojsonUrl);
        File root = android.os.Environment.getExternalStorageDirectory();

        File storageDir = new File(root.getAbsolutePath() + "/" + "Android" + "/" + "data" + "/" +
                UAAppContext.getInstance().getContext().getPackageName() + "/" + UAAppContext.getInstance().getUserID() + "/" + "Offlinemap" + "/" + "Geojson");

        geojsonFilePath = Utils.getInstance().findFile(urlFile.getName(), storageDir);
        if (geojsonFilePath == null || geojsonFilePath.isEmpty()) {
            IncomingImage geojsonFile = new IncomingImage(Constants.MAP_GEOJSON, null, geojsonUrl);
            NewImageDownloaderService downloadMarker = new NewImageDownloaderService(geojsonFile);
            downloadMarker.execute();
        }

        return geojsonFilePath;
    }

    public String getMapElementIcon(String markerUrl) {
        String markerFilePath = "";

        if (markerUrl != null && !markerUrl.isEmpty()) {
            File urlFile = new File(markerUrl);
            File root = android.os.Environment.getExternalStorageDirectory();
            File imageDirectory = new File(root.getAbsolutePath() + "/" + "Android" + "/" + "data" + "/" +
                    UAAppContext.getInstance().getContext().getPackageName() + "/"
                    + UAAppContext.getInstance().getContext().getResources().getString(R.string.app_name));

            if (!imageDirectory.exists()) {
                boolean makeDir = imageDirectory.mkdirs();
            }

            markerFilePath = Utils.getInstance().findFile(urlFile.getName(), imageDirectory);
            if (markerFilePath == null || markerFilePath.isEmpty()) {
                IncomingImage markerIcon = new IncomingImage(Constants.MAP_ICONS, null, markerUrl);
                NewImageDownloaderService downloadMarker = new NewImageDownloaderService(markerIcon);
                downloadMarker.execute();
            }
        }
        return markerFilePath;
    }

    public List<Map<String, String>> getProjectsForOfflineNavigation(String destination, String appId) {

        List<Map<String, String>> projects = new ArrayList<>();

        ProjectList projectList = UAAppContext.getInstance().getDBHelper().getProjectsForUser(UAAppContext
                .getInstance().getUserID(), appId);

        if (projectList != null && projectList.mProjects != null && !projectList.mProjects.isEmpty()) {
            for (Project project : projectList.mProjects) {
                if (!project.mProjectId.equalsIgnoreCase(destination) && project.mLatitude != null
                        && !project.mLatitude.isEmpty() && project.mLongitude != null && !project.mLongitude.isEmpty()) {
                    Map<String, String> projectInfo = new HashMap<>();
                    projectInfo.put("id", project.mProjectId);
                    projectInfo.put("name", project.mProjectName);
                    projectInfo.put("lat", project.mLatitude);
                    projectInfo.put("lon", project.mLongitude);
                    projects.add(projectInfo);
                }
            }
        }

        return projects;
    }

    public void setScrollableAreaLimit(MapView mapView, String bbox){
	    // Bbox for India and default zoom level
//        BoundingBox boundingBox = new BoundingBox();
//        boundingBox.set(35.4940095078, 97.4025614766, 7.96553477623, 68.1766451354);
//        mapView.setMinZoomLevel(5.0);
//        mapView.zoomToBoundingBox(boundingBox, true);

//        BoundingBox boundingBox = new BoundingBox(0, 1, 2, 3);

        if (bbox != null && !bbox.isEmpty()) {
            String[] boundingBoxLatLon = bbox.split("_");
            BoundingBox boundingBoxForZoom = new BoundingBox();
            boundingBoxForZoom.set(Double.valueOf(boundingBoxLatLon[0]), Double.valueOf(boundingBoxLatLon[2]),
                    Double.valueOf(boundingBoxLatLon[1]), Double.valueOf(boundingBoxLatLon[3]));
            mapView.setScrollableAreaLimitDouble(boundingBoxForZoom);
            mapView.zoomToBoundingBox(boundingBoxForZoom, true);
        }
        mapView.setMinZoomLevel(5.0);
    }

    public List<OfflineMapFile> getLayerFiles(){

        List<OfflineMapFile> offlineMapLayerFiles = new ArrayList<>();
        MapConfigurationV1 mapConfig = UAAppContext.getInstance().getMapConfig();
        if (mapConfig != null && mapConfig.files != null){
            for (OfflineMapFile offlineMapFile : mapConfig.files){
                if (offlineMapFile.fileName.contains(".geojson")){
                    offlineMapLayerFiles.add(offlineMapFile);
                }
            }
        }
        return offlineMapLayerFiles;
    }
}

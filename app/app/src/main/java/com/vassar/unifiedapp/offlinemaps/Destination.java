package com.vassar.unifiedapp.offlinemaps;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Destination {
    private static Destination destination = new Destination();
    List<String> file_names = new ArrayList<>();
    private GeoPoint startPoint, endPoint;
    private String startPointName, endPointName, filePath, fileName;
    private List<Map<String, String>> navExtraProjects = new ArrayList<>();
    private String mAppId;

    private Destination() {
    }

    public static Destination getDestination() {
        return destination;
    }

    public List<String> getFile_names() {
        return file_names;
    }

    public void setFile_names(List<String> file_names) {
        this.file_names = file_names;
    }

    public List<Map<String, String>> getNavExtraProjects() {
        return navExtraProjects;
    }

    public void setNavExtraProjects(List<Map<String, String>> navExtraProjects) {
        this.navExtraProjects = new ArrayList<>();
        this.navExtraProjects.addAll(navExtraProjects);
    }

    /**
     * @return a string of la: latitude, lo: longitude
     */
    public String getStartPointToString() {
        if (startPoint != null) {
            String la = String.valueOf(startPoint.getLatitude());
            String lo = String.valueOf(startPoint.getLongitude());
            return la + "," + lo;
        }
        return null;
    }

    /**
     * @return a string of la: latitude, lo: longitude
     */
    public String getEndPointToString() {
        if (endPoint != null) {
            String la = String.valueOf(endPoint.getLatitude());
            String lo = String.valueOf(endPoint.getLongitude());
            return la + "," + lo;
        }
        return null;
    }

    public GeoPoint getStartPoint() {
        return startPoint;
    }

    public String getStartPointName() {
        return startPointName;
    }

    public void setStartPoint(GeoPoint startPoint, String startPointName) {
        this.startPoint = startPoint;
        this.startPointName = startPointName;
    }

    public GeoPoint getEndPoint() {
        return endPoint;
    }

    public String getEndPointName() {
        return endPointName;
    }

    public void setEndPoint(GeoPoint endPoint, String endPointName) {
        this.endPoint = endPoint;
        this.endPointName = endPointName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setExtraProjects(List<Map<String, String>> projects) {
        navExtraProjects = new ArrayList<>();
        navExtraProjects.addAll(projects);
    }

    public String getmAppId() {
        return mAppId;
    }

    public void setmAppId(String mAppId) {
        this.mAppId = mAppId;
    }

    public List<Map<String, String>> getExtraProjects() {
        if (navExtraProjects != null) {
            return navExtraProjects;
        }
        return null;
    }

}

package com.peritus.peritusofflinemap;


import org.osmdroid.util.GeoPoint;

public class Destination {
    private GeoPoint startPoint, endPoint;
    private String startPointName, endPointName;
    private static Destination destination = new Destination();

    private Destination() {}

    public static Destination getDestination() {
        return destination;
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
}

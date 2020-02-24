//package com.vassar.unifiedapp.listener;
//
//import android.content.Context;
//import android.view.MotionEvent;
//
//import com.esri.arcgisruntime.geometry.GeometryEngine;
//import com.esri.arcgisruntime.geometry.Point;
//import com.esri.arcgisruntime.geometry.SpatialReferences;
//import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
//import com.esri.arcgisruntime.mapping.view.MapView;
//
//public class MapSingleTapListener extends DefaultMapViewOnTouchListener {
//
//    MapView mMapView;
//    Context mContext;
//    String mLatitude;
//    String mLongitude;
//
//    public MapSingleTapListener(Context context, MapView mapView) {
//        super(context, mapView);
//        mContext = context;
//        mMapView = mapView;
//    }
//
//    @Override
//    public boolean onSingleTapConfirmed(MotionEvent e) {
//        android.graphics.Point screenPoint = new android.graphics.Point(Math.round(e.getX()),
//                Math.round(e.getY()));
//        // create a map point from screen point
//        Point mapPoint = mMapView.screenToLocation(screenPoint);
//        // convert to WGS84 for lat/lon format
//        Point wgs84Point = (Point) GeometryEngine.project(mapPoint, SpatialReferences.getWgs84());
//
//        mLatitude = String.valueOf(wgs84Point.getY());
//        mLongitude = String.valueOf(wgs84Point.getX());
//        return true;
//    }
//}

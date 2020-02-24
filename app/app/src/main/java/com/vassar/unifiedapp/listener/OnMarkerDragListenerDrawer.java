package com.vassar.unifiedapp.listener;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.util.ArrayList;

class OnMarkerDragListenerDrawer implements Marker.OnMarkerDragListener {

    private ArrayList<GeoPoint> mTrace;
    private Activity mActivity;
    private MapView mMapView;

    public OnMarkerDragListenerDrawer(Activity context) {
        mTrace = new ArrayList<GeoPoint>(100);
        mActivity = context;
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        //mTrace.add(marker.getPosition());
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        mTrace.add(marker.getPosition());
        Log.d("New Position", marker.getPosition().getLatitude() + "@" + marker.getPosition().getLongitude());
        Toast.makeText(mActivity, "New Pos" + marker.getPosition().getLatitude() + "@" + marker.getPosition().getLongitude(), Toast.LENGTH_SHORT).show();
        // TODO : Consume the new
        mMapView.invalidate();
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        //mTrace.add(marker.getPosition());
    }

    static public void closeAllInfoWindowsOn(MapView mapView) {
        ArrayList<InfoWindow> opened = InfoWindow.getOpenedInfoWindowsOn(mapView);
        for (InfoWindow infoWindow : opened) {
            infoWindow.close();
        }
    }
}
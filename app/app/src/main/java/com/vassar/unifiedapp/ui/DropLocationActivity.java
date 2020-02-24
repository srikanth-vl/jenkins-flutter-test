package com.vassar.unifiedapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.peritus.peritusofflinemap.MapInterface;
import com.peritus.peritusofflinemap.OfflineMap;
import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.utils.Constants;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.util.ArrayList;

public class DropLocationActivity extends AppCompatActivity {

    private double mProjectLatitude;
    private double mProjectLongitude;

    private String mDroppedLatitude = null;
    private String mDroppedLongitude = null;

    private MapView mMapView;
    private MapInterface mMapInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drop_location);

        mProjectLatitude = Double.valueOf(getIntent().getStringExtra("lat"));
        mProjectLongitude = Double.valueOf(getIntent().getStringExtra("lon"));

        mMapView = findViewById(R.id.mapView);

        // Initializing the map
        String file_path = Environment.getExternalStorageDirectory() + "/" + getPackageName()
                + "/" + UAAppContext.getInstance().getUserID() + "/" + "Offlinemap";
        String file_name = "ap_map.map";

        // Creating map with centroid of all projects
        GeoPoint geoPoint = new GeoPoint(mProjectLatitude, mProjectLongitude);
        float zoomLevel = 12.0F;
        mMapInterface = new OfflineMap(this, this);
        mMapInterface.setMap(mMapView, file_path, file_name, geoPoint, zoomLevel);

        dropMarker();
    }

    private void  dropMarker() {
        Marker marker =  new Marker(mMapView);
        marker.setDraggable(true);
        marker.setOnMarkerDragListener(new OnMarkerDragListenerDrawer());
        mMapInterface.setItem(marker, mMapView, mProjectLatitude, mProjectLongitude, "Marker", "map_marker_icon", this.getPackageName());
        mDroppedLatitude = String.valueOf(mProjectLatitude);
        mDroppedLongitude = String.valueOf(mProjectLongitude);
    }

    public void submitLocation(View view) {
        if (mDroppedLatitude == null && mDroppedLongitude == null) {
            Toast.makeText(this, getResources().getString(R.string.GEOTAG_WASNT_SET), Toast.LENGTH_SHORT).show();
        } else {
            Intent data = new Intent();
            data.putExtra("lat", mDroppedLatitude);
            data.putExtra("lon", mDroppedLongitude);
            setResult(RESULT_OK, data);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent data = new Intent();
        setResult(RESULT_CANCELED, data);
        finish();
    }

    class OnMarkerDragListenerDrawer implements Marker.OnMarkerDragListener {

        private ArrayList<GeoPoint> mTrace;

        public OnMarkerDragListenerDrawer() {
            mTrace = new ArrayList<GeoPoint>(100);
        }

        @Override
        public void onMarkerDrag(Marker marker) {
            //mTrace.add(marker.getPosition());
        }

        @Override
        public void onMarkerDragEnd(Marker marker) {
            mTrace.add(marker.getPosition());
            Log.d("New Position", marker.getPosition().getLatitude() + "@" + marker.getPosition().getLongitude());
            Toast.makeText(DropLocationActivity.this, "New Pos" + marker.getPosition()
                    .getLatitude() + "@" + marker.getPosition().getLongitude(), Toast.LENGTH_SHORT).show();
            mDroppedLatitude = String.valueOf(marker.getPosition().getLatitude());
            mDroppedLongitude = String.valueOf(marker.getPosition().getLongitude());
            mMapView.invalidate();
        }

        @Override
        public void onMarkerDragStart(Marker marker) {
            //mTrace.add(marker.getPosition());
        }

        public void closeAllInfoWindowsOn() {
            ArrayList<InfoWindow> opened = InfoWindow.getOpenedInfoWindowsOn(mMapView);
            for (InfoWindow infoWindow : opened) {
                infoWindow.close();
            }
        }
    }

    // MAKING MAPS CLICKABLE
    // TODO : DO NOT DELETE
//    @Override
//    public boolean longPressHelper(GeoPoint p) {
//
//        // Can only place one marker
//        // TODO : Cannot drag or move it at the moment, make marker movable
//
//        if (!mMarkerPlaced) {
//            Constant.markerClickLocation = new GeoPoint(p);
//            Marker marker =  new Marker(mMapView);
//            marker.setInfoWindow(new CustomMarkerInfoWindow(mMapView, null, null));
//            mMapInterface.setItem(marker, mMapView, p.getLatitude(), p.getLongitude(), "Marker", "map_marker_icon", this.getPackageName());
//            mDroppedLatitude = String.valueOf(p.getLatitude());
//            mDroppedLongitude = String.valueOf(p.getLongitude());
//            mMarkerPlaced = true;
//        } else {
//            Toast.makeText(this, getResources().getString(R.string.one_marker_only),
//                    Toast.LENGTH_LONG).show();
//        }
//
//        return true;
//    }
//
//    // On single tap you can get the location
//    @Override
//    public boolean singleTapConfirmedHelper(GeoPoint p) {
//        return true;
//    }
//
//    private void makeMapClickable() {
//        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this);
//        mMapView.getOverlays().add(0, mapEventsOverlay);
//    }
}

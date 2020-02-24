package com.peritus.peritusofflinemap;


import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

public class CustomMarkerInfoWindow extends MarkerInfoWindowOffline {

    MapView MmapView;

    public CustomMarkerInfoWindow(MapView mapView) {
        super(R.layout.custom_info, mapView);
        MmapView = mapView;


    }

    @Override
    public void onOpen(Object item) {

        closeAllInfoWindowsOn(MmapView);
        super.onOpen(item);

        mView.setVisibility(View.VISIBLE);
    }

}

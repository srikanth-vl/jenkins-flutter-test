package com.peritus.peritusofflinemap;


import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

public class CustomMarkerInfoWindowNew extends MarkerInfoWindow {

    MapView MmapView;
    public CustomMarkerInfoWindowNew(MapView mapView) {
        super(R.layout.marker_click, mapView);
        MmapView = mapView;
        Button btn = (Button)(mView.findViewById(org.osmdroid.bonuspack.R.id.bubble_moreinfo));

        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "Button clicked Project", Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override public void onOpen(Object item){

        super.onOpen(item);
        mView.findViewById(org.osmdroid.bonuspack.R.id.bubble_moreinfo).setVisibility(View.VISIBLE);
    }

}

package com.vassar.unifiedapp.ui;


import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

//import com.mapbox.geojson.Feature;
//import com.mapbox.geojson.FeatureCollection;
//import com.mapbox.geojson.Point;
//import com.mapbox.mapboxsdk.camera.CameraPosition;
//import com.mapbox.mapboxsdk.geometry.LatLng;
//import com.mapbox.mapboxsdk.maps.MapView;
//import com.mapbox.mapboxsdk.maps.MapboxMap;
//import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
//import com.mapbox.mapboxsdk.maps.Style;
//import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
//import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
//import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.model.Project;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProjectListMapViewFragment extends Fragment {

    private ArrayList<Project> mProjects = new ArrayList<>();
    private double mCenterLat;
    private double mCenterLon;
//    MapView mapView;

    public ProjectListMapViewFragment() {
        // Required empty public constructor
    }

    @SuppressLint("ValidFragment")
    public ProjectListMapViewFragment(ArrayList<Project> projects) {
        mProjects.clear();
        mProjects.addAll(projects);

        double sumLat = 0.0;
        double sumLon = 0.0;
        for (Project project : mProjects) {
            sumLat += Double.valueOf(project.mLatitude);
            sumLon += Double.valueOf(project.mLongitude);
        }

        // Calculating the center of all the project geotag, for loading map
        mCenterLat = sumLat/mProjects.size();
        mCenterLon = sumLon/mProjects.size();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_project_list_map_view,
                container, false);

        final double lat = mCenterLat;
        final double lon = mCenterLon;

//        mapView = (MapView) view.findViewById(R.id.project_list_map_view);
//        mapView.getMapAsync(new OnMapReadyCallback() {
//            @Override
//            public void onMapReady(@NonNull MapboxMap mapboxMap) {
//
//                mapboxMap.setCameraPosition(new CameraPosition.Builder()
//                        .target(new LatLng(lat, lon))
//                        .zoom(5)
//                        .build());
//
//                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
//                    @Override
//                    public void onStyleLoaded(@NonNull Style style) {
//
//                        // Map is set up and the style has loaded.
//                        // Now you can add data or make other map adjustments
//                        List<Feature> markerCoordinates = new ArrayList<>();
//
//                        for (Project project : mProjects) {
//
//                            markerCoordinates.add(Feature.fromGeometry(
//                                    Point.fromLngLat(Double.parseDouble(project.mLongitude)
//                                            , Double.parseDouble(project.mLatitude))));
//                        }
//
//                        style.addSource(new GeoJsonSource("marker-source",
//                                FeatureCollection.fromFeatures(markerCoordinates)));
//
//                        // Add the marker image to map
//                        style.addImage("my-marker-image", BitmapFactory.decodeResource(
//                                getActivity().getResources(), R.drawable.map_marker_icon));
//
//                        // Adding an offset so that the bottom of the blue icon gets fixed to the coordinate, rather than the
//                        // middle of the icon being fixed to the coordinate point.
//                        style.addLayer(new SymbolLayer("marker-layer", "marker-source")
//                                .withProperties(PropertyFactory.iconImage("my-marker-image"),
//                                        PropertyFactory.iconOffset(new Float[]{0f, -9f})));
//
//                        // Add the selected marker source and layer
//                        style.addSource(new GeoJsonSource("selected-marker"));
//
//                        // Adding an offset so that the bottom of the blue icon gets fixed to the coordinate, rather than the
//                        // middle of the icon being fixed to the coordinate point.
//                        style.addLayer(new SymbolLayer("selected-marker-layer", "selected-marker")
//                                .withProperties(PropertyFactory.iconImage("my-marker-image"),
//                                        PropertyFactory.iconOffset(new Float[]{0f, -9f})));
//                    }
//                });
//            }
//        });
        return view;
    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        mapView.onStart();
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        mapView.onResume();
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        mapView.onPause();
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        mapView.onStop();
//    }
//
//    @Override
//    public void onLowMemory() {
//        super.onLowMemory();
//        mapView.onLowMemory();
//    }
}

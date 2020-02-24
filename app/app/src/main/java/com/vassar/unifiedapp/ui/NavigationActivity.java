package com.vassar.unifiedapp.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.application.UnifiedAppApplication;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.model.MapConfigurationV1;
import com.vassar.unifiedapp.model.Project;
import com.vassar.unifiedapp.offlinemaps.Destination;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.MapUtils;
import com.vassar.unifiedapp.utils.Utils;

import org.mapsforge.map.rendertheme.ExternalRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderTheme;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.mapsforge.MapsForgeTileProvider;
import org.osmdroid.mapsforge.MapsForgeTileSource;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.modules.IFilesystemCache;
import org.osmdroid.tileprovider.modules.SqlTileWriter;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.NetworkLocationIgnorer;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.DirectedLocationOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class NavigationActivity extends Activity implements MapEventsReceiver, LocationListener, SensorEventListener {

    protected static int DEST_INDEX = -1;
    private static Location mCurrentLocation;
    //------------ LocationListener implementation ------------//
    private final NetworkLocationIgnorer mIgnorer = new NetworkLocationIgnorer();
    protected MapView map;
    protected GeoPoint startPoint, destinationPoint;
    protected ArrayList<GeoPoint> viaPoints;
    protected FolderOverlay mItineraryMarkers;
    protected Marker markerDestination;
    protected DirectedLocationOverlay myLocationOverlay;
    protected LocationManager mLocationManager;
    protected boolean mTrackingMode;
    protected boolean isNavigationOn;
    Context context;
    TextView speedTxt;
    MyLocationNewOverlay myLocationNewOverlay;
    ImageButton buttonMyLocation, buttonNavigation;
    float mAzimuthAngleSpeed = 0.0f;
    ArrayList<GeoPoint> locationList;
    List<String> fileNames;
    //Structure Alert Radius
    float radiusVal = 100F;
    //Other Structure Alert Radius
    float radiusValOther = 50F;
    String filePath = null;
    String fileName = null;

    private Project mCurrentProject;

    private List<Map<String, String>> navExtraProjects;
    private Map<String, Map<String, String>> coordinateToProjectMap;

	/* String getBestProvider(){
		String bestProvider = null;
		//bestProvider = locationManager.getBestProvider(new Criteria(), true); // => returns "Network Provider"!
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
			bestProvider = LocationManager.GPS_PROVIDER;
		else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
			bestProvider = LocationManager.NETWORK_PROVIDER;
		return bestProvider;
	} */
    long mLastTime = 0; // milliseconds
    double mSpeed = 0.0; // km/h

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        isNavigationOn = false;

        locationList = new ArrayList<>();
        fileNames = new ArrayList<>();

        Configuration.getInstance().setMapViewHardwareAccelerated(true);
        MapsForgeTileSource.createInstance(getApplication());

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.navigation_activity, null);
        setContentView(v);

        SharedPreferences prefs = getSharedPreferences("OFFLINENAVIGATOR", MODE_PRIVATE);
        speedTxt = findViewById(R.id.tv_speed);
        map = v.findViewById(R.id.map);
        map.setTilesScaledToDpi(true);

//      map.getZoomController().setZoomInEnabled(false);
//      map.getZoomController().setZoomOutEnabled(false);
//      map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
//      map.setMultiTouchControls(true);
//        map.setMinZoomLevel(12.0);
//        map.setMaxZoomLevel(18.0);
        map.setVerticalMapRepetitionEnabled(false);
//        map.setScrollableAreaLimitLatitude(TileSystem.MaxLatitude, -TileSystem.MaxLatitude, 0/*map.getHeight()/2*/);

        IMapController mapController = map.getController();

        //To use MapEventsReceiver methods, we add a MapEventsOverlay:
        MapEventsOverlay overlay = new MapEventsOverlay(this);
        map.getOverlays().add(overlay);

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        mapController.setZoom(18.0);
        mapController.setCenter(new GeoPoint((double) prefs.getFloat("MAP_CENTER_LAT", 48.5f),
                (double) prefs.getFloat("MAP_CENTER_LON", 2.5f)));

//        myLocationNewOverlay = new MyLocationNewOverlay(map);
//        myLocationNewOverlay.enableMyLocation();
//        map.getOverlays().add(myLocationNewOverlay);

        myLocationOverlay = new DirectedLocationOverlay(this);

        map.getOverlays().add(myLocationOverlay);

        if (Destination.getDestination().getFilePath() != null && (Destination.getDestination().getFileName() != null || Destination.getDestination().getFile_names() != null)) {
//            filePath = Destination.getDestination().getFilePath();
//            if (Destination.getDestination().getFileName() != null) {
//                fileName = Destination.getDestination().getFileName();
//                //Set Map to MapView
//                setMapsForgeTileProvider(filePath, fileName);
//            } else {
//                fileNames = Destination.getDestination().getFile_names();
//                //Set Map to MapView
//                setMapsForgeTileProviderMultipleFiles(filePath, fileNames);
//            }

            loadMapView();
        }

        if (savedInstanceState == null) {
            Location location = null;
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location == null)
                    location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (location != null) {
                //location known:
                onLocationChanged(location);
            } else {
                //no location known: hide myLocationOverlay
                myLocationOverlay.setEnabled(false);
            }
            startPoint = null;
            destinationPoint = null;
            viaPoints = new ArrayList<GeoPoint>();
        } else {
            myLocationOverlay.setLocation(savedInstanceState.getParcelable("location"));
            //TODO: restore other aspects of myLocationOverlay...
            startPoint = savedInstanceState.getParcelable("start");
            destinationPoint = savedInstanceState.getParcelable("destination");

        }

//        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(map);
//        map.getOverlays().add(scaleBarOverlay);

        // Itinerary markers:
        mItineraryMarkers = new FolderOverlay();
        mItineraryMarkers.setName(getString(R.string.itinerary_markers_title));
        map.getOverlays().add(mItineraryMarkers);

        updateUIWithItineraryMarkers();

        if (savedInstanceState != null) {
            mTrackingMode = savedInstanceState.getBoolean("tracking_mode");
            showCurrentLocation();
        } else
            mTrackingMode = false;

        //My Location Button:
        buttonMyLocation = findViewById(R.id.buttonMyLocation);
        buttonMyLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mTrackingMode = !mTrackingMode;
                showCurrentLocation();
            }
        });

        //Navigation Button
        buttonNavigation = findViewById(R.id.buttonNavigation);
        buttonNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isNavigationOn) {
                    isNavigationOn = false;
                    buttonNavigation.setImageResource(R.drawable.ic_navigation_black_24dp);
                    Toast.makeText(context, "Navigation Off", Toast.LENGTH_LONG).show();
                    removePoint();
                    isNavigationOn = false;
//                    GeoPoint geoPoint = null;
//                    destinationPoint = null;
//                    Destination.getDestination().setEndPoint(geoPoint, "No Destination");
                } else {
                    if (Destination.getDestination().getEndPoint() != null) {
                        buttonNavigation.setImageResource(R.drawable.ic_stop_black_24dp);
                        Toast.makeText(context, "Navigation On", Toast.LENGTH_LONG).show();
                        destinationPoint = Destination.getDestination().getEndPoint();
                        addExtraStructuresForNavigation();
                        startNavigation(destinationPoint, true);
                    } else {
                        Toast.makeText(context, "Destination not defined", Toast.LENGTH_LONG).show();
                    }

                }
            }
        });

        mTrackingMode = !mTrackingMode;
        showCurrentLocation();
    }

    /**
     * Used to add all projects (other than destination) to the map, to notify the user in case
     * they are close to any project.
     * TODO : Test
     */
    private void addExtraStructuresForNavigation() {
        navExtraProjects = new ArrayList<>();
        if (Destination.getDestination().getExtraProjects() != null) {
            navExtraProjects.addAll(Destination.getDestination().getExtraProjects());
        }

        if (!navExtraProjects.isEmpty()) {
            ArrayList<GeoPoint> geolist = new ArrayList<>();
            for (Map<String, String> projectInfo : navExtraProjects) {
                String lat = projectInfo.get("lat");
                String lon = projectInfo.get("lon");
                geolist.add(new GeoPoint(Double.parseDouble(lat), Double.parseDouble(lon)));
                addToCoordinateToProjectInfoMap((lat + "," + lon), projectInfo);
            }
            locationList = geolist;
        }
    }

    private void addToCoordinateToProjectInfoMap(String coordinate, Map<String, String> projectInfo) {
        if (coordinateToProjectMap != null) {
            coordinateToProjectMap.put(coordinate, projectInfo);
        }
    }

    boolean startLocationUpdates() {
        boolean result = false;
        for (final String provider : mLocationManager.getProviders(true)) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationManager.requestLocationUpdates(provider, 2 * 1000, 0.0f, this);
                result = true;
            }
        }
        return result;
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isOneProviderEnabled = startLocationUpdates();
        myLocationOverlay.setEnabled(isOneProviderEnabled);
        //TODO: not used currently
        //mSensorManager.registerListener(this, mOrientation, SensorManager.SENSOR_DELAY_NORMAL);
        //sensor listener is causing a high CPU consumption...
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationManager.removeUpdates(this);
        }
        //TODO: mSensorManager.unregisterListener(this);
    }

    void showCurrentLocation() {
        if (mTrackingMode) {

            if (myLocationOverlay.isEnabled() && myLocationOverlay.getLocation() != null) {
                map.getController().animateTo(myLocationOverlay.getLocation());
            }
            map.setMapOrientation(-mAzimuthAngleSpeed);
            buttonMyLocation.setKeepScreenOn(true);
        } else {

            map.setMapOrientation(0.0f);
            map.getController().animateTo(myLocationOverlay.getLocation());
            buttonMyLocation.setKeepScreenOn(false);
        }
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        return false;
    }

    /**
     * Geocoding of the departure or destination address
     */
    public void startNavigation(GeoPoint destinationPoint, boolean isNavOn) {
        isNavigationOn = isNavOn;
        mTrackingMode = true;
        removePoint();
        map.invalidate();
//                destinationPoint = new GeoPoint(new GeoPoint(17.810117, 83.389324));
//                destinationPoint = new GeoPoint(new GeoPoint(17.801240, 83.379016));

        markerDestination = updateItineraryMarker(markerDestination, destinationPoint, DEST_INDEX,
                R.string.destination, R.drawable.destination_icon, -1, Destination.getDestination().getEndPointName());
        map.getController().setCenter(destinationPoint);
    }

    /**
     * Update (or create if null) a marker in itineraryMarkers.
     */
    public Marker updateItineraryMarker(Marker marker, GeoPoint p, int index,
                                        int titleResId, int markerResId, int imageResId, String address) {
        if (marker == null) {
            marker = new Marker(map);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mItineraryMarkers.add(marker);
        }
        String title = getResources().getString(titleResId);
        marker.setTitle(title);
        marker.setPosition(p);
        Drawable icon = ResourcesCompat.getDrawable(getResources(), markerResId, null);
        marker.setIcon(icon);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        if (imageResId != -1)
            marker.setImage(ResourcesCompat.getDrawable(getResources(), imageResId, null));
        marker.setRelatedObject(index);
        map.invalidate();

        return marker;
    }

    public void removePoint() {

//        destinationPoint = null;
        if (markerDestination != null) {
            markerDestination.closeInfoWindow();
//            mItineraryMarkers.remove(markerDestination);
//            markerDestination = null;
        }


    }

    public void updateUIWithItineraryMarkers() {
        mItineraryMarkers.closeAllInfoWindows();
        mItineraryMarkers.getItems().clear();
//        //Start marker:
//        if (startPoint != null) {
//            markerStart = updateItineraryMarker(null, startPoint, START_INDEX,
//                    R.string.departure, R.drawable.marker_departure, -1, null);
//        }
        //Destination marker if any:
        if (destinationPoint != null) {
            markerDestination = updateItineraryMarker(null, destinationPoint, DEST_INDEX,
                    R.string.destination, R.drawable.destination_icon, -1, null);
        }
    }

    boolean setMapsForgeTileProviderMultipleFiles(String filePath, List<String> fileName) {

        try {

//            String path = Environment.getExternalStorageDirectory().getPath() + "/OfflineMap/";
            String path = filePath;
//            Toast.makeText(context, "Loading Map... " + path, Toast.LENGTH_LONG).show();
            File folder = new File(path);
            File[] listOfFiles = folder.listFiles();
            if (listOfFiles == null)
                return false;
            //Build a list with only .map files; get rendering config file if any:
            File renderingFile = null;
            ArrayList<File> listOfMapFiles = new ArrayList<>(listOfFiles.length);
            for (File file : listOfFiles) {

                for (String file_name : fileName) {

                    if (file.isFile() && file.getName().endsWith(".map") && file.getName().equalsIgnoreCase(file_name)) {

                        listOfMapFiles.add(file);
                    } else if (file.isFile() && file.getName().endsWith(".xml")) {
                        renderingFile = file;
                    }
                }
            }
            listOfFiles = new File[listOfMapFiles.size()];
            listOfFiles = listOfMapFiles.toArray(listOfFiles);

            //Use rendering file if any
            XmlRenderTheme theme = null;
            try {
                if (renderingFile != null)
                    theme = new ExternalRenderTheme(renderingFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            MapsForgeTileSource source = MapsForgeTileSource.createFromFiles(listOfFiles, theme, "rendertheme-v4");
            MapsForgeTileProvider mfProvider = new MapsForgeTileProvider(new SimpleRegisterReceiver(context), source, null);
            map.setTileProvider(mfProvider);


        } catch (Exception e) {
            Toast.makeText(context, "MAP has not been downloaded completely.Please download MAP again", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    boolean setMapsForgeTileProvider(String filePath, String fileName) {
        String path = filePath;
        Toast.makeText(this, "Loading Map..", Toast.LENGTH_LONG).show();
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles == null)
            return false;

        //Build a list with only .map files; get rendering config file if any:
        File renderingFile = null;
        ArrayList<File> listOfMapFiles = new ArrayList<>(listOfFiles.length);
        for (File file : listOfFiles) {
            if (file.isFile() && file.getName().endsWith(".map") && file.getName().equalsIgnoreCase(fileName)) {
                listOfMapFiles.add(file);
            } else if (file.isFile() && file.getName().endsWith(".xml")) {
                renderingFile = file;
            }
        }
        listOfFiles = new File[listOfMapFiles.size()];
        listOfFiles = listOfMapFiles.toArray(listOfFiles);

        //Use rendering file if any
        XmlRenderTheme theme = null;
        try {
            if (renderingFile != null)
                theme = new ExternalRenderTheme(renderingFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        MapsForgeTileSource source = MapsForgeTileSource.createFromFiles(listOfFiles, theme, "rendertheme-v4");
        MapsForgeTileProvider mfProvider = new MapsForgeTileProvider(new SimpleRegisterReceiver(context), source, null);
        map.setTileProvider(mfProvider);
		/*
		map.getController().setZoom((double)source.getMinimumZoomLevel());
		map.zoomToBoundingBox(source.getBoundsOsmdroid(), true);
		*/

        return true;
    }

    @Override
    public void onLocationChanged(final Location pLoc) {
        if (pLoc != null) {
            mCurrentLocation = pLoc;
            long currentTime = System.currentTimeMillis();
            if (mIgnorer.shouldIgnore(pLoc.getProvider(), currentTime))
                return;
            double dT = currentTime - mLastTime;
            if (dT < 100.0) {
                //Toast.makeText(this, pLoc.getProvider()+" dT="+dT, Toast.LENGTH_SHORT).show();
                return;
            }
            mLastTime = currentTime;

            GeoPoint newLocation = new GeoPoint(pLoc);
            if (!myLocationOverlay.isEnabled()) {
                //we get the location for the first time:
                myLocationOverlay.setEnabled(true);
                map.getController().animateTo(newLocation);
            }

            GeoPoint prevLocation = myLocationOverlay.getLocation();
            myLocationOverlay.setLocation(newLocation);

            //Bhavani
//            Drawable d2 = context.getDrawable(R.drawable.nav_arrow_96);
//            Bitmap bitmap2 = ((BitmapDrawable) d2).getBitmap();
//            myLocationOverlay.setDirectionArrow(bitmap2);
            int id = context.getResources().getIdentifier(context.getPackageName() + ":drawable/" + "nav_arrow_96", null, null);
            Drawable d2 = context.getResources().getDrawable(id);
            Bitmap bitmap2 = ((BitmapDrawable) d2).getBitmap();
            Bitmap bmp = Bitmap.createScaledBitmap(bitmap2,100, 100,true);
            myLocationOverlay.setDirectionArrow(bmp);

            //
            myLocationOverlay.setAccuracy((int) pLoc.getAccuracy());

            if (prevLocation != null && pLoc.getProvider().equals(LocationManager.GPS_PROVIDER)) {

                mSpeed = pLoc.getSpeed() * 3.6;
                long speedInt = Math.round(mSpeed);
                speedTxt.setText(speedInt + " km/h");

                if (isNavigationOn) {

                    //TODO: check if speed is not too small
                    if (mSpeed >= 0.1) {

                        float bearingDestination = bearing(pLoc.getLatitude(), pLoc.getLongitude(), Destination.getDestination().getEndPoint().getLatitude(), Destination.getDestination().getEndPoint().getLongitude());

                        Location currentLoc = ((UnifiedAppApplication)getApplication()).mLocation;

                        Location location = new Location(LocationManager.GPS_PROVIDER);
                        location.setLatitude(Destination.getDestination().getEndPoint().getLatitude());
                        location.setLongitude(Destination.getDestination().getEndPoint().getLongitude());

//
//                        if (currentLoc != null){
//                            bearingDestination = bearingDestination
//                        }else{
                            bearingDestination = currentLoc.bearingTo(location);
//                        }

                        myLocationOverlay.setBearing(bearingDestination);


                    } else {

                        float bearingDestination = bearing(pLoc.getLatitude(), pLoc.getLongitude(), Destination.getDestination().getEndPoint().getLatitude(), Destination.getDestination().getEndPoint().getLongitude());

                        Location currentLoc = ((UnifiedAppApplication)getApplication()).mLocation;

                        Location location = new Location(LocationManager.GPS_PROVIDER);
                        location.setLatitude(Destination.getDestination().getEndPoint().getLatitude());
                        location.setLongitude(Destination.getDestination().getEndPoint().getLongitude());
                        location.setLongitude(Destination.getDestination().getEndPoint().getLongitude());

//                        if (currentLoc != null){
//                            bearingDestination = bearingDestination - currentLoc.getBearing();
//                        }else{
                            bearingDestination = currentLoc.bearingTo(location);
//                        }
//
                        myLocationOverlay.setBearing(bearingDestination);
                    }
                } else {

                    myLocationOverlay.setBearing(pLoc.getBearing());
                }
            }

            if (isNavigationOn) {
                //keep the map view centered on current location:
                map.getController().animateTo(newLocation);
                map.setMapOrientation(-mAzimuthAngleSpeed);
                map.getController().setCenter(newLocation);

                alertUser(newLocation);

            } else {
                //just redraw the location overlay:
                map.invalidate();

            }

//        map.getController().animateTo(newLocation);
//        map.getController().setCenter(newLocation);
        }

    }

    public void alertUser(final GeoPoint mcLatLong) {

        if (mcLatLong != null) {
            if (checkDistance(mcLatLong.getLatitude(), mcLatLong.getLongitude(), Destination.getDestination().getEndPoint().getLatitude(), Destination.getDestination().getEndPoint().getLongitude())) {
                playAlertSound();
            } else {
                isThereAnyStructureNearTOMe();
            }
        }
    }

    private void playAlertSound() {


        try {
            int resID = getResources().getIdentifier("alert", "raw", getPackageName());
            MediaPlayer mediaPlayer = MediaPlayer.create(this, resID);
            mediaPlayer.start();
        } catch (Exception e) {
        }
    }

    private boolean checkDistance(double currentLocLat, double currentLocLong, double placeLocLat, double placeLocLong) {
        float radius = radiusVal;
        float[] results = new float[3];
        Location.distanceBetween(currentLocLat, currentLocLong, placeLocLat, placeLocLong, results);
        return (results[0] <= radius);

    }

    public void isThereAnyStructureNearTOMe() {

        for (int i = 0; i < locationList.size(); i++) {
            GeoPoint g1 = new GeoPoint(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            GeoPoint g2 = new GeoPoint(locationList.get(i).getLatitude(), locationList.get(i).getLongitude());
            double d = g1.distanceToAsDouble(g2);

            if (d < radiusValOther) {

                playOtherAlertSound(i);

                showProjectSnackbar(i);

                break;

            }
        }
    }

    private void showProjectSnackbar(int index) {
        if (locationList != null && !locationList.isEmpty()) {
            double lat = locationList.get(index).getLatitude();
            double lon = locationList.get(index).getLongitude();

            if (coordinateToProjectMap != null && !coordinateToProjectMap.isEmpty()) {
                Map<String, String> projectInfo = coordinateToProjectMap.get(lat + "," + lon);
                if (projectInfo != null && !projectInfo.isEmpty()) {
                    String id = projectInfo.get("id");
                    String name = projectInfo.get("name");
                    mCurrentProject = UAAppContext.getInstance().getProjectFromProjectList(id);
                    Snackbar mySnackbar = Snackbar.make(findViewById(R.id.snackbar_message),
                            name, Snackbar.LENGTH_LONG);
                    mySnackbar.setAction(getResources().getString(R.string.EDIT), new MySnackBarButtonListener());
                    mySnackbar.setActionTextColor(Color.WHITE);
                    mySnackbar.show();

                    // TODO : What should be done about the navigation. Does it resume after the form is submitted or canceled??
                }
            }
        }
    }

    private void playOtherAlertSound(int i) {

        try {
            int resID = getResources().getIdentifier("other_alert", "raw", getPackageName());
            MediaPlayer mediaPlayer = MediaPlayer.create(this, resID);
            mediaPlayer.start();
        } catch (Exception e) {
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    //------------ SensorEventListener implementation
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        myLocationOverlay.setAccuracy(accuracy);
        map.invalidate();
    }

    //static float mAzimuthOrientation = 0.0f;
    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ORIENTATION:
                if (mSpeed < 0.1) {
					/* TODO Filter to implement...
					float azimuth = event.values[0];
					if (Math.abs(azimuth-mAzimuthOrientation)>2.0f){
						mAzimuthOrientation = azimuth;
						myLocationOverlay.setBearing(mAzimuthOrientation);
						if (mTrackingMode)
							map.setMapOrientation(-mAzimuthOrientation);
						else
							map.invalidate();
					}
					*/
                }
                //at higher speed, we use speed vector, not phone orientation.
                break;
            default:
                break;
        }
    }

    protected float bearing(double startLatitude, double startLongitude, double endLatitude, double endLongitude) {

        double lat1 = startLatitude;
        double lng1 = startLongitude;

// destination

        double lat2 = endLatitude;
        double lng2 = endLongitude;

        double dLon = (lng2 - lng1);
        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
        double brng = Math.toDegrees((Math.atan2(y, x)));
        brng = (360 - ((brng + 360) % 360));
        return (float) brng;

    }

    /**
     * callback to store activity status before a restart (orientation change for instance)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("location", myLocationOverlay.getLocation());
        outState.putBoolean("tracking_mode", mTrackingMode);
        outState.putParcelable("start", startPoint);
        outState.putParcelable("destination", destinationPoint);


        savePrefs();
    }

    void savePrefs() {
        SharedPreferences prefs = getSharedPreferences("OFFLINENAVIGATOR", MODE_PRIVATE);
        SharedPreferences.Editor ed = prefs.edit();
        ed.putFloat("MAP_ZOOM_LEVEL_F", (float) map.getZoomLevelDouble());
        GeoPoint c = (GeoPoint) map.getMapCenter();
        ed.putFloat("MAP_CENTER_LAT", (float) c.getLatitude());
        ed.putFloat("MAP_CENTER_LON", (float) c.getLongitude());
        MapTileProviderBase tileProvider = map.getTileProvider();
        String tileProviderName = tileProvider.getTileSource().name();
        ed.putString("TILE_PROVIDER", tileProviderName);

        ed.apply();
    }

    private class CacheClearer extends AsyncTask<Void, Void, Boolean> {
        protected Boolean doInBackground(Void... params) {
            IFilesystemCache tileWriter = map.getTileProvider().getTileWriter();
            if (tileWriter instanceof SqlTileWriter) {
                return ((SqlTileWriter) tileWriter).purgeCache();
            } else
                return false;
        }

        protected void onPostExecute(Boolean result) {
            if (result)
                Toast.makeText(map.getContext(), "Cache Purge successful", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(map.getContext(), "Cache Purge failed", Toast.LENGTH_SHORT).show();
        }
    }

    public class MySnackBarButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getBaseContext(), ProjectFormActivity.class);
            intent.putExtra(Constants.PROJECT_FORM_INTENT_APP_ID, Destination.getDestination().getmAppId());
            intent.putExtra(Constants.PROJECT_FORM_INTENT_PROJECT_ID, mCurrentProject.mProjectId);
            if (mCurrentProject.mState.equalsIgnoreCase("New")) {
                intent.putExtra(Constants.FORM_ACTION_TYPE, Constants.INSERT_FORM_KEY);
            } else {
                intent.putExtra(Constants.FORM_ACTION_TYPE, Constants.UPDATE_FORM_KEY);
            }
            intent.putExtra(Constants.PROJECT_LIST_GROUPING_ATTRIBUTE, "");
            intent.putExtra(Constants.PROJECT_LIST_GROUPING_ATTRIBUTE_VALUE, "");

            startActivity(intent);
        }
    }

    private void loadMapView() {

        map.setUseDataConnection(false);

        GeoPoint geoPoint = null;
        double currentLat = ((UnifiedAppApplication) getApplicationContext()).mUserLatitude;
        double currentLon = ((UnifiedAppApplication) getApplicationContext()).mUserLongitude;

        if (currentLat != Constants.DEFAULT_LATITUDE && currentLon != Constants.DEFAULT_LONGITUDE){
            geoPoint = new GeoPoint(currentLat, currentLon);

        } else{
            Utils.logInfo("LAT LON ARE NUll");
        }

        MapConfigurationV1 mapConfig = UAAppContext.getInstance().getMapConfig();
        String mapSourceName = mapConfig.mapSourceName;

        IMapController mapController = null;
        int minZoom = UAAppContext.getInstance().getMapConfig().getMinZoom();
        int maxZoom = UAAppContext.getInstance().getMapConfig().getMaxZoom();
        if (mapSourceName != null && !mapSourceName.isEmpty()) {
            map.setTileSource(new XYTileSource(mapSourceName, minZoom, maxZoom, 256, ".png", new String[]{""}));
            map.setMultiTouchControls(true);
            mapController = map.getController();
            mapController.setCenter(geoPoint);
            mapController.setZoom(14.0);

            MapUtils.getInstance().setScrollableAreaLimit(map, mapConfig.boundingBox);

            CustomZoomButtonsController zoomController = map.getZoomController();
            zoomController.setOnZoomListener(new CustomZoomButtonsController.OnZoomListener() {
                @Override
                public void onVisibilityChanged(boolean b) {

                }
                @Override
                public void onZoom(boolean b) {
                    markerDestination = updateItineraryMarker(null, destinationPoint, DEST_INDEX,
                            R.string.destination, R.drawable.destination_icon, -1, null);
                }
            });

            map.invalidate();
        } else {
            // Error - Project geolocations inconsistent
            Toast.makeText(this, "No valid map source. Please sync and try again later.", Toast.LENGTH_LONG).show();
        }
    }
}

package com.vassar.unifiedapp.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peritus.peritusofflinemap.MapInterface;
import com.peritus.peritusofflinemap.OfflineMap;
import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.application.UnifiedAppApplication;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.GpsValidation;
import com.vassar.unifiedapp.model.MapConfigurationV1;
import com.vassar.unifiedapp.model.MapFieldInfo;
import com.vassar.unifiedapp.model.MapInfo;
import com.vassar.unifiedapp.model.OfflineMapFile;
import com.vassar.unifiedapp.offlinemaps.MapHelper;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.MapUtils;
import com.vassar.unifiedapp.utils.StringUtils;
import com.vassar.unifiedapp.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapActivity extends AppCompatActivity {

    private MapInterface mMapInterface;
    private MapView mMapView;

    private String mMapFunctionType;
    private MapFieldInfo mMapFieldInfo;
    private GpsValidation mGpsValidation;
    private String mCenterLat;
    private String mCenterLon;
    private LinearLayout mLegendLayout;
    private FloatingActionButton mFab;

    private List<String> mapLayers = new ArrayList<>();

    private ArrayList<String> invisibleLayers = new ArrayList<>();

    private String mAppId;

    private Marker mMarker;

    private String mDroppedLat;
    private String mDroppedLon;

    private boolean mMarkerDropped = false;

    private Map<String, JSONObject> mSubmittedFields = new HashMap<>();
    private HashMap<String, String> submittedFieldFromIntent = new HashMap<>();

    private String submittedLat;
    private String submittedLon;
    private boolean isLegendOpen = false;
    private ProgressBar mProgressBar;

    private List<OfflineMapFile> offlineMapLayers = new ArrayList<>();

    /* Different use cases of the activity
     * 1. Static marker (Type 1)
     * 2. Drop Marker (Type 2)
     * 3. Drag & Drop marker (Type 3)
     * 4. Load marker from the intent
     * 5. Load layers from intent
     * 6. Return result, if required */

    /* Intent data
     * 1. Map Function Type - static/drop/dragAndDrop
     * 2. Latitude
     * 3. Longitude
     * 4. Drag&Drop enabled?? */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mMapView = findViewById(R.id.mapView);
        mLegendLayout = findViewById(R.id.legend_scroll_view_layout);
        mFab = findViewById(R.id.map_activity_fab);
        mProgressBar = findViewById(R.id.map_activity_progress_bar);

        if (savedInstanceState == null) {
            mMapFieldInfo = getIntent().getParcelableExtra("map_field_info");
            mGpsValidation = getIntent().getParcelableExtra("gps_validation");
            submittedFieldFromIntent = (HashMap<String, String>) getIntent().getSerializableExtra("submittedFields");
            mSubmittedFields = convertStringToJSONObjectInMap(submittedFieldFromIntent);

            if (mMapFieldInfo != null && mMapFieldInfo.getMapActivityType() != null && !mMapFieldInfo.getMapActivityType().isEmpty()) {
                mMapFunctionType = mMapFieldInfo.getMapActivityType();
            }

            mCenterLat = getIntent().getStringExtra(Constants.PROJECT_LATITUDE);
            mCenterLon = getIntent().getStringExtra(Constants.PROJECT_LONGITUDE);

            submittedLat = getIntent().getStringExtra(Constants.LATITUDE_KEY);
            submittedLon = getIntent().getStringExtra(Constants.LONGITUDE_KEY);

            mAppId = getIntent().getStringExtra(Constants.PROJECT_TYPE_CONFIG_APP_ID_KEY);
            causeDelayAndLoadMap();
        } else {
            mCenterLat = savedInstanceState.getString(Constants.LATITUDE_KEY);
            mCenterLon = savedInstanceState.getString(Constants.LONGITUDE_KEY);

            mMapFunctionType = savedInstanceState.getString("mapFunction");

            mDroppedLat = savedInstanceState.getString("droppedLat");
            mDroppedLon = savedInstanceState.getString("droppedLon");

            mMarkerDropped = savedInstanceState.getBoolean("markerDropped");
            mAppId = savedInstanceState.getString(Constants.PROJECT_TYPE_CONFIG_APP_ID_KEY);

            submittedFieldFromIntent = (HashMap<String, String>) savedInstanceState.getSerializable("submittedFieldsFromIntent");
            mSubmittedFields = convertStringToJSONObjectInMap(submittedFieldFromIntent);

            submittedLat = savedInstanceState.getString(Constants.LATITUDE_KEY);
            submittedLon = savedInstanceState.getString(Constants.LONGITUDE_KEY);

        }

        if (mMapFieldInfo != null && mMapFieldInfo.getMapActivityLayers() != null) {
            mapLayers = mMapFieldInfo.getMapActivityLayers();
            getFormSpecificLayers();
        }

        createActivityButton();
        ((UnifiedAppApplication)this.getApplication()).initializeLocation();
        ((UnifiedAppApplication)this.getApplication()).startLocationUpdates();
    }

    private void causeDelayAndLoadMap() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading Map Layers");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setProgress(0);
        progressDialog.show();

        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        loadMapView();
                        progressDialog.dismiss();
                    }
                });
            }
        }).start();
    }

    private void createActivityButton() {
        Button button = findViewById(R.id.map_activity_button);
        switch (mMapFunctionType) {
            case "drop":
            case "dragAndDrop":
                button.setText(getResources().getString(R.string.submit));
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mDroppedLat != null && mDroppedLon != null &&
                                !mDroppedLat.isEmpty() && !mDroppedLon.isEmpty()) {

                            // TODO : Check if we need to validate the form field
                            // If valid return success, otherwise prompt the user

                            if (mGpsValidation == null) {
                                submitResult();
                                return;
                            }

                            if (mGpsValidation.getType() != null && !mGpsValidation.getType().isEmpty() && mGpsValidation.getRadius() > 0) {
                                switch (mGpsValidation.getType()) {
                                    case "circular":
                                        if (mGpsValidation.getSource().equalsIgnoreCase("project")) {
                                            if (mCenterLat != null && !mCenterLat.isEmpty() && mCenterLon != null && !mCenterLon.isEmpty()){
                                                 if(mDroppedLat != null && !mDroppedLat.isEmpty() && mDroppedLon != null && !mDroppedLon.isEmpty()) {
                                                     boolean valid = validateGps(mCenterLat, mCenterLon, mDroppedLat, mDroppedLon);
                                                     if (valid) {
                                                         submitResult();
                                                     } else {
                                                         Toast.makeText(getBaseContext(), "Please select a valid location.", Toast.LENGTH_SHORT).show();
                                                     }
                                                 }
                                            } else {
                                                Toast.makeText(getBaseContext(), "Values for validation not found.", Toast.LENGTH_SHORT).show();
                                                submitResult();
                                            }
                                        } else if (mGpsValidation.getSource().equalsIgnoreCase("key")) {
                                            String valueFromSource = getSubmittedFieldFromKey(mSubmittedFields, mGpsValidation.getSource());
                                            List<String> geotag = getGeotagFromValue(mGpsValidation.getKeyType(), valueFromSource);

                                            if (geotag.get(0) != null && !geotag.get(0).isEmpty() && geotag.get(1) != null && !geotag.get(1).isEmpty()) {
                                                if (mDroppedLat != null && !mDroppedLat.isEmpty() && mDroppedLon != null && !mDroppedLon.isEmpty()) {
                                                    boolean valid = validateGps(geotag.get(0), geotag.get(1), mDroppedLat, mDroppedLon);
                                                    if (valid) {
                                                        submitResult();
                                                    } else {
                                                        Toast.makeText(getBaseContext(), "Please select a valid location.", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }else {
                                                Toast.makeText(getBaseContext(), "Values for validation not found.", Toast.LENGTH_SHORT).show();
                                                submitResult();
                                            }
                                        }
                                        break;

                                    case "bbox":

                                        break;
                                    default:
                                }
                            }

                        } else {
                            Toast.makeText(MapActivity.this, "Please select a valid location.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                break;
            case "static":
            default:
                button.setText(getResources().getString(R.string.cancel));
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.PROJECT_LATITUDE, mCenterLat);
        outState.putString(Constants.PROJECT_LONGITUDE, mCenterLon);
        outState.putString("mapFunction", mMapFunctionType);
        outState.putString("droppedLat", mDroppedLat);
        outState.putString("droppedLon", mDroppedLon);
        outState.putBoolean("markerDropped", mMarkerDropped);
        outState.putString(Constants.PROJECT_TYPE_CONFIG_APP_ID_KEY, mAppId);
        outState.putParcelable("gps_validation", mGpsValidation);
        outState.putSerializable("submittedFieldsFromIntent", submittedFieldFromIntent);
        outState.putString(Constants.LATITUDE_KEY, submittedLat);
        outState.putString(Constants.LONGITUDE_KEY, submittedLon);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent data = new Intent();
        setResult(RESULT_CANCELED, data);
        finish();
    }

    private void submitResult() {
        Intent data = new Intent();
        data.putExtra("lat", mDroppedLat);
        data.putExtra("lon", mDroppedLon);
        setResult(RESULT_OK, data);
        finish();
    }

    private void loadMapView() {

        // Initializing the map
        mMapInterface = new OfflineMap(this, this);
        GeoPoint geoPoint;
        IMapController mapController = null;

        // Loading maps from zip files
        mMapView.setUseDataConnection(false);
        MapConfigurationV1 mapConfig = UAAppContext.getInstance().getMapConfig();
        String mapSourceName = mapConfig.mapSourceName;
        int minZoom = UAAppContext.getInstance().getMapConfig().getMinZoom();
        int maxZoom = UAAppContext.getInstance().getMapConfig().getMaxZoom();
        if(mapSourceName != null && !mapSourceName.isEmpty()) {
            mMapView.setTileSource(new XYTileSource(mapSourceName, minZoom, maxZoom, 256, ".png", new String[]{""}));
            mMapView.setMultiTouchControls(true);
            mapController = mMapView.getController();
            mapController.setZoom(14.0);

            MapUtils.getInstance().setScrollableAreaLimit(mMapView, mapConfig.boundingBox);

        } else {
            // Error - Project geolocations inconsistent
            Toast.makeText(this, "No valid map source. Please sync and try again later.", Toast.LENGTH_LONG).show();
            Utils.logError(LogTags.MAP_CONFIG, "No valid map source" +
                    "-- continuing without loading map view");
        }

        switch (mMapFunctionType) {
            case "static":
                geoPoint = new GeoPoint(Double.parseDouble(mCenterLat), Double.parseDouble(mCenterLon));
                mapController.setCenter(geoPoint);
                // Drop marker that isnt moveable
                createMarker(null, null);

                break;
            case "drop":
                // TODO : Can load map in the centre of AP??

                // The map should be clickable
                // Long press will drop a marker (only 1 marker)
                // That marker can be moved

                MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(new MapEventsReceiver() {
                    @Override
                    public boolean singleTapConfirmedHelper(GeoPoint p) {
                        return true;
                    }

                    @Override
                    public boolean longPressHelper(GeoPoint p) {

                        if (!mMarkerDropped) {
                            Toast.makeText(MapActivity.this, "New Pos" + p.getLatitude()
                                    + "@" + p.getLongitude(), Toast.LENGTH_SHORT).show();
                            mDroppedLat = String.valueOf(p.getLatitude());
                            mDroppedLon = String.valueOf(p.getLongitude());
                            Utils.logInfo("Dropped lat lon : ", mDroppedLat + " , " + mDroppedLon);
                            createMarker(mDroppedLat, mDroppedLon);
                            makeMarkerDragable();
                            mMarkerDropped = true;
                        } else {
                            Toast.makeText(MapActivity.this, "Only one marker can be " +
                                    "dropped on the map. It can be dragged and relocated.", Toast.LENGTH_LONG).show();
                        }
                        return true;
                    }
                });
                mMapInterface.addClickListener(mMapView, mapEventsOverlay,"clicklistener");

                break;
            case "dragAndDrop":
                geoPoint = new GeoPoint(Double.parseDouble(mCenterLat), Double.parseDouble(mCenterLon));
                mapController.setCenter(geoPoint);
                // drop marker that is moveable
                createMarker(null, null);
                makeMarkerDragable();

                break;
            default:
                break;
        }

        loadLayers();
        loadLegend();
        addNewLayerTogglesToLegend();
        initializeFloatingButtonListener();

        if (mCenterLat != null && !mCenterLat.isEmpty()
                && mCenterLon != null && !mCenterLon.isEmpty()) {
            mMapInterface.drawCircle(mMapView,new GeoPoint(Double.parseDouble(mCenterLat),
                    Double.parseDouble(mCenterLon)), (float) mGpsValidation.getRadius(),"#00000000","#000000", 2,"","21");
        }
    }

    private void createMarker(String droppedLat, String droppedLon) {

        mMarker = new Marker(mMapView);
        if (droppedLat != null && !droppedLat.isEmpty() && droppedLon != null && !droppedLon.isEmpty()){
            mMapInterface.setItemCIP(mMarker, mMapView, Double.parseDouble(droppedLat),
                    Double.parseDouble(droppedLon), "Project",
                    "map_marker_icon", this.getPackageName(), 1, "20");
            Utils.logInfo("DROPPED LAT_LON ", droppedLat + " , " + droppedLon);
        } else {
            if (submittedLat != null && !submittedLat.isEmpty()
                    && submittedLon != null && !submittedLon.isEmpty()) {

                Utils.logInfo("PREVIOUS LAT_LON ", submittedLat + " , " + submittedLon);
                mMapInterface.setItemCIP(mMarker, mMapView, Double.parseDouble(submittedLat),
                        Double.parseDouble(submittedLon), "Project",
                        "map_marker_icon", this.getPackageName(), 1, "20");

            } else if (mCenterLat != null && mCenterLon != null
                    && !mCenterLat.isEmpty() && !mCenterLon.isEmpty() && mMapInterface != null) {

                Utils.logInfo("PROJECT LAT_LON ", mCenterLat + " , " + mCenterLon);
                mMapInterface.setItemCIP(mMarker, mMapView, Double.parseDouble(mCenterLat),
                        Double.parseDouble(mCenterLon), "Project",
                        "map_marker_icon", this.getPackageName(), 1, "20");
            }
        }
    }

    private void makeMarkerDragable() {
        if (mMapInterface != null && mMarker != null && mMapView != null) {
            mMarker.setDraggable(true);
            mMarker.setOnMarkerDragListener(new MapActivity.OnMarkerDragListenerDrawer());
        }
    }

    private class OnMarkerDragListenerDrawer implements Marker.OnMarkerDragListener {

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
            Toast.makeText(MapActivity.this, "New Pos" + marker.getPosition()
                    .getLatitude() + "@" + marker.getPosition().getLongitude(), Toast.LENGTH_SHORT).show();
            mDroppedLat = String.valueOf(marker.getPosition().getLatitude());
            mDroppedLon = String.valueOf(marker.getPosition().getLongitude());
            mMapView.invalidate();
        }

        @Override
        public void onMarkerDragStart(Marker marker) {
            //mTrace.add(marker.getPosition());
        }
    }

    public String getSubmittedFieldFromKey(Map<String, JSONObject> submittedFields, String sskey) {
        for (String key : submittedFields.keySet()) {
            JSONObject valueObject = submittedFields.get(key);
            String[] keys = key.split("#");
            String finalKey = getKeyName(keys[keys.length - 1]);
            if (finalKey.equalsIgnoreCase(sskey)) {
                try {
                    String val = valueObject.getString("val");
                    return val;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private String getKeyName(String keyName) {
        List<String> keys = Arrays.asList
                (keyName.trim().split("\\$\\$"));
        String key = null;
        if (keys != null && !keys.isEmpty()) {
            key = keys.get(keys.size() - 1);
        }
        return key;
    }

    // Render Overlays method
    public void loadLayers() {

        File root = android.os.Environment.getExternalStorageDirectory();

        ArrayList<String> mapLayerFiles = new ArrayList<>();
        ArrayList<String> mapTypes = new ArrayList<>();
        ArrayList<String> layerIndices = new ArrayList<>();

        String storageDir = "";

        MapConfigurationV1 mapConfig = UAAppContext.getInstance().getMapConfig();
        if(mapConfig != null && mapConfig.getFiles() != null && !mapConfig.getFiles().isEmpty()) {

            for (OfflineMapFile offlineMapFile : offlineMapLayers) {

                mapLayerFiles.add(offlineMapFile.fileName);
                if (offlineMapFile.fileAdditionalInfo != null && !offlineMapFile.fileAdditionalInfo.isEmpty()) {
                    if (offlineMapFile.fileAdditionalInfo.containsKey(Constants.PROJECT_LIST_MAP_ELEMENT_LABEL)){
                        mapTypes.add(offlineMapFile.getFileAdditionalInfo().get(Constants.PROJECT_LIST_MAP_ELEMENT_LABEL));
                    }
                    if (offlineMapFile.fileAdditionalInfo.containsKey(Constants.PROJECT_LIST_MAP_ELEMENT_INDEX)){
                        layerIndices.add(offlineMapFile.getFileAdditionalInfo().get(Constants.PROJECT_LIST_MAP_ELEMENT_INDEX));
                    }
                }
                if (offlineMapFile.fileStoragePath != null && !offlineMapFile.fileStoragePath.isEmpty()) {
                    storageDir = root.getAbsolutePath() + offlineMapFile.fileStoragePath + "/";
                }
            }

            for (OfflineMapFile mapLayer : offlineMapLayers) {
                if (!mapLayerFiles.contains(mapLayer.fileName)){
                    invisibleLayers.add(mapLayer.fileName);
                }
            }
            mMapInterface.showGeoJsonObjectsCIP(mMapView, mapLayerFiles, mapTypes, this.getPackageName(), 2, storageDir, layerIndices);

        } else {
            Toast.makeText(this, "Cannot load map overlays.", Toast.LENGTH_LONG).show();
            Utils.logError(LogTags.MAP_CONFIG, "Error getting MapConfig -- null -- continuing without loading overlays");
        }
    }

    private void loadLegend() {
        if (mLegendLayout != null) {
            mLegendLayout.removeAllViews();
            ArrayList<String> legendData = createLegendData();
            ArrayList<String> legendNames = createLegendNamesData();

            // No icons are found in storage
            if (legendData.isEmpty()) {
                ArrayList<String> icons = new ArrayList<>();
                for (int i = 0; i < legendNames.size(); i++) {
                    icons.add("map_marker_icon");
                }
                mMapInterface.showLegendsItemsCIP(icons, legendNames, mLegendLayout, 1);
            } else {
                mMapInterface.showLegendsItemsCIP(legendData, legendNames, mLegendLayout, 2);
            }
        } else {
            // Error - No legend view initialized
            Utils.logError(LogTags.PROJECT_LIST, "No legend view initialized" +
                    "-- continuing without loading legend");
        }
    }

    //Legend methods
    private ArrayList<String> createLegendData() {

        ArrayList<String> icons = new ArrayList<>();
        ArrayList<String> mapLayerName = new ArrayList<>();

        for (OfflineMapFile offlineMapFile : offlineMapLayers){
            if (!invisibleLayers.contains(offlineMapFile.fileName) && !mapLayerName.contains(offlineMapFile.fileName)){
                if (offlineMapFile.fileAdditionalInfo != null && !offlineMapFile.fileAdditionalInfo.isEmpty() &&
                        offlineMapFile.fileAdditionalInfo.containsKey(Constants.PROJECT_LIST_MAP_ELEMENT_ICON_URL)){
                    String iconPath = MapUtils.getInstance().getMapElementIcon(
                            offlineMapFile.fileAdditionalInfo.get(Constants.PROJECT_LIST_MAP_ELEMENT_ICON_URL));
                    if (iconPath != null && !iconPath.isEmpty()){
                        icons.add(iconPath);
                    }
                    mapLayerName.add(offlineMapFile.fileName);
                }
            }
        }
        return icons;
    }

    private ArrayList<String> createLegendNamesData() {

        ArrayList<String> names = new ArrayList<>();

        for (OfflineMapFile mapInfo : offlineMapLayers) {
            if (mapInfo.fileAdditionalInfo != null && !mapInfo.fileAdditionalInfo.isEmpty()) {
                String layerLabel = mapInfo.fileAdditionalInfo.get(Constants.PROJECT_LIST_MAP_ELEMENT_LABEL);
                if (!invisibleLayers.contains(mapInfo.fileName) && !names.contains(layerLabel)) {
                    names.add(layerLabel);
                }
            }
        }
        return names;
    }

    private boolean validateGps(String centerLatitude, String centerLongitude, String droppedLatitude, String droppedLongtiude) {
        double centerLat = Double.parseDouble(centerLatitude);
        double centerLon = Double.parseDouble(centerLongitude);

        double droppedLat = Double.parseDouble(droppedLatitude);
        double droppedLon = Double.parseDouble(droppedLongtiude);

        double radius = mGpsValidation.getRadius();

        float[] results = new float[3];
        Location.distanceBetween(centerLat, centerLon, droppedLat, droppedLon, results);
        Utils.logInfo("DISTANCE BETWEEN CENTER AND NEW LOCATION ", String.valueOf(results[0]));
        return (results[0] <= radius);
    }

    private Map<String, JSONObject> convertStringToJSONObjectInMap(Map<String, String> map){
        ObjectMapper mapper = new ObjectMapper();
        Map<String, JSONObject> convertedMap = new HashMap<>();
        for (String key : map.keySet()){
            String jsonString = map.get(key);
            if (jsonString != null && !jsonString.isEmpty()){
                JSONObject jsonObject = mapper.convertValue(jsonString, JSONObject.class);
                convertedMap.put(key, jsonObject);
            }
        }
        return convertedMap;
    }

    public List<String> getGeotagFromValue(String keyType, String value){
        List<String> geotag = new ArrayList<>();
        if (keyType.equalsIgnoreCase("image")){
            List<String> geotagWithUUID = StringUtils.getStringListFromDelimiter(value, Constants.IMAGE_UUID_LONG_LAT_SEPARATOR);
            geotag.add(geotagWithUUID.get(1));
            geotag.add(geotagWithUUID.get(2));
        } else if (keyType.equalsIgnoreCase("geotag")){
            geotag = StringUtils.getStringListFromDelimiter(value, ",");
        }
        return geotag;
    }

    // Add toggle method
    private void addNewLayerTogglesToLegend() {

        List<LinearLayout> legendItemLayouts = new ArrayList<>();
        List<CheckBox> legendCheckboxes = new ArrayList<>();

        legendItemLayouts.addAll(mMapInterface.getLegendItemLayouts());
        legendCheckboxes.addAll(mMapInterface.getLegendItemCheckboxes());

        for (int i = 0; i < legendItemLayouts.size(); i++) {

            CheckBox checkBox = legendCheckboxes.get(i);
            LinearLayout linearLayout = legendItemLayouts.get(i);
            TextView legendText = (TextView) linearLayout.getChildAt(1);

            OfflineMapFile mapElement = getInfoFromEntityName(legendText.getText().toString());

            checkBox.setChecked(true);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    int index = 1;
                    if (mapElement != null && mapElement.fileAdditionalInfo != null
                            && !mapElement.fileAdditionalInfo.isEmpty() &&
                            mapElement.fileAdditionalInfo.containsKey(Constants.PROJECT_LIST_MAP_ELEMENT_INDEX)) {

                        String layerIndex = mapElement.fileAdditionalInfo.get(Constants.PROJECT_LIST_MAP_ELEMENT_INDEX);
                        if (layerIndex != null && !layerIndex.isEmpty()) {
                            index = Integer.parseInt(layerIndex);
                        }
                    }
                    if (!isChecked) {
                        mMapInterface.hideOrShowLayer(mProgressBar, mMapView, index, false);
                        mMapView.invalidate();

                    } else {
                        mMapInterface.hideOrShowLayer(mProgressBar, mMapView, index, true);
                        mMapView.invalidate();
                    }
                }
            });
        }
    }

    // Get MapInfo from map entity name( for layers only)
    public OfflineMapFile getInfoFromEntityName(String entityName) {

        for (OfflineMapFile mapLayer : offlineMapLayers) {
            if (mapLayer.fileAdditionalInfo != null && !mapLayer.fileAdditionalInfo.isEmpty()) {
                if (mapLayer.fileAdditionalInfo.containsKey(Constants.PROJECT_LIST_MAP_ELEMENT_LABEL)) {
                    if (mapLayer.fileAdditionalInfo.get(Constants.PROJECT_LIST_MAP_ELEMENT_LABEL).equalsIgnoreCase(entityName)) {
                        return mapLayer;
                    }
                }
            }
        }
        return null;
    }

    private void initializeFloatingButtonListener() {
        if (mFab != null && mLegendLayout != null) {
            mFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isLegendOpen) {
                        // Legend is open, collapse
                        mLegendLayout.setVisibility(View.GONE);
                        isLegendOpen = false;
                    } else {
                        // Open legend
                        mLegendLayout.setVisibility(View.VISIBLE);
                        isLegendOpen = true;
                    }
                }
            });
        }
    }

    private void getFormSpecificLayers(){

        for (OfflineMapFile offlineMapFile : MapUtils.getInstance().getLayerFiles()){
            for (String layerLabel : mapLayers){
                if (offlineMapFile.fileAdditionalInfo != null && !offlineMapFile.fileAdditionalInfo.isEmpty()
                        && offlineMapFile.fileAdditionalInfo.containsKey(Constants.PROJECT_LIST_MAP_ELEMENT_LABEL)){
                    if (layerLabel.equalsIgnoreCase(offlineMapFile.fileAdditionalInfo.get(Constants.PROJECT_LIST_MAP_ELEMENT_LABEL))){
                        offlineMapLayers.add(offlineMapFile);
                    }
                }
            }
        }
    }
}

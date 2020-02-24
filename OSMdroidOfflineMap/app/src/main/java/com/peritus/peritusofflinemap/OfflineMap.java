package com.peritus.peritusofflinemap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.peritus.peritusofflinemap.server.RetrofitInterface;

import org.mapsforge.map.rendertheme.ExternalRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderTheme;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.KmlFeature;
import org.osmdroid.bonuspack.kml.KmlLineString;
import org.osmdroid.bonuspack.kml.KmlPlacemark;
import org.osmdroid.bonuspack.kml.KmlPoint;
import org.osmdroid.bonuspack.kml.KmlPolygon;
import org.osmdroid.bonuspack.kml.KmlTrack;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.mapsforge.MapsForgeTileProvider;
import org.osmdroid.mapsforge.MapsForgeTileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.mylocation.DirectedLocationOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.content.ContentValues.TAG;
import static android.content.Context.DOWNLOAD_SERVICE;

public class OfflineMap implements MapInterface, LocationListener {

    private static final String DOWNLOAD_PATH = "com.peritus.peritusofflinemap_DownloadMapService_Download_path";
    private static final String DOWNLOAD_FILE_NAME = "com.peritus.peritusofflinemap_DownloadMapService_Download_file_name";
    private static final String DESTINATION_PATH = "com.peritus.peritusofflinemap_DownloadMapService_Destination_path";

    DownloadManager downloadManager;
    long File_DownloadId = 0;
    String filePath = null;
    static List<String> download_ids;

    public ProgressDialog progressDialog;
    public static ArrayList<String> Percolation_Zone;
    public static ArrayList<String> Percolation_Storage_Zone;
    public static ArrayList<String> Storage_Zone;
    public static ArrayList<String> Not_Suitable_Zone;
    public static ArrayList<String> Other_Zone;


    public static ArrayList<String> overlaysList;


    public ArrayList<String> legendIcon;
    public ArrayList<String> LegendName;


    public static ArrayList<String> ZonesList;

    Context context;
    Activity activity;


    private static Location mCurrentLocation;
    private Location mLastLocation;
    private LocationManager locationManager;

    protected DirectedLocationOverlay myLocationOverlay;

    NotificationCompat.Builder notificationBuilder;
    NotificationManagerCompat notificationManagerCompat;
    int DOWNLOAD_NOTIFICATION_ID = 100;
    String contentTitle = "Downloading Map";
    boolean success;
    DownloadZipFileTask downloadZipFileTask;
    MapView map;
    GeoPoint mClickLocation;

    String clickMarkerName = null;
    String clickMarkerIcon = null;
    String markerpackageName = null;
    MapEventsOverlay mapEventsOverlay;

    //your items
    Bitmap myBitmap;

    List<LinearLayout> legendItemLayouts;
    List<CheckBox> legendItemCheckboxes;


    public OfflineMap(Context context, Activity activity, MapView map) {
        this.context = context;
        this.activity = activity;
        this.map = map;

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        MapsForgeTileSource.createInstance(activity.getApplication());

        checkGpsAvailability(activity);
        ensureLastLocationInit();
        updateCurrentLocation(null);

    }

    public void setMarkerItems(String markerName, String markerIcon, String packageName) {

        clickMarkerName = markerName;
        clickMarkerIcon = markerIcon;
        markerpackageName = packageName;
    }

    public OfflineMap(Context context, Activity activity) {
        this.context = context;

        this.activity = activity;
        Percolation_Zone = new ArrayList<>();
        Percolation_Storage_Zone = new ArrayList<>();
        Storage_Zone = new ArrayList<>();
        Not_Suitable_Zone = new ArrayList<>();
        Other_Zone = new ArrayList<>();
        ZonesList = new ArrayList<>();
        overlaysList = new ArrayList<>();


        legendIcon = new ArrayList<>();
        LegendName = new ArrayList<>();
        download_ids = new ArrayList<>();

        legendItemLayouts = new ArrayList<>();
        legendItemCheckboxes = new ArrayList<>();


        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        MapsForgeTileSource.createInstance(activity.getApplication());

        checkGpsAvailability(activity);
        ensureLastLocationInit();
        updateCurrentLocation(null);


    }


    private void ensureLastLocationInit() {
        if (mLastLocation != null) {
            return;
        }
        try {
            Location lonet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (lonet != null) {
                mLastLocation = lonet;
                return;
            }
        } catch (SecurityException | IllegalArgumentException e) {
            log("NET-Location is not supported: " + e.getMessage());
        }
        try {
            Location logps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (logps != null) {
                mLastLocation = logps;
                return;
            }
        } catch (SecurityException | IllegalArgumentException e) {
            log("GPS-Location is not supported: " + e.getMessage());
        }
    }

    private void log(String str) {
        Log.i(this.getClass().getName(), str);
    }

    private void checkGpsAvailability(Activity context) {
        boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
            showGpsSelector(context);
        }
    }

    public static void showGpsSelector(final Activity activity) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
        builder1.setTitle(R.string.gps);
        builder1.setCancelable(true);
        builder1.setTitle(R.string.gps_is_off);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int buttonNr) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                activity.startActivity(intent);
            }
        };
        builder1.setPositiveButton(R.string.gps_settings, listener);
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }


    public void displayMessage(String s) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();

    }


    @Override
    public void setMap(MapView map, String filePath, String fileName) {
        String path = filePath + "/" + fileName;
        File file = new File(path);
        if (file.exists()) {


            map.setTileSource(TileSourceFactory.MAPNIK);

            map.setMultiTouchControls(true);
            IMapController mapController = map.getController();
            mapController.setZoom(16.0);
            GeoPoint startPoint = new GeoPoint(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            mapController.setCenter(startPoint);

            myLocationOverlay = new DirectedLocationOverlay(context);
            map.getOverlays().add(0, myLocationOverlay);
            overlaysList.add(0, "999999");
            Bitmap personBitmap = getBitmap(R.drawable.current_location);
            Bitmap arrowBitmap = getBitmap(R.drawable.ic_user_navigation_black_24dp);
            MyLocationNewOverlay myLocationNewOverlay = new MyLocationNewOverlay(map);
            myLocationNewOverlay.setDirectionArrow(personBitmap, arrowBitmap);
            myLocationNewOverlay.enableMyLocation();
            myLocationNewOverlay.setDrawAccuracyEnabled(false);
            map.getOverlays().add(1, myLocationNewOverlay);
            overlaysList.add(1, "999999");
            setMapsForgeTileProvider(map, fileName);
        } else {
            displayMessage("Map not Available");
        }


    }

    @Override
    public void setMultipleMap(MapView map, String filePath, List<String> fileName) {
        for (int pos = 0; pos < fileName.size(); pos++) {
            String path = filePath + "/" + fileName.get(pos);
            File file = new File(path);

            if (!file.exists()) {
                displayMessage("Map not Available");
                return;
            }
        }
        map.setTileSource(TileSourceFactory.MAPNIK);

        map.setMultiTouchControls(true);
        IMapController mapController = map.getController();
        mapController.setZoom(16.0);
        GeoPoint startPoint = new GeoPoint(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        mapController.setCenter(startPoint);

        myLocationOverlay = new DirectedLocationOverlay(context);
        map.getOverlays().add(0, myLocationOverlay);
        overlaysList.add(0, "999999");
        Bitmap personBitmap = getBitmap(R.drawable.current_location);
        Bitmap arrowBitmap = getBitmap(R.drawable.ic_user_navigation_black_24dp);
        MyLocationNewOverlay myLocationNewOverlay = new MyLocationNewOverlay(map);
        myLocationNewOverlay.setDirectionArrow(personBitmap, arrowBitmap);
        myLocationNewOverlay.enableMyLocation();
        myLocationNewOverlay.setDrawAccuracyEnabled(false);
        map.getOverlays().add(1, myLocationNewOverlay);
        overlaysList.add(1, "999999");
        setMapsForgeTileProviderMultipleFiles(map, filePath, fileName);


    }

    private Bitmap getBitmap(int drawableRes) {
        Drawable drawable = context.getResources().getDrawable(drawableRes);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    @Override
    public void setMap(MapView map, String filePath, String fileName, GeoPoint geoPoint, float zoomLevel) {
        String path = filePath + "/" + fileName;
        File file = new File(path);
        if (file.exists()) {

            map.setTileSource(TileSourceFactory.MAPNIK);
            map.setMultiTouchControls(true);
            IMapController mapController = map.getController();
            mapController.setZoom(zoomLevel);
            GeoPoint startPoint = new GeoPoint(geoPoint);
            mapController.setCenter(startPoint);

            myLocationOverlay = new DirectedLocationOverlay(context);
            map.getOverlays().add(0, myLocationOverlay);
            overlaysList.add(0, "999999");
            Bitmap personBitmap = getBitmap(R.drawable.current_location);
            Bitmap arrowBitmap = getBitmap(R.drawable.ic_user_navigation_black_24dp);
            MyLocationNewOverlay myLocationNewOverlay = new MyLocationNewOverlay(map);
            myLocationNewOverlay.setDirectionArrow(personBitmap, arrowBitmap);
            myLocationNewOverlay.enableMyLocation();
            myLocationNewOverlay.setDrawAccuracyEnabled(false);
            map.getOverlays().add(1, myLocationNewOverlay);
            overlaysList.add(1, "999999");

            setMapsForgeTileProvider(map, fileName);
        } else {
            displayMessage("Map not Available");
        }
    }

    @Override
    public void setMultipleMap(MapView map, String filePath, List<String> fileName, GeoPoint geoPoint, float zoomLevel) {
        for (int pos = 0; pos < fileName.size(); pos++) {
            String path = filePath + "/" + fileName.get(pos);
            File file = new File(path);

            if (!file.exists()) {
                displayMessage("Map not Available");
                return;
            }
        }
        map.setTileSource(TileSourceFactory.MAPNIK);

        map.setMultiTouchControls(true);
        IMapController mapController = map.getController();
        mapController.setZoom(zoomLevel);
        GeoPoint startPoint = new GeoPoint(geoPoint);
        mapController.setCenter(startPoint);

        myLocationOverlay = new DirectedLocationOverlay(context);
        map.getOverlays().add(0, myLocationOverlay);
        overlaysList.add(0, "999999");
        Bitmap personBitmap = getBitmap(R.drawable.current_location);
        Bitmap arrowBitmap = getBitmap(R.drawable.ic_user_navigation_black_24dp);
        MyLocationNewOverlay myLocationNewOverlay = new MyLocationNewOverlay(map);
        myLocationNewOverlay.setDirectionArrow(personBitmap, arrowBitmap);
        myLocationNewOverlay.enableMyLocation();
        myLocationNewOverlay.setDrawAccuracyEnabled(false);
        map.getOverlays().add(1, myLocationNewOverlay);
        overlaysList.add(1, "999999");
        setMapsForgeTileProviderMultipleFiles(map, filePath, fileName);


    }

    boolean setMapsForgeTileProvider(MapView map, String fileName) {

        try {

            String path = Environment.getExternalStorageDirectory().getPath() + "/OfflineMap/";
//            Toast.makeText(context, "Loading Map... " + path, Toast.LENGTH_LONG).show();
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


        } catch (Exception e) {
            Toast.makeText(context, "MAP has not been downloaded completely.Please download MAP again", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    boolean setMapsForgeTileProviderMultipleFiles(MapView map, String filePath, List<String> fileName) {

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


    public static long getFileFolderSize(File dir) {
        long size = 0;
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isFile()) {
                    size += file.length();
                } else
                    size += getFileFolderSize(file);
            }
        } else if (dir.isFile()) {
            size += dir.length();
        }
        return size;
    }

    @Override
    public void onLocationChanged(Location location) {
//        Toast.makeText(context, "Location changed", Toast.LENGTH_SHORT).show();
        updateCurrentLocation(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void updateCurrentLocation(Location location) {

        if (location != null) {
            mCurrentLocation = location;
        } else if (mLastLocation != null && mCurrentLocation == null) {
            mCurrentLocation = mLastLocation;
        }

        if (mCurrentLocation != null) {
            GeoPoint mcLatLong = new GeoPoint(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

        }

    }


    public void setKMLFromAssets(MapView map, String filename) {
        String file_name = filename;
        KmlDocument kmlDocument = new KmlDocument();
        AssetManager assetManager = context.getAssets();
        InputStream streamDeKml;

        try {
            streamDeKml = assetManager.open(file_name);
            kmlDocument.parseKMLStream(streamDeKml, null);
            streamDeKml.close();

            Drawable defaultMarker = context.getResources().getDrawable(R.drawable.location_marker);
            Bitmap defaultBitmap = ((BitmapDrawable) defaultMarker).getBitmap();

            Style defaultStyle = new Style(defaultBitmap, context.getResources().getColor(R.color.fbutton_color_carrot), 3.0f, 0x20AA1010);
            KmlFeature.Styler styler = new MyKmlStyler(defaultStyle, kmlDocument, map);
            FolderOverlay kmlOverlay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(map, defaultStyle, styler, kmlDocument);

            BoundingBox bb = kmlDocument.mKmlRoot.getBoundingBox();

            map.getController().setCenter(bb.getCenter());

            map.getOverlays().add(kmlOverlay);

            map.invalidate();

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

    }

    public void setGeojson(MapView map, String filename) {
        String jsonString = null;
        try {
            InputStream jsonStream = context.getAssets().open(filename);
            int size = jsonStream.available();
            byte[] buffer = new byte[size];
            jsonStream.read(buffer);
            jsonStream.close();
            jsonString = new String(buffer, "UTF-8");

            Drawable defaultMarker = context.getResources().getDrawable(R.drawable.location_marker);
            Bitmap defaultBitmap = ((BitmapDrawable) defaultMarker).getBitmap();

            Style defaultStyle = new Style(defaultBitmap, context.getResources().getColor(R.color.fbutton_color_midnight_blue), 3.0f, 0);
            KmlDocument kmlDocument = new KmlDocument();
            kmlDocument.parseGeoJSON(jsonString);
            FolderOverlay myOverLay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(map, defaultStyle, null, kmlDocument);
            map.getOverlays().add(myOverLay);
            map.invalidate();
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        } catch (IndexOutOfBoundsException e) {
            Toast.makeText(context, "Invalid GeoJSON File", Toast.LENGTH_SHORT).show();
        }
    }


    public void setItem(MapView map, double lat, double longg, String name, String icon, String packageName) {
        int id = context.getResources().getIdentifier(packageName + ":drawable/" + icon, null, null);
        GeoPoint loc = new GeoPoint(lat, longg);

        Marker marker = new Marker(map);
        marker.setPosition(loc);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        marker.setIcon(context.getResources().getDrawable(id));
        marker.setTitle(name);

        map.getOverlays().add(marker);

        map.invalidate();
    }

    public void setItem(Marker mMarker, MapView map, double lat, double longg, String name, String icon, String packageName) {
        int id = context.getResources().getIdentifier(packageName + ":drawable/" + icon, null, null);
        GeoPoint loc = new GeoPoint(lat, longg);

        Marker marker = mMarker;

        marker.setPosition(loc);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        marker.setIcon(context.getResources().getDrawable(id));
        marker.setTitle(name);
        map.getOverlays().add(marker);

        map.invalidate();
    }


    public void setMultipleItems(MapView map, ArrayList<ArrayList<String>> mlocations, String packageName) {

        ArrayList<ArrayList<String>> mlm = mlocations;

        if (mlm != null) {
            for (ArrayList<String> ml : mlm) {

                int id = context.getResources().getIdentifier(packageName + ":drawable/" + ml.get(1), null, null);

                GeoPoint geoPoint = new GeoPoint(Double.parseDouble(ml.get(3)), Double.parseDouble(ml.get(4)));
                Marker multipleMarker = new Marker(map);
                multipleMarker.setTitle(ml.get(2));
                multipleMarker.setSnippet(ml.get(0));
                multipleMarker.setPosition(geoPoint);
                multipleMarker.setIcon(context.getResources().getDrawable(id));
                multipleMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                map.getOverlays().add(multipleMarker);
            }


            map.invalidate();
        }
    }

    public void setMultipleItems(MapView map, ArrayList<Marker> markers, ArrayList<ArrayList<String>> mlocations, String packageName) {
        int list = 0;
        ArrayList<ArrayList<String>> mlm = mlocations;
        ArrayList<Marker> mMarkers = markers;
        if (overlaysList.size() == 0) {
            list = 0;
        } else {
            list = map.getOverlays().size();
        }

        if (mlm != null) {
            for (int mData = 0; mData < mMarkers.size(); mData++) {


                int id = context.getResources().getIdentifier(packageName + ":drawable/" + mlm.get(mData).get(1), null, null);

                GeoPoint geoPoint = new GeoPoint(Double.parseDouble(mlm.get(mData).get(3)), Double.parseDouble(mlm.get(mData).get(4)));
                mMarkers.get(mData).setTitle(mlm.get(mData).get(2));
                mMarkers.get(mData).setSnippet(mlm.get(mData).get(0));
                mMarkers.get(mData).setPosition(geoPoint);
                mMarkers.get(mData).setIcon(context.getResources().getDrawable(id));
                mMarkers.get(mData).setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                map.getOverlays().add(mMarkers.get(mData));

                overlaysList.add(list, "5");
                list++;

            }
            map.invalidate();
        }
    }

    public int alertUser(int distance, ArrayList<GeoPoint> locationList) {

        for (int i = 0; i < locationList.size(); i++) {
            GeoPoint g1 = new GeoPoint(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            GeoPoint g2 = new GeoPoint(locationList.get(i).getLatitude(), locationList.get(i).getLongitude());
            double d = g1.distanceToAsDouble(g2);

            if (d < distance) {
//                Log.d("alert", String.valueOf(i));
                playOtherAlertSound();

                return i;

            }
        }

        return -1;
    }


    private void playOtherAlertSound() {


        try {
            int resID = context.getResources().getIdentifier("other_alert", "raw", context.getPackageName());
            MediaPlayer mediaPlayer = MediaPlayer.create(context, resID);
            mediaPlayer.start();


        } catch (Exception e) {
        }
    }

    public float bearing(double startLatitude, double startLongitude, double endLatitude, double endLongitude) {

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

    public void mapDownload(String baseurl, String fileurl, final String filepath) {
        String file_path = filepath;
        File folder = new File(file_path);
        if (!folder.exists()) {
            folder.mkdir();
        }
        String path = file_path + "/" + fileurl;
        downloadUsingRetrofit(baseurl, fileurl, path);
    }

    public <T> T createService(Class<T> serviceClass, String baseUrl) {
        int timeout = 600;
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .readTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(httpClient)
                .build();
        return retrofit.create(serviceClass);
    }

    public void downloadUsingRetrofit(String baseurl, String fileurl, final String filepath) {

        RetrofitInterface downloadService = createService(RetrofitInterface.class, baseurl);
        Call<ResponseBody> call = downloadService.downloadFileWithDynamicUrlSync(fileurl);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                if (response.isSuccessful()) {


                    Toast.makeText(context, "Downloading...", Toast.LENGTH_SHORT).show();

                    downloadZipFileTask = new DownloadZipFileTask(filepath);
                    downloadZipFileTask.execute(response.body());

                } else {

                    try {
                        Toast.makeText(context, response.message(), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                    }


                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Log.e(TAG, t.getMessage());
            }
        });

    }


    private class DownloadZipFileTask extends AsyncTask<ResponseBody, Pair<Integer, Long>, String> {
        String filepath = "";

        public DownloadZipFileTask(String filepath) {
            this.filepath = filepath;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            notificationManagerCompat = NotificationManagerCompat.from(context);


            Intent notifyIntent = new Intent();

            PendingIntent notifyPendingIntent = PendingIntent.getActivity(context, DOWNLOAD_NOTIFICATION_ID, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent cancelIntent = new Intent();
//


            notificationBuilder = createNotificationBuilder("downloader_channel");
            notificationBuilder.setContentIntent(notifyPendingIntent);
            notificationBuilder.setTicker("Start downloading from the server");
            notificationBuilder.setOngoing(true);
            notificationBuilder.setAutoCancel(false);
            notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_download);
            notificationBuilder.setContentTitle(contentTitle);
            notificationBuilder.setContentText("0%");
            notificationBuilder.setProgress(100, 0, false);
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_LOW);
            notificationBuilder.addAction(R.drawable.ic_cancel_black_24dp, "Cancel download", onNewIntent(cancelIntent));
            notificationManagerCompat.notify(DOWNLOAD_NOTIFICATION_ID, notificationBuilder.build());


        }

        @Override
        protected String doInBackground(ResponseBody... urls) {
            //Copy you logic to calculate progress and call
            saveToDisk(urls[0], filepath);
            return null;
        }

        protected void onProgressUpdate(Pair<Integer, Long>... progress) {

            if (progress[0].first == 100) {
                Toast.makeText(context, "File downloaded successfully", Toast.LENGTH_SHORT).show();
                contentTitle = "Downloaded";
                success = true;
                String statusText = success ? "Done" : "Fail";
                int resId = success ? android.R.drawable.stat_sys_download_done : android.R.drawable.stat_notify_error;
                notificationBuilder.setContentTitle(contentTitle);
                notificationBuilder.setSmallIcon(resId);
                notificationBuilder.setOngoing(false);
                notificationBuilder.setAutoCancel(true);
                notificationBuilder.setContentText(statusText);
                notificationBuilder.setProgress(0, 0, false);
                notificationManagerCompat.notify(DOWNLOAD_NOTIFICATION_ID, notificationBuilder.build());
            }
            if (progress[0].second > 0) {
                int currentProgress = (int) ((double) progress[0].first / (double) progress[0].second * 100);
                notificationBuilder.setContentText(currentProgress + "%");
                notificationBuilder.setProgress(100, currentProgress, false);
                notificationManagerCompat.notify(DOWNLOAD_NOTIFICATION_ID, notificationBuilder.build());
            }
            if (progress[0].first == -1) {
                Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show();
                contentTitle = "Download failed";
                success = false;
                String statusText = success ? "Done" : "Fail";
                int resId = success ? android.R.drawable.stat_sys_download_done : android.R.drawable.stat_notify_error;
                notificationBuilder.setContentTitle(contentTitle);
                notificationBuilder.setSmallIcon(resId);
                notificationBuilder.setOngoing(false);
                notificationBuilder.setAutoCancel(true);
                notificationBuilder.setContentText(statusText);
                notificationBuilder.setProgress(0, 0, false);
                notificationManagerCompat.notify(DOWNLOAD_NOTIFICATION_ID, notificationBuilder.build());
            }
        }

        public void doProgress(Pair<Integer, Long> progressDetails) {
            publishProgress(progressDetails);
        }

        @Override
        protected void onPostExecute(String result) {
        }
    }


    private PendingIntent onNewIntent(Intent cancelIntent) {

        PendingIntent cancelPendingIntent = PendingIntent.getActivity(context, DOWNLOAD_NOTIFICATION_ID, cancelIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        return null;
    }


    private void saveToDisk(ResponseBody body, String filename) {
        try {

            File destinationFile = new File(filename);

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(destinationFile);
                byte data[] = new byte[4096];
                int count;
                int progress = 0;
                long fileSize = body.contentLength();
//                Log.d(TAG, "File Size=" + fileSize);
                while ((count = inputStream.read(data)) != -1) {
                    outputStream.write(data, 0, count);
                    progress += count;
                    Pair<Integer, Long> pairs = new Pair<>(progress, fileSize);
                    downloadZipFileTask.doProgress(pairs);
//                    Log.d(TAG, "Progress: " + progress + "/" + fileSize + " >>>> " + (float) progress / fileSize);
                }

                outputStream.flush();

//                Log.d(TAG, destinationFile.getParent());
                Pair<Integer, Long> pairs = new Pair<>(100, 100L);
                downloadZipFileTask.doProgress(pairs);
                return;
            } catch (IOException e) {
                e.printStackTrace();
                Pair<Integer, Long> pairs = new Pair<>(-1, Long.valueOf(-1));
                downloadZipFileTask.doProgress(pairs);
//                Log.d(TAG, "Failed to save the file!");

                return;
            } finally {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
//            Log.d(TAG, "Failed to save the file!");

            return;
        }
    }

    private NotificationCompat.Builder createNotificationBuilder(String channelId) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String channelName = context.getString(R.string.app_name);
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);

            }
        }
        return new NotificationCompat.Builder(context, channelId);
    }

    public void setDestination(GeoPoint destinationGeoPoint, String destinationName) {
        Toast.makeText(context, "Destination has been set", Toast.LENGTH_SHORT).show();
        Destination.getDestination().setEndPoint(destinationGeoPoint, destinationName);
    }

    public void downloadMap(String file_url, String path) {

        Toast.makeText(context, "Downloading Map...", Toast.LENGTH_SHORT).show();
        DirectoryHelper.ROOT_DIRECTORY_NAME = path;
        String MAP_DOWNLOAD_PATH = file_url;

        context.startService(DownloadMapService.getDownloadService(context, MAP_DOWNLOAD_PATH, DirectoryHelper.ROOT_DIRECTORY_NAME.concat("/")));

    }

    public void downloadMap(String file_url, String file_name, String path) {
        deleteDirectory(path, file_name);
        Toast.makeText(context, "Downloading Map...", Toast.LENGTH_SHORT).show();
        DirectoryHelper.ROOT_DIRECTORY_NAME = path;
        String MAP_DOWNLOAD_PATH = file_url + file_name;

        context.startService(DownloadMapService.getDownloadService(context, MAP_DOWNLOAD_PATH, DirectoryHelper.ROOT_DIRECTORY_NAME.concat("/")));

    }


    public Location getCurrentLocation() {

        Location location = mCurrentLocation;
        return location;
    }

    public void showGeoJsonObjects(MapView map, String str, String mapType, String packageName) {
        KmlFeature.Styler styler = null;
        int fillcolor = 0x20AA1010;
        int linecolor = 0x901010AA;
        if (mapType.equalsIgnoreCase("cadastral")) {
            fillcolor = 0;
            linecolor = context.getResources().getColor(R.color.cadastral_map_line_color);
        } else if (mapType.equalsIgnoreCase("drain")) {
            fillcolor = 0;
            linecolor = context.getResources().getColor(R.color.drain_map_line_color);
        } else if (mapType.equalsIgnoreCase("goodbad")) {
            linecolor = context.getResources().getColor(R.color.fbutton_color_pomegranate);
            fillcolor = context.getResources().getColor(R.color.fbutton_color_green_sea);
        }
        String jsonString = null;
        try {
            InputStream jsonStream = context.getAssets().open(str);
            int size = jsonStream.available();
            byte[] buffer = new byte[size];
            jsonStream.read(buffer);
            jsonStream.close();
            jsonString = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        } catch (IndexOutOfBoundsException e) {
            Toast.makeText(context, "Invalid GeoJSON File", Toast.LENGTH_SHORT).show();
        }
        KmlDocument kmlDocument = new KmlDocument();

        kmlDocument.parseGeoJSON(jsonString);

        Drawable defaultMarker = context.getResources().getDrawable(R.drawable.default_marker);
        Bitmap defaultBitmap = ((BitmapDrawable) defaultMarker).getBitmap();
        Style defaultStyle = new Style(defaultBitmap, linecolor, 3.0f, fillcolor);
        //Add styler
        if (mapType.equalsIgnoreCase("cadastral")) {
            styler = new MyKmlStyler(defaultStyle, kmlDocument, map);
        } else if (mapType.equalsIgnoreCase("drain")) {
            styler = new MyKmlStylerDrain(defaultStyle, kmlDocument, map, packageName);
        } else if (mapType.equalsIgnoreCase("goodbad")) {
            styler = new MyKmlStylerGoodBadZone(defaultStyle, kmlDocument, map);
        } else {
            styler = new MyKmlStylerOtherLayers(defaultStyle, kmlDocument, map);
        }


        FolderOverlay kmlOverlay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(map, null, styler, kmlDocument);

        map.getOverlays().add(0, kmlOverlay);

        map.invalidate();
    }

    public void showGeoJsonObjects(MapView map, ArrayList<String> map_layers, ArrayList<String> map_types, String packageName) {
//        Collections.reverse(map_layers);
//        Collections.reverse(map_types);

        int list;
        if (overlaysList.size() == 0) {
            list = 0;
        } else {
            list = map.getOverlays().size();
        }

        for (int i = 0; i < map_layers.size(); i++) {
            KmlFeature.Styler styler = null;
            int fillcolor = 0x20AA1010;
            int linecolor = 0x901010AA;
            if (map_types.get(i).equalsIgnoreCase("cadastral")) {
                fillcolor = 0;
                linecolor = context.getResources().getColor(R.color.cadastral_map_line_color);
            } else if (map_types.get(i).equalsIgnoreCase("drain")) {
                fillcolor = 0;
                linecolor = context.getResources().getColor(R.color.drain_map_line_color);
            } else if (map_types.get(i).equalsIgnoreCase("goodbad")) {
                linecolor = context.getResources().getColor(R.color.fbutton_color_pomegranate);
                fillcolor = context.getResources().getColor(R.color.fbutton_color_green_sea);
            }
            String jsonString = null;
            try {
                //From assets
                InputStream jsonStream = context.getAssets().open(map_layers.get(i));

                //From Internal Storage
//                String path =Environment.getExternalStorageDirectory().getPath() + "/OfflineMap/"+map_layers.get(i);
//                InputStream jsonStream = new FileInputStream(new File(path));

                int size = jsonStream.available();
                byte[] buffer = new byte[size];
                jsonStream.read(buffer);
                jsonStream.close();
                jsonString = new String(buffer, "UTF-8");
            } catch (FileNotFoundException ex) {
                Toast.makeText(context, map_layers.get(i) + "\t" + "File not found", Toast.LENGTH_SHORT).show();
                return;
            } catch (IndexOutOfBoundsException e) {
                Toast.makeText(context, "Invalid GeoJSON File", Toast.LENGTH_SHORT).show();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            KmlDocument kmlDocument = new KmlDocument();

            kmlDocument.parseGeoJSON(jsonString);

            Drawable defaultMarker = context.getResources().getDrawable(R.drawable.default_marker);
            Bitmap defaultBitmap = ((BitmapDrawable) defaultMarker).getBitmap();
            Style defaultStyle = new Style(defaultBitmap, linecolor, 3.0f, fillcolor);
            //Add styler
            if (map_types.get(i).equalsIgnoreCase("cadastral")) {
                styler = new MyKmlStyler(defaultStyle, kmlDocument, map);
                overlaysList.add(list, "1");
            } else if (map_types.get(i).equalsIgnoreCase("drain")) {
                overlaysList.add(list, "2");
                styler = new MyKmlStylerDrain(defaultStyle, kmlDocument, map, packageName);
            } else if (map_types.get(i).equalsIgnoreCase("goodbad")) {
                overlaysList.add(list, "3");
                styler = new MyKmlStylerGoodBadZone(defaultStyle, kmlDocument, map);

            } else {
                overlaysList.add(list, "6");
                styler = new MyKmlStylerOtherLayers(defaultStyle, kmlDocument, map);
            }

            FolderOverlay kmlOverlay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(map, null, styler, kmlDocument);

            map.getOverlays().add(list, kmlOverlay);
            list++;

            map.invalidate();

        }

    }

    public void showGeoJsonObjectsCIP(MapView map, ArrayList<String> map_layers, ArrayList<String> map_types, String packageName, int fileFrom, String filePath, ArrayList<String> layerIndex) {
//        Collections.reverse(map_layers);
//        Collections.reverse(map_types);

        int list;
        if (overlaysList.size() == 0) {
            list = 0;
        } else {
            list = map.getOverlays().size();
        }

        for (int i = 0; i < map_layers.size(); i++) {
            KmlFeature.Styler styler = null;
            int fillcolor = 0x20AA1010;
            int linecolor = 0x901010AA;
            if (map_types.get(i).equalsIgnoreCase("cadastral")) {
                fillcolor = 0;
                linecolor = context.getResources().getColor(R.color.cadastral_map_line_color);
            } else if (map_types.get(i).equalsIgnoreCase("drain")) {
                fillcolor = 0;
                linecolor = context.getResources().getColor(R.color.drain_map_line_color);
            } else if (map_types.get(i).equalsIgnoreCase("goodbad")) {
                linecolor = context.getResources().getColor(R.color.fbutton_color_pomegranate);
                fillcolor = context.getResources().getColor(R.color.fbutton_color_green_sea);
            }
            String jsonString = null;
            InputStream jsonStream = null;
            try {
                if (fileFrom == 1) {
                    //From assets
                    String path = filePath + "/" + map_layers.get(i);
                    jsonStream = context.getAssets().open(path);
                } else if (fileFrom == 2) {
                    //From Internal Storage
//                String path =Environment.getExternalStorageDirectory().getPath() + "/OfflineMap/"+map_layers.get(i);
                    String path = filePath + map_layers.get(i);
                    jsonStream = new FileInputStream(new File(path));
                }

                int size = jsonStream.available();
                byte[] buffer = new byte[size];
                jsonStream.read(buffer);
                jsonStream.close();
                jsonString = new String(buffer, "UTF-8");
            } catch (FileNotFoundException ex) {
//                Toast.makeText(context, map_layers.get(i) + "\t" + "File not found", Toast.LENGTH_SHORT).show();
                Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show();
                return;
            } catch (IndexOutOfBoundsException e) {
                Toast.makeText(context, "Invalid GeoJSON File", Toast.LENGTH_SHORT).show();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            KmlDocument kmlDocument = new KmlDocument();

            kmlDocument.parseGeoJSON(jsonString);

            Drawable defaultMarker = context.getResources().getDrawable(R.drawable.default_marker);
            Bitmap defaultBitmap = ((BitmapDrawable) defaultMarker).getBitmap();
            Style defaultStyle = new Style(defaultBitmap, linecolor, 3.0f, fillcolor);
            //Add styler
            if (map_types.get(i).equalsIgnoreCase("cadastral")) {
                Log.d("cur_index_c", layerIndex.get(i));
//                styler = new MyKmlStyler(defaultStyle, kmlDocument, map,index);
                styler = new MyKmlStylerCadastral(defaultStyle, kmlDocument, map, layerIndex.get(i));
                overlaysList.add(list, layerIndex.get(i));
            } else if (map_types.get(i).equalsIgnoreCase("drain")) {
                Log.d("cur_index_d", layerIndex.get(i));
                overlaysList.add(list, layerIndex.get(i));
                styler = new MyKmlStylerDrain(defaultStyle, kmlDocument, map, packageName);
            } else if (map_types.get(i).equalsIgnoreCase("goodbad")) {
                Log.d("cur_index_g", layerIndex.get(i));
                overlaysList.add(list, layerIndex.get(i));
                styler = new MyKmlStylerGoodBadZone(defaultStyle, kmlDocument, map);

            } else {
                Log.d("cur_index_o", layerIndex.get(i));
                overlaysList.add(list, layerIndex.get(i));
                styler = new MyKmlStylerOtherLayers(defaultStyle, kmlDocument, map);
            }

            FolderOverlay kmlOverlay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(map, null, styler, kmlDocument);

            map.getOverlays().add(list, kmlOverlay);
            list++;

            map.invalidate();

        }

    }


    public void hideOrShowLayer1(final MapView map, final int index, final boolean flag) {


        displayProgressDialog("Please wait...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (overlaysList != null && overlaysList.size() > 0) {
                    for (int i = 0; i < overlaysList.size(); i++) {

                        String element = overlaysList.get(i);
                        String layer = String.valueOf(index);
                        if (element.equalsIgnoreCase(layer)) {
                            map.getOverlays().get(i).setEnabled(flag);
                        }
                    }
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgressDialog();
                    }
                });
            }
        }).start();
    }

    class MyTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog pd = new ProgressDialog(context);
        MapView map;
        int index;
        boolean flag;

        public MyTask(MapView map, int index, boolean flag) {
            this.map = map;
            this.index = index;
            this.flag = flag;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setMessage("Please wait . . .");
            pd.show();
            pd.setCancelable(false);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (overlaysList != null && overlaysList.size() > 0) {

                for (int i = 0; i < overlaysList.size(); i++) {

                    String element = overlaysList.get(i);
                    String layer = String.valueOf(index);
                    if (element.equalsIgnoreCase(layer)) {

                        map.getOverlays().get(i).setEnabled(flag);

                    }
                }
            }
            return null;//download file
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            pd.dismiss();
        }
    }

    public void hideOrShowLayer2(MapView map, int index, boolean flag) {
        new MyTask(map, index, flag).execute();
    }

    public void hideOrShowLayer(ProgressBar pb, MapView map, int index, boolean flag) {


        InfoWindow.closeAllInfoWindowsOn(map);


        if (overlaysList != null && overlaysList.size() > 0) {
            pb.setVisibility(View.VISIBLE);
            for (int i = 0; i < overlaysList.size(); i++) {

                String element = overlaysList.get(i);
                String layer = String.valueOf(index);
                if (element.equalsIgnoreCase(layer)) {

                    map.getOverlays().get(i).setEnabled(flag);

                }
            }

            pb.setVisibility(View.GONE);


        }
    }

    public void hideOrShowLayer(MapView map, int index, boolean flag) {

        if (overlaysList != null && overlaysList.size() > 0) {

            for (int i = 0; i < overlaysList.size(); i++) {

                String element = overlaysList.get(i);
                String layer = String.valueOf(index);
                if (element.equalsIgnoreCase(layer)) {

                    map.getOverlays().get(i).setEnabled(flag);

                }
            }


        }
    }


    public void displayProgressDialog(String title) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(title);
        progressDialog.show();
    }

    public void dismissProgressDialog() {
        if (progressDialog.isShowing() && progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    class MyKmlStyler implements KmlFeature.Styler {
        Style mDefaultStyle;
        KmlDocument mKmlDocument;
        MapView Mmap;
        float mGroundOverlayBearing = 90.0f;
        int list;


        MyKmlStyler(Style defaultStyle, KmlDocument kmlDocument, MapView map) {
            mDefaultStyle = defaultStyle;
            mKmlDocument = kmlDocument;


            Mmap = map;
            if (overlaysList.size() == 0) {
                list = 0;
            } else {
                list = map.getOverlays().size();
            }
        }

        @Override
        public void onLineString(Polyline polyline, KmlPlacemark kmlPlacemark, KmlLineString kmlLineString) {
            //Custom styling:
            polyline.setColor(Color.GREEN);
            polyline.setWidth(Math.max(kmlLineString.mCoordinates.size() / 200.0f, 3.0f));
        }

        @Override
        public void onPolygon(Polygon polygon, final KmlPlacemark kmlPlacemark, KmlPolygon kmlPolygon) {

//            GroundOverlay myGroundOverlay2 = new GroundOverlay();
////            GeoPoint geoPoint = new GeoPoint(kmlPolygon.getBoundingBox().getCenterLatitude(),kmlPolygon.getBoundingBox().getCenterLongitude());
//            GeoPoint geoPoint2 = new GeoPoint(0.0, 0.0);
//            myGroundOverlay2.setPosition(geoPoint2);
//            BitmapDrawable b2 = writeOnDrawable(R.drawable.transparent_icon, "");
//            myGroundOverlay2.setImage(b2);
//            myGroundOverlay2.setDimensions(50.0f);
//            //myGroundOverlay.setTransparency(0.25f);
//            myGroundOverlay2.setBearing(mGroundOverlayBearing);
//            Mmap.getOverlays().add(myGroundOverlay2);

            double[] doubles = centroid(polygon.getPoints());
            double lat = doubles[0];
            double longg = doubles[1];

            polygon.setTitle(kmlPlacemark.mName);
            polygon.setStrokeWidth(1.0f);

            GroundOverlay myGroundOverlay = new GroundOverlay();
//            GeoPoint geoPoint = new GeoPoint(kmlPolygon.getBoundingBox().getCenterLatitude(),kmlPolygon.getBoundingBox().getCenterLongitude());
            GeoPoint geoPoint = new GeoPoint(lat, longg);
            myGroundOverlay.setPosition(geoPoint);
            BitmapDrawable b = writeOnDrawable(R.drawable.transparent_icon, kmlPlacemark.mName);
            myGroundOverlay.setImage(b);
            myGroundOverlay.setDimensions(50.0f);
            //myGroundOverlay.setTransparency(0.25f);
            myGroundOverlay.setBearing(mGroundOverlayBearing);

            Mmap.getOverlays().add(myGroundOverlay);
            overlaysList.add(list, "1");
            list++;
            polygon.setOnClickListener(new Polygon.OnClickListener() {
                @Override
                public boolean onClick(Polygon polygon, MapView mapView, GeoPoint eventPos) {
                    return false;
                }
            });

            kmlPolygon.applyDefaultStyling(polygon, null, kmlPlacemark, mKmlDocument, Mmap);
        }

        @Override
        public void onTrack(Polyline polyline, KmlPlacemark kmlPlacemark, KmlTrack kmlTrack) {
            //Keeping default styling:
            kmlTrack.applyDefaultStyling(polyline, mDefaultStyle, kmlPlacemark, mKmlDocument, Mmap);

        }

        @Override
        public void onPoint(Marker marker, KmlPlacemark kmlPlacemark, KmlPoint kmlPoint) {

            if (kmlPlacemark.getExtendedData("maxspeed") != null)
                kmlPlacemark.mStyle = "maxspeed";

            kmlPoint.applyDefaultStyling(marker, mDefaultStyle, kmlPlacemark, mKmlDocument, Mmap);
        }

        @Override
        public void onFeature(Overlay overlay, KmlFeature kmlFeature) {
            //If nothing to do, do nothing.


        }


    }

    class MyKmlStylerCadastral implements KmlFeature.Styler {
        Style mDefaultStyle;
        KmlDocument mKmlDocument;
        MapView Mmap;
        float mGroundOverlayBearing = 90.0f;
        int list;
        String index = null;

        MyKmlStylerCadastral(Style defaultStyle, KmlDocument kmlDocument, MapView map, String index) {
            mDefaultStyle = defaultStyle;
            mKmlDocument = kmlDocument;
            this.index = index;

            Mmap = map;
            if (overlaysList.size() == 0) {
                list = 0;
            } else {
                list = map.getOverlays().size();
            }
        }

        @Override
        public void onLineString(Polyline polyline, KmlPlacemark kmlPlacemark, KmlLineString kmlLineString) {
            //Custom styling:
            polyline.setColor(Color.GREEN);
            polyline.setWidth(Math.max(kmlLineString.mCoordinates.size() / 200.0f, 3.0f));
        }

        @Override
        public void onPolygon(Polygon polygon, final KmlPlacemark kmlPlacemark, KmlPolygon kmlPolygon) {

//            GroundOverlay myGroundOverlay2 = new GroundOverlay();
////            GeoPoint geoPoint = new GeoPoint(kmlPolygon.getBoundingBox().getCenterLatitude(),kmlPolygon.getBoundingBox().getCenterLongitude());
//            GeoPoint geoPoint2 = new GeoPoint(0.0, 0.0);
//            myGroundOverlay2.setPosition(geoPoint2);
//            BitmapDrawable b2 = writeOnDrawable(R.drawable.transparent_icon, "");
//            myGroundOverlay2.setImage(b2);
//            myGroundOverlay2.setDimensions(50.0f);
//            //myGroundOverlay.setTransparency(0.25f);
//            myGroundOverlay2.setBearing(mGroundOverlayBearing);
//            Mmap.getOverlays().add(myGroundOverlay2);

            double[] doubles = centroid(polygon.getPoints());
            double lat = doubles[0];
            double longg = doubles[1];

            polygon.setTitle(kmlPlacemark.mName);
            polygon.setStrokeWidth(1.0f);

            GroundOverlay myGroundOverlay = new GroundOverlay();
//            GeoPoint geoPoint = new GeoPoint(kmlPolygon.getBoundingBox().getCenterLatitude(),kmlPolygon.getBoundingBox().getCenterLongitude());
            GeoPoint geoPoint = new GeoPoint(lat, longg);
            myGroundOverlay.setPosition(geoPoint);
            BitmapDrawable b = writeOnDrawable(R.drawable.transparent_icon, kmlPlacemark.mName);
            myGroundOverlay.setImage(b);
            myGroundOverlay.setDimensions(50.0f);
            //myGroundOverlay.setTransparency(0.25f);
            myGroundOverlay.setBearing(mGroundOverlayBearing);

            Mmap.getOverlays().add(myGroundOverlay);
            overlaysList.add(list, index);
            list++;
            polygon.setOnClickListener(new Polygon.OnClickListener() {
                @Override
                public boolean onClick(Polygon polygon, MapView mapView, GeoPoint eventPos) {
                    return false;
                }
            });

            kmlPolygon.applyDefaultStyling(polygon, null, kmlPlacemark, mKmlDocument, Mmap);
        }

        @Override
        public void onTrack(Polyline polyline, KmlPlacemark kmlPlacemark, KmlTrack kmlTrack) {
            //Keeping default styling:
            kmlTrack.applyDefaultStyling(polyline, mDefaultStyle, kmlPlacemark, mKmlDocument, Mmap);

        }

        @Override
        public void onPoint(Marker marker, KmlPlacemark kmlPlacemark, KmlPoint kmlPoint) {

            if (kmlPlacemark.getExtendedData("maxspeed") != null)
                kmlPlacemark.mStyle = "maxspeed";

            kmlPoint.applyDefaultStyling(marker, mDefaultStyle, kmlPlacemark, mKmlDocument, Mmap);
        }

        @Override
        public void onFeature(Overlay overlay, KmlFeature kmlFeature) {
            //If nothing to do, do nothing.


        }


    }

    public static double[] centroid(List<GeoPoint> points) {
        double[] centroid = {0.0, 0.0};

        for (int i = 0; i < points.size(); i++) {
            centroid[0] += points.get(i).getLatitude();
            centroid[1] += points.get(i).getLongitude();
        }

        int totalPoints = points.size();
        centroid[0] = centroid[0] / totalPoints;
        centroid[1] = centroid[1] / totalPoints;

        return centroid;
    }

    public BitmapDrawable writeOnDrawable(int drawableId, String text) {

        Bitmap bm = getBitmap(drawableId);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setTextSize(20);
        paint.setTextAlign(Paint.Align.CENTER);

        Canvas canvas = new Canvas(bm);
        int xPos = (canvas.getWidth() / 2);
        int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));
        canvas.rotate(-90, xPos, yPos);
        canvas.drawText(text, xPos, yPos, paint);

        return new BitmapDrawable(bm);
    }


    class MyKmlStylerGoodBadZone implements KmlFeature.Styler {
        Style mDefaultStyle;
        KmlDocument mKmlDocument;
        MapView Mmap;
        int flag_percolation = 0, flag_storage = 0, flag_percolation_storage = 0, flag_not_suitable = 0, flag_other = 0;

        MyKmlStylerGoodBadZone(Style defaultStyle, KmlDocument kmlDocument, MapView map) {
            mDefaultStyle = defaultStyle;
            mKmlDocument = kmlDocument;
            Mmap = map;

        }

        @Override
        public void onLineString(Polyline polyline, KmlPlacemark kmlPlacemark, KmlLineString kmlLineString) {
            //Custom styling:
            polyline.setColor(Color.GREEN);
            polyline.setWidth(Math.max(kmlLineString.mCoordinates.size() / 200.0f, 3.0f));

        }

        @Override
        public void onPolygon(Polygon polygon, final KmlPlacemark kmlPlacemark, KmlPolygon kmlPolygon) {
            //Keeping default styling:
            polygon.setTitle(kmlPlacemark.mName);
            polygon.setStrokeWidth(1.0f);
            polygon.setOnClickListener(new Polygon.OnClickListener() {
                @Override
                public boolean onClick(Polygon polygon, MapView mapView, GeoPoint eventPos) {
                    return false;
                }
            });
            polygon.setStrokeColor(context.getResources().getColor(R.color.black));
            if (kmlPlacemark.mName.equalsIgnoreCase("percolation")) {
                if (flag_percolation == 0) {
                    ZonesList.add("percolation");
                    flag_percolation = 1;
                }
                polygon.setFillColor(context.getResources().getColor(R.color.goodbad_percolation_color));
            } else if (kmlPlacemark.mName.equalsIgnoreCase("storage")) {
                if (flag_storage == 0) {
                    ZonesList.add("storage");
                    flag_storage = 1;
                }
                polygon.setFillColor(context.getResources().getColor(R.color.goodbad_storge_color));
            } else if (kmlPlacemark.mName.equalsIgnoreCase("percolation_storage")) {
                if (flag_percolation_storage == 0) {
                    ZonesList.add("percolation_storage");
                    flag_percolation_storage = 1;
                }
                polygon.setFillColor(context.getResources().getColor(R.color.goodbad_percolation_storage_color));
            } else if (kmlPlacemark.mName.equalsIgnoreCase("not_suitable")) {
                if (flag_not_suitable == 0) {
                    ZonesList.add("not_suitable");
                    flag_not_suitable = 1;
                }
                polygon.setFillColor(context.getResources().getColor(R.color.goodbad_not_suitable_color));
            } else {
                if (flag_other == 0) {
                    ZonesList.add("other");
                    flag_other = 1;
                }
                polygon.setFillColor(context.getResources().getColor(R.color.transparent_color));
            }


            kmlPolygon.applyDefaultStyling(polygon, null, kmlPlacemark, mKmlDocument, Mmap);
        }

        @Override
        public void onTrack(Polyline polyline, KmlPlacemark kmlPlacemark, KmlTrack kmlTrack) {
            //Keeping default styling:
            kmlTrack.applyDefaultStyling(polyline, mDefaultStyle, kmlPlacemark, mKmlDocument, Mmap);
        }

        @Override
        public void onPoint(Marker marker, KmlPlacemark kmlPlacemark, KmlPoint kmlPoint) {
            //Styling based on ExtendedData properties:
            if (kmlPlacemark.getExtendedData("maxspeed") != null)
                kmlPlacemark.mStyle = "maxspeed";

            kmlPoint.applyDefaultStyling(marker, mDefaultStyle, kmlPlacemark, mKmlDocument, Mmap);
        }

        @Override
        public void onFeature(Overlay overlay, KmlFeature kmlFeature) {
            //If nothing to do, do nothing.
        }


    }

    class MyKmlStylerOtherLayers implements KmlFeature.Styler {
        Style mDefaultStyle;
        KmlDocument mKmlDocument;
        MapView Mmap;
        int flag_percolation = 0, flag_storage = 0, flag_percolation_storage = 0, flag_not_suitable = 0, flag_other = 0;

        MyKmlStylerOtherLayers(Style defaultStyle, KmlDocument kmlDocument, MapView map) {
            mDefaultStyle = defaultStyle;
            mKmlDocument = kmlDocument;
            Mmap = map;

        }

        @Override
        public void onLineString(Polyline polyline, KmlPlacemark kmlPlacemark, KmlLineString kmlLineString) {
            //Custom styling:
            polyline.setColor(context.getResources().getColor(R.color.fbutton_color_midnight_blue));
            polyline.setWidth(Math.max(kmlLineString.mCoordinates.size() / 200.0f, 3.0f));

        }

        @Override
        public void onPolygon(Polygon polygon, final KmlPlacemark kmlPlacemark, KmlPolygon kmlPolygon) {
            //Keeping default styling:
            polygon.setTitle(kmlPlacemark.mName);
            polygon.setStrokeWidth(1.0f);
            polygon.setOnClickListener(new Polygon.OnClickListener() {
                @Override
                public boolean onClick(Polygon polygon, MapView mapView, GeoPoint eventPos) {
                    return false;
                }
            });
            polygon.setStrokeColor(context.getResources().getColor(R.color.fbutton_color_midnight_blue));
            polygon.setFillColor(context.getResources().getColor(R.color.otherlayers));

            kmlPolygon.applyDefaultStyling(polygon, null, kmlPlacemark, mKmlDocument, Mmap);
        }

        @Override
        public void onTrack(Polyline polyline, KmlPlacemark kmlPlacemark, KmlTrack kmlTrack) {
            //Keeping default styling:
            kmlTrack.applyDefaultStyling(polyline, mDefaultStyle, kmlPlacemark, mKmlDocument, Mmap);
        }

        @Override
        public void onPoint(Marker marker, KmlPlacemark kmlPlacemark, KmlPoint kmlPoint) {
            //Styling based on ExtendedData properties:
            if (kmlPlacemark.getExtendedData("maxspeed") != null)
                kmlPlacemark.mStyle = "maxspeed";

            kmlPoint.applyDefaultStyling(marker, mDefaultStyle, kmlPlacemark, mKmlDocument, Mmap);
        }

        @Override
        public void onFeature(Overlay overlay, KmlFeature kmlFeature) {
            //If nothing to do, do nothing.
        }


    }


    class MyKmlStylerDrain implements KmlFeature.Styler {
        Style mDefaultStyle;
        String mPackageName;
        KmlDocument mKmlDocument;
        MapView Mmap;


        MyKmlStylerDrain(Style defaultStyle, KmlDocument kmlDocument, MapView map, String packageName) {
            mDefaultStyle = defaultStyle;
            mKmlDocument = kmlDocument;
            Mmap = map;
            mPackageName = packageName;

        }

        @Override
        public void onLineString(Polyline polyline, KmlPlacemark kmlPlacemark, KmlLineString kmlLineString) {
            //Custom styling:
            polyline.setColor(context.getResources().getColor(R.color.drain_map_line_color));
            polyline.setWidth(Math.max(kmlLineString.mCoordinates.size() / 200.0f, 4.0f));
            polyline.setOnClickListener(new Polyline.OnClickListener() {
                @Override
                public boolean onClick(Polyline polyline, MapView mapView, GeoPoint eventPos) {
                    return false;
                }
            });

        }

        @Override
        public void onPolygon(Polygon polygon, final KmlPlacemark kmlPlacemark, KmlPolygon kmlPolygon) {
            //Keeping default styling:
            polygon.setTitle(kmlPlacemark.mName);
            polygon.setId(kmlPlacemark.mId);
            polygon.setStrokeColor(context.getResources().getColor(R.color.drain_map_line_color));
            kmlPolygon.applyDefaultStyling(polygon, null, kmlPlacemark, mKmlDocument, Mmap);
        }

        @Override
        public void onTrack(Polyline polyline, KmlPlacemark kmlPlacemark, KmlTrack kmlTrack) {
            //Keeping default styling:
            kmlTrack.applyDefaultStyling(polyline, mDefaultStyle, kmlPlacemark, mKmlDocument, Mmap);
        }

        @Override
        public void onPoint(Marker marker, KmlPlacemark kmlPlacemark, KmlPoint kmlPoint) {
            //Styling based on ExtendedData properties:
            if (kmlPlacemark.getExtendedData("maxspeed") != null)
                kmlPlacemark.mStyle = "maxspeed";
            marker.setInfoWindow(new CustomMarkerInfoWindow(Mmap));
            String iconName = getIconName(marker.getSubDescription());

            int id = context.getResources().getIdentifier(mPackageName + ":drawable/" + iconName, null, null);
            Drawable defaultMarker = context.getResources().getDrawable(id);
            Bitmap defaultBitmap = ((BitmapDrawable) defaultMarker).getBitmap();
            Style defaultStyle = new Style(defaultBitmap, 0, 3.0f, 0);
            kmlPoint.applyDefaultStyling(marker, defaultStyle, kmlPlacemark, mKmlDocument, Mmap);

        }

        @Override
        public void onFeature(Overlay overlay, KmlFeature kmlFeature) {
            //If nothing to do, do nothing.
        }


    }

    public String getIconName(String input) {

        String iconName = null;
        String regex_pre = "icon=";
        String regex_post = "<br>\n";
        String output = input.replaceAll(regex_pre, "");
        iconName = output.replaceAll(regex_post, "");
        return iconName;
    }

    public ArrayList<String> getPercolationZones() {

        return Percolation_Zone;

    }

    public ArrayList<String> getPercolationStorageZones() {

        return Percolation_Storage_Zone;

    }

    public ArrayList<String> getStorageZones() {

        return Storage_Zone;

    }

    public ArrayList<String> getNotSuitableZones() {

        return Not_Suitable_Zone;

    }

    public ArrayList<String> getOtherZones() {

        return Other_Zone;

    }

    public ArrayList<String> getZonesList() {

        return ZonesList;

    }


    public void showLegends(ArrayList<String> legendList, ArrayList<String> legendNamesList, LinearLayout linearLayout) {
        Collections.reverse(legendList);
        Collections.reverse(legendNamesList);
        float density = context.getResources().getDisplayMetrics().density;
        for (int position = 0; position < legendList.size(); position++) {

            LinearLayout.LayoutParams params;
            LinearLayout ll;
            LinearLayout layout_viewsContainer = linearLayout;

            ll = new LinearLayout(context);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            ll.setPadding((int) (5.0 * density), (int) (5.0 * density), (int) (5.0 * density), 0);

            ll.setLayoutParams(params);
            params.setMargins((int) (0 * density), (int) (0 * density), (int) (0 * density), (int) (0 * density));

            LinearLayout.LayoutParams layout_legend = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            LinearLayout ll_legend = new LinearLayout(context);
            ll_legend.setOrientation(LinearLayout.HORIZONTAL);
            ll_legend.setWeightSum(1);
            ll_legend.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            ll_legend.setLayoutParams(layout_legend);

            LinearLayout.LayoutParams layout_legend_main = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            layout_legend_main.weight = 0.5f;
            LinearLayout ll_legend_sub = new LinearLayout(context);
            ll_legend_sub.setOrientation(LinearLayout.HORIZONTAL);
            ll_legend_sub.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            ll_legend_sub.setLayoutParams(layout_legend_main);

            CheckBox cb_legend = getCheckBox(density);
            ll_legend_sub.addView(cb_legend);

            ImageView iv_legend = getImageView(density);
            int id = context.getResources().getIdentifier(context.getPackageName() + ":drawable/" + legendList.get(position), null, null);
            iv_legend.setImageDrawable(context.getResources().getDrawable(id));
            ll_legend_sub.addView(iv_legend);

            TextView tv_legend = getTextView(density);
            tv_legend.setText(legendNamesList.get(position));
            ll_legend_sub.addView(tv_legend);

            ll_legend.addView(ll_legend_sub);
            ll.addView(ll_legend);

            layout_viewsContainer.addView(ll, 0);
        }
    }

    private TextView getTextView(float density) {
        TextView textView = new TextView(context);
        LinearLayout.LayoutParams tv_layout_category = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tv_layout_category.setMargins(0, (int) (4 * density), 0, 0);
        textView.setLayoutParams(tv_layout_category);
        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        textView.setTextColor(context.getResources().getColor(R.color.fbutton_color_carrot));
        textView.setTextSize(14);
        return textView;
    }

    private ImageView getImageView(float density) {
        ImageView imageView = new ImageView(context);

        LinearLayout.LayoutParams iv_layout_category = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        iv_layout_category.gravity = Gravity.CENTER;
        iv_layout_category.setMargins(0, (int) (4 * density), 0, 0);
        imageView.setLayoutParams(iv_layout_category);
        return imageView;
    }

    private CheckBox getCheckBox(float density) {
        CheckBox checkBox = new CheckBox(context);
        LinearLayout.LayoutParams iv_layout_category = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        iv_layout_category.gravity = Gravity.CENTER;
        iv_layout_category.setMargins(0, (int) (4 * density), 0, 0);
        checkBox.setLayoutParams(iv_layout_category);
        return checkBox;
    }

    public int getListSize() {
        return overlaysList.size();
    }

    public boolean deleteDirectory(String path, List<String> file_name) {
        for (int i = 0; i < file_name.size(); i++) {

            String temp_name = "/storage/emulated/0/" + path + "/" + file_name.get(i);
            File file = new File(temp_name);

            if (file.delete()) {
//                Log.d("Status", "File deleted");
            } else {
//                Log.d("Status", "File doesn't exist");
            }
        }
        return true;
    }

    public boolean deleteDirectory(String path, String file_name) {


        String temp_name = "/storage/emulated/0/" + path + "/" + file_name;
        File file = new File(temp_name);

        if (file.delete()) {
//                Log.d("Status", "File deleted");
        } else {
//                Log.d("Status", "File doesn't exist");
        }

        return true;
    }

    public void showLegendsItems(ArrayList<String> legendList, ArrayList<String> legendNamesList, LinearLayout linearLayout) {

        LegendViewReferences legendViewReferences = new LegendViewReferences();

        legendItemLayouts = new ArrayList<>();
        legendItemCheckboxes = new ArrayList<>();

        Collections.reverse(legendList);
        Collections.reverse(legendNamesList);
        float density = context.getResources().getDisplayMetrics().density;
        for (int position = 0; position < legendList.size(); position++) {

            LinearLayout.LayoutParams params;
            LinearLayout ll;
            LinearLayout layout_viewsContainer = linearLayout;

            ll = new LinearLayout(context);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            ll.setPadding((int) (5.0 * density), (int) (5.0 * density), (int) (5.0 * density), 0);

            ll.setLayoutParams(params);
            params.setMargins((int) (0 * density), (int) (0 * density), (int) (0 * density), (int) (0 * density));

            LinearLayout.LayoutParams layout_legend = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            LinearLayout ll_legend = new LinearLayout(context);
            ll_legend.setOrientation(LinearLayout.HORIZONTAL);
            ll_legend.setWeightSum(1);
            ll_legend.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            ll_legend.setLayoutParams(layout_legend);

            LinearLayout.LayoutParams layout_legend_main = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            layout_legend_main.weight = 0.5f;
            LinearLayout ll_legend_sub = new LinearLayout(context);
            ll_legend_sub.setOrientation(LinearLayout.HORIZONTAL);
            ll_legend_sub.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            ll_legend_sub.setLayoutParams(layout_legend_main);

            CheckBox cb_legend = getCheckBox(density);
            ll_legend.addView(cb_legend);

            ImageView iv_legend = getImageView(density);
            int id = context.getResources().getIdentifier(context.getPackageName() + ":drawable/" + legendList.get(position), null, null);
            iv_legend.setImageDrawable(context.getResources().getDrawable(id));
            ll_legend_sub.addView(iv_legend);

            TextView tv_legend = getTextView(density);
            tv_legend.setText(legendNamesList.get(position));
            ll_legend_sub.addView(tv_legend);

            ll_legend.addView(ll_legend_sub);
            ll.addView(ll_legend);

            layout_viewsContainer.addView(ll, 0);

            legendItemLayouts.add(ll_legend_sub);
            legendItemCheckboxes.add(cb_legend);

            Collections.reverse(legendItemLayouts);
            Collections.reverse(legendItemCheckboxes);

            legendViewReferences.setLegendItemLayouts(legendItemLayouts);
            legendViewReferences.setLegendCheckboxes(legendItemCheckboxes);


        }
    }

    public List<LinearLayout> getLegendItemLayouts() {

        return legendItemLayouts;
    }


    public List<CheckBox> getLegendItemCheckboxes() {

        return legendItemCheckboxes;
    }

    public void setMultipleItemsCIP(MapView map, ArrayList<Marker> markers, ArrayList<ArrayList<String>> mlocations, String packageName, int typeOfPath, ArrayList<String> layerIndex) {


        Bitmap b = null;
        int list = 0;
        ArrayList<ArrayList<String>> mlm = mlocations;
        ArrayList<Marker> mMarkers = markers;
        if (overlaysList.size() == 0) {
            list = 0;
        } else {
            list = map.getOverlays().size();
        }

        if (mlm != null) {
            for (int mData = 0; mData < mMarkers.size(); mData++) {


                int id = context.getResources().getIdentifier(packageName + ":drawable/" + mlm.get(mData).get(1), null, null);

                GeoPoint geoPoint = new GeoPoint(Double.parseDouble(mlm.get(mData).get(3)), Double.parseDouble(mlm.get(mData).get(4)));
                mMarkers.get(mData).setTitle(mlm.get(mData).get(2));
                mMarkers.get(mData).setSnippet(mlm.get(mData).get(0));
                mMarkers.get(mData).setPosition(geoPoint);

                if (typeOfPath == 1) {
                    //get image from drawable
                    mMarkers.get(mData).setIcon(context.getResources().getDrawable(id));
                } else if (typeOfPath == 2) {
                    //get image from path
//                    Drawable d = Drawable.createFromPath(mlm.get(mData).get(1));
                    Drawable d = resizeImage(context, mlm.get(mData).get(1), 60, 80);

                    mMarkers.get(mData).setIcon(d);


//                    Drawable d = Drawable.createFromPath(mlm.get(mData).get(1));
//                    if (d != null) {
//                        try {
//                            BitmapFactory.Options options = new BitmapFactory.Options();
//                            options.inJustDecodeBounds = true;
//                          Bitmap b2 =  BitmapFactory.decodeFile(String.valueOf(new File(mlm.get(mData).get(1))), options);

//
//                            int imageHeight = options.outHeight;
//                            int imageWidth = options.outWidth;
//                            Log.d("widhei",""+imageHeight+""+imageWidth);
//                            Drawable d2 = new BitmapDrawable(context.getResources(), bitmap);
//                            mMarkers.get(mData).setIcon(d2);
//                        } catch (Exception e) {
//                        }

                } else if (typeOfPath == 3) {
                    try {
                        b = new RetrieveImageTask().execute(mlm.get(mData).get(1)).get();
                    } catch (Exception e) {
                    }
                    Drawable d = new BitmapDrawable(context.getResources(), b);
                    mMarkers.get(mData).setIcon(d);
                }
                mMarkers.get(mData).setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                map.getOverlays().add(mMarkers.get(mData));

                overlaysList.add(list, layerIndex.get(mData));
                list++;

            }
            map.invalidate();
        }
    }

    public void setItemCIP(MapView map, double lat, double longg, String name, String icon, String packageName, int typeOfPath, String layerIndex) {
        int list = 0;
        if (overlaysList.size() == 0) {
            list = 0;
        } else {
            list = map.getOverlays().size();
        }

        Bitmap b = null;
        int id = context.getResources().getIdentifier(packageName + ":drawable/" + icon, null, null);
        GeoPoint loc = new GeoPoint(lat, longg);

        Marker marker = new Marker(map);
        marker.setPosition(loc);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        if (typeOfPath == 1) {
            marker.setIcon(context.getResources().getDrawable(id));
        } else if (typeOfPath == 2) {
            //get image from path
            Drawable d = resizeImage(context, icon, 60, 80);
            marker.setIcon(d);
        } else if (typeOfPath == 3) {
            try {
                b = new RetrieveImageTask().execute(icon).get();
            } catch (Exception e) {
            }
            Drawable d = new BitmapDrawable(context.getResources(), b);
            marker.setIcon(d);
        }
        marker.setTitle(name);

        map.getOverlays().add(marker);

        overlaysList.add(list, layerIndex);

        map.invalidate();
    }

    public void setItemCIP(Marker mMarker, MapView map, double lat, double longg, String name, String icon, String packageName, int typeOfPath, String layerIndex) {

        int list = 0;
        if (overlaysList.size() == 0) {
            list = 0;
        } else {
            list = map.getOverlays().size();
        }

        Bitmap b = null;
        int id = context.getResources().getIdentifier(packageName + ":drawable/" + icon, null, null);
        GeoPoint loc = new GeoPoint(lat, longg);

        Marker marker = mMarker;

        marker.setPosition(loc);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

//        marker.setIcon(context.getResources().getDrawable(id));
        if (typeOfPath == 1) {
            marker.setIcon(context.getResources().getDrawable(id));
        } else if (typeOfPath == 2) {
            //get image from path
            Drawable d = resizeImage(context, icon, 60, 80);
            marker.setIcon(d);
        } else if (typeOfPath == 3) {
            try {
                b = new RetrieveImageTask().execute(icon).get();
            } catch (Exception e) {
            }
            Drawable d = new BitmapDrawable(context.getResources(), b);
            marker.setIcon(d);
        }
        marker.setTitle(name);
        map.getOverlays().add(marker);
        overlaysList.add(list, layerIndex);
        map.invalidate();
    }


    public void setMultipleItemsCIP(MapView map, ArrayList<ArrayList<String>> mlocations, String packageName, int typeOfPath, ArrayList<String> layerIndex) {
        Bitmap b = null;
        ArrayList<ArrayList<String>> mlm = mlocations;
        int list = 0;
        if (overlaysList.size() == 0) {
            list = 0;
        } else {
            list = map.getOverlays().size();
        }

        if (mlm != null) {
//            for (ArrayList<String> ml : mlm) {
            for (int ml = 0; ml <= mlm.size(); ml++) {

                int id = context.getResources().getIdentifier(packageName + ":drawable/" + mlm.get(ml).get(1), null, null);

                GeoPoint geoPoint = new GeoPoint(Double.parseDouble(mlm.get(ml).get(3)), Double.parseDouble(mlm.get(ml).get(4)));
                Marker multipleMarker = new Marker(map);
                multipleMarker.setTitle(mlm.get(ml).get(2));
                multipleMarker.setSnippet(mlm.get(ml).get(0));
                multipleMarker.setPosition(geoPoint);
//                multipleMarker.setIcon(context.getResources().getDrawable(id));
                if (typeOfPath == 1) {
                    multipleMarker.setIcon(context.getResources().getDrawable(id));
                } else if (typeOfPath == 2) {
                    //get image from path
                    Drawable d = resizeImage(context, mlm.get(ml).get(1), 60, 80);
                    multipleMarker.setIcon(d);


                } else if (typeOfPath == 3) {
                    try {
                        b = new RetrieveImageTask().execute(mlm.get(ml).get(1)).get();
                    } catch (Exception e) {
                    }
                    Drawable d = new BitmapDrawable(context.getResources(), b);
                    multipleMarker.setIcon(d);
                }
                multipleMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);


                map.getOverlays().add(multipleMarker);
//            }
                overlaysList.add(list, layerIndex.get(ml));
                list++;
            }

            map.invalidate();
        }
    }

    public void showLegendsItemsCIP(ArrayList<String> legendList, ArrayList<String> legendNamesList, LinearLayout linearLayout, int typeOfPath) {
        Bitmap b = null;
        LegendViewReferences legendViewReferences = new LegendViewReferences();

        legendItemLayouts = new ArrayList<>();
        legendItemCheckboxes = new ArrayList<>();

        Collections.reverse(legendList);
        Collections.reverse(legendNamesList);
        float density = context.getResources().getDisplayMetrics().density;
        for (int position = 0; position < legendList.size(); position++) {

            LinearLayout.LayoutParams params;
            LinearLayout ll;
            LinearLayout layout_viewsContainer = linearLayout;

            ll = new LinearLayout(context);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            ll.setPadding((int) (5.0 * density), (int) (5.0 * density), (int) (5.0 * density), 0);

            ll.setLayoutParams(params);
            params.setMargins((int) (0 * density), (int) (0 * density), (int) (0 * density), (int) (0 * density));

            LinearLayout.LayoutParams layout_legend = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            LinearLayout ll_legend = new LinearLayout(context);
            ll_legend.setOrientation(LinearLayout.HORIZONTAL);
            ll_legend.setWeightSum(1);
            ll_legend.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            ll_legend.setLayoutParams(layout_legend);

            LinearLayout.LayoutParams layout_legend_main = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            layout_legend_main.weight = 0.5f;
            LinearLayout ll_legend_sub = new LinearLayout(context);
            ll_legend_sub.setOrientation(LinearLayout.HORIZONTAL);
            ll_legend_sub.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            ll_legend_sub.setLayoutParams(layout_legend_main);

            CheckBox cb_legend = getCheckBox(density);
            ll_legend.addView(cb_legend);

            ImageView iv_legend = getImageView(density);

            int id = context.getResources().getIdentifier(context.getPackageName() + ":drawable/" + legendList.get(position), null, null);
            if (typeOfPath == 1) {
//                iv_legend.setImageDrawable(context.getResources().getDrawable(id));
                Bitmap bitmap = ((BitmapDrawable) context.getResources().getDrawable(id)).getBitmap();
                iv_legend.setImageBitmap(bitmap);
            } else if (typeOfPath == 2) {
                //get image from path
//                iv_legend.setImageDrawable(Drawable.createFromPath(legendList.get(position)));
                Bitmap bitmap = ((BitmapDrawable) Drawable.createFromPath(legendList.get(position))).getBitmap();
                iv_legend.setImageBitmap(bitmap);
            } else if (typeOfPath == 3) {
                try {
                    b = new RetrieveImageTask().execute(legendList.get(position)).get();
                } catch (Exception e) {
                }
                Drawable d = new BitmapDrawable(context.getResources(), b);
//                iv_legend.setImageDrawable(d);
                Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
                iv_legend.setImageBitmap(bitmap);
            }

//            iv_legend.setImageDrawable(context.getResources().getDrawable(id));
            ll_legend_sub.addView(iv_legend);

            TextView tv_legend = getTextView(density);
            tv_legend.setText(legendNamesList.get(position));
            ll_legend_sub.addView(tv_legend);

            ll_legend.addView(ll_legend_sub);
            ll.addView(ll_legend);

            layout_viewsContainer.addView(ll, 0);

            legendItemLayouts.add(ll_legend_sub);
            legendItemCheckboxes.add(cb_legend);

            Collections.reverse(legendItemLayouts);
            Collections.reverse(legendItemCheckboxes);

            legendViewReferences.setLegendItemLayouts(legendItemLayouts);
            legendViewReferences.setLegendCheckboxes(legendItemCheckboxes);


        }
    }

    class RetrieveImageTask extends AsyncTask<String, Void, Bitmap> {

        public Bitmap doInBackground(String... urls) {

            myBitmap = null;
            String src = urls[0];
            try {
                URL url = new URL(src);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (IOException e) {
                // Log exception
                return null;
            }
        }

        public void onPostExecute(Bitmap feed) {
            // TODO: check this.exception

        }

    }

    public void addClickListener(MapView map, MapEventsOverlay mapEventsOverlay, String id) {
        int list = 0;
        if (overlaysList.size() == 0) {
            list = 0;
        } else {
            list = map.getOverlays().size();
        }
        map.getOverlays().add(list, mapEventsOverlay); //inserted at the "bottom" of all overlays
        overlaysList.add(list, id);
    }

    public void drawCircle(MapView map) {

        GeoPoint p = new GeoPoint(16.299961, 79.654553);
        Polygon circle = new Polygon(map);
        circle.setPoints(Polygon.pointsAsCircle(p, 1000.0));


        circle.setFillColor(context.getResources().getColor(R.color.circle_fill));
        circle.setStrokeColor(context.getResources().getColor(R.color.circle_stroke));
        circle.setStrokeWidth(2);
        map.getOverlayManager().add(circle);
        circle.setTitle("Centered on " + p.getLatitude() + "," + p.getLongitude());
        map.invalidate();
    }

    public void drawCircle(MapView map, GeoPoint geoPoint, Float radius, String fillColor, String strokeColor, int strokeWidth, String title, String layerIndex) {
        int list = 0;
        if (overlaysList.size() == 0) {
            list = 0;
        } else {
            list = map.getOverlays().size();
        }

        GeoPoint p = new GeoPoint(geoPoint);
        Polygon circle = new Polygon(map);
        circle.setPoints(Polygon.pointsAsCircle(p, radius));

        circle.setFillColor(Color.parseColor(fillColor));
        circle.setStrokeColor(Color.parseColor(strokeColor));

//        circle.setFillColor(context.getResources().getColor(R.color.circle_fill));
//        circle.setStrokeColor(context.getResources().getColor(R.color.circle_stroke));
        circle.setStrokeWidth(strokeWidth);
//        map.getOverlayManager().add(circle);

        map.getOverlays().add(circle);
        overlaysList.add(list, layerIndex);

        circle.setTitle(title);
        map.invalidate();
    }

    public void downloadMapList(String file_url, List<String> file_name, String path) {
        download_ids = new ArrayList<>();
        deleteDirectory(path, file_name);
        Toast.makeText(context, "Downloading Map...", Toast.LENGTH_SHORT).show();
        DirectoryHelper.ROOT_DIRECTORY_NAME = path;
        for (int list_i = 0; list_i < file_name.size(); list_i++) {
            String MAP_DOWNLOAD_PATH = file_url + file_name.get(list_i);
//            context.startService(DownloadMapService.getDownloadService(context, MAP_DOWNLOAD_PATH, DirectoryHelper.ROOT_DIRECTORY_NAME.concat("/")));
            context.startService(DownloadJobIntentService.getDownloadService(context, MAP_DOWNLOAD_PATH, DirectoryHelper.ROOT_DIRECTORY_NAME.concat("/"), file_name.get(list_i)));

        }

//        preferenceManager
//                = PreferenceManager.getDefaultSharedPreferences(context);
//        downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
//
//        deleteDirectory(path, file_name);
//        Toast.makeText(context, "Downloading Map...", Toast.LENGTH_SHORT).show();
//        DirectoryHelper.ROOT_DIRECTORY_NAME = path;
//
//        for (int list_i = 0; list_i < file_name.size(); list_i++) {
//            String MAP_DOWNLOAD_PATH = file_url + file_name.get(list_i);
//
//            Uri uri = Uri.parse(MAP_DOWNLOAD_PATH); // Path where you want to download file.
//            DownloadManager.Request request = new DownloadManager.Request(uri);
//            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);  // Tell on which network you want to download file.
//            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);  // This will show notification on top when downloading the file.
//            request.setTitle("Downloading Map"); // Title for notification.
//            request.setVisibleInDownloadsUi(true);
//            request.setDestinationInExternalPublicDir(DirectoryHelper.ROOT_DIRECTORY_NAME.concat("/"), uri.getLastPathSegment());  // Storage directory path
//            DownloadManager downloadManager = ((DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE)); // This will start downloading
//
//            long id = downloadManager.enqueue(request);
//            download_ids.add(String.valueOf(id) + "@" + file_name.get(list_i));
//        }

    }

    public List<String> getDownloadIDs() {

        return download_ids;
    }

    public void CheckDownloadStatus(List<String> download_ids, String path) {

        // TODO Auto-generated method stub
        DownloadManager.Query query = new DownloadManager.Query();
        for (String id : download_ids) {

            String[] fileNameId = id.split("@");
            query.setFilterById(Long.parseLong(fileNameId[0]));
            Cursor cursor = downloadManager.query(query);
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                int status = cursor.getInt(columnIndex);
                int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
                int reason = cursor.getInt(columnReason);

                switch (status) {
                    case DownloadManager.STATUS_FAILED:
                        String failedReason = "";
                        switch (reason) {
                            case DownloadManager.ERROR_CANNOT_RESUME:
                                failedReason = "ERROR_CANNOT_RESUME";
                                break;
                            case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                                failedReason = "ERROR_DEVICE_NOT_FOUND";
                                break;
                            case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                                failedReason = "ERROR_FILE_ALREADY_EXISTS";
                                break;
                            case DownloadManager.ERROR_FILE_ERROR:
                                failedReason = "ERROR_FILE_ERROR";
                                break;
                            case DownloadManager.ERROR_HTTP_DATA_ERROR:
                                failedReason = "ERROR_HTTP_DATA_ERROR";
                                break;
                            case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                                failedReason = "ERROR_INSUFFICIENT_SPACE";
                                break;
                            case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                                failedReason = "ERROR_TOO_MANY_REDIRECTS";
                                break;
                            case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                                failedReason = "ERROR_UNHANDLED_HTTP_CODE";
                                break;
                            case DownloadManager.ERROR_UNKNOWN:
                                failedReason = "ERROR_UNKNOWN";
                                break;
                        }
                        Log.d("AndroidDownloadManager", "FAILED: " + failedReason);
                        deleteDirectory(path, fileNameId[1]);
                        break;
                    case DownloadManager.STATUS_PAUSED:
                        String pausedReason = "";

                        switch (reason) {
                            case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
                                pausedReason = "PAUSED_QUEUED_FOR_WIFI";
                                break;
                            case DownloadManager.PAUSED_UNKNOWN:
                                pausedReason = "PAUSED_UNKNOWN";
                                break;
                            case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
                                pausedReason = "PAUSED_WAITING_FOR_NETWORK";
                                break;
                            case DownloadManager.PAUSED_WAITING_TO_RETRY:
                                pausedReason = "PAUSED_WAITING_TO_RETRY";
                                break;
                        }
                        Log.d("AndroidDownloadManager", "FAILED: " + "PAUSED: " + pausedReason);
//                        DownloadManager.Query query2 = new DownloadManager.Query();
//                        query2.setFilterByStatus(DownloadManager.STATUS_FAILED | DownloadManager.STATUS_PENDING | DownloadManager.STATUS_RUNNING);
//                        DownloadManager dm = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
//                        Cursor c = dm.query(query2);
//                        while (c.moveToNext()) {
//                            dm.remove(c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID)));
//                        }
                        break;
                    case DownloadManager.STATUS_PENDING:
                        Log.d("AndroidDownloadManager", "PENDING");

//                        DownloadManager.Query query3 = new DownloadManager.Query();
//                        query3.setFilterByStatus(DownloadManager.STATUS_FAILED | DownloadManager.STATUS_PENDING | DownloadManager.STATUS_RUNNING);
//                        DownloadManager dm2 = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
//                        Cursor c2 = dm2.query(query3);
//                        while (c2.moveToNext()) {
//                            dm2.remove(c2.getLong(c2.getColumnIndex(DownloadManager.COLUMN_ID)));
//                        }
                        break;
                    case DownloadManager.STATUS_RUNNING:
                        Log.d("AndroidDownloadManager", "RUNNING");

                        break;
                    case DownloadManager.STATUS_SUCCESSFUL:
                        Log.d("AndroidDownloadManager", "SUCCESSFUL" + id);

                        break;
                }
            }
        }
    }

    public void downloadMapListWithStatus(String file_url, List<String> file_name, String path) {

        download_ids = new ArrayList<>();
        deleteDirectory(path, file_name);
        Toast.makeText(context, "Downloading Map...", Toast.LENGTH_SHORT).show();

        for (int list_i = 0; list_i < file_name.size(); list_i++) {
            String MAP_DOWNLOAD_PATH = file_url + file_name.get(list_i);
            long id = downloadMapListNew(MAP_DOWNLOAD_PATH, file_name.get(list_i), path);
            download_ids.add(String.valueOf(id) + "@" + file_name.get(list_i));
        }
        Log.d("AndroidD", "" + download_ids.size());
        String DownloadId_Temp = download_ids.get(download_ids.size() - 1);
        String[] dt = DownloadId_Temp.split("@");
        File_DownloadId = Long.parseLong(dt[0]);
        filePath = path;
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        context.registerReceiver(downloadReceiver, filter);

    }

    public long downloadMapListNew(String file_url, String file_name, String path) {

        long downloadReference = 0;
        DirectoryHelper.ROOT_DIRECTORY_NAME = path;
        Uri file_uri = Uri.parse(file_url);

        downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(file_uri);

        //Setting title of request
        request.setTitle("Map Files Downloading");

        //Setting description of request
        request.setDescription("Map Files Downloading...");

        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setTitle("Downloading Map");
        request.setVisibleInDownloadsUi(true);
        request.setDestinationInExternalPublicDir(DirectoryHelper.ROOT_DIRECTORY_NAME.concat("/"), file_uri.getLastPathSegment());

//        request.setDestinationInExternalFilesDir(context, DirectoryHelper.ROOT_DIRECTORY_NAME.concat("/"), file_name);

        //Enqueue download and save into referenceId
        downloadReference = downloadManager.enqueue(request);


        return downloadReference;


    }

    public static Drawable resizeImage(Context ctx, String path, int iconWidth,
                                       int iconHeight) {

        // load the origial Bitmap
        Bitmap BitmapOrg = ((BitmapDrawable) Drawable.createFromPath(path)).getBitmap();

        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();
        int newWidth = iconWidth;
        int newHeight = iconHeight;

        // calculate the scale
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the Bitmap
        matrix.postScale(scaleWidth, scaleHeight);

        // if you want to rotate the Bitmap
        // matrix.postRotate(45);

        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
                height, matrix, true);

        // make a Drawable from Bitmap to allow to set the Bitmap
        // to the ImageView, ImageButton or what ever
        return new BitmapDrawable(ctx.getResources(), resizedBitmap);

    }

    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            //check if the broadcast message is for our enqueued download
            long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            CheckDownloadStatus(download_ids, filePath);
            if (referenceId == File_DownloadId) {
                Log.d("AndroidDD", "File Download Complete");
                Toast toast = Toast.makeText(context,
                        "File Download Complete", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 25, 400);
                toast.show();
            }

        }
    };
}

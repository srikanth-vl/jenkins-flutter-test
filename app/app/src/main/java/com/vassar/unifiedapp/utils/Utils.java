package com.vassar.unifiedapp.utils;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Camera;
import android.location.Location;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.api.LatestProjectDataService;
import com.vassar.unifiedapp.application.UnifiedAppApplication;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.db.UnifiedAppDBHelper;
import com.vassar.unifiedapp.db.UnifiedAppDbContract;
import com.vassar.unifiedapp.listener.OnFormTaskCompletedListener;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.log.LogType;
import com.vassar.unifiedapp.model.ConfigFile;
import com.vassar.unifiedapp.model.FormImage;
import com.vassar.unifiedapp.model.FormMedia;
import com.vassar.unifiedapp.model.IncomingImage;
import com.vassar.unifiedapp.model.LatestFieldValue;
import com.vassar.unifiedapp.model.Project;
import com.vassar.unifiedapp.model.ProjectIconInfo;
import com.vassar.unifiedapp.model.ProjectList;
import com.vassar.unifiedapp.model.ProjectTypeConfiguration;
import com.vassar.unifiedapp.model.ProjectTypeModel;
import com.vassar.unifiedapp.model.RootConfig;
import com.vassar.unifiedapp.model.UserMetaData;
import com.vassar.unifiedapp.newflow.AppBackgroundSync;
import com.vassar.unifiedapp.synchronization.AlarmHelper;
import com.vassar.unifiedapp.ui.ProjectFormActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Utils {

    private static boolean shouldShowLog = true;

    // static variable mInstance of type Utils
    private static Utils mInstance = null;

    // private constructor restricted to this class itself
    private Utils() {
    }

    // static method to create instance of Singleton class
    public static Utils getInstance() {
        if (mInstance == null)
            mInstance = new Utils();
        return mInstance;
    }

    public static void logError(String tag, String log) {
        // TODO: Use Android logger class
        if (shouldShowLog) {
            logMsg(LogType.ERROR, tag, log);
        }
    }

    public static void logError(String tag, String log, Throwable e) {
        // TODO: Use Android logger class
        if (shouldShowLog) {
            logMsg(LogType.ERROR, tag, log, e);
        }
    }

    public static void logWarn(String tag, String log) {
        // TODO: Use Android logger class
        if (shouldShowLog) {
            logMsg(LogType.WARN, tag, log);
        }
    }

    public static void logWarn(String tag, String log, Throwable e) {
        // TODO: Use Android logger class
        if (shouldShowLog) {
            logMsg(LogType.ERROR, tag, log, e);
        }
    }

    public static void logDebug(String tag, String log) {
        // TODO: Use Android logger class
        if (shouldShowLog) {
            logMsg(LogType.DEBUG, tag, log);
        }
    }

    public static void logInfo(String log) {
        // TODO: Use Android logger class
        if (shouldShowLog) {
            logMsg(LogType.INFO, log);
        }
    }

    public static void logInfo(String tag, String log) {
        // TODO: Use Android logger class
        if (shouldShowLog) {
            logMsg(LogType.INFO, tag, log);
        }
    }

    private static void logMsg(String logType, String log) {
        if (shouldShowLog) {
            System.out.println(getCurrentTime() + ":" + logType + ":" + log);
        }
    }

    private static void logMsg(String logType, String tag, String log) {
        if (shouldShowLog) {
            System.out.println(getCurrentTime() + ":" + logType + ":" + tag + ":" + log);
        }
    }

    private static void logMsg(String logType, String tag, String log, Throwable e) {
        if (shouldShowLog) {
            System.out.println(getCurrentTime() + ":" + logType + ":" + tag + ":" + log + " -- " + e);
        }
    }

    public static String getCurrentTime() {
        return tsToDate(System.currentTimeMillis());
    }

    public static String tsToDate(long timestamp) {
        Date date = new Date(timestamp);
        Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss.SSS");
        return format.format(date);
    }

    public static List<String> getImageUUIDList(List<String> imageUUIDWithLongitudeAndLatitudeList) {
        List<String> uuids = new ArrayList<>();
        if (imageUUIDWithLongitudeAndLatitudeList != null && !imageUUIDWithLongitudeAndLatitudeList.isEmpty()) {
            for (String imageIdWithLongitudeAndLatitude : imageUUIDWithLongitudeAndLatitudeList) {
                List<String> imageIdString = StringUtils.getStringListFromDelimiter(Constants.IMAGE_UUID_LONG_LAT_SEPARATOR, imageIdWithLongitudeAndLatitude);
                if (imageIdString.size() > 0) {
                    uuids.add(imageIdString.get(0));
                }
            }
        }
        return uuids;
    }

    public boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) UAAppContext.getInstance().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnected();

        return isConnected;
    }

    public boolean isSessionValid(String userId, String appId, UnifiedAppDBHelper dbHelper) {
        UserMetaData userMetaData = dbHelper.getUserMeta(userId);
        long lastServerTimestamp = userMetaData.mTimestamp;
        long currentTimestamp = System.currentTimeMillis();
        String expiryDays = null;
        RootConfig rootConfig = null;

        ConfigFile rootConfigFile = dbHelper.getConfigFile(userId, Constants.ROOT_CONFIG_DB_NAME);
        if (rootConfigFile != null && rootConfigFile.getConfigContent() != null &&
                !rootConfigFile.getConfigContent().isEmpty()) {
            String rootConfigString = rootConfigFile.getConfigContent();
            if (rootConfigString != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    rootConfig = objectMapper.readValue(rootConfigString, RootConfig.class);
                } catch (IOException e) {
                    Utils.logError(LogTags.UTILITY, "Failed to parse RootConfig File");
                    e.printStackTrace();
                }

            }

            if (rootConfig != null) {
                for (ProjectTypeModel projectTypeModel : rootConfig.mApplications) {
                    if (projectTypeModel.mAppId.equals(appId)) {
                        expiryDays = projectTypeModel.mClientExpiryDays;
                        break;
                    }
                }
            }
        }

        if (expiryDays != null) {
            if (currentTimestamp < (lastServerTimestamp + (Integer.parseInt(expiryDays) * 86400000))) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public String readFile(Activity activity, int file) throws IOException {
        BufferedReader reader = null;
        reader = new BufferedReader(new InputStreamReader(activity.getResources()
                .openRawResource(file), "UTF-8"));
        String content = "";
        String line;
        while ((line = reader.readLine()) != null) {
            content = content + line;
        }
        System.out.println("JSON CONFIGURATION STRING : " + content);
        return content;
    }

    public ProjectList loadProjectListConfiguration(String jsonString) {
        ObjectMapper objectMapper = new ObjectMapper();
        ProjectList projectList = null;
        try {
            projectList = objectMapper.readValue(jsonString, ProjectList.class);
        } catch (IOException e) {
            Utils.logError(LogTags.UTILITY, "Faield to Parse Project List Json :: " + jsonString);
            e.printStackTrace();
        }
        return projectList;
    }

    public ProjectTypeConfiguration loadAppConfiguration(String jsonString) {
        ObjectMapper objectMapper = new ObjectMapper();
        ProjectTypeConfiguration projectTypeConfiguration = null;
        try {
            projectTypeConfiguration = objectMapper.readValue(jsonString, ProjectTypeConfiguration.class);
        } catch (IOException e) {
            Utils.logError(LogTags.UTILITY, "Faield to Parse ProjectTypeConfiguration Json :: " + jsonString);
            e.printStackTrace();
        }
        return projectTypeConfiguration;
    }

    public void showLog(String tag, String log) {
        if (shouldShowLog) {
            System.out.println(tag + " LOG: " + log);
        }
    }

    public void updateUserLastNetworkConnectionTimestamp(UnifiedAppDBHelper dbHelper, String username, long timestamp) {
        ContentValues values = new ContentValues();
        values.put(UnifiedAppDbContract.UserMetaEntry.COLUMN_LAST_NETWORK_SYNC_TIME, timestamp);

        // Adding to database
        int update = dbHelper.updateUsermeta(values, username);
        Utils.getInstance().showLog("UDPATE", String.valueOf(update));
        if (update == 0) {
            // If the user does'nt exist on the database already
            showLog("Updating user timestamp : ", "User not present on database");
        }
    }

    /**
     * Check if this device has a camera
     */
    public boolean checkCameraHardware(AppCompatActivity context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /**
     * Returns pixels for a dp value
     */
    public float dpToPx(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    public ArrayList<View> getViewsByTag(ViewGroup root, String tag) {
        ArrayList<View> views = new ArrayList<View>();
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                views.addAll(getViewsByTag((ViewGroup) child, tag));
            }

            final Object tagObj = child.getTag();
            if (tagObj != null && tagObj.equals(tag)) {
                views.add(child);
            }

        }
        return views;
    }

    public boolean checkForPermission(AppCompatActivity activity, String permission) {
        if (ContextCompat.checkSelfPermission(activity, permission)
                != PackageManager.PERMISSION_GRANTED)
            // Permission is not granted
            return false;
        else
            // Permission is granted
            return true;
    }

    // Stores incoming stream as an image and return the local path of the image
    public String saveImageToStorage(Context context, InputStream inputStream, String imageUrl) {

        File root = android.os.Environment.getExternalStorageDirectory();
        File imageDirectory = new File(root.getAbsolutePath() + "/"+ "Android" + "/" + "data" + "/" +
                UAAppContext.getInstance().getContext().getPackageName() + "/" +
                UAAppContext.getInstance().getContext().getResources().getString(R.string.app_name));

        if (!imageDirectory.exists()) {
            boolean makeDir = imageDirectory.mkdirs();
        }

        if (!imageDirectory.exists()) {
            if (imageDirectory.mkdir()) ;
        }

        File f = new File(imageUrl);

        OutputStream fileOutputStream = null;
        File file = new File(imageDirectory, f.getName());

        Bitmap bm = BitmapFactory.decodeStream(inputStream);

        try {
            fileOutputStream = new FileOutputStream(file);

            Utils.getInstance().showLog("INCOMINGIMAGE FILEOUTPUTSTREAM", fileOutputStream.toString());
            BufferedOutputStream outputStream = new BufferedOutputStream(fileOutputStream);
            Utils.getInstance().showLog("INCOMINGIMAGE OUTPUTSTREAM", outputStream.toString());
            bm.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return file.getAbsolutePath();
    }

    public void saveImagesToDatabase(UnifiedAppDBHelper dbHelper, IncomingImage image) {
        ContentValues values = new ContentValues();
        values.put(UnifiedAppDbContract.IncomingImagesEntry.COLUMN_IMAGE_TYPE
                , image.getImageType());
        values.put(UnifiedAppDbContract.IncomingImagesEntry.COLUMN_IMAGE_URL
                , image.getImageUrl());
        values.put(UnifiedAppDbContract.IncomingImagesEntry.COLUMN_IMAGE_LOCAL_PATH
                , image.getImageLocalPath());
        int changed = dbHelper.updateImage(values, image.getImageUrl());
        if (changed == 0) {
            // If the file does'nt exist on the DB, it adds a new row
            dbHelper.addToDatabase(UnifiedAppDbContract.IncomingImagesEntry.TABLE_IMAGES, values);
        }
    }

    // Service to upload Image to Server
    public void uploadFormImage(SharedPreferences preferences, FormImage formImage, String appId, String userId,
                                OnFormTaskCompletedListener listener) {
        UploadFormImageTask uploadImageTask = new UploadFormImageTask(preferences, formImage,
                listener, appId, userId);
        uploadImageTask.execute();
    }

    public void getAllTablesCount(UnifiedAppDBHelper dbHelper) {
        // Getting count of all the config files on the database
        Utils.getInstance().showLog("CONFIG FILES", String.valueOf(dbHelper
                .getTableCount(UnifiedAppDbContract.ConfigFilesEntry.TABLE_CONFIG)));
        Utils.getInstance().showLog("IMCOMING IMAGES", String.valueOf(dbHelper
                .getTableCount(UnifiedAppDbContract.IncomingImagesEntry.TABLE_IMAGES)));
        Utils.getInstance().showLog("USERS", String.valueOf(dbHelper
                .getTableCount(UnifiedAppDbContract.UserMetaEntry.TABLE_USER)));
        Utils.getInstance().showLog("PROJECTS SAVED", String.valueOf(dbHelper
                .getTableCount(UnifiedAppDbContract.ProjectSubmissionEntry.TABLE_PROJECT_SUBMISSION)));
    }

    public boolean deleteImageFromStorage(Context context, String path) {
        File file = new File(path);
        boolean deleted = false;
        if (file.exists()) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                Uri deleteFileUri = FileProvider.getUriForFile(context.getApplicationContext(),
                        Constants.FILE_PROVIDER, file);
                context.grantUriPermission(context.getApplicationContext().getPackageName(),
                        deleteFileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.getContentResolver().delete(deleteFileUri, null, null);
                deleted = true;
            } else {
                deleted = file.delete();
            }
        }
        return deleted;
    }

    public boolean deleteVideoFromStorage(Context context, String path) {
        File file = new File(path);
        boolean deleted = false;
        if (file.exists()) {
//            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
//                Uri deleteFileUri = FileProvider.getUriForFile(context.getApplicationContext(),
//                        Constants.FILE_PROVIDER, file);
//                context.grantUriPermission(context.getApplicationContext().getPackageName(),
//                        deleteFileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                context.getContentResolver().delete(deleteFileUri, null, null);
//                deleted = true;
//            } else {
//                deleted = file.delete();
//            }
            deleted = file.delete();
        }
        return deleted;
    }

    public void showAlertDialog(AppCompatActivity context, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                //set icon
                .setIcon(android.R.drawable.ic_dialog_alert)
                //set title
                .setTitle("Submission")
                //set message
                .setMessage(message)
                .setIcon(0)
                //set positive button
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //set what would happen when positive button is clicked
                        ((ProjectFormActivity) context).dismissDialog(((ProjectFormActivity) context).DIALOG_LOADING);
                        ((ProjectFormActivity) context).finish();
                    }
                })
                .show();
    }

    public String changeTimeToSeconds(String time) {
        List<String> separatedTime = Arrays.asList
                (time.trim().split(":"));
        long seconds = Integer.parseInt(separatedTime.get(0)) * 60 * 60;
        seconds += Integer.parseInt(separatedTime.get(1)) * 60;
        return String.valueOf(seconds);
    }

    public String changeTimeToHhMmFormat(long timeInSeconds) {
        long hours = timeInSeconds / 3600;
        long mins = (timeInSeconds % 3600) / 60;
        String time = String.format("%02d", (int) hours) + ":" + String.format("%02d", (int) mins);
        return time;
    }

    public void checkForLocation(AppCompatActivity activity) {
        if (((UnifiedAppApplication) activity.getApplication()).mFusedLocationClient == null) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            ((UnifiedAppApplication) activity.getApplication()).mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                                ((UnifiedAppApplication) activity.getApplication()).mUserLatitude = location.getLatitude();
                                ((UnifiedAppApplication) activity.getApplication()).mUserLongitude = location.getLongitude();
                                ((UnifiedAppApplication) activity.getApplication()).mAccuracy = location.getAccuracy();
                                System.out.println("LOCATIONUPDATE : " + String.valueOf(((UnifiedAppApplication) activity.getApplication()).mUserLatitude) +
                                        "     " + String.valueOf(((UnifiedAppApplication) activity.getApplication()).mUserLongitude) + "   accuracy : "
                                        + String.valueOf(((UnifiedAppApplication) activity.getApplication()).mAccuracy));
                            } else {
                                System.out.println("LOCATIONUPDATE : Location is null!");
                            }
                        }
                    });
        }
    }

    public void createLocationRequest(AppCompatActivity activity) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        mLocationCallback = new LocationCallback() {
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//                if (locationResult == null) {
//                    return;
//                }
//                for (Location location : locationResult.getLocations()) {
//                    if (location != null) {
//                        //TODO: UI updates.
//                        mUserLatitude = location.getLatitude();
//                        mUserLongitude = location.getLongitude();
//                        mAccuracy = location.getAccuracy();
//                        System.out.println("LOCATIONUPDATE : " + String.valueOf(mUserLatitude) +
//                                "     " + String.valueOf(mUserLongitude) + "   accuracy : "
//                                + String.valueOf(mAccuracy));
//                    }
//                }
//            }
//        };
//        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        if (locationRequest != null) {
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            SettingsClient client = LocationServices.getSettingsClient(activity);
            Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

            task.addOnSuccessListener(activity, new OnSuccessListener<LocationSettingsResponse>() {
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                    // All location settings are satisfied. The client can initialize
                    // location requests here.
                    // ...
                    System.out.println("LOCATIONUPDATE : Task successful");

                    checkForLocation(activity);
                }
            });

            task.addOnFailureListener(activity, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    System.out.println("LOCATIONUPDATE : Task unsuccessful");
                    if (e instanceof ResolvableApiException) {
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(activity,
                                    Constants.REQUEST_LOCATION_SETTINGS);
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        }
                    }
                }
            });
        }

    }

    public boolean geotagValidateImage(double latitude, double longitude, String validation) {

        String[] geotaggedCoordinates = validation.split(",");

        if (geotaggedCoordinates.length == 4) {
            // If BBox values are given for validation
            double minx = Double.parseDouble(geotaggedCoordinates[0]);
            double miny = Double.parseDouble(geotaggedCoordinates[1]);
            double maxx = Double.parseDouble(geotaggedCoordinates[2]);
            double maxy = Double.parseDouble(geotaggedCoordinates[3]);
            if (minx <= latitude && latitude <= maxx
                    && miny <= longitude && longitude <= maxy) {
                return true;
            }
        } else if (geotaggedCoordinates.length == 3) {
            // If center-radius values are given for validation
            double centerx = Double.parseDouble(geotaggedCoordinates[0]);
            double centery = Double.parseDouble(geotaggedCoordinates[1]);
            double radius = Double.parseDouble(geotaggedCoordinates[2]);
            GeoPoint geopoint1 = new GeoPoint(latitude, longitude);
            GeoPoint geopoint2 = new GeoPoint(centerx, centery);
            double result = geopoint1.distanceToAsDouble(geopoint2);
            Utils.logInfo("DISTANCE BETWEEN POINTS ", String.valueOf(result));
            Utils.logInfo("VALIDATION RADIUS ", String.valueOf(radius));
//            double result = Math.pow((latitude - centerx), 2) + Math.pow((longitude - centery), 2) * 111.324;
            if (result <= radius) {
                return true;
            }
        } else {
            return true;
        }

        return false;
    }

    public boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            // JSONArray is valid as well...
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    public boolean isValidUUID(String test) {
        if (test.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) {
            return true;
        } else {
            return false;
        }
    }

    public void cancelAsyncThreads() {
        AppBackgroundSync.isSyncInProgress = false;
        AlarmHelper.cancelBackgroundSyncAlarm();
        AlarmHelper.cancelMediaAlarm();
    }

    public void deleteAllMediaFromStorage(List<String> mediaPaths) {

        if (mediaPaths != null && !mediaPaths.isEmpty()) {
            for (String path : mediaPaths) {
                File file = new File(path);
                if (file != null && file.exists()) {
                    file.delete();
                }
            }
        }
    }

    public void resizeImage(String filePath) {

        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

        float maxHeight = 816.0f;
        float maxWidth = 612.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);

            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filePath);
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }
        return inSampleSize;
    }

    public void deleteSyncedMediaFromStorageAndDB(String appId, String userId, String projectId, int days) {

        List<FormMedia> formMediaList = UAAppContext.getInstance().getDBHelper().getMediaForProjectWithGivenStatus
                (appId, projectId, userId, MediaRequestStatus.SUCCESS_STATUS.getValue(), days);

        List<String> mediaPaths = new ArrayList<>();

        for (int i = 0; i < formMediaList.size(); i++) {

            UAAppContext.getInstance().getDBHelper().deleteFormMedia(formMediaList.get(i).getmUUID());
            mediaPaths.add(formMediaList.get(i).getLocalPath());
        }

        deleteAllMediaFromStorage(mediaPaths);
    }

    public int getUnsyncedMediaCount(String mAppId) {
        List<Integer> statusList = new ArrayList<>();
        statusList.add(MediaRequestStatus.NEW_STATUS.getValue());
        statusList.add(MediaRequestStatus.PENDING_RETRY_STATUS.getValue());
        int unsyncedMediaCount = UAAppContext.getInstance().getDBHelper().getFormMediaCountForApp(mAppId,
                UAAppContext.getInstance().getUserID(), statusList);
        return unsyncedMediaCount;
    }

    public int getUnsyncedProjectCount(String mAppId) {
        List<ProjectSubmissionUploadStatus> projectStatusList = new ArrayList<>();
        projectStatusList.add(ProjectSubmissionUploadStatus.SERVER_ERROR);
        projectStatusList.add(ProjectSubmissionUploadStatus.UNSYNCED);
        int unsyncedProjectCount = UAAppContext.getInstance().getDBHelper().getProjectSubmissionCountForApp(mAppId,
                UAAppContext.getInstance().getUserID(), projectStatusList);

        return unsyncedProjectCount;
    }

    public String getProjectIcon(Project project, String mAppId, ProjectIconInfo projectIconInfo) {
        String iconUrl = null;
//        if (project.mProjectIcon != null) {
//            projectIconInfo = project.mProjectIcon;
//        }

            if (projectIconInfo != null && projectIconInfo.mStaticUrl != null && !projectIconInfo.mStaticUrl.isEmpty()) {
                iconUrl = projectIconInfo.mStaticUrl;
            }
            if (projectIconInfo != null && projectIconInfo.mDynamicKeyName != null && !projectIconInfo.mDynamicKeyName.isEmpty()) {
                LatestProjectDataService latestProjectDataService = LatestProjectDataService.getInstance();
                Map<String, LatestFieldValue> latestFieldValueMapSubmittedByUser = latestProjectDataService
                        .getKeyLatestValuesMapForProject(UAAppContext.getInstance().getUserID(), mAppId, project.mProjectId);


                String keyValue = latestProjectDataService.getLatestValueForKey(latestFieldValueMapSubmittedByUser, project.mFields, projectIconInfo.mDynamicKeyName);
                keyValue = StringUtils.getFormattedText(keyValue);
                if (keyValue != null) {
                    iconUrl = iconUrl == null ? null : iconUrl + keyValue + ".png";
                }
            }
//        }
        return iconUrl;
    }

    public String findFile(String name, File file) {
        File[] list = file.listFiles();
        if (list != null) {
            for (File fil : list) {
                if (fil.isDirectory()) {
                    findFile(name, fil);
                } else if (name.equalsIgnoreCase(fil.getName())) {
                    return fil.getAbsolutePath();
                }
            }
        }
        return "";
    }

    public void listFilesInDir(File folder) {

        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                System.out.println("File " + listOfFiles[i].getName());
            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }
        }
    }

    public static class UploadFormImageTask extends AsyncTask<Void, Void, String> {

        FormImage mFormImage;
        SharedPreferences mAppPreferences;
        OnFormTaskCompletedListener mOnTaskCompletedListener;
        String mAppId;
        String mUserId;

        public UploadFormImageTask(SharedPreferences preferences, FormImage formImage
                , OnFormTaskCompletedListener listener, String appId, String userId) {
            this.mAppPreferences = preferences;
            this.mFormImage = formImage;
            this.mOnTaskCompletedListener = listener;
            this.mAppId = appId;
            this.mUserId = userId;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                final MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");
                RequestBody requestBody;

                Utils.getInstance().showLog("IMAGE PATH FROM SERVER", mFormImage.mLocalPath);
                File file = new File(mFormImage.mLocalPath);
                if (mFormImage.hasGeotag()) {
                    List<String> latLng = Arrays.asList(mFormImage.mGeotag.split("\\s*,\\s*"));
                    if (latLng.size() == 2) {
                        requestBody = new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("superapp", Constants.SUPER_APP_ID)
                                .addFormDataPart("appid", mFormImage.mAppId)
                                .addFormDataPart("projectid", mFormImage.mProjectId)
                                .addFormDataPart("imageid", mFormImage.mUUID)
                                .addFormDataPart("token", mAppPreferences.getString
                                        (Constants.USER_TOKEN_PREFERENCE_KEY
                                                , Constants.USER_TOKEN_PREFERENCE_DEFAULT))
                                .addFormDataPart("userid", mFormImage.mUserId)
                                .addFormDataPart("syncts", String.valueOf(System.currentTimeMillis()))
                                .addFormDataPart("lat", latLng.get(0))
                                .addFormDataPart("lon", latLng.get(1))
                                .addFormDataPart("key", "")
                                .addFormDataPart("image", mFormImage.mUUID, RequestBody.create(MEDIA_TYPE_JPEG, file))
                                .build();
                    } else {
                        requestBody = new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("superapp", Constants.SUPER_APP_ID)
                                .addFormDataPart("appid", mFormImage.mAppId)
                                .addFormDataPart("projectid", mFormImage.mProjectId)
                                .addFormDataPart("imageid", mFormImage.mUUID)
                                .addFormDataPart("token", mAppPreferences.getString
                                        (Constants.USER_TOKEN_PREFERENCE_KEY
                                                , Constants.USER_TOKEN_PREFERENCE_DEFAULT))
                                .addFormDataPart("userid", mFormImage.mUserId)
                                .addFormDataPart("syncts", String.valueOf(System.currentTimeMillis()))
                                .addFormDataPart("lat", "0")
                                .addFormDataPart("lon", "0")
                                .addFormDataPart("key", "")
                                .addFormDataPart("image", mFormImage.mUUID, RequestBody.create(MEDIA_TYPE_JPEG, file))
                                .build();
                    }
                } else {
                    requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("superapp", Constants.SUPER_APP_ID)
                            .addFormDataPart("appid", mFormImage.mAppId)
                            .addFormDataPart("projectid", mFormImage.mProjectId)
                            .addFormDataPart("imageid", mFormImage.mUUID)
                            .addFormDataPart("token", mAppPreferences.getString
                                    (Constants.USER_TOKEN_PREFERENCE_KEY
                                            , Constants.USER_TOKEN_PREFERENCE_DEFAULT))
                            .addFormDataPart("lat", "0")
                            .addFormDataPart("lon", "0")
                            .addFormDataPart("userid", mFormImage.mUserId)
                            .addFormDataPart("syncts", String.valueOf(System.currentTimeMillis()))
                            .addFormDataPart("key", "")
                            .addFormDataPart("image", mFormImage.mUUID, RequestBody.create(MEDIA_TYPE_JPEG, file))
                            .build();
                }

                Request request = new Request.Builder()
                        .url(Constants.BASE_URL + "submitimage")
                        .post(requestBody)
                        .build();

                OkHttpClient client = new OkHttpClient();
                Response response = client.newCall(request).execute();

                Utils.getInstance().showLog("IMAGE RESPONSE", response.toString());

                if (response.isSuccessful()) {
                    return response.body().string();
                } else {
                    return null;
                }

            } catch (UnknownHostException | UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            mOnTaskCompletedListener.onTaskCompleted(s, mAppId, mUserId, mFormImage);
        }
    }

    public String getRealPathFromUri(Uri contentURI, Context context) {
        String result;
        Cursor cursor = context.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            File newFile = new File(contentURI.getPath());
            result = newFile.getAbsolutePath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    public void storeValueInAppPreferences(String key, String value){
        SharedPreferences preferences = UAAppContext.getInstance().getAppPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getValueFromAppPreferences(String key){
        String value = UAAppContext.getInstance().getAppPreferences().getString(key, "");
        return value;
    }

    public Map<String, String> getMapFromString(String mapString){
        Map<String, String> map = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            map = objectMapper.readValue(mapString, new TypeReference<Map<String, String>>() {});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    public long getFileSizeInBytes(String path){
        File file = new File(path);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] newBytes = trim(bytes);
        return newBytes.length;
    }

    private byte[] trim(byte[] bytes) {
        int i = bytes.length - 1;
        while (i >= 0 && bytes[i] == 0) {
            --i;
        }
        return Arrays.copyOf(bytes, i + 1);
    }

    public void setEditTextMaxLength(EditText editText, int length) {
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(length);
        editText.setFilters(filterArray);
    }

    public void setAlphabetFilter(EditText editText){

        if (editText != null && editText.getText() != null) {
            editText.setFilters(new InputFilter[]{
                    new InputFilter() {
                        @Override
                        public CharSequence filter(CharSequence cs, int start,
                                                   int end, Spanned spanned, int dStart, int dEnd) {

                            CharSequence res = "";
                            for (int i = start; i < end; i++) {
                                if (!Character.isLetter(cs.charAt(i)) && !Character.isWhitespace(cs.charAt(i))) { // Accept only letter & digits ; otherwise just return

                                } else{
                                    res = res.toString() + cs.charAt(i);
                                }
                            }
                            if (res.length() == cs.length())
                                return null;
                            else
                                return res;
                        }
                    }
            });
        }
    }
}

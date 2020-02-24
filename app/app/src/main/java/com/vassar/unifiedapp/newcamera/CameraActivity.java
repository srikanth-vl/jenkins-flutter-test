package com.vassar.unifiedapp.newcamera;

import android.Manifest;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.picasso.Picasso;
import com.vassar.unifiedapp.R;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.vassar.unifiedapp.application.UnifiedAppApplication;
import com.vassar.unifiedapp.asynctask.TimestampImageTask;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.db.UnifiedAppDBHelper;
import com.vassar.unifiedapp.model.FormMedia;
import com.vassar.unifiedapp.model.GpsValidation;
import com.vassar.unifiedapp.model.Project;
import com.vassar.unifiedapp.model.ProjectList;
import com.vassar.unifiedapp.ui.OpenCameraImageThumbnail;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.MediaRequestStatus;
import com.vassar.unifiedapp.utils.MediaSubType;
import com.vassar.unifiedapp.utils.MediaType;
import com.vassar.unifiedapp.utils.PropertyReader;
import com.vassar.unifiedapp.utils.StringUtils;
import com.vassar.unifiedapp.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.vassar.unifiedapp.utils.Constants.PICK_FROM_GALLERY;


public class CameraActivity extends AppCompatActivity {

    private CameraPreview mPreview;

    private LinearLayout mCameraImagesLayout;
    private String mDatatype;
    private int mMaxImages;
    private String mProjectId;
    private String mProjectListConfigurationString;
    private ProjectList mProjectListConfiguration;
    private double mLatitude;
    private double mLongitude;
    private double mAccuracy;
    private float mBearing;
    private Context context;
    private Button captureButton;
    private ImageButton galleryButton;

    private ArrayList<FormMedia> mFormMedia = new ArrayList<>();
    private FormMedia mCurrentImage;
    private static final String SEPARATOR = ",";
    private int mImageCount = 0;
    private UnifiedAppDBHelper mDBHelper;
    private String mAppId;
    private String mUserId;
    private File pictureFile = null;
    private Project mProject;
    private TextView accuracy;
    private GpsValidation gpsValidation;
    private Map<String, JSONObject> mSubmittedFields = new HashMap<>();
    private HashMap<String, String> submittedFieldFromIntent = new HashMap<>();
    private TextView bearing;
    private String mUIType;

    private ArrayList<String> uuids = null;
    private static final String TAG = CameraActivity.class.getName();

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        ((UnifiedAppApplication)getApplication()).initializeLocation();

        // Create our Preview view and set it as the content of our activity.

        // currentTag & maxImages in the intent
        mDatatype = getIntent().getStringExtra("datatype");
        mMaxImages = getIntent().getIntExtra("max", Constants.DEFAULT_MAX_IMAGES);
        mProjectId = getIntent().getStringExtra("projectId");
        mAppId = getIntent().getStringExtra("appId");
        mUserId = getIntent().getStringExtra("userId");
        uuids = getIntent().getStringArrayListExtra("uuids");
        mLatitude = ((UnifiedAppApplication)getApplication()).mUserLatitude;
        mLongitude = ((UnifiedAppApplication)getApplication()).mUserLongitude;
        mAccuracy = ((UnifiedAppApplication)getApplication()).mAccuracy;
        gpsValidation = getIntent().getParcelableExtra("gps_validation");
        submittedFieldFromIntent = (HashMap<String, String>) getIntent().getSerializableExtra("submittedFields");
        mSubmittedFields = convertStringToJSONObjectInMap(submittedFieldFromIntent);
        mUIType = getIntent().getStringExtra("uitype");

        mDBHelper = new UnifiedAppDBHelper(this);
        context = getBaseContext();

        // TODO : Uncomment
        mProjectListConfigurationString = null;
        mProject = UAAppContext.getInstance().getProjectFromProjectList(mProjectId);

        // Initialize Views
        initializeViews();

    }


    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            if (pictureFile == null) {
                Log.d(TAG, "Error creating media file, check storage permissions");
                return;
            }

            FileOutputStream fos = null;
            try {

                fos = new FileOutputStream(pictureFile);
                fos.write(data);

                mCurrentImage.setMediaClickTimeStamp(System.currentTimeMillis());

            } catch (FileNotFoundException e) {
                Log.d("Info", "File not found: " + e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                Log.d("TAG", "Error accessing file: " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            Utils.logInfo("ImageSizeAfterCapture", String.valueOf(pictureFile.length()/Math.pow(1024,2)));

            runOnUiThread(() -> captureButton.setEnabled(true));

            Intent i = new Intent(CameraActivity.this, CameraResultImage.class);
            i.putExtra("path", pictureFile.getAbsolutePath());
            startActivityForResult(i, Constants.REQUEST_IMAGE_RESULT);

        }
    };


    public static Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.setRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }


    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /** Get number of cameras this device has */
    private int getNumberOfCameras(Context context) {
        return Camera.getNumberOfCameras();
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            Log.e("CAMERATEST1", "camera not available -- camera.open --");
        }
        return c; // returns null if camera is unavailable
    }

    private Camera.Size getBestPreviewSize(int width, int height,
                                           Camera.Parameters parameters) {
        Camera.Size result=null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width<=width && size.height<=height) {
                if (result==null) {
                    result=size;
                }
                else {
                    int resultArea=result.width*result.height;
                    int newArea=size.width*size.height;

                    if (newArea>resultArea) {
                        result=size;
                    }
                }
            }
        }

        return(result);
    }

    private void releaseCamera(){
        Camera camera = mPreview.getCameraInitialized();
        if (camera != null){
            camera.release();        // release the camera for other applications
            //camera = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        releaseCamera();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();              // release the camera immediately on pause event
    }

    private int normalize(int degrees) {
        if (degrees > 315 || degrees <= 45) {
            return 0;
        }
        if (degrees > 45 && degrees <= 135) {
            return 90;
        }
        if (degrees > 135 && degrees <= 225) {
            return 180;
        }
        if (degrees > 225 && degrees <= 315) {
            return 270;
        }

        throw new RuntimeException("The physics as we know them are no more. Watch out for anomalies.");
    }

    // When completing a video recording, do not release the camera
    // or else your preview will be stopped

    // To make the URI support work profiles, first convert the file URI
    // to a content URI. Then, add the content URI to EXTRA_OUTPUT of an Intent.

    private void initializeLocation() {
        if (((UnifiedAppApplication) getApplication()).mFusedLocationClient != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Trying to retrieve location
                Utils.getInstance().checkForLocation(this);

            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        Constants.PERMISSION_FINE_LOCATION);
            }
        } else {
            // If the SDK is >= Marshmello, runtime permissions required
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            Constants.PERMISSION_FINE_LOCATION);
                } else {
                    ((UnifiedAppApplication) getApplication()).mFusedLocationClient =
                            LocationServices.getFusedLocationProviderClient(this);

                    // Initializing location request
                    Utils.getInstance().createLocationRequest(this);
                }
            } else {
                ((UnifiedAppApplication) getApplication()).mFusedLocationClient =
                        LocationServices.getFusedLocationProviderClient(this);

                // Initializing location request
                Utils.getInstance().createLocationRequest(this);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[]
            , int[] grantResults) {
        switch (requestCode) {
            case Constants.PERMISSION_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted!
                    ((UnifiedAppApplication) getApplication()).mFusedLocationClient =
                            LocationServices.getFusedLocationProviderClient(this);

                    // Initializing location request
                    Utils.getInstance().createLocationRequest(this);
                } else {
                    // permission denied!
                }
                break;
            }
        }
    }

    private void retainOlderImages() {
        for (String uuid : uuids) {
            FormMedia formImage = mDBHelper.getFormMedia(uuid, mAppId, mUserId);
            if (formImage != null) {
                addThumbnail(formImage);
            }
        }
    }

    private void initializeViews() {

        mCameraImagesLayout = (LinearLayout) findViewById(R.id.relative);
        accuracy = (TextView) findViewById(R.id.accuracy);
        bearing = (TextView) findViewById(R.id.bearing);


        int locationAccuracy = (int)mAccuracy;
        accuracy.setText(getResources().getString(R.string.gps_accuracy)+ String.valueOf(locationAccuracy) + " m");
        bearing.setText("Bearing : " + String.valueOf(mBearing));


        if (uuids != null && uuids.size() > 0) {
            retainOlderImages();
        }

        mPreview = new CameraPreview(this);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        // Add a listener to the Capture button
        captureButton = (Button) findViewById(R.id.capture);
        captureButton.setEnabled(true);

        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        captureButton.setEnabled(false);
                        // get an image from the camera

                        if (mImageCount < mMaxImages) {
                            try {
                                pictureFile = createImageFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            mPreview.getCameraInitialized().takePicture(null, null, mPicture);

                        }else{
                            Toast.makeText(context, getResources().getString(R.string.MAX_IMAGES_REACHED), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
        Button submitButton = (Button) findViewById(R.id.submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFormMedia.size() > 0) {

                        StringBuilder uuidBuilder = new StringBuilder();
                        for (FormMedia formImage : mFormMedia) {
                            uuidBuilder.append(formImage.getmUUID());
                            if(formImage.ismHasGeotag()) {
                                uuidBuilder.append(Constants.IMAGE_UUID_LONG_LAT_SEPARATOR +formImage.getLongitude()+ Constants.IMAGE_UUID_LONG_LAT_SEPARATOR +formImage.getLatitude());
                            }

                            uuidBuilder.append(SEPARATOR);
                        }
                        String imageUUIDs = uuidBuilder.toString();
                        //Remove last comma
                        imageUUIDs = imageUUIDs.substring(0, imageUUIDs.length() - SEPARATOR.length());

                        Intent data = new Intent();
                        data.putParcelableArrayListExtra("formImages", mFormMedia);
                        data.putExtra("imageuuids", imageUUIDs);
                        setResult(RESULT_OK, data);
                        finish();

                } else {
                    Toast.makeText(CameraActivity.this, getResources().getString(R.string.NO_IMAGES_TAKEN)
                            , Toast.LENGTH_SHORT).show();
                }
            }
        });

        galleryButton = (ImageButton) findViewById(R.id.lower_camera_gallery);
        if(PropertyReader.getBooleanProperty("UPLOAD_FROM_GALLERY")){
            galleryButton.setVisibility(View.VISIBLE);
        }
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mImageCount < mMaxImages) {
                    if (ContextCompat.checkSelfPermission(getBaseContext(),
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        intent.setAction(Intent.ACTION_PICK);
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_FROM_GALLERY);

                    }
                } else {
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.MAX_IMAGES_REACHED), Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    /** Saving the image file locally*/
    private File createImageFile() throws IOException {
        // Adding the image to the group of images

        mCurrentImage = createNewFormMedia();
        // Create an image file name
        String imageFileName = mCurrentImage.getmUUID();

        File storageDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = new File(storageDir.getAbsolutePath() + "/"+ imageFileName + ".jpg");
        try {
            imageFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mCurrentImage.setmLocalPath(imageFile.getAbsolutePath());

        return imageFile;
    }

    private byte [] makeThumbnailImage(File f, int quality) {

        Bitmap bitmap ;
        byte[] bitmapdata = null;

        ByteArrayOutputStream bmpStream = new ByteArrayOutputStream();
        bitmap = decodeFile(f,153,204);

        if(bitmap == null) {
            Utils.getInstance().showLog("ERROR", "Cannot decode file "+f);
            return null;
        }

        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bmpStream);

        bitmapdata = bmpStream.toByteArray();

        try {
            bmpStream.flush();
            bmpStream.close();
            bitmap.recycle();
        } catch (IOException e) {
            Utils.getInstance().showLog("ERROR", ""+e);
            e.printStackTrace();
        }
        return bitmapdata;
    }

    @Nullable
    private static Bitmap decodeFile(File f, int max_Height, int max_Width) {
        try {
            Bitmap bitmap = null;
            FileInputStream fileInputStream;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            try {
                fileInputStream = new FileInputStream(f);
                BitmapFactory.decodeStream(fileInputStream, null ,options);
                fileInputStream.close();
            } catch (FileNotFoundException e) {
                return null;
            }
            int actualHeight = options.outHeight;
            int actualWidth = options.outWidth;

            float imgRatio = actualWidth / actualHeight;
            float maxRatio = (float) max_Width / (float) max_Height;

            if (actualHeight > (float) max_Height || actualWidth > (float) max_Width) {
                if (imgRatio < maxRatio) {
                    imgRatio = (float) max_Height / actualHeight;
                    actualWidth = (int) (imgRatio * actualWidth);
                    actualHeight = (int) (float) max_Height;
                } else if (imgRatio > maxRatio) {
                    imgRatio = (float) max_Width / actualWidth;
                    actualHeight = (int) (imgRatio * actualHeight);
                    actualWidth = (int) (float) max_Width;
                } else {
                    actualHeight = (int) (float) max_Height;
                    actualWidth = (int) (float) max_Width;
                }
            }

            options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

            options.inJustDecodeBounds = false;

            try {
                fileInputStream = new FileInputStream(f);
                bitmap = BitmapFactory.decodeStream(fileInputStream, null, options);
                fileInputStream.close();
            } catch (OutOfMemoryError exception) {
                exception.printStackTrace();
            }
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize = inSampleSize << 1;
            }
        }
        return inSampleSize;
    }


    private void addThumbnail(FormMedia image) {

        final OpenCameraImageThumbnail view = new OpenCameraImageThumbnail(getBaseContext());

        Utils.logInfo("addthumbnail", image.getLocalPath());

        File imgFile = new File(image.getLocalPath());
        Picasso.get().load(imgFile)
                .resize(150, 150)
                .placeholder(R.drawable.placeholder)
                .into(view.getImgPhoto());

        view.setId(mImageCount);

        mCameraImagesLayout.addView(view);
        mFormMedia.add(image);
        mImageCount++;

        view.getBtnClose().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mDBHelper.deleteFormMedia(image.getmUUID());
                String fileName = imgFile.getName().substring(0, imgFile.getName().indexOf("."));
                if(image.getmUUID().equals(fileName)) {
                    Utils.getInstance().deleteImageFromStorage(getBaseContext(), image.getLocalPath());
                }
                mFormMedia.remove(image);
                mCameraImagesLayout.removeView(view);
                mImageCount--;
                captureButton.setEnabled(true);
            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case Constants.REQUEST_IMAGE_RESULT:

                if(resultCode == RESULT_CANCELED) {
                    Utils.getInstance().deleteImageFromStorage(context, pictureFile.getAbsolutePath());
                }

                else if(resultCode == RESULT_OK) {

                    boolean valid = onCameraCaptureResult();
                    if(valid){
                        addThumbnail(mCurrentImage);
                        Utils.logInfo("ImageSizeAFterThumbnail", String.valueOf(pictureFile.length()/Math.pow(1024,2)));
//                        TimestampImageTask timestampImageTask = new TimestampImageTask(context, pictureFile);
//                        timestampImageTask.execute();
                    }
                    else {
                        Utils.getInstance().deleteImageFromStorage(context, mCurrentImage.getLocalPath());
                    }
                }
                break;

            case Constants.REQUEST_LOCATION_SETTINGS:
                switch (resultCode) {
                    case 0:
                        // User didnt turn on location update

                        break;

                    case -1:
                        // User has turned on location
                        Utils.getInstance().checkForLocation(this);
                        break;
                }
                break;

            case PICK_FROM_GALLERY:

                if (resultCode == RESULT_OK && null != data) {
                    if (data.getClipData() == null) {
                        Uri newUri = data.getData();
                        String filePath = Utils.getInstance().getRealPathFromUri(newUri, this);
                        FormMedia newFormMedia = createNewFormMedia();
                        newFormMedia.setmLocalPath(filePath);
                        newFormMedia = createByteArrayForFormMedia(newFormMedia);
                        addThumbnail(newFormMedia);

                    } else {

                        ClipData mClipData = data.getClipData();
                        int pickedImageCount;

                        if (mClipData.getItemCount() > (mMaxImages - mImageCount)) {
                            Toast.makeText(this, "Cannot select more than " + (mMaxImages - mImageCount) + " images.",
                                    Toast.LENGTH_SHORT).show();
                        } else {

                            for (pickedImageCount = 0; pickedImageCount < mClipData.getItemCount();
                                 pickedImageCount++) {
                                String filePath = Utils.getInstance().getRealPathFromUri(
                                        mClipData.getItemAt(pickedImageCount).getUri(), this);
                                FormMedia newFormMedia = createNewFormMedia();
                                newFormMedia.setmLocalPath(filePath);
                                newFormMedia = createByteArrayForFormMedia(newFormMedia);
                                addThumbnail(newFormMedia);
                            }
                        }
                    }
                }
                break;
            }
        }


    private boolean onCameraCaptureResult() {

        boolean validImage = true;

        // Picking image from the currently stored path and saving bitmap for formImage
        createByteArrayForFormMedia(mCurrentImage);

        //getting last updated location
        mLongitude = ((UnifiedAppApplication) getApplication()).mUserLongitude;
        mLatitude = ((UnifiedAppApplication) getApplication()).mUserLatitude;
        mAccuracy = ((UnifiedAppApplication) getApplication()).mAccuracy;
        mBearing = ((UnifiedAppApplication) getApplication()).mUserBearing;

        accuracy.setText(getResources().getString(R.string.gps_accuracy)+ String.valueOf((int)mAccuracy) + " m");
        bearing.setText("Bearing : " + String.valueOf(mBearing));

        if (mUIType.equals("geotagimagefused") || mUIType.equals("geotagimage")) {

            mCurrentImage.setmHasGeotag(true);
            boolean isValid = true;

            if (mProject != null) {
                if (mLatitude != Constants.DEFAULT_LATITUDE && mLongitude != Constants.DEFAULT_LONGITUDE &&
                        mAccuracy != Constants.DEFAULT_ACCURACY) {

                    if (gpsValidation != null && gpsValidation.getType() != null && !gpsValidation.getType().isEmpty() && gpsValidation.getRadius() > 0) {
                        switch (gpsValidation.getType()) {
                            case "circular":
                                if (gpsValidation.getSource().equalsIgnoreCase("project")) {
                                    if (mProject.mLatitude != null && !mProject.mLatitude.isEmpty() && mProject.mLongitude != null && !mProject.mLongitude.isEmpty()){
                                        boolean valid = validateGps(mProject.mLatitude, mProject.mLongitude, String.valueOf(mLatitude), String.valueOf(mLongitude));
                                        if (valid) {
                                        } else {
                                            Toast.makeText(this, getResources().getString(R.string.SELECT_VALID_LOCATION), Toast.LENGTH_SHORT).show();
                                            isValid = false;
                                        }
                                    } else {
                                        Toast.makeText(this, getResources().getString(R.string.VALIDATION_VALUES_NOT_FOUND), Toast.LENGTH_SHORT).show();
                                    }
                                } else if (gpsValidation.getSource().equalsIgnoreCase("key")) {
                                    String valueFromSource = getSubmittedFieldFromKey(mSubmittedFields, gpsValidation.getSource());
                                    List<String> geotag = getGeotagFromValue(gpsValidation.getKeyType(), valueFromSource);

                                    if (geotag.get(0) != null && !geotag.get(0).isEmpty() && geotag.get(1) != null && !geotag.get(1).isEmpty()) {
                                        boolean valid = validateGps(geotag.get(0), geotag.get(1), String.valueOf(mLatitude), String.valueOf(mLongitude));
                                        if (valid) {
                                        } else {
                                            Toast.makeText(this, getResources().getString(R.string.SELECT_VALID_LOCATION), Toast.LENGTH_SHORT).show();
                                            isValid = false;
                                        }
                                    }else {
                                        Toast.makeText(this, getResources().getString(R.string.VALIDATION_VALUES_NOT_FOUND), Toast.LENGTH_SHORT).show();
                                    }
                                }
                                break;

                            case "bbox":

                                break;
                            default:
                        }
                    } else if (mProject.mBBoxValidation != null && !mProject.mBBoxValidation.isEmpty()) {
                        // BBox validation
                        isValid = Utils.getInstance().geotagValidateImage(mLatitude, mLongitude, mProject.mBBoxValidation);

                    } else if (mProject.mCentroidValidation != null && !mProject.mCentroidValidation.isEmpty()) {
                        // Centroid validation
                        isValid = Utils.getInstance().geotagValidateImage(mLatitude, mLongitude, mProject.mCentroidValidation);
                    } else {

//                        Toast.makeText(getApplicationContext(), "No validation for location to be performed", Toast.LENGTH_LONG).show();
                        // No validation to be performed
                    }
                } else {
                    ((UnifiedAppApplication)this.getApplication()).initializeLocation();
                    ((UnifiedAppApplication)this.getApplication()).startLocationUpdates();

                    // Do not have the current user location, cannot perform validation
                    if((mProject.mBBoxValidation != null && !mProject.mBBoxValidation.isEmpty()) || (mProject.mCentroidValidation != null && !mProject.mCentroidValidation.isEmpty())) {
                        Toast.makeText(this, getResources().getString(R.string.NO_CURRENT_LOCATION), Toast.LENGTH_LONG).show();
                        validImage = false;
                        mCurrentImage.setAccuracy(Constants.DEFAULT_ACCURACY);
                        mCurrentImage.setLongitude(Constants.DEFAULT_LONGITUDE);
                        mCurrentImage.setLatitude(Constants.DEFAULT_LATITUDE);
                        return validImage;
                    } else{
                        mLongitude = ((UnifiedAppApplication) getApplication()).mUserLongitude;
                        mLatitude = ((UnifiedAppApplication) getApplication()).mUserLatitude;
                        mAccuracy = ((UnifiedAppApplication) getApplication()).mAccuracy;
                        mBearing = ((UnifiedAppApplication) getApplication()).mUserBearing;

                        mCurrentImage.setLongitude(mLongitude);
                        mCurrentImage.setLatitude(mLatitude);
                        mCurrentImage.setAccuracy(mAccuracy);
                        mCurrentImage.getAdditionalProps().put(Constants.MEDIA_LOCATION_BEARING, String.valueOf(mBearing));
                    }
                }

                if (isValid) {

                    mCurrentImage.setAccuracy(mAccuracy);
                    mCurrentImage.setLongitude(mLongitude);
                    mCurrentImage.setLatitude(mLatitude);
                    mCurrentImage.getAdditionalProps().put(Constants.MEDIA_LOCATION_BEARING, String.valueOf(mBearing));

                    validImage = true;

                } else {

                    // Tell the user that this image isn't valid (geotagged)
                    Toast.makeText(this, getResources().getString(R.string.GEOTAG_IMAGE_VALIDATION_FAILED), Toast.LENGTH_LONG).show();
                    validImage = false;

                }
            }
            else {
//                    Toast.makeText(getApplicationContext(), "No project associated, so no validation for location can be performed", Toast.LENGTH_LONG).show();
                // Project is null, cannot perform validation
                mLongitude = ((UnifiedAppApplication) getApplication()).mUserLongitude;
                mLatitude = ((UnifiedAppApplication) getApplication()).mUserLatitude;
                mAccuracy = ((UnifiedAppApplication) getApplication()).mAccuracy;
                mBearing = ((UnifiedAppApplication) getApplication()).mUserBearing;

                mCurrentImage.setLongitude(mLongitude);
                mCurrentImage.setLatitude(mLatitude);
                mCurrentImage.setAccuracy(mAccuracy);
                mCurrentImage.getAdditionalProps().put(Constants.MEDIA_LOCATION_BEARING, String.valueOf(mBearing));

            }
        }

        else {
            mCurrentImage.setmHasGeotag(false);
            validImage = true;
        }
        return validImage;
    }

    private FormMedia createNewFormMedia(){

        UUID uuid = UUID.randomUUID();
        FormMedia newMedia = new FormMedia();

        newMedia.setmUUID(uuid.toString());
        newMedia.setmAppId(mAppId);
        newMedia.setmUserId(mUserId);
        newMedia.setmProjectId(mProjectId);
        newMedia.setMediaType(MediaType.IMAGE.getValue());
        newMedia.setMediaSubType(MediaSubType.FULL.getValue());
        newMedia.setMediaFileExtension(Constants.IMAGE_EXT);
        newMedia.setMediaUploadRetries(Constants.DEFAULT_RETRIES);
        newMedia.setMediaRequestStatus(MediaRequestStatus.NEW_STATUS.getValue());
        newMedia.setmHasGeotag(false);
        Map<String, String> additionalProps = new HashMap<>();
        newMedia.setAdditionalProps(additionalProps);

        return newMedia;
    }

    private FormMedia createByteArrayForFormMedia(FormMedia formMedia){

        File imgFile = new File(formMedia.getLocalPath());
        Bitmap bitmap = null;

        if (imgFile.exists()) {
            byte[] bitmapByteArray = makeThumbnailImage(imgFile, 30);
            if (bitmapByteArray != null) {
                bitmap = BitmapFactory.decodeByteArray(bitmapByteArray, 0, bitmapByteArray.length);
            }
            if (bitmap != null) {
                formMedia.setmBitmap(bitmapByteArray);
            }
        }
        return formMedia;
    }

    public String getSubmittedFieldFromKey(Map<String, JSONObject> submittedFields, String sskey) {
        if (submittedFields != null) {
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

    private Map<String, JSONObject> convertStringToJSONObjectInMap(Map<String, String> map){
        ObjectMapper mapper = new ObjectMapper();
        Map<String, JSONObject> convertedMap = new HashMap<>();
        if (map != null) {
            for (String key : map.keySet()) {
                String jsonString = map.get(key);
                if (jsonString != null && !jsonString.isEmpty()) {
                    JSONObject jsonObject = mapper.convertValue(jsonString, JSONObject.class);
                    convertedMap.put(key, jsonObject);
                }
            }
        }
        return convertedMap;
    }

    public List<String> getGeotagFromValue(String keyType, String value){
        List<String> geotag = new ArrayList<>();
        if (keyType != null && value != null) {
            if (keyType.equalsIgnoreCase("image")) {
                List<String> geotagWithUUID = StringUtils.getStringListFromDelimiter(value, Constants.IMAGE_UUID_LONG_LAT_SEPARATOR);
                geotag.add(geotagWithUUID.get(1));
                geotag.add(geotagWithUUID.get(2));
            } else if (keyType.equalsIgnoreCase("geotag")) {
                geotag = StringUtils.getStringListFromDelimiter(value, ",");
            }
        }
        return geotag;
    }

    private boolean validateGps(String centerLatitude, String centerLongitude, String droppedLatitude, String droppedLongtiude){
        double centerLat = Double.parseDouble(centerLatitude);
        double centerLon = Double.parseDouble(centerLongitude);

        double droppedLat = Double.parseDouble(droppedLatitude);
        double droppedLon = Double.parseDouble(droppedLongtiude);

        double radius = gpsValidation.getRadius();
        GeoPoint g1 = new GeoPoint(centerLat, centerLon);
        GeoPoint g2 = new GeoPoint(droppedLat, droppedLon);
        double result = g1.distanceToAsDouble(g2);

        if (result <= radius){
            return true;
        } else{
            return false;
        }
    }
}

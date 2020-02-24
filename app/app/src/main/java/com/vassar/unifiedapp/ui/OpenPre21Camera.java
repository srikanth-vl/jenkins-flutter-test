//package com.vassar.unifiedapp.ui;
//
//import android.Manifest;
//import android.app.Activity;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//
//import android.graphics.drawable.Drawable;
//import android.hardware.Camera;
//import android.location.LocationManager;
//import android.media.ExifInterface;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Environment;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.v4.app.ActivityCompat;
//import android.support.v7.app.AppCompatActivity;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.FrameLayout;
//import android.widget.ImageButton;
//import android.widget.ImageView;
//import android.widget.ProgressBar;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.gson.Gson;
//import com.vassar.unifiedapp.R;
//import com.vassar.unifiedapp.application.UnifiedAppApplication;
//import com.vassar.unifiedapp.asynctask.TimestampImageTask;
//import com.vassar.unifiedapp.db.UnifiedAppDBHelper;
//import com.vassar.unifiedapp.model.FormImage;
//import com.vassar.unifiedapp.model.FormMedia;
//import com.vassar.unifiedapp.model.Project;
//import com.vassar.unifiedapp.model.ProjectList;
//import com.vassar.unifiedapp.utils.Constants;
//import com.vassar.unifiedapp.utils.MediaRequestStatus;
//import com.vassar.unifiedapp.utils.MediaSubType;
//import com.vassar.unifiedapp.utils.MediaType;
//import com.vassar.unifiedapp.utils.Utils;
//
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.Iterator;
//import java.util.List;
//import java.util.UUID;
//
//import static android.widget.Toast.LENGTH_SHORT;
//import static android.widget.Toast.makeText;
//import static com.vassar.unifiedapp.camera.CameraHelper.MEDIA_TYPE_IMAGE;
//import static com.vassar.unifiedapp.camera.CameraHelper.getDefaultCameraInstance;
//
//
//public class OpenPre21Camera extends AppCompatActivity {
//
//    private Camera mCamera;
//    private CameraPreview mPreview;
//    private Context context;
//    final private String TAG = "AndroidCameraApi";
//    private RelativeLayout relativeLayout;
//    private int mImageCount = 1;
//    private String mDatatype;
//    private int mMaxImages;
//    private String mProjectId;
//    private String mProjectListConfigurationString;
//    private ProjectList mProjectListConfiguration;
//    private String mLatitude;
//    private String mLongitude;
//    private String mAppId;
//    private String mUserId;
//    private Project mProject;
//    private static final String SEPARATOR = ",";
//    private String accuracy;
//    private TextView text;
//    private String filePath;
//    private boolean safeToTakePicture = true;
//
//    private Button captureButton;
//    private ImageView pre_image;
//    private Button submit;
//    private ProgressBar progressBar;
//    private ArrayList<FormMedia> mFormImages = new ArrayList<>();
//    private ArrayList<String> uuids = new ArrayList<>();
//    private FrameLayout preview;
//
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.main);
//
//        mLatitude = String.valueOf(((UnifiedAppApplication) getApplication()).mUserLatitude);
//        mLongitude = String.valueOf(((UnifiedAppApplication) getApplication()).mUserLongitude);
//        accuracy = String.valueOf(((UnifiedAppApplication) getApplication()).mAccuracy);
//
//        startLocationUpdates();
//
//        mDatatype = getIntent().getStringExtra("datatype");
//        mMaxImages = getIntent().getIntExtra("max", Constants.DEFAULT_MAX_IMAGES);
//        mProjectId = getIntent().getStringExtra("projectId");
//        mAppId = getIntent().getStringExtra("appId");
//        mUserId = getIntent().getStringExtra("userId");
//        mLatitude = getIntent().getStringExtra("lat");
//        mLongitude = getIntent().getStringExtra("lon");
//        uuids = getIntent().getStringArrayListExtra("uuids");
//
//        // TODO: Arjun to uncomment
////        mProjectListConfigurationString = mDBHelper.getConfigFile
////                (Constants.PROJECT_LIST_CONFIG_DB_NAME + mAppId + mUserId);
//
//        Gson gson = new Gson();
//        if (mProjectListConfigurationString != null)
//            mProjectListConfiguration = gson.fromJson(mProjectListConfigurationString, ProjectList.class);
//
//        if (mProjectListConfiguration != null) {
//            ArrayList<Project> projects = new ArrayList<>();
//            projects.addAll(mProjectListConfiguration.mProjects);
//            for (int i = 0; i < projects.size(); i++) {
//                if (projects.get(i).mProjectId.equals(mProjectId)) {
//                    mProject = projects.get(i);
//                    break;
//                }
//            }
//        }
//
//        initializeViews();
//    }
//
//    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
//
//        @Override
//        public void onPictureTaken(byte[] data, Camera camera) {
//
//            File pictureFile = null;
//            filePath = null;
//            try {
//                filePath = createImageFile();
//                pictureFile = new File(filePath);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            if (pictureFile == null){
//                safeToTakePicture = true;
//                Log.d(TAG, "Error creating media file, check storage permissions");
//                return;
//            }
//
//            try {
//                FileOutputStream fos = new FileOutputStream(pictureFile);
//                fos.write(data);
//                fos.close();
//
//                if(filePath!=null)
//                    addCustomView(filePath);
//
//                else
//                    Toast.makeText(getApplicationContext(),"filepath is null", LENGTH_SHORT).show();
//
//                mCamera.startPreview();
//
//                TimestampImageTask timestampImageTask = new TimestampImageTask(getBaseContext(), pictureFile);
//                timestampImageTask.execute();
//
//
//            } catch (FileNotFoundException e) {
//                Log.d(TAG, "File not found: " + e.getMessage());
//            } catch (IOException e) {
//                Log.d(TAG, "Error accessing file: " + e.getMessage());
//            }
//
//            safeToTakePicture = true;
//        }
//    };
//
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        releaseCamera();              // release the camera immediately on pause event
//    }
//
//    private void releaseCamera(){
//        if (mCamera != null){
//            mCamera.release();        // release the camera for other applications
//            mCamera = null;
//        }
//    }
//
//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        releaseCamera();
//        finish();
//    }
//
//
//    public void addCustomView(final String imagePath) {
//
//        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//        final OpenCameraImageThumbnail view = new OpenCameraImageThumbnail(getBaseContext());
//
//        view.setId(mImageCount);
//        Uri imageUri = Uri.parse(imagePath);
//        view.setImageUri(imageUri);
//        view.getImgPhoto().setImageURI(imageUri);
//
//        try {
//            ExifInterface exif = new ExifInterface(imagePath);
//
//            if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).compareTo("6") != 0)
//                exif.setAttribute(ExifInterface.TAG_ORIENTATION, "6");
//
//            int rotation = Integer.parseInt(exif.getAttribute(ExifInterface.TAG_ORIENTATION));
//            int rotationInDegrees = exifToDegrees(rotation);
//            view.getImgPhoto().setRotation(rotationInDegrees);
//            pre_image.setRotation(rotationInDegrees);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        if (mImageCount > 1) {
//            params.addRule(RelativeLayout.RIGHT_OF, mImageCount - 1);
//        }
//
//        relativeLayout.addView(view, params);
//        progressBar.setVisibility(View.INVISIBLE);
//
//        String uuid = imagePath.substring(imagePath.lastIndexOf("/") + 1, imagePath.indexOf(Constants.IMAGE_TYPE_PNG));
//
//        FormMedia mCurrentImage = new FormMedia();
//
//        mCurrentImage.setmUUID(uuid);
//        mCurrentImage.setMediaClickTimeStamp(System.currentTimeMillis());
//        mCurrentImage.setmAppId(mAppId);
//        mCurrentImage.setmUserId(mUserId);
//        mCurrentImage.setmProjectId(mProjectId);
//        mCurrentImage.setMediaType(MediaType.IMAGE.getValue());
//        mCurrentImage.setMediaSubType(MediaSubType.FULL.getValue());
//        mCurrentImage.setMediaFileExtension(Constants.IMAGE_EXT);
//        mCurrentImage.setMediaUploadRetries(Constants.DEFAULT_RETRIES);
//        mCurrentImage.setMediaRequestStatus(MediaRequestStatus.NEW_STATUS.getValue());
//        mCurrentImage.setmLocalPath(imagePath);
//
//        onResult(mCurrentImage);
//
//        mFormImages.add(mCurrentImage);
//
//        mImageCount++;
//
//        final ImageView img = view.getImgPhoto();
//
//        view.getImgPhoto().setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                if (img.getBackground() == null) {
//
//                    Drawable highlight = getResources().getDrawable(R.drawable.highlight);
//
//                    int x = 1;
//                    OpenCameraImageThumbnail vew = findViewById(x);
//                    ImageView img1 = vew.getImgPhoto();
//                    while (vew != null) {
//
//                        img1.setBackground(null);
//                        vew = findViewById(x + 1);
//                        if (vew == null)
//                            break;
//                        img1 = vew.getImgPhoto();
//                        x++;
//                    }
//
//                    img.setBackground(highlight);
//                    pre_image.setImageURI(imageUri);
//                    preview.removeView(mPreview);
//                    preview.addView(pre_image);
//
//                } else {
//                    img.setBackground(null);
//                    preview.removeView(pre_image);
//                    preview.addView(mPreview);
//                }
//
//            }
//        });
//
//        final ImageButton del = view.getBtnClose();
//        del.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                deleteImages(view);
//            }
//        });
//    }
//
//    private void initializeViews()
//    {
//        relativeLayout = findViewById(R.id.relate);
//        submit = (Button) findViewById(R.id.submit_pic);
//        text = (TextView) findViewById(R.id.loc_acc);
//        progressBar = (ProgressBar) findViewById(R.id.prog);
//        pre_image = (ImageView) findViewById(R.id.pre_img);
//
//        mCamera = getDefaultCameraInstance();
//        context = getBaseContext();
//
//        Camera.Parameters param = mCamera.getParameters();
//
//        Camera.Size bestSize = null;
//        List<Camera.Size> sizeList = mCamera.getParameters().getSupportedPreviewSizes();
//        bestSize = sizeList.get(0);
//        for(Camera.Size size : sizeList ){
//            if((size.width * size.height) > (bestSize.width * bestSize.height)){
//                bestSize = size;
//            }
//        }
//
//        param.setPreviewSize(bestSize.width, bestSize.height);
//        param.setPictureSize(bestSize.width, bestSize.height);
//        mCamera.setParameters(param);
//
//        // Create our Preview view and set it as the content of our activity.
//        mPreview = new CameraPreview(context, mCamera);
//        preview = (FrameLayout) findViewById(R.id.camera_preview);
//        preview.addView(mPreview);
//
//        safeToTakePicture = true;
//
//        if (uuids != null) {
//            for (String uuid : uuids) {
//                try {
//                    retainPreviousImages(uuid);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        submit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                if (mFormImages.size() > 0) {
//                    final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//                    if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//                        // Not enabled
//                        Toast.makeText(OpenPre21Camera.this, Constants.EDU_CAMERA_LOCATION_ALERT,
//                                Toast.LENGTH_LONG).show();
//                    } else {
//                        // GPS enabled
//                        StringBuilder uuidBuilder = new StringBuilder();
//                        for (FormMedia formImage : mFormImages) {
//                            uuidBuilder.append(formImage.getmUUID());
//                            uuidBuilder.append(SEPARATOR);
//                        }
//                        String imageUUIDs = uuidBuilder.toString();
//                        //Remove last comma
//                        imageUUIDs = imageUUIDs.substring(0, imageUUIDs.length() - SEPARATOR.length());
//
//                        Intent data = new Intent();
//                        data.putParcelableArrayListExtra("formImages", mFormImages);
//                        data.putExtra("imageuuids", imageUUIDs);
//                        setResult(RESULT_OK, data);
//                        finish();
//                    }
//                } else {
//                    Toast.makeText(getBaseContext(), "No Images are taken", Toast.LENGTH_LONG).show();
//                }
//            }
//        });
//
//        captureButton = (Button) findViewById(R.id.capture);
//        captureButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//
//                        // get an image from the camera
//                        if(mImageCount - 1 < mMaxImages)
//                        {
//                            if(safeToTakePicture) {
//                                progressBar.setVisibility(View.VISIBLE);
//                                mCamera.takePicture(null, null, mPicture);
//                                safeToTakePicture = false;
//                            }
//                        }
//                        else{
//                            Toast.makeText(getBaseContext(), Constants.MAX_IMAGES_REACHED, LENGTH_SHORT).show();
//                        }
//                    }
//                }
//        );
//    }
//
//    private void retainPreviousImages(String uuid) throws IOException {
//
//        String path = createImageFile();
//        Utils.getInstance().deleteImageFromStorage(context, path);
//
//        String prev = path.substring(path.lastIndexOf("/") + 1, path.indexOf(".png"));
//        String newPath = path.replaceAll(prev, uuid);
//
//        addCustomView(newPath);
//
//    }
//
//    private String createImageFile() throws IOException {
//
//        UUID uuid = UUID.randomUUID();
//
//        // Create an image file name
//        String imageFileName = uuid.toString();
//        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//        File image = File.createTempFile(
//                imageFileName,  /* prefix */
//                Constants.IMAGE_TYPE_PNG,         /* suffix */
//                storageDir      /* directory */
//        );
//
//        return image.getAbsolutePath();
//    }
//
//    private void startLocationUpdates()
//    {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//
//
//            if( ((UnifiedAppApplication) getApplication()).voidTask != null) {
//                ((UnifiedAppApplication) getApplication()).voidTask
//                        .addOnSuccessListener(this, new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void aVoid) {
//
//                                mLatitude = String.valueOf(((UnifiedAppApplication) getApplication()).mUserLatitude);
//                                mLongitude = String.valueOf(((UnifiedAppApplication) getApplication()).mUserLongitude);
//                                accuracy = String.valueOf(((UnifiedAppApplication) getApplication()).mAccuracy);
//                                //Toast.makeText(getBaseContext(), "location is available with accuracy " + accuracy, Toast.LENGTH_LONG).show();
//                                text.setText("Accuracy : " + accuracy);
//
//
//                            }
//                        });
//            }
//        }
//        else{
//            ActivityCompat.requestPermissions((Activity) getBaseContext(),
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
//                            Manifest.permission.ACCESS_COARSE_LOCATION},
//                    Constants.PERMISSION_FINE_LOCATION);
//        }
//
//    }
//
//    public void deleteImages(final OpenCameraImageThumbnail view) {
//
//        final int id = view.getId();
//
//        Utils.getInstance().deleteImageFromStorage(context, view.getImageUri().getPath());
//
//        mImageCount--;
//        String paths = view.getImageUri().getPath();
//        String uuid = paths.substring(paths.lastIndexOf("/") + 1, paths.indexOf(".png"));
//
//        if(mFormImages.size()>0)
//        {
//            for(Iterator<FormMedia> iterator = mFormImages.iterator(); iterator.hasNext();)
//            {
//                FormMedia c = iterator.next();
//                if(c.getmUUID().compareTo(uuid) == 0)
//                    iterator.remove();
//            }
//        }
//
//        int ct = id - 1;
//        OpenCameraImageThumbnail v1 = findViewById(id + 1);
//        OpenCameraImageThumbnail v2 = findViewById(id - 1);
//
//        if (v1 == null && v2 == null) {
//            relativeLayout.removeView(view);
//            preview.removeAllViews();
//            preview.addView(mPreview);
//
//        } else if (v1 == null && v2 != null) {
//            relativeLayout.removeView(view);
//
//        } else if (v1 != null && v2 != null) {
//            relativeLayout.removeView(view);
//
//            while (v1 != null) {
//                RelativeLayout.LayoutParams param1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//
//                param1.addRule(RelativeLayout.RIGHT_OF, ct);
//                relativeLayout.removeView(v1);
//                relativeLayout.addView(v1, param1);
//                ct++;
//                v1.setId(ct);
//                v1 = findViewById(ct + 2);
//            }
//        } else if (v1 != null && v2 == null) {
//            relativeLayout.removeView(view);
//            relativeLayout.removeView(v1);
//            v1.setId(id);
//            RelativeLayout.LayoutParams param1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//            relativeLayout.addView(v1);
//
//            v1 = findViewById(id + 2);
//            int cnt = id;
//            while (v1 != null) {
//                RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//                param1.addRule(RelativeLayout.RIGHT_OF, cnt);
//                relativeLayout.removeView(v1);
//                relativeLayout.addView(v1, param);
//                cnt++;
//                v1.setId(cnt);
//                v1 = findViewById(cnt + 2);
//            }
//        }
//    }
//
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        switch (requestCode) {
//            case Constants.REQUEST_GEOTAG_PIN_DROP:
//                if (resultCode == Activity.RESULT_OK) {
//
//                    String latitude = data.getStringExtra("lat");
//                    String longitude = data.getStringExtra("lon");
//                    FormMedia mCurrentImage = data.getParcelableExtra("mCurrentImage");
//                    mCurrentImage.setLatitude(Double.parseDouble(latitude));
//                    mCurrentImage.setLongitude(Double.parseDouble(longitude));
//                } else {
//                    Toast.makeText(this,
//                            Constants.GEOTAG_WASNT_SET, Toast.LENGTH_SHORT).show();
//                }
//
//            case Constants.REQUEST_CHECK_SETTINGS:
//
//            case Constants.REQUEST_LOCATION_SETTINGS:
//                System.out.println("LOCATIONUPDATE : RESULT" + resultCode);
//                switch (resultCode) {
//                    case 0:
//                        // User didnt turn on location update
//
//                        break;
//
//                    case -1:
//                        // User has turned on location
//                        Utils.getInstance().checkForLocation(this);
//                        break;
//                }
//                break;
//        }
//
//    }
//
//    protected void onResult(FormMedia mCurrentImage) {
//
//        if (mDatatype.equals("geotagimage")) {
//
//            // Picking image from the currently stored path
//            File imgFile = new File(mCurrentImage.getLocalPath());
//            Bitmap bitmap = null;
//            if (imgFile.exists()) {
//                byte[] bitmapByteArray = makeThumbnailImage(imgFile, 30);
//                bitmap = BitmapFactory.decodeByteArray(bitmapByteArray, 0, bitmapByteArray.length);
//                if (bitmap != null) {
//                    mCurrentImage.setmBitmap(bitmapByteArray);
//                }
//            }
//            mCurrentImage.setmHasGeotag(true);
//
//            if (mLatitude.equals("") && mLongitude.equals("")) {
//
//                Toast.makeText(this, "Cannot load map right now!", Toast.LENGTH_LONG).show();
//                mCurrentImage.setLatitude(Double.parseDouble(Constants.DEFAULT_LATITUDE));
//                mCurrentImage.setLongitude(Double.parseDouble(Constants.DEFAULT_LONGITUDE));
//            } else {
//                if (Utils.getInstance().isOnline(OpenPre21Camera.this)) {
//                    Intent intent = new Intent(this, DropLocationActivity.class);
//                    intent.putExtra("lat", mLatitude);
//                    intent.putExtra("lon", mLongitude);
//                    intent.putExtra("mCurrentImage", mCurrentImage);
//                    startActivityForResult(intent, Constants.REQUEST_GEOTAG_PIN_DROP);
//                } else {
//                    Toast.makeText(this, "Cannot load map without network connection!", Toast.LENGTH_LONG).show();
//                    mCurrentImage.setLatitude(Double.parseDouble(Constants.DEFAULT_LATITUDE));
//                    mCurrentImage.setLongitude(Double.parseDouble(Constants.DEFAULT_LONGITUDE));
//                }
//            }
//        } else if (mDatatype.equals("geotagimagefused")) {
//
//            mCurrentImage.setmHasGeotag(true);
//
//            boolean isValid = true;
//
//            if (((UnifiedAppApplication) getApplication()).mUserLatitude != 0.0
//                    && ((UnifiedAppApplication) getApplication()).mUserLongitude != 0.0
//                    && ((UnifiedAppApplication) getApplication()).mAccuracy != 0.0f) {
//                // The location object has been initialized
//                // The validation could be bbox validation or a circle validation
//                if (mProject != null) {
//                    if (mProject.mBBoxValidation != null && !mProject.mBBoxValidation.isEmpty()) {
//                        // BBox validation
//                        isValid = Utils.getInstance().geotagValidateImage(
//                                ((UnifiedAppApplication) getApplication()).mUserLatitude,
//                                ((UnifiedAppApplication) getApplication()).mUserLongitude,
//                                mProject.mBBoxValidation);
//                    } else if (mProject.mCentroidValidation != null && !mProject.mCentroidValidation.isEmpty()) {
//                        // Centroid validation
//                        isValid = Utils.getInstance().geotagValidateImage(
//                                ((UnifiedAppApplication) getApplication()).mUserLatitude,
//                                ((UnifiedAppApplication) getApplication()).mUserLongitude,
//                                mProject.mCentroidValidation);
//                    } else {
//                        // No validation to be performed
//                    }
//                } else {
//                    // Project is null, cannot perform validation
//                }
//            } else {
//                // Do not have the current user location, cannot perform validation
//            }
//
//            if (isValid) {
//                // Picking image from the currently stored path
//                File imgFile = new File(mCurrentImage.getLocalPath());
//                Bitmap bitmap = null;
//                if (imgFile.exists()) {
//                    byte[] bitmapByteArray = makeThumbnailImage(imgFile, 30);
//                    bitmap = BitmapFactory.decodeByteArray(bitmapByteArray, 0, bitmapByteArray.length);
//                    if (bitmap != null) {
//                        mCurrentImage.setmBitmap(bitmapByteArray);
//                    }
//                }
//
//                String latitude = String.valueOf(((UnifiedAppApplication) getApplication()).mUserLatitude);
//                String longitude = String.valueOf(((UnifiedAppApplication) getApplication()).mUserLongitude);
//                mCurrentImage.setLatitude(Double.parseDouble(latitude));
//                mCurrentImage.setLongitude(Double.parseDouble(longitude));
//
//            } else {
//                // Tell the user that this image isn't valid (geotagged)
//                Toast.makeText(getApplicationContext(), Constants.GEOTAG_IMAGE_VALIDATION_FAILED, Toast.LENGTH_LONG).show();
//            }
//        } else {
//            // Picking image from the currently stored path
//            File imgFile = new File(mCurrentImage.getLocalPath());
//            Bitmap bitmap = null;
//            if (imgFile.exists()) {
//                byte[] bitmapByteArray = makeThumbnailImage(imgFile, 30);
//                bitmap = BitmapFactory.decodeByteArray(bitmapByteArray, 0, bitmapByteArray.length);
//                if (bitmap != null) {
//                    mCurrentImage.setmBitmap(bitmapByteArray);
//                }
//            }
//            mCurrentImage.setmHasGeotag(false);
//
//        }
//    }
//
//    private byte[] makeThumbnailImage(File f, int quality) {
//
//        Bitmap bitmap;
//        byte[] bitmapdata = null;
//
//        ByteArrayOutputStream bmpStream = new ByteArrayOutputStream();
//        bitmap = decodeFile(f, 153, 204);
//
//        if (bitmap == null) {
//            Utils.getInstance().showLog("ERROR", "Cannot decode file " + f);
//            return null;
//        }
//
//        bitmap.compress(Bitmap.CompressFormat.PNG, quality, bmpStream);
//
//        bitmapdata = bmpStream.toByteArray();
//
//        try {
//            bmpStream.flush();
//            bmpStream.close();
//            bitmap.recycle();
//        } catch (IOException e) {
//            Utils.getInstance().showLog("ERROR", "" + e);
//            e.printStackTrace();
//        }
//        return bitmapdata;
//    }
//
//
//    @Nullable
//    private static Bitmap decodeFile(File f, int max_Height, int max_Width) {
//        try {
//            Bitmap bitmap = null;
//            FileInputStream fileInputStream;
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inJustDecodeBounds = true;
//            try {
//                fileInputStream = new FileInputStream(f);
//                BitmapFactory.decodeStream(fileInputStream, null, options);
//                fileInputStream.close();
//            } catch (FileNotFoundException e) {
//                return null;
//            }
//            int actualHeight = options.outHeight;
//            int actualWidth = options.outWidth;
//
//            float imgRatio = actualWidth / actualHeight;
//            float maxRatio = (float) max_Width / (float) max_Height;
//
//            if (actualHeight > (float) max_Height || actualWidth > (float) max_Width) {
//                if (imgRatio < maxRatio) {
//                    imgRatio = (float) max_Height / actualHeight;
//                    actualWidth = (int) (imgRatio * actualWidth);
//                    actualHeight = (int) (float) max_Height;
//                } else if (imgRatio > maxRatio) {
//                    imgRatio = (float) max_Width / actualWidth;
//                    actualHeight = (int) (imgRatio * actualHeight);
//                    actualWidth = (int) (float) max_Width;
//                } else {
//                    actualHeight = (int) (float) max_Height;
//                    actualWidth = (int) (float) max_Width;
//                }
//            }
//
//            options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
//
//            options.inJustDecodeBounds = false;
//
//            try {
//                fileInputStream = new FileInputStream(f);
//                bitmap = BitmapFactory.decodeStream(fileInputStream, null, options);
//                fileInputStream.close();
//            } catch (OutOfMemoryError exception) {
//                exception.printStackTrace();
//            }
//            return bitmap;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    private static int calculateInSampleSize(
//            BitmapFactory.Options options, int reqWidth, int reqHeight) {
//        final int height = options.outHeight;
//        final int width = options.outWidth;
//        int inSampleSize = 1;
//
//        if (height > reqHeight || width > reqWidth) {
//
//            final int halfHeight = height / 2;
//            final int halfWidth = width / 2;
//
//            while ((halfHeight / inSampleSize) >= reqHeight
//                    && (halfWidth / inSampleSize) >= reqWidth) {
//                inSampleSize = inSampleSize << 1;
//            }
//        }
//        return inSampleSize;
//    }
//
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//       /* if (requestCode == REQUEST_CAMERA_PERMISSION) {
//            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
//                // close the app
//                Toast.makeText(OpenCamera.this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
//                finish();
//            }
//        }*/
//        switch (requestCode) {
//            case Constants.PERMISSION_FINE_LOCATION: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // permission was granted!
//                    ((UnifiedAppApplication) getApplication()).mFusedLocationClient =
//                            LocationServices.getFusedLocationProviderClient(this);
//
//                    // Initializing location request
//                    Utils.getInstance().createLocationRequest(this);
//                } else {
//                    // permission denied!
//                }
//                break;
//            }
//
//           /* case Constants.REQUEST_IMAGE_CAPTURE:{
//                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
//                    // close the app
//                    Toast.makeText(OpenCamera.this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
//                    finish();
//                }
//
//            }*/
//        }
//
//    }
//
//
//    private static int exifToDegrees(int exifOrientation) {
//        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
//            return 90;
//        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
//            return 180;
//        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
//            return 270;
//        }
//        return 0;
//    }
//
////    private Bitmap rotate(Bitmap bitmap, String path)
////    {
////        Bitmap adjustedBitmap = null;
////        try {
////            ExifInterface exif = new ExifInterface(path);
////
////            if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).compareTo("6") != 0)
////                exif.setAttribute(ExifInterface.TAG_ORIENTATION, "6");
////
////            int rotation = Integer.parseInt(exif.getAttribute(ExifInterface.TAG_ORIENTATION));
////            int rotationInDegrees = exifToDegrees(rotation);
////
////            Log.e("rotation camera", String.valueOf(rotation));
////            Log.e("rotation exif camera", String.valueOf(rotationInDegrees));
////
////            Matrix matrix = new Matrix();
////            if (rotation != 0) {matrix.preRotate(rotationInDegrees);}
////
////            adjustedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
////
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
////        return adjustedBitmap;
////    }
//}

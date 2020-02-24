//package com.vassar.unifiedapp.ui;
//
//import android.Manifest;
//import android.animation.ObjectAnimator;
//import android.annotation.SuppressLint;
//import android.app.Activity;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Camera;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.ImageFormat;
//import android.graphics.Paint;
//import android.graphics.SurfaceTexture;
//import android.graphics.drawable.Drawable;
//import android.hardware.camera2.CameraAccessException;
//import android.hardware.camera2.CameraCaptureSession;
//import android.hardware.camera2.CameraCharacteristics;
//import android.hardware.camera2.CameraDevice;
//import android.hardware.camera2.CameraManager;
//import android.hardware.camera2.CameraMetadata;
//import android.hardware.camera2.CaptureRequest;
//import android.hardware.camera2.TotalCaptureResult;
//import android.hardware.camera2.params.StreamConfigurationMap;
//import android.location.Location;
//import android.location.LocationManager;
//import android.media.ExifInterface;
//import android.media.Image;
//import android.media.ImageReader;
//import android.net.Uri;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.Environment;
//import android.os.Handler;
//import android.os.HandlerThread;
//import android.os.Looper;
//import android.provider.MediaStore;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.annotation.RequiresApi;
//import android.support.v4.app.ActivityCompat;
//import android.support.v7.app.AppCompatActivity;
//import android.util.Log;
//import android.util.Size;
//import android.util.SparseIntArray;
//import android.view.Gravity;
//import android.view.Surface;
//import android.view.SurfaceHolder;
//import android.view.TextureView;
//import android.view.View;
//import android.view.animation.DecelerateInterpolator;
//import android.widget.Button;
//import android.widget.ImageButton;
//import android.widget.ImageView;
//import android.widget.ProgressBar;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.nio.ByteBuffer;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Calendar;
//import java.util.Iterator;
//import java.util.List;
//import java.util.UUID;
//
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.common.api.PendingResult;
//import com.google.android.gms.location.LocationCallback;
//import com.google.android.gms.location.LocationRequest;
//import com.google.android.gms.location.LocationResult;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.location.LocationSettingsRequest;
//import com.google.android.gms.location.LocationSettingsResult;
//import com.google.android.gms.location.SettingsClient;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.android.gms.tasks.Task;
//import com.google.gson.Gson;
//import com.squareup.picasso.Picasso;
//import com.vassar.unifiedapp.R;
//import com.vassar.unifiedapp.application.UnifiedAppApplication;
//import com.vassar.unifiedapp.db.UnifiedAppDBHelper;
//import com.vassar.unifiedapp.model.FormImage;
//import com.vassar.unifiedapp.model.Project;
//import com.vassar.unifiedapp.model.ProjectList;
//import com.vassar.unifiedapp.utils.Constants;
//import com.vassar.unifiedapp.utils.Utils;
//
//import static android.widget.Toast.LENGTH_SHORT;
//import static com.google.android.gms.common.api.GoogleApiClient.*;
//
//public class OpenCamera extends AppCompatActivity implements TextureView.SurfaceTextureListener{
//
//    private String mDatatype;
//    private long mInitialTimestamp;
//    private int mMaxImages;
//    private String mTag;
//    private String mProjectId;
//    private String mKey;
//    private String mProjectListConfigurationString;
//    private ProjectList mProjectListConfiguration;
//    private String mLatitude;
//    private String mLongitude;
//    private int mImageCount = 1;
//    private String mCurrentImagePath;
//    private UnifiedAppDBHelper mDBHelper;
//    private String mAppId;
//    private String mUserId;
//    private Project mProject;
//    private static final String SEPARATOR = ",";
//    private String accuracy;
//    private Location mLocation;
//    private TextView text;
//    private File storage = null;
//    private LocationRequest mLocationRequest;
//    private LocationCallback mLocationCallback;
//    private ArrayList<String> deletedImages = new ArrayList<>();
//
//    private static final String TAG = "AndroidCameraApi";
//    private Button takePictureButton;
//    private TextureView textureView;
//    private RelativeLayout layout;
//    private ImageView pre_image;
//    private Button submit;
//    private Uri uris;
//    private ProgressBar progressBar;
//    private ArrayList<FormImage> mFormImages = new ArrayList<>();
//
//    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
//
//    static {
//        ORIENTATIONS.append(Surface.ROTATION_0, 90);
//        ORIENTATIONS.append(Surface.ROTATION_90, 0);
//        ORIENTATIONS.append(Surface.ROTATION_180, 270);
//        ORIENTATIONS.append(Surface.ROTATION_270, 180);
//    }
//
//    private String cameraId;
//    protected CameraDevice cameraDevice;
//    protected CameraCaptureSession cameraCaptureSessions;
//    protected CaptureRequest captureRequest;
//    protected CaptureRequest.Builder captureRequestBuilder;
//    private Size imageDimension;
//    private ImageReader imageReader;
//    private File file;
//    private ArrayList<String> uuids = null;
//    private static final int REQUEST_CAMERA_PERMISSION = 200;
//    private boolean mFlashSupported;
//    private Handler mBackgroundHandler;
//    private HandlerThread mBackgroundThread;
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        setContentView(R.layout.activity_camera);
//
//        mLocationCallback = ((UnifiedAppApplication) getApplication()).mLocationCallback;
//        mLocationRequest = ((UnifiedAppApplication) getApplication()).mLocationRequest;
//        mLatitude = String.valueOf(((UnifiedAppApplication) getApplication()).mUserLatitude);
//        mLongitude = String.valueOf(((UnifiedAppApplication) getApplication()).mUserLongitude);
//        accuracy = String.valueOf(((UnifiedAppApplication) getApplication()).mAccuracy);
//
//        Toast.makeText(this, "Location with accuracy "+accuracy, LENGTH_SHORT).show();
//
//        //initializeLocation();
//        startLocationUpdates();
//
//        mDatatype = getIntent().getStringExtra("datatype");
//        mInitialTimestamp = getIntent().getLongExtra("initialts", 0);
//        mMaxImages = getIntent().getIntExtra("max", Constants.DEFAULT_MAX_IMAGES);
//        mTag = getIntent().getStringExtra("tag");
//        mProjectId = getIntent().getStringExtra("projectId");
//        mAppId = getIntent().getStringExtra("appId");
//        mUserId = getIntent().getStringExtra("userId");
//        mKey = getIntent().getStringExtra("key");
//        mLatitude = getIntent().getStringExtra("lat");
//        mLongitude = getIntent().getStringExtra("lon");
//        uuids = getIntent().getStringArrayListExtra("uuids");
//        mDBHelper = new UnifiedAppDBHelper(this);
//        //pathFile = getIntent().getStringExtra("filePath");
//        mProjectListConfigurationString = mDBHelper.getConfigFile
//                (Constants.PROJECT_LIST_CONFIG_DB_NAME + mAppId + mUserId);
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
//        try {
//            initializeViews();
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
//        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//        @Override
//        public void onOpened(CameraDevice camera) {
//            Log.e(TAG, "onOpened");
//            cameraDevice = camera;
//            createCameraPreview();
//        }
//
//        @Override
//        public void onDisconnected(CameraDevice camera) {
//            cameraDevice.close();
//        }
//
//        @Override
//        public void onError(CameraDevice camera, int error) {
//            cameraDevice.close();
//            cameraDevice = null;
//        }
//    };
//
//    final CameraCaptureSession.CaptureCallback captureCallbackListener = new CameraCaptureSession.CaptureCallback() {
//        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//        @Override
//        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
//            super.onCaptureCompleted(session, request, result);
//            createCameraPreview();
//        }
//    };
//
//    protected void startBackgroundThread() {
//
//        mBackgroundThread = new HandlerThread("Camera Background");
//        mBackgroundThread.start();
//        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
//    }
//
//    protected void stopBackgroundThread() {
//        mBackgroundThread.quitSafely();
//        try {
//            mBackgroundThread.join();
//            mBackgroundThread = null;
//            mBackgroundHandler = null;
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    protected void takePicture(final String uri) {
//        if (null == cameraDevice) {
//            Log.e(TAG, "cameraDevice is null");
//            Utils.getInstance().showLog("OPENCAMERATESTRUN", "CAMERA DEVICE IS NULL");
//            return;
//        }
//
//        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
//        try {
//            Utils.getInstance().showLog("OPENCAMERATESTRUN", "GETTING CAMEMRA CHARACTERISTICS");
//            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
//            Size[] jpegSizes = null;
//            if (characteristics != null) {
//                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
//            }
//            int width = 640;
//            int height = 480;
//            if (jpegSizes != null && 0 < jpegSizes.length) {
//                width = jpegSizes[0].getWidth();
//                height = jpegSizes[0].getHeight();
//            }
//            Utils.getInstance().showLog("OPENCAMERATESTRUN", "GETTING CAMEMRA CHARACTERISTICS DONE");
//
//            Utils.getInstance().showLog("OPENCAMERATESTRUN", "GETTING OUTPUT SURFACES");
//            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
//            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
//            outputSurfaces.add(reader.getSurface());
//            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
//            Utils.getInstance().showLog("OPENCAMERATESTRUN", "GETTING OUTPUT SURFACES DONE");
//
//            Utils.getInstance().showLog("OPENCAMERATESTRUN", "CREATE CAPTURE REQUEST");
//            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
//            captureBuilder.addTarget(reader.getSurface());
//            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
//            Utils.getInstance().showLog("OPENCAMERATESTRUN", "CREATE CAPTURE REQUEST DONE");
//
//            Utils.getInstance().showLog("OPENCAMERATESTRUN", "ORIENTATION");
//            // Orientation
//            int rotation = getWindowManager().getDefaultDisplay().getRotation();
//            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
//            Utils.getInstance().showLog("OPENCAMERATESTRUN", "ORIENTATION DONE");
//
//            final File file = new File(uri);
//
//            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
//                @Override
//                public void onImageAvailable(ImageReader reader) {
//                    Image image = null;
//                    try {
//                        image = reader.acquireLatestImage();
//                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
//                        byte[] bytes = new byte[buffer.capacity()];
//                        buffer.get(bytes);
//                        save(bytes);
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    } finally {
//                        if (image != null) {
//                            image.close();
//                        }
//                    }
//                }
//
//                private void save(byte[] bytes) throws IOException {
//                    OutputStream output = null;
//                    try {
//                        output = new FileOutputStream(file);
//                        output.write(bytes);
//                    } finally {
//                        if (null != output) {
//                            Log.e("OUTPUT OF SAVE", file.getAbsolutePath());
//                            output.close();
//                        }
//                    }
//                }
//            };
//            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
//
//            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
//                @Override
//                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
//                    super.onCaptureCompleted(session, request, result);
//                    File fts = timestampItAndSave(file);
//                    Handler mainHandler = new Handler(Looper.getMainLooper());
//
//                    Runnable myRunnable = new Runnable() {
//                        @Override
//                        public void run() {
//                            addCustomView(uri);
//                        } // This is your code
//                    };
//                    mainHandler.post(myRunnable);
//
//                    //Toast.makeText(CameraActivity.this, "Saved :" + file, Toast.LENGTH_SHORT).show();
//                    createCameraPreview();
//                }
//            };
//            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
//                @Override
//                public void onConfigured(CameraCaptureSession session) {
//                    try {
//                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
//                    } catch (CameraAccessException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                @Override
//                public void onConfigureFailed(CameraCaptureSession session) {
//                }
//            }, mBackgroundHandler);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//        return;
//
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    protected void createCameraPreview() {
//        try {
//
//            SurfaceTexture texture = textureView.getSurfaceTexture();
//            assert texture != null;
//            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
//            Surface surface = new Surface(texture);
//            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
//            captureRequestBuilder.addTarget(surface);
//            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
//                @Override
//                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
//                    if (null == cameraDevice) {
//                        return;
//                    }
//                    cameraCaptureSessions = cameraCaptureSession;
//                    updatePreview();
//                }
//
//                @Override
//                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
//                }
//            }, null);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private void openCamera() {
//        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
//        Log.e(TAG, "is camera open");
//        try {
//            cameraId = manager.getCameraIdList()[0];
//            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
//            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//            assert map != null;
//            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(OpenCamera.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
//                return;
//            }
//            manager.openCamera(cameraId, stateCallback, null);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//        Log.e(TAG, "openCamera X");
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    protected void updatePreview() {
//        if (null == cameraDevice) {
//            Log.e(TAG, "updatePreview error, return");
//        }
//        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
//        try {
//            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
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
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    @Override
//    protected void onResume() {
//        super.onResume();
//        Log.e(TAG, "onResume");
//        startBackgroundThread();
//        if (textureView.isAvailable()) {
//            openCamera();
//        } else {
//            textureView.setSurfaceTextureListener(this);
//        }
//    }
//
//    @Override
//    protected void onPause() {
//        Log.e(TAG, "onPause");
//        //closeCamera();
//        stopBackgroundThread();
//        super.onPause();
//    }
//
//    private File timestampItAndSave(File file) {
//
//        if (file == null) {
//            Log.e("FILE PATH", "NO file is there");
//        }
//        Bitmap bitmap = null;
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//
//        try (InputStream is = new FileInputStream(file)) {
//
//            bitmap = BitmapFactory.decodeStream(is);
//
//        } catch (FileNotFoundException e) {
//            Toast.makeText(this, "file not found", LENGTH_SHORT).show();
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        Bitmap dest = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
//
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String dateTime = sdf.format(Calendar.getInstance().getTime()); // reading local time in the system
//
//        int spSize = 25;//your sp size
//        // Convert the sp to pixels
//        float scaledTextSize = spSize * getResources().getDisplayMetrics().scaledDensity;
//
//        Canvas cs = new Canvas(dest);
//        Paint tPaint = new Paint();
//        tPaint.setTextSize(scaledTextSize);
//        tPaint.setColor(Color.BLUE);
//        tPaint.setStyle(Paint.Style.FILL);
//        tPaint.setTextAlign(Paint.Align.LEFT);
//        cs.drawBitmap(bitmap, 0f, 0f, null);
//        float height = tPaint.measureText("yY");
//        cs.drawText(dateTime, 20f, height + 20f, tPaint);
//        try {
//
//            FileOutputStream fist = new FileOutputStream(new File(file.getAbsolutePath()));
//            dest.compress(Bitmap.CompressFormat.PNG, 100, fist);
//            fist.close();
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return file;
//    }
//
//    public Uri getImageUri(Bitmap inImage) {
//        String path = MediaStore.Images.Media.insertImage(getBaseContext().getContentResolver(), inImage, "Title", null);
//        Log.e("Image uri", path);
//        return Uri.parse(path);
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    @Override
//    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
//        Toast.makeText(this, "texture is available", Toast.LENGTH_LONG).show();
//        text.setVisibility(View.VISIBLE);
//        openCamera();
//    }
//
//    @Override
//    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//
//    }
//
//    @Override
//    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
//        return false;
//    }
//
//    @Override
//    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//
//    }
//
//    public void addCustomView(final String uri) {
//
//        Toast.makeText(OpenCamera.this, "Value of count is : " + mImageCount, LENGTH_SHORT).show();
//        Log.e("FILE IN ADD", uri);
//
//        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//        final OpenCameraImageThumbnail view = new OpenCameraImageThumbnail(getBaseContext());
//        view.setId(mImageCount);
//        view.setImageUri(Uri.parse(uri));
//
//        view.getImgPhoto().setImageURI(Uri.parse(uri));
//
//        String uuid = uri.substring(uri.lastIndexOf("/") + 1, uri.indexOf(".png"));
//
//        FormImage mCurrentFormImage = new FormImage();
//        mCurrentFormImage.setLocalPath(uri);
//        mCurrentFormImage.setUUID(uuid);
//        onResult(mCurrentFormImage);
//
//        mFormImages.add(mCurrentFormImage);
//
//        if (mImageCount > 1) {
//            params.addRule(RelativeLayout.RIGHT_OF, mImageCount - 1);
//        }
//
//        mImageCount++;
//
//        final ImageView img = view.getImgPhoto();
//        view.getImgPhoto().setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                if (img.getBackground() == null) {
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
//
//                        x++;
//                    }
//                    img.setBackground(highlight);
//                    textureView.setVisibility(View.INVISIBLE);
//                    pre_image.setImageURI(Uri.parse(uri));
//                    pre_image.setVisibility(View.VISIBLE);
//
//                } else {
//                    img.setBackground(null);
//                    pre_image.setVisibility(View.INVISIBLE);
//                    textureView.setVisibility(View.VISIBLE);
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
//
//        layout.addView(view, params);
//        progressBar.setVisibility(View.INVISIBLE);
//
//    }
//
//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        this.finish();
//
//    }
//
//    private String createImageFile() throws IOException {
//        // Adding the image to the group of images
//
//        UUID uuid = UUID.randomUUID();
//        //FormImage mCurrentImage = new FormImage();
//
//        // mCurrentImage.setUUID(uuid.toString());
//
//        // Create an image file name
//        String imageFileName = uuid.toString();
//        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//        File image = File.createTempFile(
//                imageFileName,
//                ".png",
//                storageDir);
//
//
//        //mCurrentImage.setLocalPath(mCurrentImagePath);
//        String path = image.getAbsolutePath();
//        Log.e("Image path created", path);
//
//        return path;
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    public void initializeViews() throws CameraAccessException {
//
//        layout = (RelativeLayout) findViewById(R.id.relative);
//        textureView = (TextureView) findViewById(R.id.texture);
//        pre_image = (ImageView) findViewById(R.id.pre_image);
//        progressBar = (ProgressBar) findViewById(R.id.progress);
//        takePictureButton = (Button) findViewById(R.id.btn_takepicture);
//        submit = (Button) findViewById(R.id.submit);
//        text = (TextView) findViewById(R.id.accurate);
//        assert textureView != null;
//        textureView.setSurfaceTextureListener(this);
//
//        //text.setText("Accuracy : "+accuracy);
//
//        if (null == cameraDevice)
//            Log.e(TAG, "cameraDevice is null");
//
//        else {
//            CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
//            try {
//                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
//                Size[] jpegSizes = null;
//                if (characteristics != null) {
//                    jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
//                }
//                int width = 640;
//                int height = 480;
//                if (jpegSizes != null && 0 < jpegSizes.length) {
//                    width = jpegSizes[0].getWidth();
//                    height = jpegSizes[0].getHeight();
//                }
//                ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
//                List<Surface> outputSurfaces = new ArrayList<Surface>(2);
//                outputSurfaces.add(reader.getSurface());
//                outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
//            } catch (CameraAccessException e) {
//                e.printStackTrace();
//            }
//        }
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
//        assert takePictureButton != null;
//        takePictureButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                if (mImageCount - 1 < mMaxImages) {
//                    progressBar.setVisibility(View.VISIBLE);
//                    String path = null;
//                    try {
//
//                        Utils.getInstance().showLog("OPENCAMERATESTRUN", "CREATING IMAGE FILE");
//                        path = createImageFile();
//                        Utils.getInstance().showLog("OPENCAMERATESTRUN", "CREATING IMAGE FILE DONE");
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    takePicture(path);
//                } else
//                    Toast.makeText(OpenCamera.this, "No more images can be added", LENGTH_SHORT).show();
//
//            }
//        });
//
//        assert submit != null;
//
//        submit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                if (mFormImages.size() > 0) {
//
//                    final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//                    if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//                        // Not enabled
//                        Toast.makeText(OpenCamera.this, Constants.EDU_CAMERA_LOCATION_ALERT,
//                                Toast.LENGTH_LONG).show();
//                    } else {
//                        // GPS enabled
//                        StringBuilder uuidBuilder = new StringBuilder();
//                        for (FormImage formImage : mFormImages) {
//                            uuidBuilder.append(formImage.mUUID);
//                            uuidBuilder.append(SEPARATOR);
//                        }
//                        String imageUUIDs = uuidBuilder.toString();
//                        //Remove last comma
//                        imageUUIDs = imageUUIDs.substring(0, imageUUIDs.length() - SEPARATOR.length());
//
//                        Intent data = new Intent();
//                        data.putParcelableArrayListExtra("formImages", mFormImages);
//                        data.putExtra("imageuuids", imageUUIDs);
//                        data.putStringArrayListExtra("deletedImages", deletedImages);
//                        setResult(RESULT_OK, data);
//                        finish();
//                    }
//                } else {
//                    Toast.makeText(getBaseContext(), "No Images are taken", Toast.LENGTH_LONG).show();
//                }
//            }
//        });
//
//    }
//
//
//    public void deleteImages(final OpenCameraImageThumbnail view) {
//
//        final int id = view.getId();
//        String paths = view.getImageUri().getPath();
//        File file = new File(paths);
//        String uuid = paths.substring(paths.lastIndexOf("/") + 1, paths.indexOf(".png"));
//
//        if(mFormImages.size()>0)
//        {
//            for(Iterator<FormImage> iterator = mFormImages.iterator(); iterator.hasNext();)
//            {
//                FormImage c = iterator.next();
//                if(c.getUUID().compareTo(uuid) == 0)
//                    iterator.remove();
//            }
//        }
//
//        deletedImages.add(uuid);
//        boolean chk = false;
//        if (file.exists()) {
//            chk = file.delete();
//        }
//
//        mImageCount = mImageCount - 1;
//
//        int ct = id - 1;
//        OpenCameraImageThumbnail v1 = findViewById(id + 1);
//        OpenCameraImageThumbnail v2 = findViewById(id - 1);
//
//        if (v1 == null && v2 == null) {
//            layout.removeView(view);
//            view.setImageUri(null);
//            textureView.setVisibility(View.VISIBLE);
//            pre_image.setVisibility(View.INVISIBLE);
//        } else if (v1 == null && v2 != null) {
//            layout.removeView(view);
//            view.setImageUri(null);
//        } else if (v1 != null && v2 != null) {
//            layout.removeView(view);
//            view.setImageUri(null);
//
//            while (v1 != null) {
//                RelativeLayout.LayoutParams param1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//
//                param1.addRule(RelativeLayout.RIGHT_OF, ct);
//                layout.removeView(v1);
//                layout.addView(v1, param1);
//                ct++;
//                v1.setId(ct);
//                v1 = findViewById(ct + 2);
//            }
//        } else if (v1 != null && v2 == null) {
//            layout.removeView(view);
//            view.setImageUri(null);
//            layout.removeView(v1);
//            v1.setId(id);
//            RelativeLayout.LayoutParams param1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//            layout.addView(v1);
//
//            v1 = findViewById(id + 2);
//            int cnt = id;
//            while (v1 != null) {
//                RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//
//                param1.addRule(RelativeLayout.RIGHT_OF, cnt);
//                layout.removeView(v1);
//                layout.addView(v1, param);
//                cnt++;
//                v1.setId(cnt);
//                v1 = findViewById(cnt + 2);
//            }
//        }
//    }
//
//
//    private void initializeLocation() {
//        if (((UnifiedAppApplication) getApplication()).mFusedLocationClient != null) {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
//                    PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
//                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//
//                //mLocationRequest = new LocationRequest();
//               // mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//                // Create LocationSettingsRequest object using location request
//                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
//                builder.addLocationRequest(mLocationRequest);
//                LocationSettingsRequest locationSettingsRequest = builder.build();
//
//
//                SettingsClient settingsClient = LocationServices.getSettingsClient(this);
//                settingsClient.checkLocationSettings(locationSettingsRequest);
//
//                final Task<Location> locationTask = ((UnifiedAppApplication) getApplicationContext()).mFusedLocationClient.getLastLocation()
//                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
//                            @Override
//                            public void onSuccess(Location location) {
//                                // Got last known location. In some rare situations this can be null.
//                                if (location != null) {
//                                    Toast.makeText(getBaseContext(), "location is available ", Toast.LENGTH_LONG).show();
//                                    mLocation = location;
//                                    mLatitude = String.valueOf(location.getLatitude());
//                                    mLongitude = String.valueOf(location.getLongitude());
//                                    if(location.hasAccuracy())
//                                    {
//                                        accuracy = String.valueOf(location.getAccuracy());
//                                        text.setText("Accuracy is "+accuracy );
//                                        Toast.makeText(getBaseContext(), "location is available with accuracy "+accuracy, Toast.LENGTH_LONG).show();
//                                    }
//
//                                }
//                            }
//                        });
//            } else {
//
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                        Constants.PERMISSION_FINE_LOCATION);
//            }
//        } else {
//            // If the SDK is >= Marshmello, runtime permissions required
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
//                        PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
//                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                    // Permission is not granted
//                    ActivityCompat.requestPermissions(this,
//                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                            Constants.PERMISSION_FINE_LOCATION);
//                } else {
//                    ((UnifiedAppApplication) getApplication()).mFusedLocationClient =
//                            LocationServices.getFusedLocationProviderClient(this);
//
//                    // Initializing location request
//                    Utils.getInstance().createLocationRequest(this);
//                }
//            } else {
//                ((UnifiedAppApplication) getApplication()).mFusedLocationClient =
//                        LocationServices.getFusedLocationProviderClient(this);
//
//                // Initializing location request
//                Utils.getInstance().createLocationRequest(this);
//            }
//        }
//
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
//    public void retainPreviousImages(String uuid) throws IOException {
//
//        String path = createImageFile();
//        File fts = new File(path);
//        if(fts.exists())
//            fts.delete();
//
//        String chng = path.substring(path.lastIndexOf("/")+1, path.indexOf(".png"));
//        String newPath = path.replaceAll(chng, uuid);
//
//        addCustomView(newPath);
//
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        switch (requestCode) {
//            case Constants.REQUEST_GEOTAG_PIN_DROP:
//                if (resultCode == Activity.RESULT_OK) {
//
//                    String latitude = data.getStringExtra("lat");
//                    String longitude = data.getStringExtra("lon");
//                    FormImage mCurrentImage = data.getParcelableExtra("mCurrentImage");
//                    mCurrentImage.setGeotag(latitude + "," + longitude);
//
//                } else {
//                    Toast.makeText(this,
//                            getResources().getString(R.string.GEOTAG_WASNT_SET), Toast.LENGTH_SHORT).show();
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
//
//    protected void onResult(FormImage mCurrentImage) {
//
//        mCurrentImage.setTimeStamp(mInitialTimestamp);
//        mCurrentImage.setDatatype(mDatatype);
//        mCurrentImage.setAppId(mAppId);
//        mCurrentImage.setProjectId(mProjectId);
//        mCurrentImage.setUserId(mUserId);
//
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
//                    mCurrentImage.setBitmap(bitmapByteArray);
//                }
//            }
//            mCurrentImage.setHasGeotag(true);
//
//            if (mLatitude.equals("") && mLongitude.equals("")) {
//
//                Toast.makeText(this, "Cannot load map right now!", Toast.LENGTH_LONG).show();
//                mCurrentImage.setGeotag("");
//            } else {
//                if (Utils.getInstance().isOnline(OpenCamera.this)) {
//                    Intent intent = new Intent(this, DropLocationActivity.class);
//                    intent.putExtra("lat", mLatitude);
//                    intent.putExtra("lon", mLongitude);
//                    intent.putExtra("mCurrentImage", mCurrentImage);
//                    startActivityForResult(intent, Constants.REQUEST_GEOTAG_PIN_DROP);
//                } else {
//                    Toast.makeText(this, "Cannot load map without network connection!", Toast.LENGTH_LONG).show();
//                    mCurrentImage.setGeotag("");
//                }
//            }
//        } else if (mDatatype.equals("geotagimagefused")) {
//
//            mCurrentImage.setHasGeotag(true);
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
//                        mCurrentImage.setBitmap(bitmapByteArray);
//                    }
//                }
//
//                String latitude = String.valueOf(((UnifiedAppApplication) getApplication()).mUserLatitude);
//                String longitude = String.valueOf(((UnifiedAppApplication) getApplication()).mUserLongitude);
//                mCurrentImage.setGeotag(latitude + "," + longitude);
//
//            } else {
//                // Tell the user that this image isn't valid (geotagged)
//                Toast.makeText(getApplicationContext(), getResources().getString(R.string.GEOTAG_IMAGE_VALIDATION_FAILED), Toast.LENGTH_LONG).show();
//            }
//        } else {
//            // Picking image from the currently stored path
//            File imgFile = new File(mCurrentImage.getLocalPath());
//            Bitmap bitmap = null;
//            if (imgFile.exists()) {
//                byte[] bitmapByteArray = makeThumbnailImage(imgFile, 30);
//                bitmap = BitmapFactory.decodeByteArray(bitmapByteArray, 0, bitmapByteArray.length);
//                if (bitmap != null) {
//                    mCurrentImage.setBitmap(bitmapByteArray);
//                }
//            }
//            mCurrentImage.setHasGeotag(false);
//
//        }
//    }
//
//
//    protected void startLocationUpdates() {
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//
//            if (((UnifiedAppApplication) getApplication()).voidTask != null) {
//                ((UnifiedAppApplication) getApplication()).voidTask
//                        .addOnSuccessListener(this, new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void aVoid) {
//
//                                mLatitude = String.valueOf(((UnifiedAppApplication) getApplication()).mUserLatitude);
//                                mLongitude = String.valueOf(((UnifiedAppApplication) getApplication()).mUserLongitude);
//                                accuracy = String.valueOf(((UnifiedAppApplication) getApplication()).mAccuracy);
//                                Toast.makeText(getBaseContext(), "location is available with accuracy "+accuracy, Toast.LENGTH_LONG).show();
//                                text.setText(" Accuracy : "+accuracy+" ");
//
//
//                            }
//                        });
//            }
//        } else{
//            ActivityCompat.requestPermissions((Activity) getBaseContext(),
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
//                            Manifest.permission.ACCESS_COARSE_LOCATION},
//                    Constants.PERMISSION_FINE_LOCATION);
//        }
//    }
//}

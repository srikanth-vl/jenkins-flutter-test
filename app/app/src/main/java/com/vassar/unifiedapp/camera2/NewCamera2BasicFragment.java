package com.vassar.unifiedapp.camera2;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.location.Location;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.picasso.Picasso;
import com.vassar.unifiedapp.model.GpsValidation;
import com.vassar.unifiedapp.newcamera.CameraResultImage;
import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.application.UnifiedAppApplication;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.db.UnifiedAppDBHelper;
import com.vassar.unifiedapp.model.FormMedia;
import com.vassar.unifiedapp.model.Project;
import com.vassar.unifiedapp.ui.OpenCameraImageThumbnail;
import com.vassar.unifiedapp.ui.ProjectFormActivity;
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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.vassar.unifiedapp.utils.Constants.PICK_FROM_GALLERY;
//import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

public class NewCamera2BasicFragment extends Fragment
        implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    /**
     * Conversion from screen rotation to JPEG orientation.
     */
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 2;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 3;
    private static final String FRAGMENT_DIALOG = "dialog";
    private FormMedia mCurrentImage;
    private String mDatatype;
    private int mMaxImages;
    private String mProjectId;
    private String mAppId;
    private String mUserId;
    private ArrayList<String> uuids;
    private File imageFile = null;
    private double mLatitude;
    private double mLongitude;
    private double mAccuracy;
    private float mBearing;
    private Project mProject;
    private int mImageCount = 0;
    private UnifiedAppDBHelper mDBHelper;
    private LinearLayout mCameraImagesLayout;
    private TextView accuracy;
    private Button submit;
    private ImageButton gallery;
    private GpsValidation gpsValidation;
    private Map<String, JSONObject> mSubmittedFields = new HashMap<>();
    private HashMap<String, String> submittedFieldFromIntent = new HashMap<>();
    private String mUIType;
    private Button captureButton;
    private boolean isTakingPicture = false;

    private TextView bearing;

    private static final String SEPARATOR = ",";


    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    /**
     * Tag for the {@link Log}.
     */
    private static final String TAG = "Camera2BasicFragment";

    /**
     * Camera state: Showing camera preview.
     */
    private static final int STATE_PREVIEW = 0;

    /**
     * Camera state: Waiting for the focus to be locked.
     */
    private static final int STATE_WAITING_LOCK = 1;

    /**
     * Camera state: Waiting for the exposure to be precapture state.
     */
    private static final int STATE_WAITING_PRECAPTURE = 2;

    /**
     * Camera state: Waiting for the exposure state to be something other than precapture.
     */
    private static final int STATE_WAITING_NON_PRECAPTURE = 3;

    /**
     * Camera state: Picture was taken.
     */
    private static final int STATE_PICTURE_TAKEN = 4;

    /**
     * Max preview width that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_WIDTH = 1920;

    /**
     * Max preview height that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
     * {@link TextureView}.
     */
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }

    };

    /**
     * ID of the current {@link CameraDevice}.
     */
    private String mCameraId;

    /**
     * An {@link AutoFitTextureView} for camera preview.
     */
    private AutoFitTextureView mTextureView;

    /**
     * A {@link CameraCaptureSession } for camera preview.
     */
    private CameraCaptureSession mCaptureSession;

    /**
     * A reference to the opened {@link CameraDevice}.
     */
    private CameraDevice mCameraDevice;

    /**
     * The {@link android.util.Size} of camera preview.
     */
    private Size mPreviewSize;

    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state.
     */
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            Activity activity = getActivity();
            if (null != activity) {
                activity.finish();
            }
        }

    };

    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;

    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler mBackgroundHandler;

    /**
     * An {@link ImageReader} that handles still image capture.
     */
    private ImageReader mImageReader;

    /**
     * This a callback object for the {@link ImageReader}. "onImageAvailable" will be called when a
     * still image is ready to be saved.
     */
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(), imageFile));
        }

    };

    /**
     * {@link CaptureRequest.Builder} for the camera preview
     */
    private CaptureRequest.Builder mPreviewRequestBuilder;

    /**
     * {@link CaptureRequest} generated by {@link #mPreviewRequestBuilder}
     */
    private CaptureRequest mPreviewRequest;

    /**
     * The current state of camera state for taking pictures.
     *
     * @see #mCaptureCallback
     */
    private int mState = STATE_PREVIEW;

    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    /**
     * Whether the current camera device supports Flash or not.
     */
    private boolean mFlashSupported;

    /**
     * Orientation of the camera sensor
     */
    private int mSensorOrientation;

    /**
     * A {@link CameraCaptureSession.CaptureCallback} that handles events related to JPEG capture.
     */
    private CameraCaptureSession.CaptureCallback mCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result) {
            switch (mState) {
                case STATE_PREVIEW: {
                    // We have nothing to do when the camera preview is working normally.
                    break;
                }
                case STATE_WAITING_LOCK: {
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    if (afState == null) {
                        captureStillPicture();
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                            CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        // CONTROL_AE_STATE can be null on some devices
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        if (aeState == null ||
                                aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            mState = STATE_PICTURE_TAKEN;
                            captureStillPicture();
                        } else {
                            runPrecaptureSequence();
                        }
                    } else {
                        captureStillPicture();
                    }
                    break;
                }
                case STATE_WAITING_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        mState = STATE_WAITING_NON_PRECAPTURE;
                    }
                    break;
                }
                case STATE_WAITING_NON_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        mState = STATE_PICTURE_TAKEN;
                        captureStillPicture();
                    }
                    break;
                }
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            process(result);
        }

    };

    /**
     * Shows a {@link Toast} on the UI thread.
     *
     * @param text The message to show
     */
    private void showToast(final String text) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Given {@code choices} of {@code Size}s supported by a camera, choose the smallest one that
     * is at least as large as the respective texture view size, and that is at most as large as the
     * respective max size, and whose aspect ratio matches with the specified value. If such size
     * doesn't exist, choose the largest one that is at most as large as the respective max size,
     * and whose aspect ratio matches with the specified value.
     *
     * @param choices           The list of sizes that the camera supports for the intended output
     *                          class
     * @param textureViewWidth  The width of the texture view relative to sensor coordinate
     * @param textureViewHeight The height of the texture view relative to sensor coordinate
     * @param maxWidth          The maximum width that can be chosen
     * @param maxHeight         The maximum height that can be chosen
     * @param aspectRatio       The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
                                          int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                    option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    public static NewCamera2BasicFragment newInstance() {
        return new NewCamera2BasicFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_new_camera_2_basic, container, false);

        view.findViewById(R.id.picture).setOnClickListener(this);
        view.findViewById(R.id.submit).setOnClickListener(this);

        if(PropertyReader.getBooleanProperty("UPLOAD_FROM_GALLERY")) {
            view.findViewById(R.id.gallery).setVisibility(View.VISIBLE);
            view.findViewById(R.id.gallery).setOnClickListener(this);
        }

        mTextureView = (AutoFitTextureView) view.findViewById(R.id.texture);
        mCameraImagesLayout = (LinearLayout) view.findViewById(R.id.thumbnail_layout);
        accuracy = (TextView) view.findViewById(R.id.accuracy);
        bearing = (TextView) view.findViewById(R.id.bearing);
        captureButton = (Button) view.findViewById(R.id.picture);

        mDBHelper = UAAppContext.getInstance().getDBHelper();

        if(savedInstanceState == null){
            mDatatype = getArguments().getString("datatype");
            mAppId = getArguments().getString("appId");
            mUserId = getArguments().getString("userId");
            mMaxImages = getArguments().getInt("max");
            mProjectId = getArguments().getString("projectId");
            mUIType = getArguments().getString("uitype");
            uuids = new ArrayList<>();
            uuids.addAll(getArguments().getStringArrayList("uuids"));
            mLatitude = ((UnifiedAppApplication)(getActivity().getApplication())).mUserLatitude;
            mLongitude = ((UnifiedAppApplication)(getActivity().getApplication())).mUserLongitude;
            mAccuracy = ((UnifiedAppApplication)(getActivity().getApplication())).mAccuracy;
            mBearing = ((UnifiedAppApplication)(getActivity().getApplication())).mUserBearing;
            gpsValidation = getArguments().getParcelable("gps_validation");
            submittedFieldFromIntent = (HashMap<String, String>) getArguments().getSerializable("submittedFields");

            mSubmittedFields = convertStringToJSONObjectInMap(submittedFieldFromIntent);

            mProject = UAAppContext.getInstance().getProjectFromProjectList(mProjectId);

            for (String uuid : uuids) {
                FormMedia formImage = mDBHelper.getFormMedia(uuid, mAppId, mUserId);
                if (formImage != null) {
                    ((NewCamera2Activity)getActivity()).mFormMedia.add(formImage);
                    mImageCount++;
                }
            }

        } else {

            mDatatype = savedInstanceState.getString("datatype");
            mAppId = savedInstanceState.getString("appId");
            mUserId = savedInstanceState.getString("userId");
            mMaxImages = savedInstanceState.getInt("max");
            mProjectId = savedInstanceState.getString("projectId");
            uuids = new ArrayList<>();
            uuids.addAll(savedInstanceState.getStringArrayList("uuids"));
            mLatitude = savedInstanceState.getDouble("latitude");
            mLongitude = savedInstanceState.getDouble("longitude");
            mAccuracy = savedInstanceState.getDouble("accuracy");
            mProject = savedInstanceState.getParcelable("project");
            mImageCount = savedInstanceState.getInt("imageCount");
            mCurrentImage = savedInstanceState.getParcelable("currentImage");
            gpsValidation = savedInstanceState.getParcelable("gps_validation");
            submittedFieldFromIntent = (HashMap<String, String>) savedInstanceState.getSerializable("submittedFieldsFromIntent");
            mSubmittedFields = convertStringToJSONObjectInMap(submittedFieldFromIntent);
            mBearing = savedInstanceState.getFloat("bearing");
            mUIType = savedInstanceState.getString("uitype");

        }

        Utils.logInfo("MAX IMAGES :: ", String.valueOf(mMaxImages));
        accuracy.setText(getResources().getString(R.string.gps_accuracy)+ String.valueOf((int)mAccuracy) + " m");
        bearing.setText("Bearing : " + String.valueOf(mBearing));

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("datatype", mDatatype);
        outState.putString("appId", mAppId);
        outState.putString("userId", mUserId);
        outState.putInt("max", mMaxImages);
        outState.putString("projectId", mProjectId);
        outState.putStringArrayList("uuids", uuids);
        outState.putDouble("latitude", mLatitude);
        outState.putDouble("longitude", mLongitude);
        outState.putDouble("accuracy", mAccuracy);
        outState.putParcelable("project", mProject);
        outState.putInt("imageCount", mImageCount);
        outState.putParcelable("currentImage", mCurrentImage);
        outState.putParcelable("gps_validation", gpsValidation);
        outState.putSerializable("submittedFieldsFromIntent", submittedFieldFromIntent);
        outState.putFloat("bearing", mBearing);
        outState.putString("uitype", mUIType);
    }

//    @Override
//    public void onViewCreated(final View view, Bundle savedInstanceState) {
//
//
//    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // TODO : Need to account for multiple files
        // TODO : Remove from here and call it when the ImageSaver class is called!!
//        mFile = new File(getActivity().getExternalFilesDir(null), "pic.jpg");
//        retainOlderImages();
        for(FormMedia formMedia : ((NewCamera2Activity)getActivity()).mFormMedia) {
            if(formMedia != null) {
                addThumbnail(formMedia);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();

        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    private void requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            new ConfirmationDialog().show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ErrorDialog.newInstance(getString(R.string.request_permission))
                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            }
        } else if(requestCode == REQUEST_READ_EXTERNAL_STORAGE){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                gallery.callOnClick();
            }
        }else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Sets up member variables related to camera.
     *
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    @SuppressWarnings("SuspiciousNameCombination")
    private void setUpCameraOutputs(int width, int height) {
        Activity activity = getActivity();
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics
                        = manager.getCameraCharacteristics(cameraId);

                // We don't use a front facing camera in this sample.
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }

                StreamConfigurationMap map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }

                // For still image captures, we use the largest available size.
                Size largest = Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        new CompareSizesByArea());
                mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
                        ImageFormat.JPEG, /*maxImages*/2);
                mImageReader.setOnImageAvailableListener(
                        mOnImageAvailableListener, mBackgroundHandler);

                // Find out if we need to swap dimension to get the preview size relative to sensor
                // coordinate.
                int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
                //noinspection ConstantConditions
                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                boolean swappedDimensions = false;
                switch (displayRotation) {
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                        if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                            swappedDimensions = true;
                        }
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                            swappedDimensions = true;
                        }
                        break;
                    default:
                        Log.e(TAG, "Display rotation is invalid: " + displayRotation);
                }

                Point displaySize = new Point();
                activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
                int rotatedPreviewWidth = width;
                int rotatedPreviewHeight = height;
                int maxPreviewWidth = displaySize.x;
                int maxPreviewHeight = displaySize.y;

                if (swappedDimensions) {
                    rotatedPreviewWidth = height;
                    rotatedPreviewHeight = width;
                    maxPreviewWidth = displaySize.y;
                    maxPreviewHeight = displaySize.x;
                }

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH;
                }

                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT;
                }

                // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
                // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
                // garbage capture data.
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                        maxPreviewHeight, largest);

                // We fit the aspect ratio of TextureView to the size of preview we picked.
                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mTextureView.setAspectRatio(
                            mPreviewSize.getWidth(), mPreviewSize.getHeight());
                } else {
                    mTextureView.setAspectRatio(
                            mPreviewSize.getHeight(), mPreviewSize.getWidth());
                }

                // Check if the flash is supported.
                Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                mFlashSupported = available == null ? false : available;

                mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            ErrorDialog.newInstance(getString(R.string.camera_error))
                    .show(getChildFragmentManager(), FRAGMENT_DIALOG);
        }
    }

    /**
     * Opens the camera specified by {@link NewCamera2BasicFragment#mCameraId}.
     */
    private void openCamera(int width, int height) {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
            return;
        }
        setUpCameraOutputs(width, height);
        configureTransform(width, height);
        Activity activity = getActivity();
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }

    /**
     * Closes the current {@link CameraDevice}.
     */
    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new {@link CameraCaptureSession} for camera preview.
     */
    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            // This is the output Surface we need to start preview.
            Surface surface = new Surface(texture);

            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder
                    = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);

            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == mCameraDevice) {
                                return;
                            }

                            // When the session is ready, we start displaying the preview.
                            mCaptureSession = cameraCaptureSession;
                            try {
                                // Auto focus should be continuous for camera preview.
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                // Flash is automatically enabled when necessary.
                                setAutoFlash(mPreviewRequestBuilder);

                                // Finally, we start displaying the camera preview.
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mCaptureSession.setRepeatingRequest(mPreviewRequest,
                                        mCaptureCallback, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                            showToast("Failed");
                        }
                    }, null
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    private void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = getActivity();
        if (null == mTextureView || null == mPreviewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    /**
     * Initiate a still image capture.
     */
    private void takePicture() {

        try {
            createImageFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        lockFocus();
    }

    /**
     * Lock the focus as the first step for a still image capture.
     */
    private void lockFocus() {
        try {
            // This is how to tell the camera to lock focus.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the lock.
            mState = STATE_WAITING_LOCK;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Run the precapture sequence for capturing a still image. This method should be called when
     * we get a response in {@link #mCaptureCallback} from {@link #lockFocus()}.
     */
    private void runPrecaptureSequence() {
        try {
            // This is how to tell the camera to trigger.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the precapture sequence to be set.
            mState = STATE_WAITING_PRECAPTURE;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Capture a still picture. This method should be called when we get a response in
     * {@link #mCaptureCallback} from both {@link #lockFocus()}.
     */
    private void captureStillPicture() {
        try {
            final Activity activity = getActivity();
            if (null == activity || null == mCameraDevice) {
                return;
            }
            // This is the CaptureRequest.Builder that we use to take a picture.
            final CaptureRequest.Builder captureBuilder =
                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());

            // Use the same AE and AF modes as the preview.
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            setAutoFlash(captureBuilder);

            // Orientation
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));

            CameraCaptureSession.CaptureCallback CaptureCallback
                    = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
//                    showToast("Saved: " + imageFile.getAbsolutePath());
                    Log.d(TAG, imageFile.toString());
                    unlockFocus();

                    Intent i = new Intent(activity, CameraResultImage.class);
                    i.putExtra("path", imageFile.getAbsolutePath());
                    startActivityForResult(i, Constants.REQUEST_IMAGE_RESULT);

                }
            };

            if (mCaptureSession != null) {
                mCaptureSession.stopRepeating();
                mCaptureSession.abortCaptures();
                mCaptureSession.capture(captureBuilder.build(), CaptureCallback, null);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the JPEG orientation from the specified screen rotation.
     *
     * @param rotation The screen rotation.
     * @return The JPEG orientation (one of 0, 90, 270, and 360)
     */
    private int getOrientation(int rotation) {
        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
        // We have to take that into account and rotate JPEG properly.
        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
    }

    /**
     * Unlock the focus. This method should be called when still image capture sequence is
     * finished.
     */
    private void unlockFocus() {
        try {
            // Reset the auto-focus trigger
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            setAutoFlash(mPreviewRequestBuilder);
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
            // After this, the camera will go back to the normal state of preview.
            mState = STATE_PREVIEW;
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.picture:
                if(mImageCount < mMaxImages) {
                    isTakingPicture = true;
                    takePicture();
                } else {
                    Toast.makeText(getContext(), getResources().getString(R.string.MAX_IMAGES_REACHED), Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.submit :
                if (!isTakingPictureValue()) {
                    if (((NewCamera2Activity) getActivity()).mFormMedia.size() > 0) {

                        StringBuilder uuidBuilder = new StringBuilder();
                        for (FormMedia formImage : ((NewCamera2Activity) getActivity()).mFormMedia) {
                            uuidBuilder.append(formImage.getmUUID());
                            if (formImage.ismHasGeotag()) {
                                uuidBuilder.append(Constants.IMAGE_UUID_LONG_LAT_SEPARATOR + formImage.getLongitude() + Constants.IMAGE_UUID_LONG_LAT_SEPARATOR + formImage.getLatitude());
                            }
                            uuidBuilder.append(SEPARATOR);
                        }
                        String imageUUIDs = uuidBuilder.toString();
                        //Remove last comma
                        imageUUIDs = imageUUIDs.substring(0, imageUUIDs.length() - SEPARATOR.length());

                        Intent data = new Intent();
                        data.putParcelableArrayListExtra("formImages", ((NewCamera2Activity) getActivity()).mFormMedia);
                        data.putExtra("imageuuids", imageUUIDs);
                        getActivity().setResult(RESULT_OK, data);
                        getActivity().finish();

                    } else {
                        Toast.makeText(getContext(), getResources().getString(R.string.NO_IMAGES_TAKEN)
                                , Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case R.id.gallery :

                if (ContextCompat.checkSelfPermission((NewCamera2Activity)getActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    if (mImageCount < mMaxImages) {

                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        intent.setAction(Intent.ACTION_PICK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS);
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_FROM_GALLERY);

                    } else {
                        Toast.makeText(getContext(), getResources().getString(R.string.MAX_IMAGES_REACHED), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                REQUEST_READ_EXTERNAL_STORAGE);
                        }
                    }
                break;

            default:
        }
    }

    private void setAutoFlash(CaptureRequest.Builder requestBuilder) {
        if (mFlashSupported) {
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
        }
    }

    /**
     * Saves a JPEG {@link Image} into the specified {@link File}.
     */
    private static class ImageSaver implements Runnable {

        /**
         * The JPEG image
         */
        private final Image mImage;
        /**
         * The file we save the image into.
         */
        private final File mFile;


        ImageSaver(Image image, File file) {
            mImage = image;
            mFile = file;
        }

        @Override
        public void run() {
            if (mImage != null) {
                ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                FileOutputStream output = null;
                try {
                    output = new FileOutputStream(mFile);
                    output.write(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    mImage.close();

                    if (null != output) {
                        try {
                            output.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    /**
     * Shows an error message dialog.
     */
    public static class ErrorDialog extends DialogFragment {

        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            activity.finish();
                        }
                    })
                    .create();
        }

    }

    /**
     * Shows OK/Cancel confirmation dialog about camera permission.
     */
    public static class ConfirmationDialog extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Fragment parent = getParentFragment();
            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.request_permission)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            parent.requestPermissions(new String[]{Manifest.permission.CAMERA},
                                    REQUEST_CAMERA_PERMISSION);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Activity activity = parent.getActivity();
                                    if (activity != null) {
                                        activity.finish();
                                    }
                                }
                            })
                    .create();
        }
    }

    private void createImageFile() throws IOException {
        // Adding the image to the group of images

        mCurrentImage = createNewFormMedia();
        // Create an image file name
        String imageFileName = mCurrentImage.getmUUID();
        Utils.logInfo("file name from uuid : ", imageFileName);

        File storageDir = ((NewCamera2Activity)getActivity()).getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        imageFile = new File(storageDir.getAbsolutePath() + "/"+ imageFileName + ".jpg");
        try {
            imageFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mCurrentImage.setmLocalPath(imageFile.getAbsolutePath());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == Constants.REQUEST_IMAGE_RESULT) {
            isTakingPicture = false;
            if (resultCode == RESULT_CANCELED) {
                if (imageFile != null && imageFile.exists()) {
                    Utils.getInstance().deleteImageFromStorage(getContext(), imageFile.getAbsolutePath());
                }
            } else if (resultCode == RESULT_OK) {

                boolean valid = onCameraCaptureResult();
//                boolean valid = true;
                if (valid) {

                    mImageCount++;
                    ((NewCamera2Activity) getActivity()).mFormMedia.add(mCurrentImage);
                    uuids.add(mCurrentImage.getmUUID());
                    addThumbnail(mCurrentImage);

//                        TimestampImageTask timestampImageTask = new TimestampImageTask(getContext(), imageFile);
//                        timestampImageTask.execute();
                } else {
                    if (imageFile != null && imageFile.exists()) {
                        Utils.getInstance().deleteImageFromStorage(getContext(), imageFile.getAbsolutePath());
                    }
                }
            }
        } else if(PICK_FROM_GALLERY == requestCode){

            if(resultCode == RESULT_OK && null != data) {
                if (data.getClipData() == null) {
                    mImageCount++;
                    Uri newUri = data.getData();
                    String filePath = Utils.getInstance().getRealPathFromUri(newUri, getActivity());
                    FormMedia newFormMedia = createNewFormMedia();
                    newFormMedia.setmLocalPath(filePath);
                    newFormMedia = createByteArrayForFormMedia(newFormMedia);
                    ((NewCamera2Activity) getActivity()).mFormMedia.add(newFormMedia);
                    uuids.add(newFormMedia.getmUUID());
                    addThumbnail(newFormMedia);

                } else {

                    ClipData mClipData = data.getClipData();
                    int pickedImageCount;

                    if (mClipData.getItemCount() > (mMaxImages - mImageCount)) {
                        Toast.makeText(getActivity(), "Cannot select more than " + (mMaxImages - mImageCount) + " images.",
                                Toast.LENGTH_SHORT).show();
                    } else {

                        for (pickedImageCount = 0; pickedImageCount < mClipData.getItemCount();
                             pickedImageCount++) {
                            mImageCount++;
                            String filePath = Utils.getInstance().getRealPathFromUri(
                                    mClipData.getItemAt(pickedImageCount).getUri(), getActivity());
                            FormMedia newFormMedia = createNewFormMedia();
                            newFormMedia.setmLocalPath(filePath);
                            newFormMedia = createByteArrayForFormMedia(newFormMedia);
                            ((NewCamera2Activity) getActivity()).mFormMedia.add(newFormMedia);
                            uuids.add(newFormMedia.getmUUID());
                            addThumbnail(newFormMedia);
                        }
                    }
                }
            }

        }
    }

    //for thumbnail
    private boolean onCameraCaptureResult() {

        boolean validImage = true;

        createByteArrayForFormMedia(mCurrentImage);

        //getting last updated location
        mLongitude = ((UnifiedAppApplication) getActivity().getApplication()).mUserLongitude;
        mLatitude = ((UnifiedAppApplication) getActivity().getApplication()).mUserLatitude;
        mAccuracy = ((UnifiedAppApplication) getActivity().getApplication()).mAccuracy;
        mBearing = ((UnifiedAppApplication) getActivity().getApplication()).mUserBearing;

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
                                            Toast.makeText(getActivity(), getResources().getString(R.string.SELECT_VALID_LOCATION), Toast.LENGTH_SHORT).show();
                                            isValid = false;
                                        }
                                    } else {
                                        Toast.makeText(getActivity(), getResources().getString(R.string.VALIDATION_VALUES_NOT_FOUND), Toast.LENGTH_SHORT).show();
                                    }
                                } else if (gpsValidation.getSource().equalsIgnoreCase("key")) {
                                    String valueFromSource = getSubmittedFieldFromKey(mSubmittedFields, gpsValidation.getSource());
                                    List<String> geotag = getGeotagFromValue(gpsValidation.getKeyType(), valueFromSource);

                                    if (geotag.get(0) != null && !geotag.get(0).isEmpty() && geotag.get(1) != null && !geotag.get(1).isEmpty()) {
                                        boolean valid = validateGps(geotag.get(0), geotag.get(1), String.valueOf(mLatitude), String.valueOf(mLongitude));
                                        if (valid) {
                                        } else {
                                            Toast.makeText(getActivity(), getResources().getString(R.string.SELECT_VALID_LOCATION), Toast.LENGTH_SHORT).show();
                                            isValid = false;
                                        }
                                    }else {
                                        Toast.makeText(getActivity(), getResources().getString(R.string.VALIDATION_VALUES_NOT_FOUND), Toast.LENGTH_SHORT).show();
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
                    ((UnifiedAppApplication)getActivity().getApplication()).initializeLocation();
                    ((UnifiedAppApplication)getActivity().getApplication()).startLocationUpdates();

                    // Do not have the current user location, cannot perform validation
                    if((mProject.mBBoxValidation != null && !mProject.mBBoxValidation.isEmpty()) || (mProject.mCentroidValidation != null && !mProject.mCentroidValidation.isEmpty())) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.NO_CURRENT_LOCATION), Toast.LENGTH_LONG).show();
                        validImage = false;
                        mCurrentImage.setAccuracy(Constants.DEFAULT_ACCURACY);
                        mCurrentImage.setLongitude(Constants.DEFAULT_LONGITUDE);
                        mCurrentImage.setLatitude(Constants.DEFAULT_LATITUDE);
                        return validImage;
                    } else{
                        mLongitude = ((UnifiedAppApplication) getActivity().getApplication()).mUserLongitude;
                        mLatitude = ((UnifiedAppApplication) getActivity().getApplication()).mUserLatitude;
                        mAccuracy = ((UnifiedAppApplication) getActivity().getApplication()).mAccuracy;
                        mBearing = ((UnifiedAppApplication) getActivity().getApplication()).mUserBearing;

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
                    Toast.makeText(getActivity(), getResources().getString(R.string.GEOTAG_IMAGE_VALIDATION_FAILED), Toast.LENGTH_LONG).show();
                    validImage = false;

                }
            }
            else {
//                    Toast.makeText(getApplicationContext(), "No project associated, so no validation for location can be performed", Toast.LENGTH_LONG).show();
                // Project is null, cannot perform validation
                mLongitude = ((UnifiedAppApplication) getActivity().getApplication()).mUserLongitude;
                mLatitude = ((UnifiedAppApplication) getActivity().getApplication()).mUserLatitude;
                mAccuracy = ((UnifiedAppApplication) getActivity().getApplication()).mAccuracy;
                mBearing = ((UnifiedAppApplication) getActivity().getApplication()).mUserBearing;

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

        final OpenCameraImageThumbnail view = new OpenCameraImageThumbnail(getContext());
        File imgFile = new File(image.getLocalPath());
        Picasso.get().load(imgFile)
                .resize(150, 150)
                .placeholder(R.drawable.placeholder)
                .into(view.getImgPhoto());

        mCameraImagesLayout.addView(view);

        view.getBtnClose().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mDBHelper.deleteFormMedia(image.getmUUID());
                String fileName = imgFile.getName().substring(0, imgFile.getName().indexOf("."));
                if(image.getmUUID().equals(fileName)) {
                    Utils.getInstance().deleteImageFromStorage(getContext(), image.getLocalPath());
                }
                ((NewCamera2Activity)getActivity()).mFormMedia.remove(image);
                mCameraImagesLayout.removeView(view);
                mImageCount--;
            }
        });
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
        if(map == null) {
            return convertedMap;
        }
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

    public void disableCaptureButton(){
        if (captureButton != null){
            captureButton.setEnabled(false);
        }
    }
    public boolean isTakingPictureValue(){
        return isTakingPicture;
    }
}
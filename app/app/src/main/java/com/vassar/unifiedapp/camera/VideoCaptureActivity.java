package com.vassar.unifiedapp.camera;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.model.FormMedia;
import com.vassar.unifiedapp.model.FormVideo;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.MediaActionType;
import com.vassar.unifiedapp.utils.MediaRequestStatus;
import com.vassar.unifiedapp.utils.MediaSubType;
import com.vassar.unifiedapp.utils.MediaType;
import com.vassar.unifiedapp.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VideoCaptureActivity extends Activity {

    private Camera mCamera;
    private TextureView mPreview;
    private MediaRecorder mMediaRecorder;
    private File mOutputFile;
    private long media_click_ts;
    private boolean isRecording = false;
    private static final String TAG = "Recorder";
    private Button captureButton;

    private String mDatatype;
    private long mInitialTimestamp;
    private int mMaxDuration;
    private String mTag;
    private String mProjectId;
    private String mKey;
    private String mAppId;
    private String mUserId;

    private UUID mUuid;
    private String mVideoLocalPath;
    private Chronometer video_timer;

    //OrientationEventListener
    private OrientationEventListener myOrientationEventListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_capture);

        Utils.getInstance().showLog("VIDEO CAPTURE", "ON CREATE CALLED");

        // currentTag & maxImages in the intent
        mDatatype = getIntent().getStringExtra("datatype");
        mInitialTimestamp = getIntent().getLongExtra("initialts", 0);
        mMaxDuration = getIntent().getIntExtra("max", Constants.DEFAULT_MAX_VIDEO);
        mTag = getIntent().getStringExtra("tag");
        mProjectId = getIntent().getStringExtra("projectId");
        mAppId = getIntent().getStringExtra("appId");
        mUserId = getIntent().getStringExtra("userId");
        mKey = getIntent().getStringExtra("key");

        mPreview = (TextureView) findViewById(R.id.surface_view);
        captureButton = (Button) findViewById(R.id.button_capture);
        video_timer = (Chronometer) findViewById(R.id.record_timer);

        // TODO : Can look into this later
//        setContentView(R.layout.activity_video_capture);
//        if (null == savedInstanceState) {
//            getFragmentManager().beginTransaction()
//                    .replace(R.id.container, VideoCaptureFragment.newInstance())
//                    .commit();
//        }

        //calling method for fining orientation state from accelerometer
        setOrientationListenerAndRotateScreen();
    }
    private void setOrientationListenerAndRotateScreen(){
        myOrientationEventListener=new OrientationEventListener(getApplicationContext(), SensorManager.SENSOR_DELAY_FASTEST) {
            @Override
            public void onOrientationChanged(int orientation) {
                //Log.i("Orientation detector : ","angle "+orientation);

                if (orientation == 0 ) {
                    Log.i("Orientation detector : ","Portrait");
                    //Getting the auto-rotation state from System setting
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
                else if (orientation == 180 ) {
                    Log.i("Orientation detector : ","Portrait");

                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);

                }
                else if (orientation == 90) {
                    Log.i("Orientation detector : ","landscape");

                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                }
                else if (orientation == 270) {

                    Log.i("Orientation detector : ","landscape");

                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            }
        };

        if (myOrientationEventListener.canDetectOrientation()) {
            //enabling Accelerometer
            myOrientationEventListener.enable();
            Log.i("SensorAvailability","Sensor Detected");
        }else{
            Log.i("SensorAvailability","Sensor not detected");
            Toast.makeText(this, "Sensor not detected", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * The capture button controls all user interaction. When recording, the button click
     * stops recording, releases {@link android.media.MediaRecorder} and {@link android.hardware.Camera}. When not recording,
     * it prepares the {@link android.media.MediaRecorder} and starts recording.
     *
     * @param view the view generating the event.
     */
    public void onCaptureClick(View view) {

        Utils.getInstance().showLog("VIDEO CAPTURE", "ON CLICK");

        if (isRecording) {
            // BEGIN_INCLUDE(stop_release_media_recorder)

            // TODO : Recording done

            // stop recording and release camera
            try {
                mMediaRecorder.stop();  // stop the recording
                video_timer.stop();
                media_click_ts = System.currentTimeMillis();
            } catch (RuntimeException e) {
                // RuntimeException is thrown when stop() is called immediately after start().
                // In this case the output file is not properly constructed ans should be deleted.
                Log.d(TAG, "RuntimeException: stop() is called immediately after start()");
                //noinspection ResultOfMethodCallIgnored
                mOutputFile.delete();
            }
            releaseMediaRecorder(); // release the MediaRecorder object
            mCamera.lock();         // take camera access back from MediaRecorder

            // inform the user that recording has stopped
            setCaptureButtonText("Capture");
            isRecording = false;
            releaseCamera();

            videoRecorded();
            // END_INCLUDE(stop_release_media_recorder)

        } else {

            // BEGIN_INCLUDE(prepare_start_media_recorder)

            new MediaPrepareTask().execute(null, null, null);

            // END_INCLUDE(prepare_start_media_recorder)

        }
    }

    private void setCaptureButtonText(String title) {
        captureButton.setText(title);
    }

    @Override
    protected void onPause() {

        Utils.getInstance().showLog("VIDEO CAPTURE", "ON PAUSE CALLED");

        super.onPause();
        // if we are using MediaRecorder, release it first
        releaseMediaRecorder();
        // release the camera immediately on pause event
        releaseCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Utils.getInstance().showLog("VIDEO CAPTURE", "ON RESUME CALLED");
    }

    private void releaseMediaRecorder(){

        Utils.getInstance().showLog("VIDEO CAPTURE", "RELEASE MEDIA RECORDER");

        if (mMediaRecorder != null) {
            // clear recorder configuration
            mMediaRecorder.reset();
            // release the recorder object
            mMediaRecorder.release();
            mMediaRecorder = null;
            // Lock camera for later use i.e taking it back from MediaRecorder.
            // MediaRecorder doesn't need it anymore and we will release it if the activity pauses.
            mCamera.lock();
        }
    }

    private void releaseCamera(){

        Utils.getInstance().showLog("VIDEO CAPTURE", "RELEASE CAMERA");

        if (mCamera != null){
            // release the camera for other applications
            mCamera.release();
            mCamera = null;
        }
    }

    private boolean prepareVideoRecorder() {

        Utils.getInstance().showLog("VIDEO CAPTURE", "PREPARE VIDEO RECORDER");

        // BEGIN_INCLUDE (configure_preview)
        mCamera = CameraHelper.getDefaultCameraInstance();

        // We need to make sure that our preview and recording video size are supported by the
        // camera. Query camera to find all the sizes and choose the optimal size given the
        // dimensions of our preview surface.
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
        List<Camera.Size> mSupportedVideoSizes = parameters.getSupportedVideoSizes();
        Camera.Size optimalSize = CameraHelper.getOptimalVideoSize(mSupportedVideoSizes,
                mSupportedPreviewSizes, mPreview.getWidth(), mPreview.getHeight());

        // Use the same size for recording profile.
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        profile.videoFrameWidth = optimalSize.width;
        profile.videoFrameHeight = optimalSize.height;

        // likewise for the camera object itself.
        parameters.setPreviewSize(profile.videoFrameWidth, profile.videoFrameHeight);
        mCamera.setParameters(parameters);
        mCamera.setDisplayOrientation(90);
        try {
            // Requires API level 11+, For backward compatibility use {@link setPreviewDisplay}
            // with {@link SurfaceView}
            mCamera.setPreviewTexture(mPreview.getSurfaceTexture());
        } catch (IOException e) {
            Log.e(TAG, "Surface texture is unavailable or unsuitable" + e.getMessage());
            return false;
        }
        // END_INCLUDE (configure_preview

        // BEGIN_INCLUDE (configure_media_recorder)
        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Setting max duration
        int maxDuration = mMaxDuration * 1000;
        mMediaRecorder.setMaxDuration(maxDuration);

        mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if(what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED){
                    // Recording is done
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.MAX_VIDEO_LENGTH_REACHED),
                            Toast.LENGTH_LONG).show();
                    videoRecorded();
                    finish();
                }
            }
        });

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(profile);

        // Step 4: Set output file
        mOutputFile = getOutputMediaFile();
//        mOutputFile = getOutputMediaFile();
        if (mOutputFile == null) {
            return false;
        }
        mMediaRecorder.setOutputFile(mOutputFile.getPath());
//        mMediaRecorder.setOutputFile(mOutputFile.getAbsolutePath());
        // END_INCLUDE (configure_media_recorder)

        // Step 5: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    /**
     * Asynchronous task for preparing the {@link android.media.MediaRecorder} since it's a long blocking
     * operation.
     */
    class MediaPrepareTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            // initialize video camera
            if (prepareVideoRecorder()) {
                // Camera is available and unlocked, MediaRecorder is prepared,
                // now you can start recording
                mMediaRecorder.start();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        video_timer.setBase(SystemClock.elapsedRealtime());
                        video_timer.start();
                    }
                });

                isRecording = true;
            } else {
                // prepare didn't work, release the camera
                releaseMediaRecorder();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                VideoCaptureActivity.this.finish();
            }
            // inform the user that recording has started
            setCaptureButtonText("Stop");

        }
    }

    /**
     * Creates a media file in the {@code Environment.DIRECTORY_PICTURES} directory. The directory
     * is persistent and available to other applications like gallery.
     *
     * @return A file object pointing to the newly created file.
     */
    public File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        if (!Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return  null;
        }

        mUuid = UUID.randomUUID();

        String fileName = mUuid.toString();
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        File mediaFile = new File(storageDir.getPath() + File.separator +
                fileName + Constants.VIDEO_TYPE_MP4);

        mVideoLocalPath = mediaFile.getAbsolutePath();

        return mediaFile;
    }

    public void videoRecorded() {
        if (mUuid != null && mVideoLocalPath != null){
            byte[] emptyArray = new byte[0];
//            FormMedia formVideo = new FormMedia(mUuid.toString(),emptyArray, mVideoLocalPath, mAppId, mUserId, mProjectId, false, Constants.DEFAULT_LATITUDE, Constants.DEFAULT_LONGITUDE, Constants.DEFAULT_ACCURACY, media_click_ts , Constants.VIDEO_EXT, MediaActionType.UPLOAD.getValue(), MediaType.VIDEO.getValue(), MediaSubType.FULL.getValue(), MediaRequestStatus.NEW_STATUS.getValue(), Constants.DEFAULT_RETRIES, Constants.UPLOAD_TIMESTAMP_DEFAULT, Constants.FORM_SUBMISSION_TIMESTAMP_DEFAULT);

            FormMedia formVideo = new FormMedia();
            formVideo.setmUUID(mUuid.toString());
            formVideo.setmBitmap(emptyArray);
            formVideo.setmLocalPath(mVideoLocalPath);
            formVideo.setmAppId(mAppId);
            formVideo.setmUserId(mUserId);
            formVideo.setmProjectId(mProjectId);
            formVideo.setmHasGeotag(false);
            formVideo.setLatitude(Constants.DEFAULT_LATITUDE);
            formVideo.setLongitude(Constants.DEFAULT_LONGITUDE);
            formVideo.setAccuracy(Constants.DEFAULT_ACCURACY);
            formVideo.setMediaActionType(MediaActionType.UPLOAD.getValue());
            formVideo.setMediaType(MediaType.VIDEO.getValue());
            formVideo.setMediaSubType(MediaSubType.FULL.getValue());
            formVideo.setMediaClickTimeStamp(media_click_ts);
            formVideo.setMediaUploadTimestamp(Constants.UPLOAD_TIMESTAMP_DEFAULT);
            formVideo.setMediaUploadRetries(Constants.DEFAULT_RETRIES);
            formVideo.setMediaFileExtension(Constants.VIDEO_EXT);
            formVideo.setFormSubmissionTimestamp(Constants.FORM_SUBMISSION_TIMESTAMP_DEFAULT);
            formVideo.setMediaRequestStatus(MediaRequestStatus.NEW_STATUS.getValue());

            Intent data = new Intent();
            ArrayList<FormMedia> formVideos = new ArrayList<>();
            formVideos.add(formVideo);
            data.putParcelableArrayListExtra("formVideos", formVideos);
            data.putExtra("videouuids", mUuid);
            setResult(RESULT_OK, data);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // The user cancelled the video recording
        setResult(RESULT_CANCELED);
        finish();
    }
}
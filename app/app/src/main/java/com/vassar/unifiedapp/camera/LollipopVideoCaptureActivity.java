package com.vassar.unifiedapp.camera;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.OrientationEventListener;
import android.widget.Toast;

import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.model.FormMedia;
import com.vassar.unifiedapp.model.FormVideo;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.MediaActionType;
import com.vassar.unifiedapp.utils.MediaRequestStatus;
import com.vassar.unifiedapp.utils.MediaSubType;
import com.vassar.unifiedapp.utils.MediaType;

import java.util.ArrayList;
import java.util.UUID;

public class LollipopVideoCaptureActivity extends AppCompatActivity {

    private String mDatatype;
    private long mInitialTimestamp;
    public int mMaxDuration;
    private String mTag;
    private String mProjectId;
    private String mKey;
    private String mAppId;
    private String mUserId;

    public UUID mUuid;
    public String mVideoLocalPath;

    //OrientationEventListener
    private OrientationEventListener myOrientationEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lollipop_video_capture);

        // currentTag & maxImages in the intent
        mDatatype = getIntent().getStringExtra("datatype");
        mInitialTimestamp = getIntent().getLongExtra("initialts", 0);
        mMaxDuration = getIntent().getIntExtra("max", Constants.DEFAULT_MAX_VIDEO);
        mTag = getIntent().getStringExtra("tag");
        mProjectId = getIntent().getStringExtra("projectId");
        mAppId = getIntent().getStringExtra("appId");
        mUserId = getIntent().getStringExtra("userId");
        mKey = getIntent().getStringExtra("key");

        if (null == savedInstanceState) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, Camera2VideoFragment.newInstance())
                    .commit();
        }
        //calling method for fining orientation state from accelerometer
        setOrientationListenerAndRotateScreen();
    }

    private void setOrientationListenerAndRotateScreen(){
        myOrientationEventListener = new OrientationEventListener(getApplicationContext(), SensorManager.SENSOR_DELAY_FASTEST) {
            @Override
            public void onOrientationChanged(int orientation) {

                // Log.i("Orientation detector : ","angle "+orientation);

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

    public void videoRecorded() {
        if (mUuid != null && mVideoLocalPath != null){
            FormVideo formVideo = new FormVideo(mUuid.toString(), mKey, mDatatype,
                    mInitialTimestamp, mVideoLocalPath, mAppId, mUserId
                    , mProjectId);

            Intent data = new Intent();
            ArrayList<FormVideo> formVideos = new ArrayList<>();
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

    public void onVideoRecordingCompleted() {
        if (mUuid != null && mVideoLocalPath != null) {

//            FormVideo formVideo = new FormVideo(mUuid.toString(), mKey, mDatatype,
//                    mInitialTimestamp, mVideoLocalPath, mAppId, mUserId
//                    , mProjectId);

            byte[] empty = new byte[0];

            FormMedia formVideo = new FormMedia();
            formVideo.setmUUID(mUuid.toString());
            formVideo.setMediaClickTimeStamp(System.currentTimeMillis());
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
            formVideo.setMediaUploadTimestamp(Constants.UPLOAD_TIMESTAMP_DEFAULT);
            formVideo.setMediaUploadRetries(Constants.DEFAULT_RETRIES);
            formVideo.setMediaFileExtension(Constants.VIDEO_EXT);
            formVideo.setFormSubmissionTimestamp(Constants.FORM_SUBMISSION_TIMESTAMP_DEFAULT);
            formVideo.setMediaRequestStatus(MediaRequestStatus.NEW_STATUS.getValue());
            formVideo.setmBitmap(empty);


            Intent data = new Intent();
            ArrayList<FormMedia> formVideos = new ArrayList<>();
            formVideos.add(formVideo);
            data.putParcelableArrayListExtra("formVideos", formVideos);
            data.putExtra("videouuids", mUuid);
            setResult(RESULT_OK, data);
            finish();
        }
    }
}

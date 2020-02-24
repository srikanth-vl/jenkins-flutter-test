package com.vassar.unifiedapp.camera2;

import android.content.pm.ActivityInfo;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.OrientationEventListener;
import android.widget.Toast;

import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.application.UnifiedAppApplication;
import com.vassar.unifiedapp.model.FormMedia;
import com.vassar.unifiedapp.model.GpsValidation;
import com.vassar.unifiedapp.model.ProjectIconInfo;
import com.vassar.unifiedapp.ui.BaseActivity;
import com.vassar.unifiedapp.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;

public class NewCamera2Activity extends BaseActivity {

    private String mDatatype;
    private int mMaxImages;
    private String mProjectId;
    private String mAppId;
    private String mUserId;
    private ArrayList<String> uuids;
    private GpsValidation gpsValidation;
    private HashMap<String, String> submittedFieldFromIntent = new HashMap<>();
    private String mUIType;
    private NewCamera2BasicFragment camera2BasicFragment;


    public ArrayList<FormMedia> mFormMedia = new ArrayList<>();
    //OrientationEventListener
    private OrientationEventListener myOrientationEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_camera_2);

//        Toast.makeText(this, Constants.EDU_CAMERA_ALERT, Toast.LENGTH_LONG).show();

        ((UnifiedAppApplication)getApplication()).initializeLocation();

        if (savedInstanceState == null) {
            // New instance of the activity
            mDatatype = getIntent().getStringExtra("datatype");
            mMaxImages = getIntent().getIntExtra("max", Constants.DEFAULT_MAX_IMAGES);
            mProjectId = getIntent().getStringExtra("projectId");
            mAppId = getIntent().getStringExtra("appId");
            mUserId = getIntent().getStringExtra("userId");
            uuids = new ArrayList<>();
            uuids.addAll(getIntent().getStringArrayListExtra("uuids"));
            gpsValidation = getIntent().getParcelableExtra("gps_validation");
            submittedFieldFromIntent = (HashMap<String, String>) getIntent().getSerializableExtra("submittedFields");
            mUIType = getIntent().getStringExtra("uitype");


            Bundle bundle = new Bundle();
            bundle.putString("datatype", mDatatype);
            bundle.putInt("max", mMaxImages);
            bundle.putString("projectId", mProjectId);
            bundle.putString("appId", mAppId);
            bundle.putString("userId", mUserId);
            bundle.putStringArrayList("uuids", uuids);
            bundle.putParcelable("gps_validation", gpsValidation);
            bundle.putSerializable("submittedFields", submittedFieldFromIntent);
            bundle.putString("uitype", mUIType);

            NewCamera2BasicFragment newCameraFragment = new NewCamera2BasicFragment();
            newCameraFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, newCameraFragment)
                    .commit();
            camera2BasicFragment = newCameraFragment;
        } else {
            // Activity is being restored, orientation is changing
            mDatatype = savedInstanceState.getString("datatype");
            mMaxImages = savedInstanceState.getInt("max");
            mProjectId = savedInstanceState.getString("projectId");
            mAppId = savedInstanceState.getString("appId");
            mUserId = savedInstanceState.getString("userId");
            mUIType = savedInstanceState.getString("uitype");
            uuids = new ArrayList<>();
            if (savedInstanceState.getStringArrayList("uuids") != null &&
                    !savedInstanceState.getStringArrayList("uuids").isEmpty()) {
                uuids.addAll(savedInstanceState.getStringArrayList("uuids"));
            }
            mFormMedia = new ArrayList<>();
            if (savedInstanceState.getParcelableArrayList("formMedia") != null &&
                    !savedInstanceState.getParcelableArrayList("formMedia").isEmpty()) {
                mFormMedia.addAll(savedInstanceState.getParcelableArrayList("formMedia"));
            }
            gpsValidation = savedInstanceState.getParcelable("gps_validation");
            submittedFieldFromIntent = (HashMap<String, String>) savedInstanceState.getSerializable("submittedFields");
        }

        //calling method for fining orientation state from accelerometer
        setOrientationListenerAndRotateScreen();
    }

    //method for fining orientation state from accelerometer
    private void setOrientationListenerAndRotateScreen() {
        myOrientationEventListener = new OrientationEventListener(getApplicationContext(), SensorManager.SENSOR_DELAY_FASTEST) {

            @Override
            public void onOrientationChanged(int orientation) {
                //Log.e("Oientation detector : ","angle "+orientation);

                if (orientation == 0 ) {
                   // Log.e("Oientation detector : ","Portrait");
                    //Getting the auto-rotation state from System setting
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
                else if (orientation == 180 ) {
                    //Log.e("Oientation detector : ","Portrait");

                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);

                }
                else if (orientation == 90) {
                    //Log.e("Oientation detector : ","landscape");

                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                }
                else if (orientation == 270) {

                    //Log.e("Oientation detector : ","landscape");

                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }

                // Previous code
//                if (orientation == 0 ) {
//                    Log.e("Oientation detector : ","Portrait");
//                    //Getting the auto-rotation state from System setting
//                    if (android.provider.Settings.System.getInt(getContentResolver(),Settings.System.ACCELEROMETER_ROTATION, 0) == 1){
//                        //changing the orientation programmatically when auto rotation is ON
//                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//                    }
//                    else {
//                        //changing the orientation programmatically when auto rotation is OFF
//                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//                    }
//                }
//                else if (orientation == 180 ) {
//                    Log.e("Oientation detector : ","Portrait");
//
//                    if (android.provider.Settings.System.getInt(getContentResolver(),Settings.System.ACCELEROMETER_ROTATION, 0) == 1){
//                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
//                    }
//                    else{
//                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
//                    }
//
//                }
//                else if (orientation == 90) {
//                    Log.e("Oientation detector : ","landscape");
//
//                    if (android.provider.Settings.System.getInt(getContentResolver(),Settings.System.ACCELEROMETER_ROTATION, 0) == 1){
//                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
//                    }
//                    else{
//                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
//                    }
//                }
//                else if (orientation == 270) {
//
//                    Log.e("Oientation detector : ","landscape");
//
//                    if (android.provider.Settings.System.getInt(getContentResolver(),Settings.System.ACCELEROMETER_ROTATION, 0) == 1){
//                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//                    }
//                    else{
//
//                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//                    }
//                }
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("datatype", mDatatype);
        outState.putInt("max", mMaxImages);
        outState.putString("projectId", mProjectId);
        outState.putString("appId", mAppId);
        outState.putString("userId", mUserId);
        outState.putStringArrayList("uuids", uuids);
        outState.putParcelableArrayList("formMedia", mFormMedia);
        outState.putParcelable("gps_validation", gpsValidation);
        outState.putSerializable("submittedFields", submittedFieldFromIntent);
        outState.putString("uitype", mUIType);
    }

    @Override
    public void onBackPressed() {

        if (camera2BasicFragment != null && camera2BasicFragment.isTakingPictureValue()){
            return;
        }

        if (camera2BasicFragment != null){
            camera2BasicFragment.disableCaptureButton();
        }
        super.onBackPressed();
    }
}

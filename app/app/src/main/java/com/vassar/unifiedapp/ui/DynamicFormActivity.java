package com.vassar.unifiedapp.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.Utils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DynamicFormActivity extends AppCompatActivity {

    private ProgressBar mProgressBar;
    private FragmentManager mFragmentManager;
    public boolean moveToHome = false;

    private Map<String, JSONObject> mSubmissionArray = new HashMap<>();

    public void addToSubmissionArray(String key, JSONObject value) {
        if (mSubmissionArray != null) {
            mSubmissionArray.put(key, value);
        }
    }

    public JSONObject getFromSubmissionArray(String key) {
        if (mSubmissionArray != null) {
            return mSubmissionArray.get(key);
        }
        return null;
    }

    public boolean presentInSubmissionArray(String key) {
        boolean isPresent = false;
        if (mSubmissionArray != null) {
            if (mSubmissionArray.get(key) != null) {
                isPresent = true;
            }
        }

        return isPresent;
    }

    public Map<String, JSONObject> getSubmissionArray() {
        return mSubmissionArray;
    }

    public void setSubmissionArray(Map<String, JSONObject> submissionArray) {
        this.mSubmissionArray = submissionArray;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_form);

        // FORM
        // 1. Should show toolbar??
        // 2. What renderer should be used??
        // 3. What is the action that is to be performed on the data??
        //      -- NormalForm(FormActivity)
        //      -- ProjectSpecificForms(GroupRenderer)
        //      -- OnStartupActivity

        // FRAGMENT
        // Should only have the form as parameter
        // Should render the form

        mProgressBar = (ProgressBar) findViewById(R.id.dynamic_form_progress);
        mFragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            // The activity is being loaded for the first time

            if (getIntent().hasExtra(Constants.MOVE_TO_HOME)) {
                moveToHome = getIntent().getBooleanExtra(Constants.MOVE_TO_HOME, false);
            }

            // By default - Toolbar is visible
            boolean shouldShowToolbar = true;
            if (getIntent().hasExtra(Constants.FORM_SHOULD_SHOW_TOOLBAR)) {
                shouldShowToolbar = getIntent()
                        .getBooleanExtra(Constants.FORM_SHOULD_SHOW_TOOLBAR, shouldShowToolbar);
            }
            initializeToolbar(getResources().getString(R.string.app_name), shouldShowToolbar);
        }

        // Check for the phone state permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean permission = checkForReadPhoneStatePermission();
            if (permission) {
                // Permission given
                if (savedInstanceState == null) {
                    loadFormFragment();
                }
            } else {
                // Permission is not granted
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        Constants.REQUEST_READ_PHONE_STATE);
            }
        } else {
            if (savedInstanceState == null) {
                loadFormFragment();
            }
        }
    }

    private void initializeToolbar(String text, boolean showToolbar) {
        Toolbar toolbar = findViewById(R.id.dynamic_form_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(text);
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        if (showToolbar) {
            // Show toolbar
        } else {
            // Hide toolbar
            toolbar.setVisibility(View.GONE);
        }
    }

    public void showProgressBar() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    public void hideProgressBar() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void loadFormFragment() {
        // Load form fragment
        OnStartupActivityFragment fragment = new OnStartupActivityFragment();
        mFragmentManager.beginTransaction().add(R.id.dynamic_form_activity_root, fragment).commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.REQUEST_READ_PHONE_STATE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Utils.logInfo(LogTags.RUNTIME_PERMISSION, "Granted");
                    loadFormFragment();
                } else {
                    Utils.logInfo(LogTags.RUNTIME_PERMISSION, "Denied");
                }
                break;
        }
    }

    public boolean checkForReadPhoneStatePermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) !=
                PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            return false;
        }
        return true;
    }
}

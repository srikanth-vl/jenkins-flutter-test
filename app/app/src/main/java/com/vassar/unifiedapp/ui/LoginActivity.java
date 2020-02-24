package com.vassar.unifiedapp.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.vassar.unifiedapp.BuildConfig;
import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.api.NewAuthenticationService;
import com.vassar.unifiedapp.api.NewImageDownloaderService;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.err.UAAppErrorCodes;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.AppMetaData;
import com.vassar.unifiedapp.model.AuthenticationResponse;
import com.vassar.unifiedapp.model.IncomingImage;
import com.vassar.unifiedapp.model.OnStartupAction;
import com.vassar.unifiedapp.model.UserMetaData;
import com.vassar.unifiedapp.newflow.AppStartupThread2;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.PropertyReader;
import com.vassar.unifiedapp.utils.Utils;

import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import okhttp3.internal.Util;

import static com.vassar.unifiedapp.context.UAAppContext.getInstance;

public class LoginActivity extends BaseActivity {

    private EditText mUsername;
    private EditText mPassword;
    private Context context;
    private Button mSubmit;
    private TextView mForgotPassword;
    private TextView mClickHere;
    private TextView appVersion;
    private AppCompatCheckBox show_password;
    private ProgressBar mProgressBar;
    public static boolean isRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = this;

        Utils.logInfo(LogTags.LOGIN_SCREEN, "Start of Login Screen");

        initializeComponents();
        initializeViews();
        defineClickListeners();
        isRunning = true;
        clearStartUpDataPreferenceIfExists();
    }

    private void initializeComponents() {
        initComponents(this);
        hideActionBar(this);

        Utils.logInfo(LogTags.LOGIN_SCREEN, "Components initialized + toolbar hidden");
    }

    private void initializeViews() {
        IncomingImage loginIcon;
        AppMetaData appMetaData = UAAppContext.getInstance().getAppMDConfig();
        if (appMetaData != null && appMetaData.mSplashScreenProperties != null && appMetaData.mSplashScreenProperties.mLoginIcon != null && !appMetaData.mSplashScreenProperties.mLoginIcon.isEmpty()) {
            String url = appMetaData.mSplashScreenProperties.mLoginIcon;
            loginIcon = UAAppContext.getInstance().getDBHelper()
                    .getIncomingImageWithUrl(url);
        } else {
            loginIcon = UAAppContext.getInstance().getDBHelper().getImage(Constants.LOGIN_ICON_IMAGE);
        }

        mProgressBar = (ProgressBar) findViewById(R.id.login_progress);
        mForgotPassword = (TextView) findViewById(R.id.activity_login_forgot);
        mClickHere = (TextView) findViewById(R.id.activity_login_click_here);
        mUsername = (EditText) findViewById(R.id.activity_login_username);
        mPassword = (EditText) findViewById(R.id.activity_login_password);
        mSubmit = (Button) findViewById(R.id.activity_login_submit);
        appVersion = (TextView) findViewById(R.id.app_version);

        appVersion.setText("Version : " + BuildConfig.VERSION_NAME);

        ImageView loginImage = (ImageView) findViewById(R.id.activity_login_imageview);
        show_password = (AppCompatCheckBox) findViewById(R.id.show_password_login);

        if(PropertyReader.getProperty("USERNAME_TYPE").equalsIgnoreCase("phone")){
            mUsername.setInputType(InputType.TYPE_CLASS_PHONE);
            Utils.getInstance().setEditTextMaxLength(mUsername, 10);
        }

        if (loginIcon != null) {
            if (loginIcon.getImageLocalPath() == null || loginIcon.getImageLocalPath().isEmpty()) {
                Utils.logError(UAAppErrorCodes.IMAGE_DOWNLOAD_ERROR, "No local path available for the image");

                // Setting the default image, while the icon downloads
                loginImage.setImageResource(R.drawable.login_image);

                // Image download failed the first time, download again
                NewImageDownloaderService newImageDownloaderService = new NewImageDownloaderService(loginIcon);
                newImageDownloaderService.execute();
            } else {
                File imgFile = new File(loginIcon.getImageLocalPath());
                if (imgFile.exists()) {
                    if (imgFile.length() == 0) {
                        Utils.logError(UAAppErrorCodes.IMAGE_DOWNLOAD_ERROR, "Image download failed");

                        // Setting the default image, while the icon downloads
                        loginImage.setImageResource(R.drawable.login_image);

                        // Image download failed the first time, download again
                        NewImageDownloaderService newImageDownloaderService = new NewImageDownloaderService(loginIcon);
                        newImageDownloaderService.execute();
                    } else {
                        Picasso.get().load(imgFile).into(loginImage);
                    }
                } else {
                    // Default splash icon loaded
                    loginImage.setImageResource(R.drawable.login_image);
                }
            }
        } else {
            // Default background loaded
            loginImage.setImageResource(R.drawable.login_image);
        }

        if(!checkIfAppSupportsProperty("FORGOT_PASSWORD")) {
            mForgotPassword.setVisibility(View.GONE);
            mClickHere.setVisibility(View.GONE);

        }

        Utils.logInfo(LogTags.LOGIN_SCREEN, "Views initialized");
    }

    private void defineClickListeners() {

        // Setting the click listener for the show password checkbox
        addShowPasswordClickListener();

        // Setting the click listener for the submit button
        addSubmitButtonClickListener();

        // Setting the click listener for the submit button
        addForgotPasswordClickListener();
    }

    private void addShowPasswordClickListener() {
        show_password.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (!isChecked) {
                    // hide password
                    mPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                } else {
                    // show password
                    mPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                mPassword.setSelection(mPassword.getText().length());
            }
        });
    }

    private void addSubmitButtonClickListener() {

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!mUsername.getText().toString().isEmpty()
                        && !mPassword.getText().toString().isEmpty()) {

                    boolean loginValue[] = new boolean[1];

                    //Initializing separate thread for Login authentication
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            if (!Thread.currentThread().isInterrupted()) {

                                runOnUiThread(() -> mProgressBar.setVisibility(ProgressBar.VISIBLE));
                                runOnUiThread(() -> getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE));

                                if (Utils.getInstance().isOnline(context)) {
                                    // User is online
                                    loginValue[0] = onlineLogin(mUsername.getText().toString(), mPassword.getText().toString());
                                } else {
                                    //User is offline
                                    UserMetaData userMetaData = mDBHelper.getUserMeta(mUsername.getText().toString());
                                    loginValue[0] = offlineLogin(mUsername.getText().toString(), mPassword.getText().toString(), userMetaData);
                                }

                                UAAppContext.getInstance().setUserID(mUsername.getText().toString());

                                if (loginValue[0]) {
                                    // Initiating AST2 and waiting here for response
                                    AppStartupThread2 appStartupThread2 = new AppStartupThread2();
                                    try {
                                        appStartupThread2.execute().get();
                                    } catch (ExecutionException | InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    Utils.logInfo(LogTags.LOGIN_SCREEN, "COMPLETED APP STARTUP THREAD 2");

                                    // Dismissing throbber
                                    runOnUiThread(() -> mProgressBar.setVisibility(ProgressBar.INVISIBLE));

                                    // Either move to home, or perform a set of tasks
                                    callOnStartUp();
                                } else {

                                    return;

                                }
                            }
                        }
                    }).start();

                } else {

                    if (mUsername.getText().toString().isEmpty())
                        showErrorMessageAndFinishActivity(getResources().getString(R.string.MISSING_USERNAME), false);

                    else if (mPassword.getText().toString().isEmpty())
                        showErrorMessageAndFinishActivity(getResources().getString(R.string.MISSING_PASSWORD), false);
                }

                runOnUiThread(() -> mProgressBar.setVisibility(ProgressBar.INVISIBLE));
                runOnUiThread(() -> getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE));
            }
        });
    }

    private void addForgotPasswordClickListener() {
        mClickHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordScreen.class);
                startActivity(intent);
            }
        });
    }

    private boolean onlineLogin(String username, String password) {
        // The user is online, network login
        final AuthenticationResponse authenticationResponse;
        NewAuthenticationService newAuthenticationService = new
                NewAuthenticationService(username, password);
        try {
            authenticationResponse = newAuthenticationService.execute().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            //authenticationResponse = null;

            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    mProgressBar.setVisibility(View.INVISIBLE);
                    showErrorMessageAndFinishActivity(UAAppErrorCodes.SERVER_FETCH_ERROR, false);
                }
            });

            //showErrorMessageAndFinishActivity(UAAppErrorCodes.SERVER_FETCH_ERROR, false);
            Utils.logError(UAAppErrorCodes.SERVER_FETCH_ERROR, "Error Authenticating from server");
            return false;
        }

        if (authenticationResponse != null) {
            if (authenticationResponse.isAuthenticated()) {
                if (authenticationResponse.getUserMetaData() != null) {
                    // Successful login
                    return true;
                } else {

                    this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            mProgressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.DATA_INITIALIZATION_FAILED), Toast.LENGTH_SHORT).show();
                        }
                    });
                    // Error - no user meta data for successful login
                    Utils.logError(LogTags.USER_DATA_ERROR, "No User Data after successful login" +
                            "-- cannot continue");
                    return false;
                }
            } else {
                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        mProgressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(LoginActivity.this, authenticationResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                return false;
            }
        }
        return false;
    }

    private boolean offlineLogin(String username, String password, UserMetaData userMetaData) {
        boolean successfulLogin = false;
        if (userMetaData != null) {
            // Offline data available
            if (username.compareTo(userMetaData.mUserId) == 0 && password.compareTo(userMetaData.mPassword) == 0) {
                successfulLogin = true;
                updateOfflineLoginPreferences(username);
            } else {
                // Offline credentials do not match
                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        mProgressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.INCORRECT_CREDENTIALS), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        } else {
            // Offline data does'nt exist for this user
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    mProgressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.NO_OFFLINE_USER_DATA), Toast.LENGTH_SHORT).show();
                }
            });
        }
        return successfulLogin;
    }

    private void updateOfflineLoginPreferences(String username) {
        SharedPreferences.Editor editor = mAppPreferences.edit();
        editor.putString(Constants.USER_ID_PREFERENCE_KEY, username);
        editor.putBoolean(Constants.USER_IS_LOGGED_IN_PREFERENCE_KEY, true);
        UserMetaData userMetaData = mDBHelper.getUserMeta(username);
        editor.putString(Constants.USER_TOKEN_PREFERENCE_KEY, userMetaData.mToken);
        editor.apply();
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void callOnStartUp(){

        if (UAAppContext.getInstance().getAppMDConfig().onStartUp != null &&
                UAAppContext.getInstance().getAppMDConfig().onStartUp.size() > 0) {

            List<OnStartupAction> startUpData = UAAppContext.getInstance()
                    .getAppMDConfig().onStartUp;

            for (OnStartupAction onStartupAction : startUpData) {
                String type = onStartupAction.getType();
                switch (type) {
                    case Constants.ON_STARTUP_DEFAULT:
                        moveToHomeScreen();
                        break;

                    case Constants.ON_STARTUP_SERVICE :
                        // TODO : Call Service
                        break;

                    case Constants.ON_STARTUP_ACTIVITY :
                        Intent i = new Intent(this, DynamicFormActivity.class);
                        i.putExtra(Constants.FORM_SHOULD_SHOW_TOOLBAR, false);
                        i.putExtra(Constants.MOVE_TO_HOME, true);
                        startActivity(i);
                        this.finish();
                        break;

                    default:
                        moveToHomeScreen();
                        break;
                }
            }
        } else {
            moveToHomeScreen();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isRunning = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isRunning = false;
    }

    public void clearStartUpDataPreferenceIfExists() {

        SharedPreferences sharedPreferences = getSharedPreferences(Constants.APP_PREFERENCES_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.ON_STARTUP_DATA_EXISTS, false);
        editor.putString(Constants.ON_STARTUP_DATA, "");
        editor.apply();
    }
}
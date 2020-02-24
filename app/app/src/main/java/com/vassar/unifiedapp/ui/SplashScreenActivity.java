package com.vassar.unifiedapp.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.api.NewImageDownloaderService;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.err.UAAppErrorCodes;
import com.vassar.unifiedapp.model.AppMetaData;
import com.vassar.unifiedapp.model.AppStartupThreadResponse;
import com.vassar.unifiedapp.model.ConfigFile;
import com.vassar.unifiedapp.model.IncomingImage;
import com.vassar.unifiedapp.newflow.AppStartupThread;
import com.vassar.unifiedapp.newflow.AppStartupThread2;
import com.vassar.unifiedapp.utils.CompareAppVersionUtil;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class SplashScreenActivity extends BaseActivity {

    Context context;
    private ImageView mSplashBackground;
    private ImageView mSplashIcon;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        mProgressBar = findViewById(R.id.splash_progress);
        mSplashBackground = findViewById(R.id.splash_background);
        mSplashIcon = findViewById(R.id.splash_icon);

        initComponents(this);
        hideActionBar(this);

        mProgressBar.setVisibility(View.VISIBLE);

        //for update the application
       /* if (Constants.FORCE_UPDATE  && Utils.getInstance().isOnline(context)){
            checkForForceUpdate(this);
        }else{
            // Getting the WRITE_TO_EXTERNAL permission
            // If the SDK is >= Marshmellow, runtime permissions required
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkForPermission();
            } else {
                // Runtime permissions not required
                initializeComponents();
            }
        }*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkForPermission();
        } else {
            // Runtime permissions not required
            initializeComponents();
        }
    }

    /**
     * getting current version of the application and passing to ForceUpdateAsync class
     *
     * @param context
     */
    private void checkForForceUpdate(Context context,  AppStartupThreadResponse response) {
        String currentVersion = "";
        try {
            currentVersion = getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            Log.d("Current Version", "::" + currentVersion);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        //Initialisation of AsyncTask class
        new ForceUpdateAsync(context, currentVersion, response).execute();
    }

    public void checkForPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Constants.PERMISSION_WRITE_EXTERNAL_STORAGE);
        } else {
            // Permission is granted
            initializeComponents();
        }
    }

    public void initializeComponents() {
        // Load initial properties related only to the App (nothing project specific)
//        UniappControllerHelper.getInstance().initProperties(getApplication());

        // Initializing the views on the Splash Screen
        initializeSplashViews();
//        callDelay();

        // Create AppStartup thread
        AppStartupThreadResponse response = null;
        AppStartupThread appStartupThread = new AppStartupThread();
        try {
            response = appStartupThread.execute().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            response = new AppStartupThreadResponse(false, false);
            response.setCompleted(false);
        }

        // Call delay here
//        long splashDuration = getSplashDuration();

//        System.out.println("XXXXXXXXXXXXXXXXXXXXXXX SPLASH " +splashDuration);
//
//        SystemClock.sleep(splashDuration);

//        long t= System.currentTimeMillis();
//        long end = t+splashDuration;
//        while(System.currentTimeMillis() < end) {
        // do something
        // pause to avoid churning
//            Utils.logInfo("delay", "delay at splash");
//        }

        if (response == null || !response.isCompleted()) {
            Utils.logError(UAAppErrorCodes.SERVER_FETCH_ERROR, "AsyncTask failed for authenticating");
            showErrorMessageAndFinishActivity(getResources().getString(R.string.SOMETHING_WENT_WRONG), true);
        } else {
            // The thread was completed successfully
            mProgressBar.setVisibility(View.INVISIBLE);

            // 1. Fetch appMetaData from the DB
            // 2. Check if force update is allowed for this super app from the appMetaData
            // 3. If appMDConfig is not available, continue without checking for force update
            // 4. Check for force update only if it is allowed for super app in appMDConfig
            // 5. Cannot check for force update, if no network available. For this, all
            // submissions contain app versions.
            AppMetaData appMetaConfig =  UAAppContext.getInstance().getAppMDConfig();


                if (appMetaConfig != null && appMetaConfig.mEnableForceUpdate != null && appMetaConfig.mEnableForceUpdate) {

                    if (Utils.getInstance().isOnline(context)) {

                        checkForForceUpdate(this, response);
                        mProgressBar.setVisibility(View.VISIBLE);
                    } else {
                        contiueToLoginOrHome(response);
                    }
                }else{
                    contiueToLoginOrHome(response);
                }

                // Dismissing throbber
//                mProgressBar.setVisibility(View.INVISIBLE);
//                // Moving to home screen
//                moveToHomeScreen();

        }
    }

    private void contiueToLoginOrHome(AppStartupThreadResponse response) {

        // The thread was completed successfully
        mProgressBar.setVisibility(View.INVISIBLE);

        if (response.isLoggedIn()) {
            // The user is logged in
            // Start the AppStartupThread2 and then redirect to home on
            // Successful completion
            AppStartupThread2 appStartupThread2 = new AppStartupThread2();
            try {
                appStartupThread2.execute().get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Dismissing throbber
            mProgressBar.setVisibility(View.INVISIBLE);
            // Moving to home screen
            moveToHomeScreen();

        } else {
            // The user is not logged in
            // They are redirected to the home page
            // AppStartupThread2 is called after successful user authentication
            moveToLoginScreen();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions
            , int[] grantResults) {
        switch (requestCode) {
            case Constants.PERMISSION_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted!
                    initializeComponents();
                } else {
                    // permission denied!
                    showErrorMessageAndFinishActivity(getResources().getString(R.string.CANNOT_USE_WITHOUT_PERMISSION), true);
                }
                return;
            }
        }
    }

    private void initializeSplashViews() {
        addSplashIcon();

        addSplashBackground();
    }

    private void addSplashIcon() {
        IncomingImage splashIcon;
        AppMetaData appMetaData = UAAppContext.getInstance().getAppMDConfig();
        if (appMetaData != null && appMetaData.mSplashScreenProperties != null &&
                appMetaData.mSplashScreenProperties.mSplashIconUrl != null &&
                !appMetaData.mSplashScreenProperties.mSplashIconUrl.isEmpty()) {
            String url = appMetaData.mSplashScreenProperties.mSplashIconUrl;
            splashIcon = UAAppContext.getInstance().getDBHelper()
                    .getIncomingImageWithUrl(url);
        } else {
            splashIcon = mDBHelper.getImage(Constants.SPLASH_IMAGE_FOREGROUND);
        }

        if (splashIcon != null) {
            if (splashIcon.getImageLocalPath() == null || splashIcon.getImageLocalPath().isEmpty()) {
                Utils.logError(UAAppErrorCodes.IMAGE_DOWNLOAD_ERROR, "No local path available for the image");

                // Setting the default image, while the icon downloads
//                mSplashIcon.setImageResource(R.drawable.app_logo);
                Picasso.get().load(R.drawable.app_logo).into(mSplashIcon);

                // Image download failed the first time, download again
                NewImageDownloaderService newImageDownloaderService = new NewImageDownloaderService(splashIcon);
                newImageDownloaderService.execute();
            } else {
                File imgFile = new File(splashIcon.getImageLocalPath());

                if (imgFile.exists()) {
                    if (imgFile.length() == 0) {
                        Utils.logError(UAAppErrorCodes.IMAGE_DOWNLOAD_ERROR, "Image download failed");

                        // Setting the default image, while the icon downloads
//                        mSplashIcon.setImageResource(R.drawable.app_logo);
                        Picasso.get().load(R.drawable.app_logo).into(mSplashIcon);

                        // Image download failed the first time, download again
                        NewImageDownloaderService newImageDownloaderService = new NewImageDownloaderService(splashIcon);
                        newImageDownloaderService.execute();
                    } else {
                        // Image available
                        Picasso.get().load(imgFile).into(mSplashIcon);
                    }
                } else {
                    // Default splash icon loaded
//                    mSplashIcon.setImageResource(R.drawable.app_logo);
                    Picasso.get().load(R.drawable.app_logo).into(mSplashIcon);
                }
            }
        } else {
            // Default splash icon loaded
//            mSplashIcon.setImageResource(R.drawable.app_logo);
            Picasso.get().load(R.drawable.app_logo).into(mSplashIcon);
        }
    }

    private void addSplashBackground() {
        IncomingImage splashBackground = mDBHelper.getImage(Constants.SPLASH_IMAGE_BACKGROUND);
        if (splashBackground != null) {
            if (splashBackground.getImageLocalPath() == null || splashBackground.getImageLocalPath().isEmpty()) {
                Utils.logError(UAAppErrorCodes.IMAGE_DOWNLOAD_ERROR, "No local path available for the image");
                // Setting the default image, while the icon downloads
//                mSplashBackground.setImageResource(R.drawable.login_background);
                Picasso.get().load(R.drawable.login_background).into(mSplashBackground);
                // Image download failed the first time, download again
                NewImageDownloaderService newImageDownloaderService = new NewImageDownloaderService(splashBackground);
                newImageDownloaderService.execute();
            } else {
                File imgFile = new File(splashBackground.getImageLocalPath());
                if (imgFile.exists()) {
                    if (imgFile.length() == 0) {
                        Utils.logError(UAAppErrorCodes.IMAGE_DOWNLOAD_ERROR, "Image download failed");

                        // Setting the default image, while the icon downloads
//                        mSplashBackground.setImageResource(R.drawable.login_background);
                        Picasso.get().load(R.drawable.login_background).into(mSplashBackground);
                        // Image download failed the first time, download again
                        NewImageDownloaderService newImageDownloaderService = new NewImageDownloaderService(splashBackground);
                        newImageDownloaderService.execute();
                    } else {
                        Picasso.get().load(imgFile).into(mSplashBackground);
                    }
                } else {
                    // Default splash icon loaded
//                    mSplashBackground.setImageResource(R.drawable.login_background);
                    Picasso.get().load(R.drawable.login_background).into(mSplashBackground);
                }
            }
        }
//        else {
//            // Default background loaded
//            mSplashBackground.setImageResource(R.drawable.login_background);
//        }
    }

//    private void callDelay() {
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//
//                if (Utils.getInstance().isOnline(SplashScreenActivity.this)) {
//                    if (mIsUserLoggedIn) {
//                        // User has network and is logged in
//                        ifNetworkAndLoggedIn();
//                    } else {
//                        // User has network and is logged out
//                        ifNetworkAndLoggedOut();
//                    }
//                } else {
//                    if (mIsUserLoggedIn) {
//                        // User doesn't have network and is logged in
//                        ifNoNetworkAndLoggedIn();
//                    } else {
//                        // User doesn't have network and is logged out
//                        ifNoNetworkAndLoggedOut();
//                    }
//                }
//            }
//        }, mDelay);
//    }

    private void callDelay() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

            }
        }, 1000);
    }

    private long getSplashDuration() {
        long splashDuration;
        AppMetaData appMetaData = UAAppContext.getInstance().getAppMDConfig();
        if (appMetaData != null) {
            if (appMetaData.mSplashScreenProperties != null &&
                    appMetaData.mSplashScreenProperties.mSplashDuration != null &&
                    !appMetaData.mSplashScreenProperties.mSplashDuration.isEmpty()) {
                splashDuration = Long.parseLong(appMetaData.mSplashScreenProperties.mSplashDuration);
            } else {
                // Default splash duration
                splashDuration = Constants.SPLASH_DURATION_DEFAULT;
            }
        } else {
            // Default splash duration
            splashDuration = Constants.SPLASH_DURATION_DEFAULT;
        }
        return splashDuration;
    }

    /**
     * AsyncTask class for check updated version from the playstore
     */
    private class ForceUpdateAsync extends AsyncTask<Void, String, String> {

        private Context context;
        private String currentVersion;
        private AppStartupThreadResponse response;

        public ForceUpdateAsync(Context context, String currentVersion, AppStartupThreadResponse response) {
            this.context = context;
            this.currentVersion = currentVersion;
            this.response = response;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String newVersion = null;
            try {
                //HTML Parsing of the data coming from the url
                Document document = Jsoup.connect(context.getString(R.string.play_store_base_url) + context.getPackageName() + (context.getString(R.string.play_store_endpoint_lang)))
                        .timeout(30000)
                        .userAgent(context.getString(R.string.play_store_user_agent))
                        .referrer(context.getString(R.string.play_store_referred_url))
                        .get();
                if (document != null) {
                    Elements element = document.getElementsContainingOwnText(context.getString(R.string.play_store_current_version));
                    for (Element ele : element) {
                        if (ele.siblingElements() != null) {
                            Elements sibElemets = ele.siblingElements();
                            for (Element sibElemet : sibElemets) {
                                newVersion = sibElemet.text();
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return newVersion;
        }
        @Override
        protected void onPostExecute(String onlineVersion) {
            super.onPostExecute(onlineVersion);
            AppMetaData appMetaData = UAAppContext.getInstance().getAppMDConfig();
            if (appMetaData!= null && appMetaData.mEnableForceUpdate && onlineVersion != null && !onlineVersion.isEmpty()) {

                int res=new CompareAppVersionUtil().compareAppVersion(currentVersion,onlineVersion);

                if ( res==0 || res==1) {
                    contiueToLoginOrHome(response);
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                    alertDialog.setTitle(context.getString(R.string.update));
                    alertDialog.setMessage(context.getString(R.string.new_update_is_available));
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.update), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.market_details_url) + context.getPackageName())));
                            } catch (android.content.ActivityNotFoundException anfe) {
                                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.play_store_base_url) + context.getPackageName())));

                            }
                        }
                    });

                    alertDialog.show();
                    alertDialog.setCancelable(false);
                    //disabling progressbar when update alert dialog is visible
                    mProgressBar.setVisibility(View.INVISIBLE);
                }


            } else {
                // App is currently not on Play Store
                contiueToLoginOrHome(response);

            }
            Log.d("update", "Current version " + currentVersion + "playstore version " + onlineVersion);
        }

    }
}

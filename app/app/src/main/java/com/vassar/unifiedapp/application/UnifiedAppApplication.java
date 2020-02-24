package com.vassar.unifiedapp.application;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
//import com.mapbox.mapboxsdk.Mapbox;
import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.Utils;

public class UnifiedAppApplication extends Application {

    public FusedLocationProviderClient mFusedLocationClient;
    public double mUserLatitude = Constants.DEFAULT_LATITUDE;
    public double mUserLongitude = Constants.DEFAULT_LONGITUDE;
    public float mAccuracy = Constants.DEFAULT_ACCURACY;
    public Location mLocation;
    public LocationRequest mLocationRequest;
    public LocationCallback mLocationCallback;
    public Task<Void> voidTask;
    private UAAppContext uaAppContext = UAAppContext.getInstance();
    public float mUserBearing = Constants.DEFAULT_BEARING;

    @Override
    public void onCreate() {
        super.onCreate();

        // Mapbox Access token
//        Mapbox.getInstance(getApplicationContext()
//                , getResources().getString(R.string.mapbox_access_token));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            }
        } else {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        }

        initializeLocation();
        startLocationUpdates();
        initProperties();
    }

    public void initializeLocation() {
        if (mFusedLocationClient == null) {
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mLocationRequest = new LocationRequest();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setFastestInterval(5000)
                    .setInterval(10000);

            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    Location loc = locationResult.getLastLocation();
                    if(loc!=null)
                    {
                        mLocation = loc;
                        mUserLatitude = loc.getLatitude();
                        mUserLongitude = loc.getLongitude();
                        if(loc.hasAccuracy())
                        {
                            mAccuracy = loc.getAccuracy();
                        } else{
                            mAccuracy = Constants.DEFAULT_ACCURACY;
                        }
                        if (loc.hasBearing()) {
                            mUserBearing = loc.getBearing();
                            Utils.logInfo("LOCATION HAS BEARING : ", String.valueOf(mUserBearing));
                        } else{
                            mUserBearing = Constants.DEFAULT_BEARING;
                        }
                    }

                }
            };
        }

    }

    public void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (mFusedLocationClient != null) {
            voidTask = mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        }
    }

    public void initProperties() {

        uaAppContext.setContext(this);

        // Initialize DBHelper
        uaAppContext.initDBHelper();

        // Initialize AppPreferences
        uaAppContext.initSharedPreferences();
    }
}

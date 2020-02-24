package com.vassar.unifiedapp.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

import com.vassar.unifiedapp.utils.Utils;

public class GPSChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LocationManager manager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            // Not enabled!
            Utils.getInstance().showLog("GPS", "Not enabled");
        } else {
            // Enabled
            Utils.getInstance().showLog("GPS", "Enabled");
        }
    }
}

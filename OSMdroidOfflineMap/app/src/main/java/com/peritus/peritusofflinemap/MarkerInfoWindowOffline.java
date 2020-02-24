package com.peritus.peritusofflinemap;

import android.util.Log;

import org.osmdroid.api.IMapView;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MarkerInfoWindowOffline extends BasicInfoWindowOffline {
    protected Marker mMarkerRef; //reference to the Marker on which it is opened. Null if none.


    public MarkerInfoWindowOffline(int layoutResId, MapView mapView) {
        super(layoutResId, mapView);
        //mMarkerRef = null;
    }

    /**
     * reference to the Marker on which it is opened. Null if none.
     * @return
     */
    public Marker getMarkerReference(){
        return mMarkerRef;
    }

    @Override public void onOpen(Object item) {
        super.onOpen(item);

        mMarkerRef = (Marker)item;
        if (mView==null) {
            Log.w(IMapView.LOGTAG, "Error trapped, MarkerInfoWindow.open, mView is null!");
            return;
        }

    }

    @Override public void onClose() {
        super.onClose();
        mMarkerRef = null;
        //by default, do nothing else
    }

}

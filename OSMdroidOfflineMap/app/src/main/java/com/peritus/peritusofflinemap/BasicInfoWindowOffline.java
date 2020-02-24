package com.peritus.peritusofflinemap;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import org.osmdroid.api.IMapView;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayWithIW;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import static org.osmdroid.views.overlay.infowindow.BasicInfoWindow.UNDEFINED_RES_ID;

public class BasicInfoWindowOffline extends InfoWindow {

    static int mTitleId = UNDEFINED_RES_ID;

    public BasicInfoWindowOffline(int layoutResId, MapView mapView) {
        super(layoutResId, mapView);
        if (mTitleId == UNDEFINED_RES_ID)
            setResIds(mapView.getContext());
        //default behavior: close it when clicking on the bubble:
        mView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_UP)
                    close();
                return true;
            }
        });
    }

    private static void setResIds(Context context) {
        String packageName = context.getPackageName(); //get application package name
        mTitleId = context.getResources().getIdentifier("id/bubble_title", null, packageName);
    }

    @Override
    public void onOpen(Object item) {


        OverlayWithIW overlay = (OverlayWithIW) item;
        String title = overlay.getTitle();
        if (title == null)
            title = "";
        if (mView == null) {
            Log.w(IMapView.LOGTAG, "Error trapped, BasicInfoWindow.open, mView is null!");
            return;
        }
        TextView temp = ((TextView) mView.findViewById(mTitleId /*R.id.title*/));

        if (temp != null) temp.setText(title);
        if (mView == null) {
            Log.w(IMapView.LOGTAG, "Error trapped, BasicInfoWindow.open, mView is null!");
            return;
        }


    }

    @Override
    public void onClose() {
        //by default, do nothing
    }
}
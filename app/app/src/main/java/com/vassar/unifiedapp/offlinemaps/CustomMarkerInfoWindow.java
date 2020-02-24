package com.vassar.unifiedapp.offlinemaps;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.model.Project;
import com.vassar.unifiedapp.ui.NavigationActivity;
import com.vassar.unifiedapp.ui.ProjectFormActivity;
import com.vassar.unifiedapp.ui.TabularFormActivity;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.PropertyReader;
import com.vassar.unifiedapp.utils.Utils;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

import java.util.ArrayList;
import java.util.List;

public class CustomMarkerInfoWindow extends MarkerInfoWindow {

    private Project mProject;
    private String mAppId;
    private Activity mActivity;

    private LinearLayout mRootView;

    public CustomMarkerInfoWindow(MapView mapView, Project project, String appId, Activity activity) {
        super(R.layout.map_view_overlay_layout, mapView);

        mProject = project;
        mAppId = appId;
        mActivity = activity;

        mRootView = (LinearLayout) mView.findViewById(R.id.map_overlay_root_layout);

        ImageButton formButton = (ImageButton) mView.findViewById(R.id.edit_form);
        ImageButton navButton = (ImageButton) mView.findViewById(R.id.get_directions);
//        ImageButton cancelButton = (ImageButton) mView.findViewById(R.id.cancel);

        TextView projectName = (TextView) mView.findViewById(R.id.project_name);
        projectName.setText(mProject.mProjectName);

        formButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =  null;
                if (PropertyReader.getProperty("SUPER_APP_ID")
                        .equalsIgnoreCase("4ca24624-3841-3d1f-b499-44903cbe829c") || PropertyReader.getProperty("SUPER_APP_ID")
                        .equalsIgnoreCase("9e49254b-d85e-11e9-beb7-b5d18d261495") || PropertyReader.getProperty("SUPER_APP_ID")
                        .equalsIgnoreCase("6b31aba3-eb49-4fa4-8f3c-3f764b8d17e5")) {
                intent = new Intent(activity, TabularFormActivity.class); } else {
                    intent = new Intent(activity, ProjectFormActivity.class);
                }

                intent.putExtra(Constants.PROJECT_FORM_INTENT_APP_ID, mAppId);
                intent.putExtra(Constants.PROJECT_FORM_INTENT_PROJECT_ID, mProject.mProjectId);
                intent.putExtra(Constants.FORM_ACTION_TYPE, Constants.UPDATE_FORM_KEY);
                activity.startActivity(intent);
            }
        });

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mProject != null && mProject.mLatitude != null && mProject.mLongitude != null
                        && !mProject.mLatitude.isEmpty() && !mProject.mLongitude.isEmpty()) {
                    startNavigationScreen(mProject.mLatitude, mProject.mLongitude);
                } else {
                    Toast.makeText(mActivity, "Navigation data not available!", Toast.LENGTH_LONG).show();
                }
            }
        });

//        cancelButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mRootView != null) {
//                    mRootView.setVisibility(View.GONE);
//                }
//            }
//        });
    }

    @Override public void onOpen(Object item){

        // Closes all the other ooen overlays
        closeAllInfoWindowsOn(mMapView);
    }

    private void startNavigationScreen(String lat, String lon) {
        if (Utils.getInstance().isOnline(null)) {
            // Google Maps
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                    Uri.parse("http://maps.google.com/maps?daddr=" + lat + "," + lon));
            mActivity.startActivity(intent);
        } else {
            // Navigation screen from OfflineMaps module
            GeoPoint destination = new GeoPoint(Double.parseDouble(lat), Double.parseDouble(lon));
            String destinationName = "";
            if (mProject.mProjectName != null && !mProject.mProjectName.isEmpty()) {
                destinationName = mProject.mProjectName;
            } else {
                destinationName = "Destination";
            }
            Destination.getDestination().setEndPoint(destination, destinationName);
            String file_path = Environment.getExternalStorageDirectory() + "/" + mActivity.getPackageName()
                    + "/" + UAAppContext.getInstance().getUserID() + "/" + "Offlinemap" + "/" + "BaseLayerData";
            List<String> map_files = new ArrayList<>(MapHelper.getInstance().getFilesFromFolder(file_path));
            Destination.getDestination().setFilePath(file_path);
            Destination.getDestination().setFile_names(map_files);
            Intent intent = new Intent(mActivity, NavigationActivity.class);
            mActivity.startActivity(intent);
        }
    }
}


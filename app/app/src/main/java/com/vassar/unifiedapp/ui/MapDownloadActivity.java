package com.vassar.unifiedapp.ui;

import android.app.DownloadManager;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.adapter.MapDownloadAdapter;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.MapConfigurationV1;
import com.vassar.unifiedapp.utils.Utils;

public class MapDownloadActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private MapConfigurationV1 mapConfig;
    private MapDownloadAdapter mapDownloadAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_download);

        initComponents(this);

        initializeToolbar();

        if (savedInstanceState == null) {
            // Get the bboxes to download, from the configurations
            // Show the current status of the downloads

            initializeViews();

            initializeMapConfig();

            mapDownloadAdapter = new MapDownloadAdapter(mapConfig, this);

            if (mapConfig != null) {
                Utils.logDebug(LogTags.MAP_CONFIG, "MapConfig : config retrieved");
//                 Creating the list of downloads
                recyclerView.setAdapter(mapDownloadAdapter);
                mapDownloadAdapter.downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                mapDownloadAdapter.checkDownloadStatusForFiles();

            } else {
                showErrorMessageAndFinishActivity("No data for offline maps.", false);
                Utils.logError(LogTags.MAP_CONFIG, "Error getting MapConfig -- null -- continuing without map config");
            }
        }
    }

    private void initializeToolbar() {
        Toolbar toolbar = findViewById(R.id.download_map_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getResources().getString(R.string.app_name));
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.download_map_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initializeMapConfig() {
        mapConfig = UAAppContext.getInstance().getMapConfig();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mapDownloadAdapter.onDownloadComplete);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        registerReceiver(mapDownloadAdapter.onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

    }
}

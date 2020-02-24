package com.peritus.peritusofflinemap;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DownloadJobIntentService extends JobIntentService {
    private static final String DOWNLOAD_PATH = "com.peritus.peritusofflinemap_DownloadMapService_Download_path";
    private static final String DOWNLOAD_FILE_NAME = "com.peritus.peritusofflinemap_DownloadMapService_Download_file_name";
    private static final String DESTINATION_PATH = "com.peritus.peritusofflinemap_DownloadMapService_Destination_path";

    static final int JOB_ID = 1000; //Unique job ID.
    //Convenience method for enqueuing work in to this service.


    public static Intent getDownloadService(final @NonNull Context callingClassContext, final @NonNull String downloadPath, final @NonNull String destinationPath, final @NonNull String downloadFileName) {
        Log.d("AndroidDownloadManager_","test");
        return new Intent(callingClassContext, DownloadMapService.class)
                .putExtra(DOWNLOAD_PATH, downloadPath)
                .putExtra(DESTINATION_PATH, destinationPath)
                .putExtra(DOWNLOAD_FILE_NAME, downloadFileName);

    }

    public static void enqueueWork(Context context, Intent work) {

        enqueueWork(context, DownloadJobIntentService.class, JOB_ID, work);
    }

    @Override

    protected void onHandleWork(@NonNull Intent intent) {
        String downloadPath = intent.getStringExtra(DOWNLOAD_PATH);
        String destinationPath = intent.getStringExtra(DESTINATION_PATH);
        String downloadFileName = intent.getStringExtra(DOWNLOAD_FILE_NAME);
      long id =   startDownload(downloadPath, destinationPath,downloadFileName);
//        Log.d("AndroidDownloadManage_1","test");
//        Log.d("AndroidDownloadManage_2","test");
// This describes what will happen when service is triggered    }
    }

    private long startDownload(String downloadPath, String destinationPath, String downloadFileName) {

        Uri uri = Uri.parse(downloadPath); // Path where you want to download file.
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);  // Tell on which network you want to download file.
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);  // This will show notification on top when downloading the file.
        request.setTitle("Downloading Map"); // Title for notification.
        request.setVisibleInDownloadsUi(true);
        request.setDestinationInExternalPublicDir(destinationPath, uri.getLastPathSegment());  // Storage directory path
//        ((DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE)).enqueue(request); // This will start downloading

        DownloadManager downloadManager   = ((DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE)); // This will start downloading
        long id = downloadManager.enqueue(request);
        OfflineMap.download_ids.add(String.valueOf(id)+"@"+downloadFileName);
//        Log.d("AndroidDownloadManage_3","test");
//        Log.d("AndroidDownloadManage_4",String.valueOf(id)+"@"+downloadFileName);

        return id;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("AndroidDownloadManager","destroy");

    }



}

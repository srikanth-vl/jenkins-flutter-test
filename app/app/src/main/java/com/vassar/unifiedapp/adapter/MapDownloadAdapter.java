package com.vassar.unifiedapp.adapter;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.MapConfigurationV1;
import com.vassar.unifiedapp.model.OfflineMapFile;
import com.vassar.unifiedapp.utils.Utils;
import com.vassar.unifiedapp.utils.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.view.View.GONE;

public class MapDownloadAdapter extends RecyclerView.Adapter<MapDownloadAdapter.MapDownloaderItemViewHolder> {

    private MapConfigurationV1 mapConfig;
    private Context context;
    public DownloadManager downloadManager;
    private List<MapDownloadAdapter.MapDownloaderItemViewHolder> holder_list=new ArrayList<>();


    public MapDownloadAdapter(MapConfigurationV1 mapConfig, Context context) {
        this.mapConfig = mapConfig;
        this.context = context;
    }

    @NonNull
    @Override
    public MapDownloadAdapter.MapDownloaderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = ((LayoutInflater) UAAppContext.getInstance().getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.map_download_item_layout, parent, false);
        return new MapDownloadAdapter.MapDownloaderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MapDownloadAdapter.MapDownloaderItemViewHolder holder, int position) {
        if (mapConfig != null && mapConfig.getFiles() != null && !mapConfig.getFiles().isEmpty()) {
            String storageDirectory = Environment.getExternalStorageDirectory().getAbsolutePath() +
                    mapConfig.getFiles().get(position).getFileStoragePath();

            holder_list.add(holder);
            File storageDir = new File(storageDirectory);
            holder.mFileName.setText(mapConfig.getFiles().get(position).fileName);
            OfflineMapFile offlineMapFile = mapConfig.getFiles().get(position);
            File mapFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                    offlineMapFile.fileStoragePath + "/" + offlineMapFile.fileName);

            if (storageDir.exists() && mapFile.exists()) {

                double mapFileSize = Double.valueOf(offlineMapFile.fileSize) * 1024;
                long fileSize = Math.round(mapFileSize);
                long fileLength = Utils.getInstance().getFileSizeInBytes(mapFile.getAbsolutePath());;

                if (fileLength >= fileSize-100) {
                    onFileAlreadyDownloaded(holder);
                } else {
                    onFilePendingDownload(holder);
                }
            } else {
                onFileNotDownloaded(position, holder);
            }
        }
    }

    private void downloadIconClicked(int position, @NonNull MapDownloadAdapter.MapDownloaderItemViewHolder holder) {
        if (mapConfig != null && mapConfig.getFiles() != null && !mapConfig.getFiles().isEmpty() && mapConfig.getFiles().get(position) != null) {
            String fileName = mapConfig.getFiles().get(position).getFileName();
            String downloadUrl = mapConfig.getFiles().get(position).getFileUrl();
            String filePath = mapConfig.getFiles().get(position).fileStoragePath;

            if (downloadUrl != null && !downloadUrl.isEmpty()) {
                // Map downloader service using download manager
                holder.mDownloadProgressBar.setVisibility(View.VISIBLE);
                holder.mDownloadStatus.setText("Downloading...");
                holder.mDownloadStatusImage.setVisibility(GONE);
                downloadMapFile(downloadUrl, fileName, filePath);

            } else {
                Toast.makeText(context, "Not a valid map download link. Please try again later.", Toast.LENGTH_LONG).show();
                Utils.logError(LogTags.MAP_CONFIG, "Error getting MapConfig -- null or empty url -- download cancelled");
            }
        } else {
            Toast.makeText(context, "Not a valid map download link. Please try again later.", Toast.LENGTH_LONG).show();
            Utils.logError(LogTags.MAP_CONFIG, "Error getting MapConfig -- null -- continuing without map config data");
        }
    }

    @Override
    public int getItemCount() {
        return this.mapConfig.getFiles().size();
    }

    public class MapDownloaderItemViewHolder extends RecyclerView.ViewHolder {

        private TextView mFileName;
        private ImageView mDownloadStatusImage;
        private TextView mDownloadStatus;
        private ProgressBar mDownloadProgressBar;

        public MapDownloaderItemViewHolder(View itemView) {
            super(itemView);
            mFileName = itemView.findViewById(R.id.map_download_item_file_name);
            mDownloadStatusImage = itemView.findViewById(R.id.map_download_status_image);
            mDownloadStatus = itemView.findViewById(R.id.map_download_item_text_status);
            mDownloadProgressBar = itemView.findViewById(R.id.map_download_progress);

        }
    }

    private void downloadMapFile(String url, String fileName, String storagePath) {

        if (storagePath == null || storagePath.isEmpty()) {
            storagePath = Constants.DEFAULT_MAP_STORAGE;
        }

        Uri uri = Uri.parse(url);

        File direct = new File(Environment.getExternalStorageDirectory()
                + storagePath);

        if (!direct.exists()){
            direct.mkdirs();
        }

        // Create request for android download manager
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                DownloadManager.Request.NETWORK_MOBILE);

        // set title and description
        request.setTitle(context.getResources().getString(R.string.app_name));
        request.setDescription("Downloading Maps!");

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        //set the local destination for download file to a path within the application's external files directory
        request.setDestinationInExternalPublicDir(storagePath, fileName);

        request.setMimeType("*/*");
        Long downloadId = downloadManager.enqueue(request);
        addIDToFileNameMapToPreferences(fileName, downloadId);
        manageDownloadProcess(fileName, downloadId);
    }

    public BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equalsIgnoreCase(action)) {
                Long id = intent.getLongExtra(
                        DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                String mapPreferences = Utils.getInstance().getValueFromAppPreferences(Constants.MAP_DOWNLOAD_PREFERENCES);
                Map<String, String> map = Utils.getInstance().getMapFromString(mapPreferences);
                String fileName = map.get(id.toString());
                DownloadManager.Query query = new DownloadManager.Query();

                query.setFilterById(id);
                Cursor c = downloadManager.query(query);
                if (c.moveToFirst()) {
                    MapDownloadAdapter.MapDownloaderItemViewHolder holder = getHolderForFile(fileName);
                    int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                        onFileAlreadyDownloaded(holder);
                        Utils.logInfo(fileName + "download successful in receiver");
                    } else if (DownloadManager.STATUS_FAILED == c.getInt(columnIndex)){
                        for (OfflineMapFile file : mapConfig.getFiles()){
                            if (file.fileName.equalsIgnoreCase(fileName)){
                                File mapFile = new File(Environment.getExternalStorageDirectory()
                                        + file.fileStoragePath + "/" + fileName);
                                if (mapFile.exists()){
                                    mapFile.delete();
                                }
                                onFileNotDownloaded(holder.getAdapterPosition(), holder);
                            }
                        }
                    }
                }
            }
        }
    };

    private void addIDToFileNameMapToPreferences(String fileName, Long downloadId){
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> map = new HashMap<>();
        String value = Utils.getInstance().getValueFromAppPreferences(Constants.MAP_DOWNLOAD_PREFERENCES);
        if (value != null && !value.isEmpty()){
            map = Utils.getInstance().getMapFromString(value);
        }
        try {
            map.put(downloadId.toString(), fileName);
            String mapToString = objectMapper.writeValueAsString(map);
            Utils.getInstance().storeValueInAppPreferences(Constants.MAP_DOWNLOAD_PREFERENCES, mapToString);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private void onFileAlreadyDownloaded(@NonNull MapDownloadAdapter.MapDownloaderItemViewHolder holder){
        holder.mDownloadStatus.setText("Downloaded");
        holder.mDownloadStatusImage.setImageResource(R.drawable.delete_file);
        holder.mDownloadStatusImage.setVisibility(View.VISIBLE);
        holder.mDownloadProgressBar.setVisibility(GONE);
        holder.mDownloadStatusImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = 0;
                for (OfflineMapFile mapFile : mapConfig.getFiles()){
                    if (mapFile.fileName.equalsIgnoreCase(holder.mFileName.getText().toString())){
                        File file = new File(Environment.getExternalStorageDirectory() + mapFile.fileStoragePath + "/" + mapFile.fileName);
                        if (file.exists()){
                            final int position = pos;
                            boolean deleted = file.delete();
                            if (deleted){
                                holder.mDownloadStatusImage.setImageResource(R.drawable.download_white);
                                holder.mDownloadStatus.setText("Not Downloaded");
                                holder.mDownloadStatusImage.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (Utils.getInstance().isOnline(null)) {
                                            downloadIconClicked(position, holder);
                                        } else{
                                            Toast.makeText(context, context.getResources().getString(R.string.CHECK_INTERNET_CONNECTION), Toast.LENGTH_SHORT).show();
                                            Utils.logError(LogTags.HOME_SCREEN, "-- No internet for user to download maps " +
                                                    "-- continue without downloading");
                                        }
                                    }
                                });
                            }
                        }
                    }
                    pos++;
                }
            }
        });
    }

    private void onFileNotDownloaded(int position, @NonNull MapDownloadAdapter.MapDownloaderItemViewHolder holder){
        holder.mDownloadStatusImage.setImageResource(R.drawable.download_white);
        holder.mDownloadStatusImage.setVisibility(View.VISIBLE);
        holder.mDownloadStatus.setText("Not Downloaded");
        holder.mDownloadProgressBar.setVisibility(View.INVISIBLE);
        holder.mDownloadStatusImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.getInstance().isOnline(null)) {
                    downloadIconClicked(position, holder);
                } else{
                    Toast.makeText(context, context.getResources().getString(R.string.CHECK_INTERNET_CONNECTION), Toast.LENGTH_SHORT).show();
                    Utils.logError(LogTags.HOME_SCREEN, "-- No internet for user to download maps " +
                            "-- continue without downloading");
                }
            }
        });
    }

    private void onFilePendingDownload(@NonNull MapDownloadAdapter.MapDownloaderItemViewHolder holder){
        if (Utils.getInstance().isOnline(null)){
            holder.mDownloadStatusImage.setVisibility(GONE);
            holder.mDownloadProgressBar.setVisibility(View.VISIBLE);
            holder.mDownloadStatus.setText("Downloading...");
        } else{
            holder.mDownloadStatusImage.setVisibility(View.VISIBLE);
            holder.mDownloadStatusImage.setImageResource(R.drawable.download_white);
            holder.mDownloadProgressBar.setVisibility(View.GONE);
            holder.mDownloadStatus.setText("Pending...");
        }
    }

    private void onFileDownloadPaused(@NonNull MapDownloadAdapter.MapDownloaderItemViewHolder holder){
        holder.mDownloadStatusImage.setVisibility(View.INVISIBLE);
        holder.mDownloadProgressBar.setVisibility(View.GONE);
        holder.mDownloadStatus.setText("Paused");
    }

    private MapDownloadAdapter.MapDownloaderItemViewHolder getHolderForFile(String fileName){
        if (holder_list != null && !holder_list.isEmpty()) {
            for (MapDownloadAdapter.MapDownloaderItemViewHolder holder : holder_list) {
                if(holder.mFileName.getText().toString().equalsIgnoreCase(fileName)) {
                    return holder;
                }
            }
        }
        return null;
    }

    private void manageDownloadProcess(final String fileName, final long downloadId) {
        DownloadManager.Query query = new DownloadManager.Query();

        final Cursor cursor = downloadManager.query(query.setFilterById(downloadId));
        MapDownloadAdapter.MapDownloaderItemViewHolder holder = getHolderForFile(fileName);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (cursor.moveToFirst()) {
                    int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    switch (status) {

                        case DownloadManager.STATUS_PENDING:{
                            if (holder != null){
                                onFilePendingDownload(holder);
                                Utils.logInfo("holder is not null in pending ", fileName);
                            }
                            manageDownloadProcess(fileName, downloadId);
                            break;
                        }

                        case DownloadManager.STATUS_RUNNING:{
                            if (holder != null){
                                onFilePendingDownload(holder);
                            }
                            manageDownloadProcess(fileName, downloadId);
                            break;
                        }
                        case DownloadManager.STATUS_PAUSED: {
                            if (holder != null){
                                onFileDownloadPaused(holder);
                            }
                            manageDownloadProcess(fileName, downloadId);
                            break;
                        }
                        case DownloadManager.STATUS_SUCCESSFUL :{
                            if (holder != null) {
                                onFileAlreadyDownloaded(holder);
                            }
                            break;
                        }
                        case DownloadManager.STATUS_FAILED : {
                            if (holder != null) {
                                onFileNotDownloaded(holder.getAdapterPosition(), holder);
                            }
                            break;
                        }
                    }
                }
            }
        }, 1000);
    }

    public void checkDownloadStatusForFiles(){
        String mapPreferences = Utils.getInstance().getValueFromAppPreferences(Constants.MAP_DOWNLOAD_PREFERENCES);
        Map<String, String> map = Utils.getInstance().getMapFromString(mapPreferences);
        for (String key : map.keySet()){
            manageDownloadProcess(map.get(key), Long.parseLong(key));
        }
    }
}
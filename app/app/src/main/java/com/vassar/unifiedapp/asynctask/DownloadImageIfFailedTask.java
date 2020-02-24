package com.vassar.unifiedapp.asynctask;

import android.content.Context;
import android.os.AsyncTask;

import com.vassar.unifiedapp.api.ImageDownloaderService;
import com.vassar.unifiedapp.db.UnifiedAppDBHelper;
import com.vassar.unifiedapp.listener.ImageDownloaderServiceListener;
import com.vassar.unifiedapp.model.IncomingImage;

import java.util.ArrayList;

public class DownloadImageIfFailedTask extends AsyncTask<Void, Void, Void> {

    private Context mContext;
    private ArrayList<IncomingImage> mIncomingImages = new ArrayList<>();
    private UnifiedAppDBHelper mDBHelper;

    public DownloadImageIfFailedTask(Context context, UnifiedAppDBHelper dbHelper, ArrayList<IncomingImage> incomingImages) {
        this.mContext = context;
        this.mDBHelper = dbHelper;
        this.mIncomingImages.clear();
        this.mIncomingImages.addAll(incomingImages);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        // Calling the image downloader service
        ImageDownloaderService imageDownloaderService = new ImageDownloaderService(mContext,
                mDBHelper, mIncomingImages, new ImageDownloaderServiceListener() {
            @Override
            public void onImageDownloadSuccessful() {

            }

            @Override
            public void onImageDownloadFailed() {

            }
        });
        imageDownloaderService.downloadImages();
        return null;
    }
}

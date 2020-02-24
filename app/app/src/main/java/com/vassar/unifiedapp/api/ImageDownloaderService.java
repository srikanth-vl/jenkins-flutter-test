package com.vassar.unifiedapp.api;

import android.content.Context;

import com.vassar.unifiedapp.db.UnifiedAppDBHelper;
import com.vassar.unifiedapp.listener.ImageDownloaderServiceListener;
import com.vassar.unifiedapp.model.IncomingImage;
import com.vassar.unifiedapp.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ImageDownloaderService {

    private ImageDownloaderServiceListener mListener;
    private UnifiedAppDBHelper mDbHelper;
    private Context mContext;
    private ArrayList<IncomingImage> mIncomingImages = new ArrayList<>();

    public ImageDownloaderService(Context context, UnifiedAppDBHelper dbHelper,
                                  ArrayList<IncomingImage> images,
                                  ImageDownloaderServiceListener listener) {
        mContext = context;
        mDbHelper = dbHelper;
        mListener = listener;
        mIncomingImages.clear();
        mIncomingImages.addAll(images);
    }

    public void downloadImages() {
        for (int i=0; i<mIncomingImages.size(); i++) {
            final IncomingImage incomingImage = mIncomingImages.get(i);

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(incomingImage.getImageUrl())
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {


                        Utils.getInstance().showLog("INCOMINGIMAGE", "SUCCESSFUL");
                        InputStream inputStream = response.body().byteStream();

                        if (inputStream != null) {
                            String localPath = Utils.getInstance().saveImageToStorage(mContext, inputStream,
                                    incomingImage.getImageUrl());
                            Utils.getInstance().saveImagesToDatabase(mDbHelper, new IncomingImage(incomingImage
                                    .getImageType(), localPath, incomingImage.getImageUrl()));
                            mListener.onImageDownloadSuccessful();
                        } else {
                            Utils.getInstance().saveImagesToDatabase(mDbHelper, new IncomingImage(incomingImage
                                    .getImageType(), null, incomingImage.getImageUrl()));
                            mListener.onImageDownloadSuccessful();
                        }
                    } else {

                        Utils.getInstance().showLog("INCOMINGIMAGE", "UNSUCCESSFUL");
                        Utils.getInstance().saveImagesToDatabase(mDbHelper, new IncomingImage(incomingImage
                                .getImageType(), null, incomingImage.getImageUrl()));
                        mListener.onImageDownloadSuccessful();
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    mListener.onImageDownloadFailed();
                }
            });
        }
    }
}

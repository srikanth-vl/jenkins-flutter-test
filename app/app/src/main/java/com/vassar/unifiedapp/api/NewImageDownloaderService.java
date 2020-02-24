package com.vassar.unifiedapp.api;

import android.os.AsyncTask;

import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.err.UAAppErrorCodes;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.IncomingImage;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.MapUtils;
import com.vassar.unifiedapp.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NewImageDownloaderService extends AsyncTask<Void, Void, Void> {

    private IncomingImage mIncomingImage;

    public NewImageDownloaderService(IncomingImage image) {
        mIncomingImage = image;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(50, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(mIncomingImage.getImageUrl())
                .build();

        Utils.logInfo("IncomingImageDownload", "service for download called for image url :: " + mIncomingImage.getImageUrl() +  ":: type"+ mIncomingImage.getImageType());

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {

                    InputStream inputStream = response.body().byteStream();

                    if (inputStream != null) {

                        if (mIncomingImage.getImageType() != null && mIncomingImage.getImageType().equalsIgnoreCase(Constants.MAP_GEOJSON)) {
                            String localPath = MapUtils.getInstance().saveMapGeojsonToStorage(
                                    UAAppContext.getInstance().getContext(), inputStream, mIncomingImage.getImageUrl());

                            Utils.logInfo("PATH OF GEOJSON : ", localPath);
                        } else {
                            String localPath = Utils.getInstance().saveImageToStorage(
                                    UAAppContext.getInstance().getContext(), inputStream,
                                    mIncomingImage.getImageUrl());
                            Utils.getInstance().saveImagesToDatabase(UAAppContext.getInstance()
                                    .getDBHelper(), new IncomingImage(mIncomingImage
                                    .getImageType(), localPath, mIncomingImage.getImageUrl()));

                            Utils.logInfo(LogTags.APP_STARTUP, "AppMDConfig image downloaded...");
                        }
                    } else {

                        Utils.logError(UAAppErrorCodes.IMAGE_DOWNLOAD_ERROR
                                , "Error downloading images from AppMDConfig : Incoming stream is null");

                        if (!mIncomingImage.getImageType().equals(Constants.MAP_GEOJSON)) {
                            Utils.getInstance().saveImagesToDatabase(UAAppContext.getInstance()
                                    .getDBHelper(), new IncomingImage(mIncomingImage
                                    .getImageType(), null, mIncomingImage.getImageUrl()));
                        }
                    }
                } else {

                    Utils.logError(UAAppErrorCodes.IMAGE_DOWNLOAD_ERROR
                            , "Error downloading images from AppMDConfig : Response is not successful" + response.toString());

                    Utils.getInstance().saveImagesToDatabase(UAAppContext.getInstance()
                            .getDBHelper(), new IncomingImage(mIncomingImage
                            .getImageType(), null, mIncomingImage.getImageUrl()));
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();

                Utils.logError(UAAppErrorCodes.IMAGE_DOWNLOAD_ERROR
                        , "Error downloading images from AppMDConfig : Network Error");
            }
        });
        return null;
    }
}

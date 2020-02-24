package com.vassar.unifiedapp.asynctask;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.vassar.unifiedapp.api.MediaActionService;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.db.UnifiedAppDBHelper;
import com.vassar.unifiedapp.db.UnifiedAppDbContract;
import com.vassar.unifiedapp.listener.MediaRequestListener;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.FormMedia;
import com.vassar.unifiedapp.receiver.MediaRequestReceiver;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.MediaActionType;
import com.vassar.unifiedapp.utils.MediaRequestStatus;
import com.vassar.unifiedapp.utils.MediaSubType;
import com.vassar.unifiedapp.utils.MediaType;
import com.vassar.unifiedapp.utils.ProjectSubmissionUploadStatus;
import com.vassar.unifiedapp.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MediaRequestHandlerTask extends AsyncTask<Void, Void, Void> {

    @SuppressLint("StaticFieldLeak")
    private Context context;
    private UnifiedAppDBHelper mDBHelper;

    public MediaRequestHandlerTask() {
        this.context = UAAppContext.getInstance().getContext();
        this.mDBHelper = UAAppContext.getInstance().getDBHelper();
    }

    protected Void doInBackground(Void... voids) {

        if (!Utils.getInstance().isOnline(null)) {
            Utils.logInfo(LogTags.MEDIA_THREAD, "Cannot initiate media sync without active internet connection");
            return null;
        } else if (Thread.currentThread().isInterrupted()) {
            return null;
        }

        ArrayList<Integer> statusList = new ArrayList<>();
        statusList.add(MediaRequestStatus.NEW_STATUS.getValue());
        statusList.add(MediaRequestStatus.PENDING_RETRY_STATUS.getValue());

        List<FormMedia> formMediaEntries = mDBHelper.getFormMediaEntries(statusList, Constants.BATCH_SIZE);

        if (formMediaEntries == null || formMediaEntries.isEmpty()) {
            Utils.logError(LogTags.MEDIA_THREAD, "No media found for upload");
            return null;
        }

        for (FormMedia formMedia : formMediaEntries) {

            MediaRequestReceiver.isMediaThreadRunning = true;
            Utils.logInfo(LogTags.MEDIA_THREAD, "media thread is running");
            Utils.logInfo(LogTags.MEDIA_THREAD, "formmedia count : "+formMediaEntries.size());

            if (formMedia.getFormSubmissionTimestamp() == 0) {
                // Media is from the current form session or are discarded images from previous sessions
                Utils.logInfo("FORMSUBMISSIONTS : ", String.valueOf(formMedia.getFormSubmissionTimestamp()));
                continue;
            }

            int sync_status = mDBHelper.getProjectSubmissionStatus(formMedia.getmAppId(),
                    formMedia.getmUserId(), formMedia.getmProjectId(), formMedia.getFormSubmissionTimestamp());

            Utils.logInfo(LogTags.MEDIA_THREAD,"sync_status : "+ProjectSubmissionUploadStatus.getStateByValue(sync_status));

            if (ProjectSubmissionUploadStatus.SYNCED == ProjectSubmissionUploadStatus.getStateByValue(sync_status)) {
                int status = formMedia.getMediaRequestStatus();

                if (formMedia.getMediaActionType() == MediaActionType.UPLOAD.getValue()) {
                    uploadMedia(formMedia, status);
                } else {
                    downloadMedia(formMedia, status);
                }
            }
            Intent intent = new Intent(Constants.POST_SYNC_BROADCAST_ACTION);
            UAAppContext.getInstance().getContext().sendBroadcast(intent);
        }

        MediaRequestReceiver.isMediaThreadRunning = false;
        Utils.logInfo(LogTags.MEDIA_THREAD, "media thread stopped running");
        return null;

    }

    private void uploadMedia(FormMedia formMedia, int status) {

        int mediaMaxRetries = UAAppContext.getInstance().getAppMDConfig().getmMediaRetries();
        MediaActionService mediaUploadService = new MediaActionService(formMedia, new MediaRequestListener() {
            @Override
            public void onRequestSuccessful() {

                Utils.logInfo(LogTags.MEDIA_THREAD, "UPLOAD SUCCESSFUL");

//                Deleting media from storage
//                if (formMedia.getMediaType() == MediaType.IMAGE.getValue()) {
//                    Utils.getInstance().deleteImageFromStorage(context, formMedia.getLocalPath());
//                } else if (formMedia.getMediaType() == MediaType.VIDEO.getValue()) {
//                    Utils.getInstance().deleteVideoFromStorage(context, formMedia.getLocalPath());
//                }

                // Check if the file is from gallery or from app form images
                File imgFile = new File(formMedia.getLocalPath());
                String fileName = imgFile.getName().substring(0, imgFile.getName().indexOf("."));

                if(formMedia.getMediaType() == MediaType.IMAGE.getValue() && formMedia.getmUUID().equals(fileName)) {
//                Just resizing media at the same path, no deletion
                Utils.getInstance().resizeImage(formMedia.getLocalPath());
                }

                int retries = formMedia.getMediaUploadRetries();
                if(status == MediaRequestStatus.PENDING_RETRY_STATUS.getValue()) {
                    retries++;
                }
                ContentValues values = getContentValueObject(MediaRequestStatus.SUCCESS_STATUS.getValue(),
                        retries, System.currentTimeMillis(), MediaSubType.PREVIEW.getValue());
                mDBHelper.updateFormMedia(values, formMedia.getmUUID());
            }

            @Override
            public void onRequestFailed() {

                Utils.logInfo(LogTags.MEDIA_THREAD, "UPLOAD FAILED");

                int retries = formMedia.getMediaUploadRetries();
                int req_status = MediaRequestStatus.PENDING_RETRY_STATUS.getValue();
                retries++;

                if(status == MediaRequestStatus.PENDING_RETRY_STATUS.getValue() && retries == mediaMaxRetries) {
                    req_status = MediaRequestStatus.FAILURE_STATUS.getValue();
                }
                ContentValues values = getContentValueObject(req_status, retries,
                        Constants.UPLOAD_TIMESTAMP_DEFAULT, MediaSubType.FULL.getValue());
                mDBHelper.updateFormMedia(values, formMedia.getmUUID());

            }
        });
        mediaUploadService.uploadMedia();

    }

    private void downloadMedia(FormMedia formMedia, int status) {

        int mediaMaxRetries = UAAppContext.getInstance().getAppMDConfig().getmMediaRetries();
        MediaActionService mediaDownloadService = new MediaActionService(formMedia, new MediaRequestListener() {

            @Override
            public void onRequestSuccessful() {

            }

            @Override
            public void onRequestFailed() {

            }
        });
        try {
            mediaDownloadService.downloadMedia();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ContentValues getContentValueObject(int status, int retries, long uploadTimestamp, int mediaSubType) {
        ContentValues values = new ContentValues();
        values.put(UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_REQUEST_STATUS, status);
        values.put(UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_UPLOAD_RETRIES, retries);
        values.put(UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_UPLOAD_TIMESTAMP, uploadTimestamp);
        values.put(UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_SUBTYPE, mediaSubType);

        return values;
    }
}

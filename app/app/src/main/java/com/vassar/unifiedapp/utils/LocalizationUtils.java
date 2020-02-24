package com.vassar.unifiedapp.utils;

import android.content.Context;

import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.log.LogTags;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

public class LocalizationUtils {
    public  static String fileName = "localization.json";

    private static Context mContext = null;

    // static variable mInstance of type LocalizationUtils
    private static LocalizationUtils mInstance = null;

    public static LocalizationUtils getInstance() {
        if (mInstance == null)
            mInstance = new LocalizationUtils();
        mContext = UAAppContext.getInstance().getContext();
        return mInstance;
    }
    public  void saveData(String mJsonResponse) {
        if(mJsonResponse == null) {
            mJsonResponse = "{\"en\":{\"Next\": \"पीछे\", \"submit\":\"सबमिट\", \"Preview\":\"पूर्वावलोकन\",\"Cancel\":\"हटाएँ\"}}";
        }
        try {
            FileWriter file = new FileWriter(mContext.getFilesDir().getPath() + "/" + fileName);
            file.write(mJsonResponse);
            file.flush();
            file.close();
        } catch (IOException e) {
            Utils.logError(LogTags.LOCALIZATION_UTILS, "Error in Writing: " + mJsonResponse);
            e.printStackTrace();
        }
    }

    public  String getData() {
        try {
            File f = new File(mContext.getFilesDir().getPath() + "/" + fileName);
            //check whether file exists
            FileInputStream is = new FileInputStream(f);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer);
        } catch (IOException e) {
            Utils.logError(LogTags.LOCALIZATION_UTILS, "Error in reading file");
            e.printStackTrace();
            return null;
        }
    }
}

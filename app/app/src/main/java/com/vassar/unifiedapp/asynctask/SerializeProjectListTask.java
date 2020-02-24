package com.vassar.unifiedapp.asynctask;

import android.os.AsyncTask;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.ProjectList;
import com.vassar.unifiedapp.utils.Utils;

import java.io.IOException;

public class SerializeProjectListTask extends AsyncTask<String, Void, ProjectList> {

    @Override
    protected ProjectList doInBackground(String... strings) {
        Utils.getInstance().showLog("TIMESTAMP GSON START", String.valueOf(System.currentTimeMillis()));
        ProjectList projectList = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
        projectList = objectMapper.readValue(strings[0], ProjectList.class);
        Utils.getInstance().showLog("TIMESTAMP GSON STOP", String.valueOf(System.currentTimeMillis()));
        } catch (IOException e) {
            Utils.logError(LogTags.PROJECT_LIST, "Failed to parse jsonString :: " + strings[0]);
            e.printStackTrace();
        }
        return projectList;
    }
}

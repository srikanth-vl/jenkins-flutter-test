package com.vassar.unifiedapp.asynctask;

import android.os.AsyncTask;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassar.unifiedapp.model.ProjectTypeConfiguration;
import com.vassar.unifiedapp.utils.Utils;

import java.io.IOException;

public class SerializeProjectTypeTask extends AsyncTask<String, Void, ProjectTypeConfiguration> {

    @Override
    protected ProjectTypeConfiguration doInBackground(String... strings) {
        Utils.getInstance().showLog("TIMESTAMP JSON Parser START", String.valueOf(System.currentTimeMillis()));
        ProjectTypeConfiguration projectTypeConfiguration = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            projectTypeConfiguration = objectMapper.readValue(strings[0], ProjectTypeConfiguration.class);
            Utils.getInstance().showLog("TIMESTAMP JSON Parser STOP", String.valueOf(System.currentTimeMillis()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return projectTypeConfiguration;
    }
}

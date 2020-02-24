package com.vassar.unifiedapp.offlinemaps;

import android.os.Environment;

import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.Project;
import com.vassar.unifiedapp.model.ProjectList;
import com.vassar.unifiedapp.model.ProjectListFieldModel;
import com.vassar.unifiedapp.model.ProjectTypeModel;
import com.vassar.unifiedapp.model.RootConfig;
import com.vassar.unifiedapp.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapHelper {

    /* 1. As the download is initialized by the user, we need to check for the districts
    for which we already have data and the ones for which we need to download data.
    2. We need to maintain a list for which the map data is already downloaded.
    3. How can we download the other layers (on what dimension) and how do we divide the layer data?
    4. Initialize map - with the map data, layers, markers (static or drag and drop).
    Everything should be driven by the backend and should change accordingly on the client.
    5. Markers and layers visibility can be toggled.
    6. Legend */

    // static variable mInstance of type Utils
    private static MapHelper mInstance = null;

    // private constructor restricted to this class itself
    private MapHelper() {
    }

    // static method to create instance of Singleton class
    public static MapHelper getInstance() {
        if (mInstance == null)
            mInstance = new MapHelper();
        return mInstance;
    }

    private List<String> getMapDownloadingDimensionList() {
        List<String> dimensionList = new ArrayList<>();
        return dimensionList;
    }

    private Map<String, List<String>> getEntityList(ProjectList projectList, List<String> keyNames) {
        Map<String, List<String>> keyToListOfData = new HashMap<>();
        for (Project project : projectList.mProjects) {
            for (ProjectListFieldModel projectListFieldModel : project.mFields) {
                if (keyNames.contains(projectListFieldModel.mIdentifier)) {
                    String key = keyNames.get(keyNames.indexOf(projectListFieldModel.mIdentifier));
                    if (!keyToListOfData.containsKey(key)) {
                        keyToListOfData.put(key, new ArrayList<>());
                    }
                    keyToListOfData.get(key).add(projectListFieldModel.mProjectListFieldValue.mValue);
                }
            }
        }
        List<String> stateIds = new ArrayList<>();
        stateIds.add("AP");
        keyToListOfData.put("state", stateIds);
        return keyToListOfData;
    }

    /* ---------------------- Download Map Data -----------------------------------
        1. Get all project types that have map data that needs to be downloaded.
        2. For each dimension(for each project type), get a list of dimension values, for that project type.
        3. Using these lists, get the list of files that need to be downloaded. */
    public Set<String> getDownloadFileNamesForMaps(RootConfig rootConfig) {
        Set<String> files = new HashSet<>();

        if (rootConfig == null || rootConfig.mApplications == null || rootConfig.mApplications.isEmpty()) {
            Utils.logError(LogTags.ROOT_CONFIG, "Error getting consistent Root Config -- exiting -- returning null");
            return null;
        }

        for (ProjectTypeModel projectType : rootConfig.mApplications) {
            if (projectType.mMapConfiguration != null &&
                    projectType.mMapConfiguration.getDownloadDimension() != null &&
                    !projectType.mMapConfiguration.getDownloadDimension().isEmpty() &&
                    projectType.mMapConfiguration.getDownloadMapping() != null) {

                // Project type contains valid download dimension and map configuration
                // Get ProjectList for this project type
                String downloadDimension = projectType.mMapConfiguration.getDownloadDimension();
                Map<String, String> downloadMapping = projectType.mMapConfiguration.getDownloadMapping();

                ProjectList projectList = UAAppContext.getInstance().getDBHelper().getProjectsForUser(UAAppContext
                        .getInstance().getUserID(), projectType.mAppId);

                if (projectList != null && projectList.mProjects != null && !projectList.mProjects.isEmpty()) {
                    // User has a valid list of projects
                    for (Project project : projectList.mProjects) {

                        if (project != null && project.mFields != null && !project.mFields.isEmpty()) {
                            for (ProjectListFieldModel field : project.mFields) {
                                if (field != null && field.mIdentifier != null &&
                                        !field.mIdentifier.isEmpty() &&
                                        field.mProjectListFieldValue != null &&
                                        field.mProjectListFieldValue.mValue != null &&
                                        !field.mProjectListFieldValue.mValue.isEmpty()) {
                                    if (field.mIdentifier.equalsIgnoreCase(downloadDimension)) {
                                        String dimensionValue = field.mProjectListFieldValue.mValue;
                                        dimensionValue = dimensionValue.toLowerCase();
                                        if (downloadMapping.containsKey(dimensionValue)) {
                                            files.add(downloadMapping.get(dimensionValue));
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        File root = android.os.Environment.getExternalStorageDirectory();

        Set<String> filesPresent = getFilesFromFolder(root.getAbsolutePath() + "/" + "Android" + "/" + "data" + "/" + UAAppContext.getInstance()
                .getContext().getPackageName() + "/" + UAAppContext.getInstance().getUserID() + "/" + "Offlinemap" + "/" + "BaseLayerData");

        files.removeAll(filesPresent);

        return files;
    }

    public Set<String> getFilesFromFolder(String folderPath) {
        Set<String> files = new HashSet<>();
        File folder = new File(folderPath);

        if(folder.isDirectory()) {
            File[] listOfFiles = folder.listFiles();
            if (listOfFiles != null && listOfFiles.length > 0) {
                for (int i = 0; i < listOfFiles.length; i++) {
                    if (listOfFiles[i].isFile()) {
                        files.add(listOfFiles[i].getName());
                    } else if (listOfFiles[i].isDirectory()) {
                        // Is a directory
                    }
                }
            }
        }

        return files;
    }
}

//package com.vassar.unifiedapp.asynctask;
//
//import android.content.ContentValues;
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.os.AsyncTask;
//
//import com.google.gson.Gson;
//import com.vassar.unifiedapp.application.UnifiedAppApplication;
//import com.vassar.unifiedapp.db.UnifiedAppDBHelper;
//import com.vassar.unifiedapp.db.UnifiedAppDbContract;
//import com.vassar.unifiedapp.model.Filter;
//import com.vassar.unifiedapp.model.Project;
//import com.vassar.unifiedapp.model.ProjectList;
//import com.vassar.unifiedapp.model.ProjectTypeModel;
//import com.vassar.unifiedapp.model.RootConfig;
//import com.vassar.unifiedapp.ui.ProjectFormActivity;
//import com.vassar.unifiedapp.utils.Constants;
//import com.vassar.unifiedapp.utils.Utils;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ExecutionException;
//
//public class CreateFiltersTask extends AsyncTask<Void, Void, Void> {
//
//    Context mContext;
////    ProjectList mProjectList;
//    UnifiedAppDBHelper mDbHelper;
//
//    public CreateFiltersTask(Context context, UnifiedAppDBHelper dbHelper) {
//        this.mContext = context;
////        this.mProjectList = projectList;
//        this.mDbHelper = dbHelper;
//    }
//
//    @Override
//    protected Void doInBackground(Void... voids) {
//
//        SharedPreferences appPreferences = mContext.getSharedPreferences(Constants.APP_PREFERENCES_KEY
//                , Context.MODE_PRIVATE);
//
//        String userId = appPreferences.getString(Constants.USER_ID_PREFERENCE_KEY
//                , Constants.USER_ID_PREFERENCE_DEFAULT);
//
//        if (userId != null && !userId.isEmpty()) {
//            String rootConfigString = mDbHelper.getConfigFile(Constants.ROOT_CONFIG_DB_NAME
//                    + userId);
//
//            if (rootConfigString != null) {
//                Gson gson = new Gson();
//                RootConfig rootConfig = gson.fromJson(rootConfigString, RootConfig.class);
//
//                if (rootConfig != null && rootConfig.mApplications != null && rootConfig.mApplications.size() > 0) {
//                    for (ProjectTypeModel projectTypeModel : rootConfig.mApplications) {
//
//                        List<String> attributes = new ArrayList<>();
//                        if (projectTypeModel.mFilteringAttributes != null && projectTypeModel.mFilteringAttributes.size() > 0) {
//                            attributes.addAll(projectTypeModel.mFilteringAttributes);
//
//                            String projectListConfigString = mDbHelper.getConfigFile
//                                    (Constants.PROJECT_LIST_CONFIG_DB_NAME + projectTypeModel.mAppId + userId);
//
//                            ProjectList savedProjectList = null;
//
//                            if (projectListConfigString != null && !projectListConfigString.isEmpty()) {
////                                try {
////                                    savedProjectList = new SerializeProjectListTask().execute(projectListConfigString).get();
////                                } catch (ExecutionException e) {
////                                    e.printStackTrace();
////                                } catch (InterruptedException e) {
////                                    e.printStackTrace();
////                                }
//                                savedProjectList = gson.fromJson(projectListConfigString, ProjectList.class);
//                            }
//
//                            Map<String, ContentValues> mapToInsert = new HashMap<>();
//                            if (savedProjectList != null && savedProjectList.mProjects != null && savedProjectList.mProjects.size() > 0) {
//                                for (Project project : savedProjectList.mProjects) {
//                                    if (project != null && project.mFilteringAttributes != null) {
////                                    ArrayList<String> valuesList = new ArrayList<>();
////                                    for (Map.Entry<String, String> entry : project.mFilteringAttributes.entrySet()) {
////                                        int index = attributes.indexOf(entry.getKey());
////                                        valuesList.add(index, entry.getValue());
////                                    }
//
//                                        String [] valuesList = new String[project.mFilteringAttributes.size()];
//                                        for (Map.Entry<String, String> entry : project.mFilteringAttributes.entrySet()) {
//                                            int index = attributes.indexOf(entry.getKey());
//                                            valuesList[index] = entry.getValue().trim().toLowerCase();
//                                        }
//                                        String dimensionValues = android.text.TextUtils.join("#", valuesList);
//                                        String dimensions = android.text.TextUtils.join("#", attributes);
//                                        String key = userId + "$$" + projectTypeModel.mAppId + "$$" + dimensionValues;
//                                        ContentValues contentValues = null;
//
//                                        if(mapToInsert.containsKey(key)) {
//                                            contentValues = mapToInsert.get(key);
//                                        }
//                                        // Filter filter = mDbHelper.getFilterRow(userId, projectTypeModel.mAppId, dimensions);
//
//                                        if (contentValues != null) {
//                                            String val = String.valueOf(contentValues.get(UnifiedAppDbContract.FilterTableEntry.COLUMN_PROJECTS));
//                                            StringBuilder projectIds = new StringBuilder(val);
//                                            projectIds.append("," + project.mProjectId);
//                                            contentValues.put(UnifiedAppDbContract.FilterTableEntry.COLUMN_PROJECTS, projectIds.toString());
//                                            //mDbHelper.updateFilter(contentValues, userId, projectTypeModel.mAppId, dimensions);
//                                        } else {
//                                            contentValues = new ContentValues();
//                                            contentValues.put(UnifiedAppDbContract.FilterTableEntry.COLUMN_PROJECTS, project.mProjectId);
//                                            contentValues.put(UnifiedAppDbContract.FilterTableEntry.COLUMN_APP, projectTypeModel.mAppId);
//                                            contentValues.put(UnifiedAppDbContract.FilterTableEntry.COLUMN_USER, userId);
//                                            contentValues.put(UnifiedAppDbContract.FilterTableEntry.COLUMN_DIMENSIONS, dimensions);
//                                            //mDbHelper.addToDatabase(UnifiedAppDbContract.FilterTableEntry.TABLE_FILTER, contentValues);
//                                            contentValues.put(UnifiedAppDbContract.FilterTableEntry.COLUMN_FILTERING_DIMENSION_VALUES, dimensionValues);
//                                            mapToInsert.put(key, contentValues);
//                                        }
//                                    }
//                                }
//                                writeToDatabase(mapToInsert);
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        return null;
//    }
//
//    private void writeToDatabase(Map<String, ContentValues> mapToInsert) {
//        List<ContentValues> listToInsert = new ArrayList<>();
//        for(String key : mapToInsert.keySet()) {
//            ContentValues contentValues = mapToInsert.get(key);
//            listToInsert.add(contentValues);
//            //mDbHelper.addOrReplaceToFilterTable(UnifiedAppDbContract.FilterTableEntry.TABLE_FILTER, contentValues);
////            mDbHelper.addToDatabase(UnifiedAppDbContract.FilterTableEntry.TABLE_FILTER, contentValues);
//        }
////        mDbHelper.performBulkInsert(UnifiedAppDbContract.FilterTableEntry.TABLE_FILTER, listToInsert);
//    }
//
//
////    @Override
////    protected Void doInBackground(Void... voids) {
////
////        SharedPreferences appPreferences = mContext.getSharedPreferences(Constants.APP_PREFERENCES_KEY
////                , Context.MODE_PRIVATE);
////
////        String userId = appPreferences.getString(Constants.USER_ID_PREFERENCE_KEY
////                , Constants.USER_ID_PREFERENCE_DEFAULT);
////
////        if (userId != null && !userId.isEmpty()) {
////            String rootConfigString = mDbHelper.getConfigFile(Constants.ROOT_CONFIG_DB_NAME
////                    + userId);
////
////            if (rootConfigString != null) {
////                Gson gson = new Gson();
////                RootConfig rootConfig = gson.fromJson(rootConfigString, RootConfig.class);
////
////                if (rootConfig != null && rootConfig.mApplications != null && rootConfig.mApplications.size() > 0) {
////                    for (ProjectTypeModel projectTypeModel : rootConfig.mApplications) {
////
////                        List<String> attributes = new ArrayList<>();
////                        if (projectTypeModel.mFilteringAttributes != null && projectTypeModel.mFilteringAttributes.size() > 0) {
////                            attributes.addAll(projectTypeModel.mFilteringAttributes);
////                        }
////
////                        String projectListConfigString = mDbHelper.getConfigFile
////                                (Constants.PROJECT_LIST_CONFIG_DB_NAME + projectTypeModel.mAppId + userId);
////
////                        ProjectList savedProjectList = null;
////
////                        if (projectListConfigString != null && !projectListConfigString.isEmpty()) {
////                            try {
////                                savedProjectList = new SerializeProjectListTask().execute(projectListConfigString).get();
////                            } catch (ExecutionException e) {
////                                e.printStackTrace();
////                            } catch (InterruptedException e) {
////                                e.printStackTrace();
////                            }
////                        }
////
////                        if (savedProjectList != null && savedProjectList.mProjects != null && savedProjectList.mProjects.size() > 0) {
////                            // Filtering Hardcoding
////                            List<String> district = new ArrayList<>();
////                            List<String> mandal = new ArrayList<>();
////                            List<String> village = new ArrayList<>();
////
////                            Utils.getInstance().showLog("FILTERING", "STARTED");
////
////                            for (Project project : savedProjectList.mProjects) {
////                                if (project.mFilteringAttributes != null) {
////                                    Map<String, String> dimensions = project.mFilteringAttributes;
////                                    if (!district.contains(dimensions.get("district")))
////                                        district.add(dimensions.get("district"));
////                                    if (!mandal.contains(dimensions.get("mandal")))
////                                        mandal.add(dimensions.get("mandal"));
////                                    if (!village.contains(dimensions.get("village")))
////                                        district.add(dimensions.get("village"));
////                                }
////                            }
////
////                            Utils.getInstance().showLog("FILTERING", "DONE");
////                        }
////                    }
////                }
////            }
////        }
////
////        return null;
////    }
//
//
////    @Override
////    protected Void doInBackground(Void... voids) {
////
////        Utils.getInstance().showLog("FILTERING TASK", "STARTED");
////
////        // Filtering Hardcoding
////        List<String> district = new ArrayList<>();
////        List<String> mandal = new ArrayList<>();
////        List<String> village = new ArrayList<>();
////
////        Utils.getInstance().showLog("FILTERING", "STARTED");
////
////        for (Project project : mProjectList.mProjects) {
////            if (project.mFilteringAttributes != null) {
////                Map<String, String> dimensions = project.mFilteringAttributes;
////                if (!district.contains(dimensions.get("district")))
////                    district.add(dimensions.get("district"));
////                if (!mandal.contains(dimensions.get("mandal")))
////                    mandal.add(dimensions.get("mandal"));
////                if (!village.contains(dimensions.get("village")))
////                    village.add(dimensions.get("village"));
////            }
////        }
////
////        ((UnifiedAppApplication) mContext.getApplicationContext()).mDistrict = new ArrayList<>();
////        ((UnifiedAppApplication) mContext.getApplicationContext()).mDistrict.addAll(district);
////
////        ((UnifiedAppApplication) mContext.getApplicationContext()).mMandal = new ArrayList<>();
////        ((UnifiedAppApplication) mContext.getApplicationContext()).mMandal.addAll(mandal);
////
////        ((UnifiedAppApplication) mContext.getApplicationContext()).mVillage = new ArrayList<>();
////        ((UnifiedAppApplication) mContext.getApplicationContext()).mVillage.addAll(village);
////
////        Utils.getInstance().showLog("FILTERING", "DONE");
////        return null;
////    }
//}

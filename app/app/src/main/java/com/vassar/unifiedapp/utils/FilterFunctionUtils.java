package com.vassar.unifiedapp.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;

//import com.vassar.unifiedapp.asynctask.CreateFiltersTask;
import com.vassar.unifiedapp.asynctask.SerializeProjectListTask;
import com.vassar.unifiedapp.db.UnifiedAppDBHelper;
import com.vassar.unifiedapp.db.UnifiedAppDbContract;
import com.vassar.unifiedapp.model.Filter;
import com.vassar.unifiedapp.model.Project;
import com.vassar.unifiedapp.model.ProjectList;
import com.vassar.unifiedapp.model.ProjectTypeModel;
import com.vassar.unifiedapp.model.RootConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FilterFunctionUtils {

    Context mContext;
    ProjectList mProjectList;
    UnifiedAppDBHelper mDbHelper;

    public FilterFunctionUtils(Context context) {
        this.mContext = context;
        mDbHelper = new UnifiedAppDBHelper(context);
//        this.mProjectList = projectList;
    }

//    public void createFilterDimensions() {
//        CreateFiltersTask createFiltersTask = new CreateFiltersTask(mContext, mDbHelper);
//        createFiltersTask.execute();
//    }
}

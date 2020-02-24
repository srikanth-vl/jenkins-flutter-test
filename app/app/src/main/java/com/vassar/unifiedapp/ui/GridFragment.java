package com.vassar.unifiedapp.ui;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.adapter.GridFragmentAdapter;
import com.vassar.unifiedapp.application.UnifiedAppApplication;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.db.UnifiedAppDBHelper;
import com.vassar.unifiedapp.err.UAAppConstants;
import com.vassar.unifiedapp.listener.RecyclerItemClickListener;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.AppMetaData;
import com.vassar.unifiedapp.model.ProjectTypeModel;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.ProjectTypeSortingService;
import com.vassar.unifiedapp.utils.Utils;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Fragment to show the application hierarchy
 */
public class GridFragment extends Fragment {

    private int GRID_SPAN = Constants.PROJECT_TYPE_GRID_SPAN;
    private RecyclerView mRecyclerView;
    private ArrayList<ProjectTypeModel> mRootProjectTypes = new ArrayList<>();
    private ArrayList<ProjectTypeModel> mProjectTypes = new ArrayList<>();
    private ArrayList<ProjectTypeModel> mChildProjectTypes = new ArrayList<>();
    private GridFragmentAdapter mAdapter;

    public GridFragment() { }

    @SuppressLint("ValidFragment")
    public GridFragment(List<ProjectTypeModel> projectTypes) {
        this.mRootProjectTypes.addAll(UAAppContext.getInstance().getRootConfig().mApplications);
        this.mProjectTypes.addAll(projectTypes);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(Constants.GRID_FRAGMENT_ROOT_PROJECT_TYPES, mRootProjectTypes);
        outState.putParcelableArrayList(Constants.GRID_FRAGMENT_PROJECT_TYPES, mProjectTypes);
        outState.putParcelableArrayList(Constants.GRID_FRAGMENT_CHILD_PROJECT_TYPES, mChildProjectTypes);
        outState.putString(Constants.GRID_FRAGMENT_USER_ID, UAAppContext.getInstance().getUserID());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState != null){
            // The fragment is being recreated

            Utils.getInstance().showLog("SAVESTATE", "GRID FRAGMENT RESTORING FROM SAVED STATE");
            mRootProjectTypes.clear();
            mRootProjectTypes.addAll(savedInstanceState.getParcelableArrayList(Constants.GRID_FRAGMENT_ROOT_PROJECT_TYPES));
            mProjectTypes.clear();
            mProjectTypes.addAll(savedInstanceState.getParcelableArrayList(Constants.GRID_FRAGMENT_PROJECT_TYPES));
            mChildProjectTypes.clear();
            mChildProjectTypes.addAll(savedInstanceState.getParcelableArrayList(Constants.GRID_FRAGMENT_CHILD_PROJECT_TYPES));
        }

        View view = inflater.inflate(R.layout.fragment_grid, container, false);
        mRecyclerView = view.findViewById(R.id.grid_fragment_recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), GRID_SPAN));
        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), mRecyclerView ,
                        new RecyclerItemClickListener.OnItemClickListener() {

                            @Override public void onItemClick(View view, int position) {
                                Utils.getInstance().showLog("TIMESTAMP PROJECT TYPE CLICKED", String.valueOf(System.currentTimeMillis()));
                                ProjectTypeModel projectTypeClicked = mProjectTypes.get(position);

                                if (projectTypeClicked != null) {
                                    if (projectTypeClicked.mStatus != null && !projectTypeClicked.mStatus.isEmpty()) {
                                        switch (projectTypeClicked.mStatus) {
                                            case Constants.ROOT_CONFIG_APPLICATION_STATUS_INACTIVE :
                                                // Should not click
                                                ((HomeActivity) getActivity()).showErrorMessageAndFinishActivity(Constants.ROOT_CONFIG_APPLICATION_STATUS_INACTIVE_MESSAGE, false);
                                                break;
                                        }
                                    } else {
                                        // Active project type
                                        String projectTypeId = projectTypeClicked.mAppId;
                                        mChildProjectTypes.clear();
                                        for (ProjectTypeModel projectTypeModel : mRootProjectTypes) {
                                            if (projectTypeModel.mParentAppId.equals(projectTypeId))
                                                mChildProjectTypes.add(projectTypeModel);
                                        }

                                        if (mChildProjectTypes.size() > 0) {
                                            // The project type clicked has sub project types
                                            ((HomeActivity) getActivity()).addNewGridFragment(mChildProjectTypes);
                                        } else {
                                            // The project type clicked has a project list
                                            ((HomeActivity) getActivity()).addNewProjectListActivity(projectTypeClicked);
                                        }
                                    }
                                } else {
                                    // Clicked project type is null, log error
                                    Utils.logError(LogTags.ROOT_CONFIG, "Error getting Application -- null -- continuing without moving to application sub app or project list");
                                    ((HomeActivity) getActivity()).showErrorMessageAndFinishActivity(Constants.SOMETHING_WENT_WRONG, false);
                                }
                            }

                            @Override public void onLongItemClick(View view, int position) {
                                // do whatever
                            }
                        })
        );

        // Project Type Sorting
        AppMetaData appMetaData = UAAppContext.getInstance().getAppMDConfig();

        if (appMetaData == null) {
            // Error - no appMetaData
            Utils.logError(LogTags.HOME_SCREEN, "No AppMetaConfig for user after AppStartupThread2" +
                    "-- redirecting user to login screen");
            ((HomeActivity) getActivity()).invalidateLogin();
            ((HomeActivity) getActivity()).moveToLoginScreen();
        }

        if (appMetaData != null) {

            if(appMetaData.mSortType != null &&
                    !appMetaData.mSortType.isEmpty()) {
                // Sorting order exists
                ProjectTypeSortingService.mList = mProjectTypes;
                ProjectTypeSortingService.mSortType = appMetaData.mSortType;
                ProjectTypeSortingService.sort();
            }
        } else {
            // Default sorting : Alphabetica
            ProjectTypeSortingService.mList = mProjectTypes;
            ProjectTypeSortingService.mSortType = Constants.PROJECT_TYPE_ALPHABETICAL_SORTING;
            ProjectTypeSortingService.sort();
        }

        mAdapter = new GridFragmentAdapter(mRootProjectTypes, mProjectTypes);
        mRecyclerView.setAdapter(mAdapter);
        return view;
    }
}

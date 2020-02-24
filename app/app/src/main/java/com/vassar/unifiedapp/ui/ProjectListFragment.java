package com.vassar.unifiedapp.ui;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.futuremind.recyclerviewfastscroll.FastScroller;
import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.adapter.ProjectListAdapter;
import com.vassar.unifiedapp.application.UnifiedAppApplication;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.listener.RecyclerItemClickListener;
import com.vassar.unifiedapp.model.Project;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.ProjectListSortingService;
import com.vassar.unifiedapp.utils.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */

public class ProjectListFragment extends Fragment {

    private int GRID_SPAN = Constants.PROJECT_TYPE_GRID_SPAN;
    private List<Project> mProjectList = new ArrayList<>();
    private List<Project> mFilteredProjectList = new ArrayList<>();
    private long mListCreationTimestamp;
    private String mSortType;
    private RecyclerView mRecyclerView;
    private FastScroller mProjectlistScrollBar;

    private ProjectListAdapter mAdapter;
    private String mAppId ;
    private boolean mFilteringPresent;

    public ProjectListFragment() { }

    @SuppressLint("ValidFragment")
    public ProjectListFragment(List<Project> projectList,
                               List<Project> filteredProjectList,
                               long listCreationTimestamp, String sortType, String appId, boolean isFilteringPresent) {
        UAAppContext.getInstance().setProjectListCache(projectList);
        this.mProjectList.clear();
        this.mProjectList.addAll(projectList);
        this.mFilteredProjectList.clear();
        if(filteredProjectList != null && filteredProjectList.size() > 0) {
            UAAppContext.getInstance().setFilteredProjectListCache(filteredProjectList);
            this.mFilteredProjectList.clear();
            this.mFilteredProjectList.addAll(filteredProjectList);
        }
        this.mListCreationTimestamp = listCreationTimestamp;
        this.mSortType = sortType;
        this.mAppId = appId;
        this.mFilteringPresent = isFilteringPresent;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        Utils.getInstance().showLog("SAVESTATE", "PROJECT LIST ON SAVE INSTANCE STATE");
//        outState.putParcelableArrayList(Constants.PROJECT_LIST_FRAGMENT_PROJECTS, (ArrayList<? extends Parcelable>) mProjectList);
        outState.putLong(Constants.PROJECT_LIST_FRAGMENT_CREATION_TIMESTAMP, mListCreationTimestamp);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            // The fragment is being recreated
            Utils.getInstance().showLog("SAVESTATE", "PROJECT LIST FRAGMENT RESTORING FROM SAVED STATE");
            mListCreationTimestamp = savedInstanceState.getLong(Constants.PROJECT_LIST_FRAGMENT_CREATION_TIMESTAMP);
        } else {
            this.mProjectList.clear();
            this.mProjectList.addAll(UAAppContext.getInstance().getProjectListCache());

            this.mFilteredProjectList.clear();
            this.mFilteredProjectList.addAll(UAAppContext.getInstance().getFilteredProjectListCache());
        }
        
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_project_list, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.project_list_fragment_recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), GRID_SPAN));

        // Check for validity of projects
        flagInvalidProjects();
        mSortType = mSortType == null ? "ALPHABETICAL": mSortType;
        // Sorting the list
        ProjectListSortingService.mList = mProjectList;
        ProjectListSortingService.mSortType = mSortType;
        ProjectListSortingService.mUserLat = ((UnifiedAppApplication) getActivity().getApplication()).mUserLatitude;
        ProjectListSortingService.mUserLong = ((UnifiedAppApplication) getActivity().getApplication()).mUserLongitude;
        ProjectListSortingService.sort();

        TextView noProjectAvailableTextView = view.findViewById(R.id.no_project_available);
        if(!mFilteringPresent) {
            mProjectList = removeUnAssignedProjects(mProjectList);
            if(mProjectList != null &&  !mProjectList.isEmpty()) {
                System.out.println("ProjectS Available");
                } else {
                System.out.println("ProjectS NOT Available");
                noProjectAvailableTextView.setText(getResources().getString(R.string.NO_PROJECTS_AVAILABLE));
            }
            mAdapter = new ProjectListAdapter(getActivity(), mProjectList, mListCreationTimestamp, mAppId);

        } else {
            if(mFilteredProjectList != null && !mFilteredProjectList.isEmpty()) {
                mFilteredProjectList = removeUnAssignedProjects(mFilteredProjectList);
                mAdapter = new ProjectListAdapter(getActivity(), mFilteredProjectList, mListCreationTimestamp,mAppId);
            } else {
                noProjectAvailableTextView.setText(getResources().getString(R.string.NO_PROJECTS_AVAILABLE));
                Toast.makeText(getActivity(), getResources().getString(R.string.NO_FILTERING_RESULTS), Toast.LENGTH_LONG).show();
                mAdapter = new ProjectListAdapter(getActivity(), new ArrayList<>(), mListCreationTimestamp, mAppId);
            }
        }

        mRecyclerView.setAdapter(mAdapter);
        mProjectlistScrollBar = (FastScroller) view.findViewById(R.id.project_list_scrollbar);
        mProjectlistScrollBar.setRecyclerView(mRecyclerView);
        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), mRecyclerView ,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override public void onItemClick(View view, int position) {
                                Project project ;
                                if(mFilteredProjectList.size() > 0) {
                                    project = mFilteredProjectList.get(position) ;
                                } else {
                                    project = mProjectList.get(position);
                                }
                                if (project.mExpired) {
                                    Toast.makeText(getActivity(), getResources().getString(R.string.SUBMISSION_DATE_CROSSED), Toast.LENGTH_LONG).show();
                                } else if(!project.isAssigned()) {
                                    Toast.makeText(getActivity(), getResources().getString(R.string.PROJECT_ACCESS_INVOKED), Toast.LENGTH_LONG).show();
                                }
//                                else if(project.mState == null || !project.mState.equalsIgnoreCase("In Progress")){
//                                    Toast.makeText(getActivity(), Constants.SUBMISSION_UNAVAILABLE, Toast.LENGTH_LONG).show();
//                                }
                                else {
                                    ((ProjectListActivity) getActivity())
                                            .moveToProjectFormActivity(project);
                                }
                            }

                            @Override public void onLongItemClick(View view, int position) {
                                // do whatever
                            }
                        }
                )
        );
        Utils.getInstance().showLog("TIMESTAMP PROJECT LIST COMPLETELY LOADED", String.valueOf(System.currentTimeMillis()));

        return view;
    }

    private void flagInvalidProjects() {
        if (mProjectList != null && mProjectList.size() > 0) {
            for (int i=0; i<mProjectList.size(); i++) {
                if (mProjectList.get(i).mLastSubDate != null) {
                    if (mListCreationTimestamp > Long.parseLong(mProjectList.get(i).mLastSubDate)) {
                        mProjectList.get(i).mExpired = true;
                    }
                }
            }
        }
    }

    private List<Project> removeUnAssignedProjects (List<Project> projectList) {

        if (projectList != null && !projectList.isEmpty()) {
            Iterator<Project> iterator = projectList.iterator();
            while (iterator.hasNext()) {
                Project project = iterator.next();
                if (project!= null && !project.mAssigned) {
                    iterator.remove();
                }
            }
        }
        return projectList;
    }
}

package com.vassar.unifiedapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.adapter.ProjectListSeparatorAdapter;
import com.vassar.unifiedapp.listener.RecyclerItemClickListener;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.ProjectList;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;

public class ProjectSeparatorActivity extends BaseActivity {

    private String mAppId;
    private String mUserId;

    private ArrayList<String> mUserTypes = new ArrayList<>();
    private boolean mShouldShowMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_seperator);

        // Data sent along with the Intent from HomeActivity
        mAppId = getIntent().getStringExtra("appId");
        Utils.getInstance().showLog("PROJECT LIST APP ID", mAppId);
        mUserId = getIntent().getStringExtra("userId");
        Utils.getInstance().showLog("PROJECT LIST USER ID", mUserId);

        initializeComponents();

        initializeViews();
    }

    /** Initializes variables, loads the ProjectListConfig */
    private void initializeComponents() {
        initComponents(this);
        // TODO: Uncomment
        String projectListConfigString = null;
//        String projectListConfigString = mDBHelper.getConfigFile
//                (Constants.PROJECT_LIST_CONFIG_DB_NAME + mAppId + mUserId);
        if (projectListConfigString != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                ProjectList projectListConfig = objectMapper.readValue(projectListConfigString, ProjectList.class);
                mUserTypes.clear();
                mUserTypes.addAll(projectListConfig.mUserTypes);
                mShouldShowMap = projectListConfig.mShouldShowMap;
            }catch (IOException e) {
                Utils.logError(LogTags.PROJECT_LIST,"Failed to parse json :: " + projectListConfigString);
                e.printStackTrace();
            }
        }
    }

    private void initializeViews() {
        RecyclerView recyclerView = (RecyclerView) findViewById
                (R.id.project_list_separator_activity_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ProjectListSeparatorAdapter(this, mInflater, mUserTypes));
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, recyclerView ,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override public void onItemClick(View view, int position) {
                                String userTypeClicked = mUserTypes.get(position);
                                Intent intent = new Intent(ProjectSeparatorActivity.this
                                        , ProjectListActivity.class);
                                intent.putExtra("userType", userTypeClicked);
                                intent.putExtra("showMap", mShouldShowMap);
                                intent.putExtra("appId", mAppId);
                                intent.putExtra("userId", mUserId);
                                startActivity(intent);
                            }

                            @Override public void onLongItemClick(View view, int position) {
                                // do whatever
                            }
                        })
        );
    }
}

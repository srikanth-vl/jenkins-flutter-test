package com.vassar.unifiedapp.ui;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.adapter.TransactionLogAdapter;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.model.TransactionLogObject;

import java.util.List;

public class TransactionLogActivity
        extends BaseActivity {

    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_log);

        initComponents(this);

        initializeToolbar();

        hideActionBar(this);
        String appId = getIntent().getStringExtra("app_id");
        initializeViews(appId);
    }

    private void initializeViews(String appId) {
        mRecyclerView = (RecyclerView) findViewById(R.id.transaction_log);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //TODO: Get number of entries in transaction log from config file
        List<TransactionLogObject> transactionLogList = UAAppContext.getInstance().getDBHelper().getLastNSubmissionsForUser(appId, UAAppContext.getInstance().getUserID(), 5);
        mRecyclerView.setAdapter(new TransactionLogAdapter(appId, transactionLogList, this));
    }

    private void initializeToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.logs_toolbar);
        toolbar.setTitle(getResources().getString(R.string.app_name));
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        setSupportActionBar(toolbar);
    }
}

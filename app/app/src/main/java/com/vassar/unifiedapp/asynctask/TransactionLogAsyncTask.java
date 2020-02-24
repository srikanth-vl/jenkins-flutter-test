package com.vassar.unifiedapp.asynctask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.api.TransactionLogService;
import com.vassar.unifiedapp.err.AppCriticalException;
import com.vassar.unifiedapp.err.ServerFetchException;
import com.vassar.unifiedapp.ui.ProjectFormActivity;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.Utils;

import java.util.List;

public class TransactionLogAsyncTask extends AsyncTask<Void, Void, List<String>>{

    private String appId;
    private String projectId;
    private String key;
    @SuppressLint("StaticFieldLeak")
    private Activity activity;
    @SuppressLint("StaticFieldLeak")
    private View view;

//    public TransactionLogAsyncTask(String appId, String projectId, String key) {
//        this.appId = appId;
//        this.projectId = projectId;
//        this.key = key;
//    }

    public TransactionLogAsyncTask(String appId, String projectId, String key, Activity activity, View view) {
        this.appId = appId;
        this.projectId = projectId;
        this.key = key;
        this.activity = activity;
        this.view = view;
    }

    @Override
    protected List<String> doInBackground(Void... voids) {
        TransactionLogService transactionLogService = new TransactionLogService();
        List<String> transactionLog = null;
        try {
            if(Utils.getInstance().isOnline(null)) {
                transactionLog = transactionLogService.callTransactionLogService(appId, projectId, key);
            } else{
                Toast.makeText((ProjectFormActivity)activity, activity.getResources().getString(R.string.CHECK_INTERNET_CONNECTION), Toast.LENGTH_SHORT).show();
            }
        } catch (AppCriticalException e) {
            e.printStackTrace();
        } catch (ServerFetchException e) {
            e.printStackTrace();
        }
        return transactionLog;
    }

    @Override
    protected void onPreExecute() {
        ((ProjectFormActivity) activity).showProgressBar();
        super.onPreExecute();
    }


    @Override
    protected void onPostExecute(List<String> strings) {
        ((ProjectFormActivity) activity).hideProgressBar();
        PopupMenu popupMenu = new PopupMenu((ProjectFormActivity)activity, view);

        if(strings != null && !strings.isEmpty()) {
            popupMenu.getMenu().add("Date                Time                Value");
            for (String log : strings) {
                log = log.replaceFirst(Constants.DEFAULT_DELIMITER, "   ");
                log = log.replaceAll(Constants.DEFAULT_DELIMITER, "          ");
                popupMenu.getMenu().add(log);// menus items
            }
            popupMenu.show(); //showing popup menu
        } else {
            Toast.makeText((ProjectFormActivity)activity, "No prior submissions.", Toast.LENGTH_SHORT).show();
        }
        super.onPostExecute(strings);
    }
}

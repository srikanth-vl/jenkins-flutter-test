package com.vassar.unifiedapp.application;

import android.app.Application;

import com.vassar.unifiedapp.application.helper.UniappControllerHelper;
import com.vassar.unifiedapp.err.AppCriticalException;

public class UniappController1
        extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

//        // Load initial properties related only to the App (nothing project specific)
//        UniappControllerHelper.getInstance().initProperties(this);
//
//        try {
//            // Load App MD Config from DB
//            //   If does not exist then read from server --> AppMDConfigService.readFromServer() & Save to DB
//            UniappControllerHelper.getInstance().initAppMDConfig();
//
//        } catch (AppCriticalException a) {
//            a.printStackTrace();
//            // Show error message and exit
//        }
    }
}

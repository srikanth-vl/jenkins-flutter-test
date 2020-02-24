package com.vassar.unifiedapp.application.helper;

import android.app.Application;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.err.AppCriticalException;

public class UniappControllerHelper {

    private static final UniappControllerHelper ourInstance = new UniappControllerHelper();

    public static UniappControllerHelper getInstance() {
        return ourInstance;
    }

    private UniappControllerHelper() {
        // Do Nothing here for now
    }

    public void initProperties(Application application) {

        UAAppContext appContext = UAAppContext.getInstance();

        appContext.setContext(application);

        // Initialize DBHelper
        appContext.initDBHelper();

        // Initialize AppPreferences
        appContext.initSharedPreferences();
    }

    public void initAppMDConfig()
        throws AppCriticalException {


    }
//    private AppMetaData fetchAppMetaData() {
//        boolean result = true;
//        AppMetaConfigService appMetaConfigService = new AppMetaConfigService(
//                new AppMetaConfigServiceListener() {
//                    @Override
//                    public void AppMetaConfigTaskCompleted(String appMetaConfigString) {
//                        // Do Nothing
//                    }
//
//                    @Override
//                    public void AppMetaConfigTaskFailed(String message) {
//                        // TODO : Can we retry?????
//                        result = false;
//                    }
//                });
//        return appMetaConfigService.callAppMetaDataService();
//
}

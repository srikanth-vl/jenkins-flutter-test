package com.vassar.unifiedapp.newflow;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.application.helper.UniappControllerHelper;

import java.util.concurrent.ExecutionException;

public class NewSplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        System.out.println("Start of NewSplashScreen");

        setContentView(R.layout.activity_splash_screen);

        // Load initial properties related only to the App (nothing project specific)
        UniappControllerHelper.getInstance().initProperties(getApplication());

        System.out.println("Start of NewSplashScreen - After init properties");

        // Create AppStartup thread
//        AppStartupThread appStartupThread = new AppStartupThread();
//        try {
//            appStartupThread.execute().get();
//        } catch (ExecutionException | InterruptedException e) {
//            e.printStackTrace();
//        }
    }
}

package com.vassar.unifiedapp.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.vassar.unifiedapp.R;

public class SettingActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        if (findViewById(R.id.fragment_container)!=null){
            if (savedInstanceState!=null)
                return;

            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container,new SettingFragment())
                    .commit();

        }
    }

}

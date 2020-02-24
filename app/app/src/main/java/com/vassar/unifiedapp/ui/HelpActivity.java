package com.vassar.unifiedapp.ui;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.PropertyReader;

public class HelpActivity extends BaseActivity implements View.OnClickListener{

    TextView mTvAppDescription,mTvPhone,mTvEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        initializeToolbar();

        mTvAppDescription=findViewById(R.id.tv_app_description);
        mTvPhone=findViewById(R.id.tv_phone);
        mTvEmail=findViewById(R.id.tv_email);

        mTvAppDescription.setText(Constants.APP_DESCRIPTION);



        mTvPhone.setOnClickListener(this);
        mTvEmail.setOnClickListener(this);
    }

    private void initializeToolbar() {
            Toolbar toolbar = findViewById(R.id.home_toolbar);
            setSupportActionBar(toolbar);
            toolbar.setTitle(getResources().getString(R.string.app_name));
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));

        }


    @Override
    public void onClick(View v) {
        if (v==mTvPhone){

            String number=mTvPhone.getText().toString();
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:"+number));
            startActivity(callIntent);

        }
        if (v==mTvEmail){

            String email=mTvEmail.getText().toString();
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", email, null));
            startActivity(Intent.createChooser(emailIntent, null));

        }
    }
}

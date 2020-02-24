package com.vassar.unifiedapp.newcamera;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import com.vassar.unifiedapp.R;

import com.squareup.picasso.Picasso;
import com.vassar.unifiedapp.camera2.NewCamera2Activity;
import com.vassar.unifiedapp.ui.BaseActivity;
import com.vassar.unifiedapp.utils.Utils;

import java.io.File;

public class CameraResultImage extends BaseActivity {

    Button cancel;
    Button accept;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_result_image);

        cancel = (Button) findViewById(R.id.cancel_button);
        accept = (Button) findViewById(R.id.accept_button);

        String path = getIntent().getStringExtra("path");

        Utils.logInfo("IMAGEPATH", path);
        File file = new File(path);

        Picasso.get().load(file).placeholder(R.drawable.placeholder).resize(768, 1024)
                .into((ImageView) findViewById(R.id.camera_result_imageview));

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    i = new Intent(CameraResultImage.this, NewCamera2Activity.class);
                } else {
                    i = new Intent(CameraResultImage.this, CameraActivity.class);
                }

                setResult(RESULT_CANCELED, i);
                finish();
            }
        });
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    i = new Intent(CameraResultImage.this, NewCamera2Activity.class);
                } else {
                    i = new Intent(CameraResultImage.this, CameraActivity.class);
                }
                setResult(RESULT_OK, i);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}

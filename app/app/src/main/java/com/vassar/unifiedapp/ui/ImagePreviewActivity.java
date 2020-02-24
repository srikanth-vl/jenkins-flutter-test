package com.vassar.unifiedapp.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.utils.Utils;

import java.io.File;

public class ImagePreviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        String imagePath = getIntent().getStringExtra("image_path");
        Utils.getInstance().showLog("Preview Image Path ", imagePath);

        ImageView imageView = (ImageView) findViewById(R.id.image_preview_activity_view);
        TextView textView = (TextView) findViewById(R.id.image_preview_textview);

        File imgFile = new  File(imagePath);

        if(imgFile.exists()) {
            Picasso.get().load(imgFile).resize(768, 1024)
                    .into(imageView);
        } else {
            // Preview not available
            imageView.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}

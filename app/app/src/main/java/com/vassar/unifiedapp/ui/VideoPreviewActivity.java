package com.vassar.unifiedapp.ui;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.utils.Utils;

public class VideoPreviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_preview);

        String imagePath = getIntent().getStringExtra("video_path");
        Utils.getInstance().showLog("Preview Image Path ", imagePath);

        VideoView videoView =(VideoView)findViewById(R.id.video_preview);

        MediaController mediaController= new MediaController(this);
        mediaController.setAnchorView(videoView);

        //specify the location of media file
        Uri uri = Uri.parse(imagePath);

        //Setting MediaController and URI, then starting the videoView
        videoView.setMediaController(mediaController);
        videoView.setVideoURI(uri);
        videoView.requestFocus();
        videoView.start();
    }
}

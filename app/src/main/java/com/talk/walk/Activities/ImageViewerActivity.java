package com.talk.walk.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.bumptech.glide.Glide;
import com.jsibbold.zoomage.ZoomageView;
import com.talk.walk.R;

public class ImageViewerActivity extends AppCompatActivity {

    private ZoomageView zoomageView;
    private CardView cvImagePreviewBack;
    private String chat_media_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        chat_media_url = getIntent().getStringExtra("chat_media_url");

        zoomageView = findViewById(R.id.myZoomageView);
        cvImagePreviewBack = findViewById(R.id.cvImagePreviewBack);
        Glide.with(this).load(chat_media_url).into(zoomageView);

        cvImagePreviewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }
}
package com.talk.walk.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.talk.walk.R;

public class VideoViewerActivity extends AppCompatActivity {

    private static final Object TAG = VideoViewerActivity.class.getSimpleName();
    private VideoView vvVideoView;
    private ProgressBar pbVideoView;

    private String media_url;
    private TextureView mTextureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_viewer);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        media_url = getIntent().getStringExtra("media_url");

        vvVideoView = findViewById(R.id.vvVideoView);
        pbVideoView = findViewById(R.id.pbVideoView);
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(vvVideoView);
        vvVideoView.setMediaController(mediaController);
        vvVideoView.setVideoPath(media_url);
        vvVideoView.start();

        pbVideoView.setVisibility(View.VISIBLE);

        vvVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                pbVideoView.setVisibility(View.GONE);
                float videoRatio = mp.getVideoWidth() / (float) mp.getVideoHeight();
                float screenRatio = vvVideoView.getWidth() / (float)
                        vvVideoView.getHeight();
                float scaleX = videoRatio / screenRatio;
                if (scaleX >= 1f) {
                    vvVideoView.setScaleX(scaleX);
                } else {
                    vvVideoView.setScaleY(1f / scaleX);
                }
            }
        });



    }

//    private void scaleVideo(MediaPlayer mPlayer) {
//
//        ViewGroup.LayoutParams videoParams = (ViewGroup.LayoutParams) mTextureView
//                .getLayoutParams();
//        DisplayMetrics dm = new DisplayMetrics();
//        VideoViewerActivity.this.getWindowManager().getDefaultDisplay()
//                .getMetrics(dm);
//
//        final int height = dm.heightPixels;
//        final int width = dm.widthPixels;
//        int videoHeight = mPlayer.getVideoHeight();
//        int videoWidth = mPlayer.getVideoWidth();
//        double hRatio = 1;
//
//        hRatio = (height * 1.0 / videoHeight) / (width * 1.0 / videoWidth);
//        videoParams.x = (int) (hRatio <= 1 ? 0 : Math.round((-(hRatio - 1) / 2)
//                * width));
//        videoParams.y = (int) (hRatio >= 1 ? 0 : Math
//                .round((((-1 / hRatio) + 1) / 2) * height));
//        videoParams.width = width - videoParams.x - videoParams.x;
//        videoParams.height = height - videoParams.y - videoParams.y;
//        Log.e(TAG, "x:" + videoParams.x + " y:" + videoParams.y);
//        mTextureView.setScaleX(1.00001f);//<-- this line enables smoothing of the picture in TextureView.
//        mTextureView.requestLayout();
//        mTextureView.invalidate();
//
//    }
}
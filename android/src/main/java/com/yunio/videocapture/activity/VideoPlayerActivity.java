package com.yunio.videocapture.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.widget.FrameLayout;
import com.yunio.videocapture.view.VideoPlayerView;

/**
 * Created by Henry on 2018/2/5.
 */

public class VideoPlayerActivity extends BaseActivity {
    private final static String VIDEO_PATH = "video_path";
    private VideoPlayerView mVideoPlayerView;

    public static void startVideoPlayer(Activity context, String path) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        intent.putExtra(VIDEO_PATH, path);
        context.startActivity(intent);
    }

    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        String path = this.getIntent().getStringExtra(VIDEO_PATH);
        mVideoPlayerView = new VideoPlayerView(getApplicationContext());
        this.setContentView(mVideoPlayerView);
        setFullScreen();
        mVideoPlayerView.setVideoPath(path);
        mVideoPlayerView.startPlay();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setFullScreen();
        }
    }

    private void setFullScreen() {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        mVideoPlayerView.setLayoutParams(layoutParams);
    }
}

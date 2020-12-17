package com.yunio.videocapture.utils;

import android.content.Intent;

import com.yunio.videocapture.activity.CaptureVideoActivity;

public class Constant {
    public static final String EXTRA_WIDTH = "width";
    public static final String EXTRA_QUALITY = "quality";
    public static final String EXTRA_IMAGE_PATHS = "image_paths";
    public static final String EXTRA_VIDEOS = "videos";

    public static final String EXTRA_VIDEO_PATH = "video_path";
    public static final String EXTRA_IMAGE_PATH = "image_path";

    public static void addImageParams(Intent intent, int width, int quality) {
        intent.putExtra(EXTRA_WIDTH, width);
        intent.putExtra(EXTRA_QUALITY, quality);
    }

    public static int getWidthFromIntent(Intent intent) {
        return intent.getIntExtra(EXTRA_WIDTH, 0);
    }

    public static int getQualityFromIntent(Intent intent) {
        return intent.getIntExtra(EXTRA_QUALITY, 0);
    }
}

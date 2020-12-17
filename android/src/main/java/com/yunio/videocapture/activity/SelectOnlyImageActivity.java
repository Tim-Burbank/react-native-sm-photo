package com.yunio.videocapture.activity;

import android.content.Context;
import android.content.Intent;

public class SelectOnlyImageActivity extends SelectImageActivity {

    public static Intent createLauncherIntent(Context context, int maxCount, int width,
                                              int quality, int minCount) {
        Intent intent = new Intent(context, SelectOnlyImageActivity.class);
        fillImageIntentData(intent, maxCount, width, quality, minCount);
        return intent;
    }

    @Override
    public boolean isSupportGif() {
        return false;
    }
}

package com.yunio.videocapture;

import android.app.Application;

public class MyApplication extends Application {
    // Build.VERSION_CODES.M
    public static final int NEED_CHECK_PERMISSION_VERION_CODE = 23;

    @Override
    public void onCreate() {
        super.onCreate();
        BaseInfoManager.init(getApplicationContext());
    }
}

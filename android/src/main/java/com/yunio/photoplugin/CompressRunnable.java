package com.yunio.photoplugin;

import android.text.TextUtils;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableNativeMap;
import com.yunio.videocapture.BaseInfoManager;
import com.yunio.videocapture.utils.FileUtils;

public class CompressRunnable implements Runnable {
    private final static String EXTRA_ORIGINAL = "original";
    private final static String EXTRA_RESULT = "result";
    private String path;
    private int maxDuration;
    private int mQuality;
    private Promise promise;

    public CompressRunnable(String path, int maxDuration, int quality, Promise promise) {
        this.path = path;
        this.maxDuration = maxDuration;
        this.mQuality = quality;
        this.promise = promise;
    }

    @Override
    public void run() {
        if (TextUtils.isEmpty(path)) {
            promise.reject(new Throwable("file not exists"));
        }
        if (!FileUtils.exists(path)) {
            promise.reject(new Throwable("file not exists"));
        }
        try {
            final String outPath = FileUtils.compressVideo(path, maxDuration, mQuality);
            BaseInfoManager.getInstance().getMainHandler().post(new Runnable() {
                @Override
                public void run() {
                    WritableNativeMap map = new WritableNativeMap();
                    map.putString(EXTRA_ORIGINAL, path);
                    map.putString(EXTRA_RESULT, outPath);
                    promise.resolve(map);
                }
            });
        } catch (Exception e) {
            promise.reject(new Throwable("error in compression"));
        }
    }
}

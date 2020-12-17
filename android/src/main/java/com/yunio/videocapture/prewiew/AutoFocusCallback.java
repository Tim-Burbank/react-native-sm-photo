package com.yunio.videocapture.prewiew;

import android.hardware.Camera;
import android.os.Handler;

import com.yunio.videocapture.utils.LogUtils;

public final class AutoFocusCallback implements Camera.AutoFocusCallback {
    private static final String TAG = AutoFocusCallback.class.getName();
    private static final long AUTO_FOCUS_INTERVAL_MS = 1300L; //自动对焦时间
    private Handler mAutoFocusHandler;
    private int mAutoFocusMessage;

    void setHandler(Handler autoFocusHandler, int autoFocusMessage) {
        this.mAutoFocusHandler = autoFocusHandler;
        this.mAutoFocusMessage = autoFocusMessage;
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        LogUtils.d(TAG, "auto focus : " + success);
        if (mAutoFocusHandler != null) {
            mAutoFocusHandler.sendEmptyMessageDelayed(mAutoFocusMessage, AUTO_FOCUS_INTERVAL_MS);
        }
    }
}

package com.yunio.videocapture.utils;

import android.util.Log;

public class LogUtils {

    public static boolean isDebug() {
        return true;
    }

    public static void d(String tag, String msg) {
        if (isDebug()) {
            Log.d(tag, msg);
        }
    }

    public static void d(String tag, String msg, Object... args) {
        if (isDebug()) {
            Log.d(tag, String.format(msg, args));
        }
    }

    public static void e(String tag, String msg) {
        if (isDebug()) {
            Log.e(tag, msg);
        }
    }

    public static void e(String tag, String msg, Object... args) {
        if (isDebug()) {
            Log.e(tag, String.format(msg, args));
        }
    }
}

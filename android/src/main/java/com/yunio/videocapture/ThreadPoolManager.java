package com.yunio.videocapture;

import android.os.Handler;
import android.os.HandlerThread;

import com.yunio.videocapture.utils.ServerThreadPool;

public class ThreadPoolManager {

    private static ServerThreadPool sDefaultThreadPool = ServerThreadPool.newInstance();
    private static ServerThreadPool sBitmapRequestThreadPool = ServerThreadPool.newInstance(3, 5);
    private static ServerThreadPool sSingleThreadPool = ServerThreadPool.newSingleThreadInstance();
    private static Handler sDatabaseHandler;

    public static ServerThreadPool getDefaultThreadPool() {
        return sDefaultThreadPool;
    }


    public static ServerThreadPool getBitmapRequestThreadPool() {
        return sBitmapRequestThreadPool;
    }

    public static ServerThreadPool getSingleThreadInstance() {
        return sSingleThreadPool;
    }

    public synchronized static Handler getDatabaseHandler() {
        if (sDatabaseHandler == null) {
            HandlerThread handlerThread = new HandlerThread("database");
            handlerThread.start();
            sDatabaseHandler = new Handler(handlerThread.getLooper());
        }
        return sDatabaseHandler;
    }
}

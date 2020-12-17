package com.yunio.videocapture;

import android.content.Context;
import android.os.Handler;
import java.io.File;

import com.yunio.videocapture.cache.BitmapLruCache;
import com.yunio.videocapture.cache.DiskFileCache;
import com.yunio.videocapture.utils.FileUtils;
import com.yunio.videocapture.utils.ToastUtils;
import com.yunio.videocapture.utils.UIUtils;

/**
 * 基础信息管理, 需要application中调用{@link #init(Context)}方法
 * 
 * @author PeterZhang
 * 
 */
public class BaseInfoManager {
    /** 外部sdcard时最大缓存字节数 */
    private static final int MAX_EXTERNAL_DISK_CACHE_SIZE = 1024 * 1024 * 256;
    /** 没有外部sdcard, 使用内部存储空间时的最大缓存大小 */
    private static final int MAX_INTERNAL_DISK_CACHE_SIZE = 1024 * 1024 * 10;
    private Context mContext;
    private Handler mMainHandler;
    private BitmapLruCache mBitmapLruCache;
    private DiskFileCache mDiskCache;

    private static BaseInfoManager sInstance;

    public static void init(Context context) {
        if (sInstance == null) {
            sInstance = new BaseInfoManager(context.getApplicationContext());
            sInstance.init();
        }
    }

    public static BaseInfoManager getInstance() {
        return sInstance;
    }

    public Context getContext() {
        return mContext;
    }

    /**
     * 取得主线程Handler
     * 
     * @return
     */
    public Handler getMainHandler() {
        return mMainHandler;
    }

    public DiskFileCache getDiskCache() {
        return mDiskCache;
    }

    public BitmapLruCache getBitmapLruCache() {
        return mBitmapLruCache;
    }

    private void init() {
        UIUtils.init(mContext);
        ToastUtils.init(mContext);
        initImageCache();
    }

    private BaseInfoManager(Context context) {
        mContext = context;
        mMainHandler = new Handler();
    }

    /**
     * 初始化磁盘图片缓存
     */
    private void initImageCache() {
        // memory cache
        long maxMemory = Runtime.getRuntime().maxMemory();
        mBitmapLruCache = new BitmapLruCache((int) (maxMemory / 8));
        // disk cache
        File imageDir = FileUtils.getCacheDir();
        boolean isExternal = true;
        if (imageDir == null) {
            imageDir = new File(mContext.getFilesDir(), "cache");
            isExternal = false;
        }
        mDiskCache = new DiskFileCache(imageDir,
                isExternal ? MAX_EXTERNAL_DISK_CACHE_SIZE : MAX_INTERNAL_DISK_CACHE_SIZE);
    }
}

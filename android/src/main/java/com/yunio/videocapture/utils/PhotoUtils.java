package com.yunio.videocapture.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;
import android.text.TextUtils;

import com.facebook.react.bridge.ReactApplicationContext;
import com.yunio.photoplugin.R;
import com.yunio.videocapture.BaseInfoManager;

import java.io.File;

public class PhotoUtils {
    private static final String TAG = "PhotoUtils";
    /**
     * 图片最大宽高
     */
    public static final int MAX_SIZE = 480;
    /**
     * 图片最大宽或高，因为超过此限制很多手机就无法加载图片
     */
    private static final int MAX_LARGE_SIZE = 4096;
    /**
     * 图片最大文件大小
     */
    // public static final int MAX_FILE_LENGTH = 1024 * 500;
    private static final int MAX_FILE_LENGTH = Integer.MAX_VALUE;

    private final static String MEDIA_TYPE_JPG = ".jpg";

    private static final String sFileProvider = BaseInfoManager.getInstance().getContext().getPackageName() + "" +
            ".fileprovider";

    /**
     * 生成图片文件名称
     *
     * @return
     */
    public static String generateFileName() {
        File imageDir = FileUtils.getImageDir();
        if (imageDir == null) {
            ToastUtils.showToast(R.string.storage_error);
            return null;
        }
        long id = System.currentTimeMillis();
        int i = 1;
        File file = new File(imageDir, id + MEDIA_TYPE_JPG);
        while (file.exists()) {
            file = new File(imageDir, id + "_" + i + MEDIA_TYPE_JPG);
            i++;
        }
        return file.getAbsolutePath();
    }

    /**
     * 生成图片文件名称
     *
     * @return
     */
    public static String generateFileName(Context context) {
        File imageDir = FileUtils.getImageDir(context);
        if (imageDir == null) {
            ToastUtils.showToast(R.string.storage_error);
            return null;
        }
        long id = System.currentTimeMillis();
        int i = 1;
        File file = new File(imageDir, id + MEDIA_TYPE_JPG);
        while (file.exists()) {
            file = new File(imageDir, id + "_" + i + MEDIA_TYPE_JPG);
            i++;
        }
        return file.getAbsolutePath();
    }


    /**
     * 用于图片压缩, 为了节省用户的流量, 本地解析图片速度也会更快。 如果是不删除原文件的情况下压缩, 则 以原文件的md5码作为目标文件的名称
     *
     * @param srcPath
     * @param deleteSource
     * @return
     */
    public final static String compress(String srcPath, boolean deleteSource) {
        return compress(srcPath, deleteSource, MAX_SIZE, 100);
    }

    /**
     * 用于图片压缩, 为了节省用户的流量, 本地解析图片速度也会更快。 如果是不删除原文件的情况下压缩, 则 以原文件的md5码作为目标文件的名称
     *
     * @param srcPath
     * @param deleteSource
     * @return
     */
    public final static String compress(String srcPath, boolean deleteSource, int maxMinSize,
                                        int quality) {
        File srcFile = new File(srcPath);
        if (!srcFile.exists()) {
            return srcPath;
        }
        String destPath = null;
        if (deleteSource) {
            destPath = srcPath + ".tmp";
        } else {
            String md5 = FileUtils.getFileMD5(srcFile);
            if (TextUtils.isEmpty(md5)) {
                destPath = generateFileName();
            } else {
                File imageDir = FileUtils.getImageDir();
                if (imageDir == null) {
                    ToastUtils.showToast(R.string.storage_error);
                    return null;
                }
                File destFile = new File(imageDir, md5 + MEDIA_TYPE_JPG);
                destPath = destFile.getAbsolutePath();
                if (destFile.exists()) {
                    LogUtils.d(TAG, "the dest file: %s of %s is exits", srcPath, destPath);
                    return destPath;
                }
            }
        }
        boolean success = BitmapUtils.compress(srcPath, destPath, maxMinSize, MAX_LARGE_SIZE,
                MAX_FILE_LENGTH, true, quality);
        if (success) {
            if (deleteSource) {
                File destFile = new File(destPath);
                destFile.renameTo(srcFile);
                destPath = srcPath;
            }
        } else {
            destPath = srcPath;
        }
        return destPath;
    }

    /**
     * 启动拍照Activity
     *
     * @param filePath
     */
    public static void startTakePhoto(Context context, int requestCode, String filePath) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        useCameraCompat(context, intent, filePath);
        if (context instanceof ReactApplicationContext) {
            ((ReactApplicationContext) context).startActivityForResult(intent, requestCode, null);
        } else if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(intent, requestCode, null);
        }
    }

    // 兼容N以上
    public static void useCameraCompat(Context context, Intent intent, String filePath) {
        useCameraCompat(context, intent, new File(filePath));
    }

    public static void useCameraCompat(Context context, Intent intent, File file) {
        Uri uri = getFileUriCompat(context, intent, file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
    }

    public static Uri getFileUriCompat(Context context, Intent intent, File file) {
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, sFileProvider, file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }
}

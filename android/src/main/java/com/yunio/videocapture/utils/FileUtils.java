package com.yunio.videocapture.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Video.VideoColumns;
import android.text.TextUtils;
import android.util.Log;

import com.yunio.ffmpeg.FfmpegUtils;
import com.yunio.photoplugin.R;
import com.yunio.videocapture.BaseInfoManager;
import com.yunio.videocapture.entity.Folder;
import com.yunio.videocapture.resource.ResourceConfigHelper;
import com.yunio.videocapture.resource.entity.StringConfig;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class FileUtils {
    public static final String MIMETYPE_VIDEO = ".3gp.mp4.rmvb.avi.wmv.mov.mkv.asf.flv.mpg.vob.";
    private static final String TAG = "FileUtil";
    private static String sApplicationDir;
    private static final int MAX_WIDTH = 540;
    private static final int MAX_HEIGHT = 960;

    public static boolean mediaMounted() {
        String state = Environment.getExternalStorageState();
        LogUtils.d(TAG, "external storage state " + state);
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    public static String getAppDir() {
        if (!mediaMounted()) {
            LogUtils.e(TAG, "no media mounted!");
            return null;
        }
        if (sApplicationDir == null) {
            File fileDir = BaseInfoManager.getInstance().getContext().getExternalFilesDir("");
            if (fileDir == null || !fileDir.exists()) {
                return null;
            }
            sApplicationDir = parent(fileDir);
        }
        return sApplicationDir;
    }

    public static String getAppDir(Context context) {
        if (!mediaMounted()) {
            LogUtils.e(TAG, "no media mounted!");
            return null;
        }
        if (sApplicationDir == null) {
            File fileDir = context.getExternalFilesDir("");
            if (fileDir == null || !fileDir.exists()) {
                return null;
            }
            sApplicationDir = parent(fileDir);
        }
        return sApplicationDir;
    }

    public static File getImageDir() {
        String appDir = getAppDir();
        if (appDir != null) {
            return mkdir(appDir + File.separator + "image");
        }
        return null;
    }

    public static File getImageDir(Context context) {
        String appDir = getAppDir(context);
        if (appDir != null) {
            return mkdir(appDir + File.separator + "image");
        }
        return null;
    }

    public static File getVideoDir() {
        String appDir = getAppDir();
        if (appDir != null) {
            return mkdir(appDir + File.separator + "video");
        }
        return null;
    }

    public static File getCacheDir() {
        String appDir = getAppDir();
        if (appDir != null) {
            return mkdir(appDir + File.separator + "cache");
        }
        return null;
    }

    public static File getFileDir() {
        String appDir = getAppDir();
        if (appDir != null) {
            return mkdir(appDir + File.separator + "files");
        }
        return null;
    }

    public static File getFileCacheDir() {
        File fileDir = getFileDir();
        if (fileDir != null) {
            return mkdir(fileDir.getAbsolutePath() + File.separator + "cache");
        }
        return null;
    }

    /**
     * 通过文件的位置读取文件MD5
     */
    public static String getFileMD5(File file) {
        byte[] digests = null;
        try {
            byte buffer[] = new byte[1024 * 16];
            int count = 0;
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            FileInputStream in = new FileInputStream(file);
            while ((count = in.read(buffer)) != -1) {
                md5.update(buffer, 0, count);
            }
            in.close();
            digests = md5.digest();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (digests == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < digests.length; i++) {
            int v = digests[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static File mkdir(String absPath) {
        File file = new File(absPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    public static String parent(File file) {
        if (file == null) {
            return null;
        } else {
            return file.getParent();
        }
    }


    public static boolean delete(String absPath) {
        if (TextUtils.isEmpty(absPath)) {
            return false;
        }

        File file = new File(absPath);
        return delete(file);
    }

    public static boolean delete(File file) {
        if (!exists(file)) {
            return true;
        }

        if (file.isFile()) {
            return file.delete();
        }

        boolean result = true;
        File[] files = file.listFiles();
        if (files == null) {
            result |= file.delete();
            return result;
        }
        for (int index = 0; index < files.length; index++) {
            if (files[index] != null) {
                result |= delete(files[index]);
            }
        }
        result |= file.delete();

        return result;
    }

    public static long size(String absPath) {
        if (absPath == null) {
            return 0;
        }
        File file = new File(absPath);
        return file.length();
    }

    public static boolean exists(String absPath) {
        if (TextUtils.isEmpty(absPath)) {
            return false;
        }
        File file = new File(absPath);
        return exists(file);
    }

    public static boolean exists(File file) {
        if (file == null) {
            LogUtils.d(TAG, "no such file");
            return false;
        }
        return file.exists();
    }

    public final static boolean isHide(String filePath) {
        File file = new File(filePath == null ? "" : filePath);
        if (!file.exists() || file.isHidden())
            return true;
        else if (file.getName().startsWith(".")) {
            return true;
        } else if (file.getParent() != null && !file.getParent().equals("")
                && file.getParent().equals("/"))
            return isHide(file.getParent());
        return false;
    }

    public final static String getFileName(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        int index = filePath.lastIndexOf("/");
        if (index > 0) {
            filePath = filePath.substring(index + 1, filePath.length());
        }
        return filePath;
    }

    public final static String getSuffix(String filePath) {
        filePath = getFileName(filePath);
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        try {
            int index = filePath.lastIndexOf(".");
            if (index < 0) {
                return null;
            }
            filePath = filePath.toLowerCase(Locale.ENGLISH);
            String suffix = filePath.substring(index + 1, filePath.length());
            return suffix;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public final static String removeSuffix(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        filePath = getFileName(filePath);
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        try {
            int index = filePath.lastIndexOf(".");
            if (index < 0) {
                return filePath;
            }
            return filePath.substring(0, index);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //    public final static List<Video> getVideos(Context context) {
    //        try {
    //            ContentResolver cr = context.getContentResolver();
    //            String[] projection = {MediaStore.Video.Media.DATA, MediaStore.Video.Media.DURATION,
    //                    MediaStore.Video.Media.DISPLAY_NAME};
    //            Cursor cursor = cr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null,
    //                    null, MediaStore.Video.Media.DATE_ADDED + " DESC");
    //            if (cursor == null || cursor.getCount() <= 0) {
    //                LogUtils.d(TAG, "cursor is empty");
    //                return null;
    //            }
    //            List<Video> videos = new ArrayList<Video>();
    //            while (cursor.moveToNext()) {
    //                int i = cursor.getColumnIndex(ImageColumns.DATA);
    //                if (i != -1) {
    //                    String path = cursor.getString(i);
    //                    if (!isVideo(path)) {
    //                        continue;
    //                    }
    //                    long duration = cursor.getLong(cursor.getColumnIndex(VideoColumns.DURATION));
    //                    LogUtils.d(TAG, "path -> %s ,duration -> %d ", path, duration);
    //                    if (duration <= 0) {
    //                        duration = getRecordDuration(path);
    //                        if (duration <= 0) {
    //                            duration = 0;
    //                        }
    //                    }
    //                    videos.add(new Video(path, duration));
    //                    //                    LogUtils.d(TAG, "path -> %s ,duration -> %d ", path, duration);
    //                }
    //            }
    //            cursor.close();
    //            return videos;
    //        } catch (Exception e) {
    //            e.printStackTrace();
    //        }
    //        return null;
    //    }

    public final static List<Folder> getImageFolders(Context context, String folderName, boolean gifEnable) {
        ArrayList<Folder> folderList = new ArrayList<Folder>();
        HashMap<String, Folder> folderMap = new HashMap<String, Folder>();
        try {
            ContentResolver cr = context.getContentResolver();
            String[] imgProjection = {Images.Media.DATA, Images.Media.DATE_ADDED};
            Cursor cursor = cr.query(Images.Media.EXTERNAL_CONTENT_URI, imgProjection, null, null,
                    Images.Media.DATE_ADDED + " DESC");
            resolveCursor(cursor, folderList, folderMap, folderName, false, gifEnable);
            if (cursor != null) {
                cursor.close();
            }
            folderMap.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return folderList;
    }

    private final static void resolveCursor(Cursor cursor, ArrayList<Folder> folderList,
                                            HashMap<String, Folder> folderMap, String folderName, boolean isVideo, boolean gifEnable) {
        Folder allFolder = folderMap.get(folderName);
        if (allFolder == null) {
            allFolder = new Folder(folderName);
            allFolder.setChildList(new ArrayList<Folder.Media>());
            folderMap.put(folderName, allFolder);
            folderList.add(allFolder);
        }
        if (cursor == null || cursor.getCount() <= 0) {
            LogUtils.d(TAG, "cursor is empty");
            return;
        }
        // String DCIM = Environment.getExternalStoragePublicDirectory(
        // Environment.DIRECTORY_DCIM).getPath();
        // int DCIMDeepth = getFileDeepth(new File(DCIM), 0);
        boolean hasNext = cursor.moveToFirst();
        long createDate = 0;
        while (hasNext) {
            int i = cursor.getColumnIndex(ImageColumns.DATA);
            int j = cursor.getColumnIndex(ImageColumns.DATE_ADDED);
            if (j != -1) {
                createDate = cursor.getLong(j);
            }
            if (i != -1) {
                String path = cursor.getString(i);
                long length = FileUtils.size(path);
                LogUtils.d(TAG, "path -> %s ,length -> %d ", path, length);
                hasNext = cursor.moveToNext();
                File f = new File(path);
                if (f.exists() && !isHide(path)) {
                    String parent = f.getParent();
                    // int deepth = getFileDeepth(parent, 0);
                    // if (deepth > DCIMDeepth + 1) {
                    // continue;
                    // }
                    String suffix = getSuffix(path);
                    if (suffix == null) {
                        continue;
                    }
                    if (!suffix.equals("jpg") && !suffix.equals("jpeg") && !suffix.equals("png") &&
                            !suffix.equals("gif")) {
                        continue;
                    }

                    if (suffix.equals("gif") && !gifEnable) {
                        continue;
                    }

                    Folder folder = folderMap.get(parent);
                    if (folder == null) {
                        folder = new Folder(parent);
                        folderMap.put(parent, folder);
                        folderList.add(folder);
                    }
                    allFolder.add(path, createDate, 0, false);
                    folder.add(path, createDate, 0, false);
                }
            }
        }
    }

    /**
     * 获取视频的集合
     *
     * @param context
     * @return
     */
    public final static List<Folder.Media> getVideoFolder(Context context) {
        try {
            ContentResolver cr = context.getContentResolver();
            String[] projection = {MediaStore.Video.Media.DATA, MediaStore.Video.Media.DURATION,
                    MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media.DATE_ADDED};
            Cursor cursor = cr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null,
                    null, MediaStore.Video.Media.DATE_ADDED + " DESC");
            if (cursor == null || cursor.getCount() <= 0) {
                LogUtils.d(TAG, "cursor is empty");
                return null;
            }
            List<Folder.Media> videos = new ArrayList<Folder.Media>();
            while (cursor.moveToNext()) {
                int i = cursor.getColumnIndex(ImageColumns.DATA);
                if (i != -1) {
                    String path = cursor.getString(i);
                    if (!isVideo(path)) {
                        continue;
                    }
                    //                    long duration = cursor.getLong(cursor.getColumnIndex(VideoColumns.DURATION));
                    long duration = getRecordDuration(path);
                    LogUtils.d(TAG, "path -> %s ,duration -> %d ", path, duration);
                    if (duration == -404) {//表示文件损坏
                        continue;
                    }
                    if (duration <= 0) {
                        duration = 0;
                    }
                    long createDate = cursor.getLong(cursor.getColumnIndex(VideoColumns.DATE_ADDED));
                    Folder.Media media = new Folder.Media();
                    media.setPath(path);
                    media.setCreateDate(createDate);
                    media.setDuration(duration);
                    media.setVideo(true);
                    videos.add(media);
                    //                    LogUtils.d(TAG, "path -> %s ,duration -> %d ", path, duration);
                }
            }
            cursor.close();
            return videos;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Folder> getVideoImages(Context context, String folderName, boolean gifEnable) {
        List<Folder.Media> videoMedia = getVideoFolder(context);
        List<Folder> imageFolders = getImageFolders(context, folderName, gifEnable);
        ArrayList<Folder.Media> childList = imageFolders.get(0).getChildList();//所有图片的

        if (!Utils.isEmpty(videoMedia)) {
            childList.addAll(videoMedia);//将视频也加到图片集合中之后排序
            Collections.sort(childList, new Comparator<Folder.Media>() {
                @Override
                public int compare(Folder.Media me1, Folder.Media me2) {
                    if (me1.getCreateDate() == me2.getCreateDate()) {
                        return 0;
                    } else if (me1.getCreateDate() > me2.getCreateDate()) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            });

            Folder folder = new Folder(context.getString(R.string.all_video));
            folder.setChildList((ArrayList<Folder.Media>) videoMedia);
            imageFolders.add(1, folder);
        }

        return imageFolders;
    }

    public static long getRecordDuration(String path) {
        MediaMetadataRetriever retriever = null;
        try {
            retriever = new MediaMetadataRetriever();
            retriever.setDataSource(path);
            String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            LogUtils.d(TAG, "video duration " + duration);
            return Long.parseLong(duration);
        } catch (Exception e) {
            e.printStackTrace();
            return -404;
        } finally {
            if (retriever != null) {
                retriever.release();
            }
        }
        //        return -1;
    }

    public static Bitmap createVideoThumbnail(String path) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = null;
        try {
            retriever = new MediaMetadataRetriever();
            retriever.setDataSource(path);
            //            String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            return retriever.getFrameAtTime(1 * 1000 * 1000, MediaMetadataRetriever.OPTION_CLOSEST);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (retriever != null) {
                retriever.release();
            }
        }
        return bitmap;
    }

    public static Bitmap createVideoThumbnail(String path, int width, int height) {
        return ThumbnailUtils.extractThumbnail(createVideoThumbnail(path), width, height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
    }

    public final static boolean isVideo(String path) {
        String suffix = getSuffix(path);
        if (TextUtils.isEmpty(suffix)) {
            return false;
        }
        String mime2 = "." + suffix + ".";
        return MIMETYPE_VIDEO.contains(mime2);
    }

    private static String getStorageError(Context context) {
        String storageError = context.getString(R.string.storage_error);
        StringConfig config = ResourceConfigHelper.getInstance().getStringConfig();
        if (config != null && !TextUtils.isEmpty(config.getStorageError())) {
            storageError = config.getStorageError();
        }
        return storageError;
    }


    private static String getResultFileName(String srcName, long srcDuration, long maxDuration, String type) {
        return removeSuffix(srcName) + "-" + srcDuration + "-" + (maxDuration * 1000) + type + ".mp4";
    }


    /**
     * @param media
     * @param maxDuration
     * @param quality     控制压缩crf参数,正常范围 18-28
     * @return
     */
    public static String compressVideo(Folder.Media media, int maxDuration, int quality) {
        String path = media.getPath();
        File srcFile = new File(path);
        int videoDuration = (int) media.getDuration();
        int resultDuration = videoDuration < maxDuration * 1000 ? 0 : maxDuration;
        File cacheDir = FileUtils.getFileCacheDir();
        if (cacheDir == null) {
            Context context = BaseInfoManager.getInstance().getContext();
            if (context != null) {
                ToastUtils.showToast(getStorageError(context));
            } else {
                ToastUtils.showToast(R.string.storage_error);
            }
            return null;
        }
        //        int fileDuration = videoDuration < maxDuration * 1000 ? videoDuration : maxDuration * 1000;
        //        File outFileTemp = new File(cacheDir,
        //                removeSuffix(srcFile.getName()) + "-" + videoDuration + "-" + (maxDuration * 1000) + "-compressed" + ".mp4");
        //        File successOutFile = new File(cacheDir, getResultFileName(srcFile.getName(), videoDuration, maxDuration, "-compressed-success"));
        //        String outPath = outFileTemp.getAbsolutePath();
        String successFilePath = cacheDir.getAbsolutePath() + File.separator + getResultFileName(srcFile.getName(),
                videoDuration, maxDuration, "-compressed-success");
        LogUtils.e(TAG, "successFilePath -- >" + successFilePath);
        if (FileUtils.exists(successFilePath)) {
            LogUtils.e(TAG, successFilePath + " >>> unnecessary compress");
            return successFilePath;
        }
        File outFileTemp = new File(cacheDir, getResultFileName(srcFile.getName(), videoDuration, maxDuration, "-compressed"));
        String outPath = outFileTemp.getAbsolutePath();
        LogUtils.e(TAG, "outPath -- >" + outPath);
        long srcLen = srcFile.length();
        boolean success = FfmpegUtils.compressVideo(path, outPath, resultDuration, MAX_WIDTH,
                30, quality);
        if (success) {
            File outFile = new File(outPath);
            Log.d(TAG, "compress file, srcLen: " + srcLen + ", nowLen: " + outFile.length());
            outFile.renameTo(new File(successFilePath));
            return successFilePath;
        }
        return "";
    }


    public static String compressVideo(String path, int maxDuration, int quality) {
        Folder.Media media = new Folder.Media();
        media.setPath(path);
        int duration = (int) getRecordDuration(path);
        media.setDuration(duration);
        return compressVideo(media, maxDuration, quality);
        //        File srcFile = new File(path);
        //        File cacheDir = FileUtils.getCacheDir();
        //        if (cacheDir == null) {
        //            ToastUtils.showToast(R.string.storage_error);
        //            return null;
        //        }
        //        File outFileTemp = new File(cacheDir,
        //                System.currentTimeMillis() + srcFile.getName() + ".mp4");
        //        String outPath = outFileTemp.getAbsolutePath();
        //        LogUtils.e(TAG, "outPath -- >" + outPath);
        //        long srcLen = srcFile.length();
        //        boolean success = FfmpegUtils.compressVideo(path, outPath, 0, getMaxWidth(),
        //                30);
        //        if (success) {
        //            File outFile = new File(outPath);
        //            Log.d(TAG, "compress file, srcLen: " + srcLen + ", nowLen: " + outFile.length());
        //            // outFile.renameTo(srcFile);
        //            return outPath;
        //        }
        //        return "";
    }

    public static String saveThumbnail(Folder.Media video) {
        String destPath;
        String videoPath = video.getPath();
        File file = new File(videoPath);
        String md5 = FileUtils.getFileMD5(file);
        if (TextUtils.isEmpty(md5)) {
            destPath = videoPath + "-tmp.jpg";
        } else {
            File imageDir = FileUtils.getImageDir();
            if (imageDir == null) {
                Context context = BaseInfoManager.getInstance().getContext();
                if (context != null) {
                    ToastUtils.showToast(getStorageError(context));
                } else {
                    ToastUtils.showToast(R.string.storage_error);
                }
                return null;
            }
            File destFile = new File(imageDir, md5 + ".jpg");
            destPath = destFile.getAbsolutePath();
        }
        if (exists(destPath)) {
            return destPath;
        }
        //
        Bitmap bitmap = createVideoThumbnail(videoPath);
        if (bitmap == null) {
            LogUtils.d(TAG, "---------------get frame is null--------------");
            bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Video.Thumbnails.MINI_KIND);
        }
        if (bitmap == null) {
            LogUtils.d(TAG, "---------------ThumbnailUtils createVideoThumbnail is null--------------");
            return "";
        }
        LogUtils.d(TAG,
                "bitmap1 width -- > " + bitmap.getWidth() + " height -- > " + bitmap.getHeight());
        float ratioW = ((float) getMaxWidth()) / bitmap.getWidth();
        float ratioH = ((float) getMaxHeight()) / bitmap.getHeight();
        float ratio = Math.min(ratioW, ratioH);
        int width = (int) (bitmap.getWidth() * ratio);
        int height = (int) (bitmap.getHeight() * ratio);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        LogUtils.d(TAG,
                "bitmap2 width -- > " + bitmap.getWidth() + " height -- > " + bitmap.getHeight());
        BitmapUtils.saveImage(bitmap, destPath);
        LogUtils.d(TAG, "destPath -- >" + destPath);
        return destPath;
    }


    public static int getMaxWidth() {
        int halfScreenWidth = UIUtils.getWidthPixels() / 2;
        return halfScreenWidth > MAX_WIDTH ? MAX_WIDTH : halfScreenWidth;
    }

    public static int getMaxHeight() {
        int halfScreenHeight = UIUtils.getHeightPixels() / 2;
        LogUtils.d(TAG, "halfScreenHeight -- >" + halfScreenHeight);
        return halfScreenHeight > MAX_HEIGHT ? MAX_HEIGHT : halfScreenHeight;
    }

    public static Bitmap getRotateBitmap(Bitmap b, float rotateDegree, boolean isFaceBack) {
        Matrix matrix = new Matrix();
        matrix.postRotate((float) rotateDegree);
        if (!isFaceBack) {
            matrix.postScale(-1, 1);   //镜像水平翻转
        }
        Bitmap rotaBitmap = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, false);
        return rotaBitmap;
    }

    public static String saveBitmap(Bitmap b) {
        String path = getImageDir().getAbsolutePath();
        long dataTake = System.currentTimeMillis();
        String jpegName = path + "/" + dataTake + ".jpg";
        Log.i(TAG, "saveBitmap:jpegName = " + jpegName);
        try {
            FileOutputStream fout = new FileOutputStream(jpegName);
            BufferedOutputStream bos = new BufferedOutputStream(fout);
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jpegName;
    }
}

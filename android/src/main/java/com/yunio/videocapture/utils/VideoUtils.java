package com.yunio.videocapture.utils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.text.TextUtils;

import com.yunio.photoplugin.PhotoPluginModule;
import com.yunio.photoplugin.R;
import com.yunio.videocapture.ThreadPoolManager;
import com.yunio.videocapture.entity.Folder;
import com.yunio.videocapture.entity.ImageSize;
import com.yunio.videocapture.resource.ResourceConfigHelper;
import com.yunio.videocapture.resource.entity.StringConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by PeterZhang on 2018/5/25.
 */

public class VideoUtils {
    private static final int MAX_WIDTH = 540;
    private static final int MAX_HEIGHT = 960;

    public static interface OnCompressCompleteListener {
        public void onCompressComplete(ArrayList<String> images, ArrayList<Folder.Media> videos);
    }

    public static Bitmap extractBigThumbnail(Bitmap bitmap) {
        ImageSize imageSize = VideoUtils.calcBigThumbnailSize(bitmap.getWidth(), bitmap.getHeight());
        if (imageSize != null) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, imageSize.getWidth(), imageSize.getHeight(),
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }
        return bitmap;
    }

    public static ImageSize calcBigThumbnailSize(int bitmapWidth, int bitmapHeight) {
        int maxWidth = getMaxWidth();
        int maxHeight = getMaxHeight();
        if (maxWidth >= bitmapWidth || maxHeight >= bitmapHeight) {
            // 不需要缩放
            return null;
        }
        float ratioW = ((float) getMaxWidth()) / bitmapWidth;
        float ratioH = ((float) getMaxHeight()) / bitmapHeight;
        float ratio = Math.min(ratioW, ratioH);
        int width = (int) (bitmapWidth * ratio);
        int height = (int) (bitmapHeight * ratio);
        return new ImageSize(width, height);
    }

    private static int getMaxWidth() {
        int halfScreenWidth = UIUtils.getWidthPixels() / 2;
        return halfScreenWidth > MAX_WIDTH ? MAX_WIDTH : halfScreenWidth;
    }

    private static int getMaxHeight() {
        int halfScreenHeight = UIUtils.getHeightPixels() / 2;
        return halfScreenHeight > MAX_HEIGHT ? MAX_HEIGHT : halfScreenHeight;
    }

    public static void performMediasSelectedComplete(final Activity activity, final int requestCode, final
    List<Folder.Media>
            selectedMedias, final int maxDuration, final int width, final int quality, int videoQuality, boolean shouldCompressVideo) {
        OnCompressCompleteListener onCompressCompleteListener = new OnCompressCompleteListener() {
            @Override
            public void onCompressComplete(ArrayList<String> images, ArrayList<Folder.Media> videos) {
                ProgressDialogUtils.dismissProgressDialog();
                Intent dataIntent = new Intent();
                //                dataIntent.putExtra(Constant.EXTRA_IMAGE_PATHS, images);
                //                dataIntent.putExtra(Constant.EXTRA_VIDEOS, videos);
                dataIntent.putStringArrayListExtra(Constant.EXTRA_IMAGE_PATHS, images);
                dataIntent.putParcelableArrayListExtra(Constant.EXTRA_VIDEOS, videos);
                //                activity.setResult(Activity.RESULT_OK, dataIntent);
                PhotoPluginModule.onActivityResult(requestCode, Activity.RESULT_OK, dataIntent);
                activity.finish();
            }
        };
        String wait_compress_image_video = activity.getString(R.string.wait_compress_image_video);
        StringConfig stringConfig = ResourceConfigHelper.getInstance().getStringConfig();
        if (stringConfig != null && !TextUtils.isEmpty(stringConfig.getWaitCompressImageVideo())) {
            wait_compress_image_video = stringConfig.getWaitCompressImageVideo();
        }
        ProgressDialogUtils.showProgressDialog(activity,
                wait_compress_image_video, false, false);
//        performMediasSelectedComplete(activity, selectedMedias, maxDuration, width, quality, shouldCompressVideo, onCompressCompleteListener);
        performMediasSelectedComplete(activity, selectedMedias, maxDuration, width, quality, videoQuality, shouldCompressVideo, onCompressCompleteListener);
    }

    /**
     * 处理多媒体文件选择结束时的逻辑
     *
     * @param activity
     * @param selectedMedias
     * @param maxDuration
     * @param width
     * @param quality
     */
    public static void performMediasSelectedComplete(final Activity activity, final List<Folder.Media>
            selectedMedias, final int maxDuration, final int width, final int quality, final int videoQuality, final boolean shouldCompressVideo, final OnCompressCompleteListener
                                                             onCompressCompleteListener) {
        if (selectedMedias.isEmpty()) {
            return;
        }
        ThreadPoolManager.getDefaultThreadPool().add(new Runnable() {

            @Override
            public void run() {
                final ArrayList<String> images = new ArrayList<String>();
                final ArrayList<Folder.Media> videos = new ArrayList<Folder.Media>();
                for (int i = 0; i < selectedMedias.size(); i++) {
                    // paths[i] = mSelectedMedias.get(i).getPath();
                    Folder.Media media = selectedMedias.get(i);
                    if (media.isVideo()) {
                        Folder.Media video = new Folder.Media();
                        String videoPath = shouldCompressVideo ? FileUtils.compressVideo(media, maxDuration, videoQuality) : media.getPath();
                        String thumbNailPath = FileUtils.saveThumbnail(media);
                        video.setPath(videoPath);
                        video.setThumbnailPath(thumbNailPath);
                        long resultDuration = media.getDuration() < maxDuration * 1000 ? media.getDuration() : maxDuration * 1000;
                        video.setDuration(resultDuration);
                        videos.add(video);
                    } else {
                        final boolean requestCompress = width > 0 && quality > 0 && quality <= 100 && !media.isGif();
                        if (requestCompress) {
                            images.add(PhotoUtils.compress(media.getPath(), false,
                                    width, quality));
                        } else {
                            images.add(selectedMedias.get(i).getPath());
                        }
                    }
                }
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        onCompressCompleteListener.onCompressComplete(images, videos);
                    }
                });
            }
        });
    }
}

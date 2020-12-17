package com.yunio.photoplugin;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.yunio.videocapture.ThreadPoolManager;
import com.yunio.videocapture.activity.CaptureCodeVideoActivity;
import com.yunio.videocapture.activity.CaptureVideoActivity;
import com.yunio.videocapture.activity.SelectAvatarActivity;
import com.yunio.videocapture.activity.SelectImageActivity;
import com.yunio.videocapture.activity.SelectOnlyImageActivity;
import com.yunio.videocapture.activity.SelectVideoActivity;
import com.yunio.videocapture.entity.Folder;
import com.yunio.videocapture.resource.ResourceConfigHelper;
import com.yunio.videocapture.utils.Constant;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * Created by JauZhou on 2018/3/15.
 */

public class PhotoPluginModule extends ReactContextBaseJavaModule {
    private final static String NAME = "PhotoPluginModule";
    private static final String ACTION_SELECT_IMAGE_NAME = "selectAlbum";
    private static final String ACTION_TAKE_CODE_VIDEO_NAME = "recordVideo";
    private static final String ACTION_SELECT_AVATAR_NAME = "selectAvatar";
    private static final String ACTION_SELECT_ALBUM_VIDEO = "selectAlbumVideo";
    private static final String ACTION_TAKE_PHOTO = "take_photo";
    private static final String ACTION_CHANGE_LANGUAGE = "change_language";
    private static final String ACTION_SELECT_IMAGE_VIDEO_NAME = "selectImageVideo";
    private static final String ACTION_TAKE_VIDEO_NAME = "captureVideo";
    public static final int REQUEST_CODE_SELECT_IMAGE = 1;
    public static final int REQUEST_CODE_RECORD_VIDEO = 2;
    public static final int REQUEST_CODE_SELECT_AVATAR = 3;
    public static final int REQUEST_CODE_SELECT_VIDEO = 4;
    public static final int REQUEST_CODE_TAKE_PHOTO = 5;
    public static final int REQUEST_CODE_SELECT_VIDEO_IMAGE = 6;
    public static final int REQUEST_CODE_CAPTURE_VIDEO = 7;
    private ReactApplicationContext mContext;
    private static Promise mCurPromise;
    public static double GifSizeLimit = -1;


    private ActivityEventListener mActivityEventListener = new ActivityEventListener() {


        @Override
        public void onNewIntent(Intent intent) {

        }

        @Override
        public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
            if (mCurPromise == null) {
                return;
            }
            if (resultCode != Activity.RESULT_OK) {
                mCurPromise.reject("2", "no path");
                resetRequest();
                return;
            }
            if (requestCode == REQUEST_CODE_SELECT_IMAGE) {
                String[] imagPaths = data.getStringArrayExtra(SelectImageActivity.EXTRA_IMAGE_PATHS);
                WritableArray array = new WritableNativeArray();
                for (String s : imagPaths) {
                    array.pushString(s);
                }
                mCurPromise.resolve(array);
            } else if (requestCode == REQUEST_CODE_RECORD_VIDEO) {
                String path = data.getStringExtra(Constant.EXTRA_VIDEO_PATH);
                mCurPromise.resolve(path);
            } else if (requestCode == REQUEST_CODE_SELECT_AVATAR) {
                String path = data.getStringExtra(SelectAvatarActivity.EXTRA_AVATAR_PATH);
                mCurPromise.resolve(path);
            } else if (requestCode == REQUEST_CODE_SELECT_VIDEO) {
                String videoPath = data.getStringExtra(Constant.EXTRA_VIDEO_PATH);
                String imagePath = data.getStringExtra(SelectVideoActivity.EXTRA_IMAGE_PATH);
                long orginalDuration = data.getLongExtra(SelectVideoActivity.EXTRA_ORGINAL_DURATION,
                        0);
                WritableArray array = new WritableNativeArray();
                array.pushString(videoPath);
                array.pushString(imagePath);
                array.pushString(String.valueOf(orginalDuration));
                mCurPromise.resolve(array);
            } else if (requestCode == REQUEST_CODE_TAKE_PHOTO) {
                String path = data.getStringExtra(Constant.EXTRA_IMAGE_PATH);
                mCurPromise.resolve(path);
            } else if (requestCode == REQUEST_CODE_SELECT_VIDEO_IMAGE) {//选择图片和视频
                List<String> imagePaths = data.getStringArrayListExtra(Constant.EXTRA_IMAGE_PATHS);
                WritableArray array = new WritableNativeArray();
                if (imagePaths != null && !imagePaths.isEmpty()) {
                    for (String s : imagePaths) {
                        array.pushString(s);
                    }
                }
                ArrayList<Folder.Media> medias = data.getParcelableArrayListExtra(Constant
                        .EXTRA_VIDEOS);
                WritableArray videos = new WritableNativeArray();
                if (medias != null && medias.size() > 0) {
                    for (Folder.Media media : medias) {
                        videos.pushArray(createVideoNativeInfo(media));
                    }
                }
                WritableMap writableMap = new WritableNativeMap();
                writableMap.putArray(Constant.EXTRA_VIDEOS, videos);
                writableMap.putArray(Constant.EXTRA_IMAGE_PATHS, array);
                mCurPromise.resolve(writableMap);

            } else if (requestCode == REQUEST_CODE_CAPTURE_VIDEO) {
                Folder.Media media = data.getParcelableExtra(Constant.EXTRA_VIDEO_PATH);
                mCurPromise.resolve(createVideoNativeInfo(media));
            }
            resetRequest();
        }
    };

    public static void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mCurPromise == null) {
            return;
        }
        if (resultCode != Activity.RESULT_OK) {
            mCurPromise.reject("2", "no path");
            resetRequest();
            return;
        }
        if (requestCode == REQUEST_CODE_SELECT_IMAGE) {
            ArrayList<String> imagPaths = data.getStringArrayListExtra(Constant.EXTRA_IMAGE_PATHS);
            WritableArray array = new WritableNativeArray();
            for (String s : imagPaths) {
                array.pushString(s);
            }
            mCurPromise.resolve(array);
        } else if (requestCode == REQUEST_CODE_RECORD_VIDEO) {
            String path = data.getStringExtra(Constant.EXTRA_VIDEO_PATH);
            mCurPromise.resolve(path);
        } else if (requestCode == REQUEST_CODE_SELECT_AVATAR) {
            String path = data.getStringExtra(SelectAvatarActivity.EXTRA_AVATAR_PATH);
            mCurPromise.resolve(path);
        } else if (requestCode == REQUEST_CODE_SELECT_VIDEO) {
            String videoPath = data.getStringExtra(Constant.EXTRA_VIDEO_PATH);
            String imagePath = data.getStringExtra(SelectVideoActivity.EXTRA_IMAGE_PATH);
            long orginalDuration = data.getLongExtra(SelectVideoActivity.EXTRA_ORGINAL_DURATION,
                    0);
            WritableArray array = new WritableNativeArray();
            array.pushString(videoPath);
            array.pushString(imagePath);
            array.pushString(String.valueOf(orginalDuration));
            mCurPromise.resolve(array);
        } else if (requestCode == REQUEST_CODE_TAKE_PHOTO) {
            String path = data.getStringExtra(Constant.EXTRA_IMAGE_PATH);
            mCurPromise.resolve(path);
        } else if (requestCode == REQUEST_CODE_SELECT_VIDEO_IMAGE) {//选择图片和视频
            List<String> imagePaths = data.getStringArrayListExtra(Constant.EXTRA_IMAGE_PATHS);
            WritableArray array = new WritableNativeArray();
            if (imagePaths != null && !imagePaths.isEmpty()) {
                for (String s : imagePaths) {
                    array.pushString(s);
                }
            }
            ArrayList<Folder.Media> medias = data.getParcelableArrayListExtra(Constant
                    .EXTRA_VIDEOS);
            WritableArray videos = new WritableNativeArray();
            if (medias != null && medias.size() > 0) {
                for (Folder.Media media : medias) {
                    videos.pushArray(createVideoNativeInfo(media));
                }
            }
            WritableMap writableMap = new WritableNativeMap();
            writableMap.putArray(Constant.EXTRA_VIDEOS, videos);
            writableMap.putArray(Constant.EXTRA_IMAGE_PATHS, array);
            mCurPromise.resolve(writableMap);

        } else if (requestCode == REQUEST_CODE_CAPTURE_VIDEO) {
            Folder.Media media = data.getParcelableExtra(Constant.EXTRA_VIDEO_PATH);
            mCurPromise.resolve(createVideoNativeInfo(media));
        }
        resetRequest();
    }

    private static void resetRequest() {
        mCurPromise = null;
        GifSizeLimit = -1;
    }

    private static WritableArray createVideoNativeInfo(Folder.Media media) {
        WritableArray arrays = new WritableNativeArray();
        arrays.pushString(media.getPath());
        arrays.pushString(media.getThumbnailPath());
        arrays.pushString(String.valueOf(media.getDuration()));
        return arrays;
    }

    public PhotoPluginModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.mContext = reactContext;
        //        mContext.addActivityEventListener(mActivityEventListener);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @ReactMethod
    public void execute(String action, ReadableArray args, Promise promise) {
        Log.d(NAME, "execute action: " + action);
        mCurPromise = promise;
        try {
            if (ACTION_SELECT_IMAGE_NAME.equals(action)) {
                int maxCount = 0;
                int width = 0;
                int quality = 0;
                int minCount = 0;
                switch (args.size()) {
                    case 4:
                      minCount = args.getInt(3);
                    case 3:
                        quality = optQuality(args.getDouble(2));
                    case 2:
                        width = args.getInt(1);
                    case 1:
                        maxCount = args.getInt(0);
                }
                onSelectImageClick(maxCount, width, quality, minCount);
            } else if (ACTION_TAKE_CODE_VIDEO_NAME.equals(action)) {
                String code = "";
                int quality = 0;
                switch (args.size()) {
                    case 2:
                        code = args.getString(0);
                    case 1:
                        quality = optVideoQuality(args.getDouble(1));
                }
                onRecordVideoClick(code, quality);
            } else if (ACTION_SELECT_AVATAR_NAME.equals(action)) {
                int width = 0;
                int quality = 0;
                switch (args.size()) {
                    case 2:
                        quality = optQuality(args.getDouble(1));
                    case 1:
                        width = args.getInt(0);
                }
                onSelectAvatarClick(width, quality);
            } else if (ACTION_SELECT_ALBUM_VIDEO.equals(action)) {
                int quality = 0;
                int duration = 0;
                switch (args.size()) {
                    case 2:
                        quality = optVideoQuality(args.getDouble(1));
                    case 1:
                        duration = args.getInt(0);
                }
                onSelectVideoClick(duration, quality, true);
            } else if (ACTION_TAKE_PHOTO.equals(action)) {
                int width = 0;
                int quality = 0;
                boolean isFront = true;
                switch (args.size()) {
                    case 3:
                        isFront = args.getBoolean(2);
                    case 2:
                        quality = optQuality(args.getDouble(1));
                    case 1:
                        width = args.getInt(0);
                }
                onTakePhoto(width, quality, isFront);
            } else if (ACTION_CHANGE_LANGUAGE.equals(action)) {
                String language = args != null && args.size() > 0 ? args.getString(0) : null;
                changeLanguage(language);
            } else if (ACTION_SELECT_IMAGE_VIDEO_NAME.equals(action)) {
                int maxCount = SelectImageActivity.DEFAULT_MAX_COUNT;
                boolean shouldCompressVideo = true;
                boolean isSupportImageAndVideo = true;
                int width = 0;
                int picQuality = 0;
                int videoQuality = 23;
                int maxDuration = 0;
                int minCount = 0;
                // [maxCount, isSupportImageAndVideo, maxDuration, width, quality]
                switch (args.size()) {
                    case 7:
                      minCount = args.getInt(6);
                    case 6:
                        videoQuality = optVideoQuality(args.getDouble(5));
                    case 5:
                        picQuality = optQuality(args.getDouble(4));
                    case 4:
                        width = args.getInt(3);
                    case 3:
                        maxDuration = args.getInt(2);
                    case 2:
                        isSupportImageAndVideo = args.getBoolean(1);
                    case 1:
                        maxCount = args.getInt(0);
                        break;
                    default:
                        break;
                }
                onSelectImageVideoClick(minCount, maxCount, width, picQuality, videoQuality, maxDuration, isSupportImageAndVideo, shouldCompressVideo);
            } else if (ACTION_TAKE_VIDEO_NAME.equals(action)) {
                // 拍摄视频
                int maxDuration = 0;
                boolean isFront = true;
                int videoQuality = 0;
                switch (args.size()) {
                    case 3:
                        isFront = args.getBoolean(2);
                    case 2:
                        videoQuality = optVideoQuality(args.getDouble(1));
                    case 1:
                        maxDuration = args.getInt(0);
                }
                onCaptureVideo(maxDuration, videoQuality, isFront);
            } else {
                promise.reject("0", "Invalid Action");
            }
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            promise.reject("1", e.getMessage());
        }
    }

    @ReactMethod
    public void selectAlbumAndUncompressedVideo(int minCount, int maxCount, int width, double picQuality, int maxDuration, Promise promise) {
        mCurPromise = promise;
        onSelectImageVideoClick(minCount, maxCount, width, optQuality(picQuality), 23, maxDuration, false, false);
    }

    /**
     * @param maxDuration 单位为秒
     * @param promise
     */
    @ReactMethod
    public void selectUncompressedVideo(int maxDuration, Promise promise) {
        mCurPromise = promise;
        onSelectVideoClick(maxDuration, 50, false);
    }

    @ReactMethod
    public void compressVideo(String path, int maxDuration, int quality, Promise promise) {
        ThreadPoolManager.getSingleThreadInstance().add(new CompressRunnable(path, maxDuration, optVideoQuality(quality), promise));
    }


    @ReactMethod
    public void selectGifAndPic(int maxCount, int width, int quality, Promise promise) {
        mCurPromise = promise;
        Intent intent = SelectImageActivity.createLauncherIntent(mContext, 0, maxCount,
                width, quality);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //        mContext.startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE, null);
        mContext.startActivity(intent);
    }

    @ReactMethod
    public void gifSizeLimit(double gifSizeLimit) {
        GifSizeLimit = gifSizeLimit;
    }


    private int optQuality(double dQuality) {
        //        if (dQuality > 0 && dQuality <= 1) {
        //            return (int) (dQuality * 100);
        //        }
        return (int) dQuality;
    }

    public int optVideoQuality(double quality) {
        int result = (int) ((100 - quality) / 2);
        if (result <= 0 || result > 51) {
            result = 23;
        }
        return result;
    }


    private void onSelectImageClick(int maxCount, int width, int quality, int minCount) {
        Intent intent = SelectOnlyImageActivity.createLauncherIntent(mContext, maxCount,
                width, quality, minCount);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //        mContext.startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE, null);
        mContext.startActivity(intent);
    }

  /**
   * @param minCount
   * @param maxCount
   * @param width
   * @param picQuality
   * @param videoQuality
   * @param maxDuration
   * @param isCanSelectImageAndVideo 是否可用同时选择图片和视频
   * @param shouldCompressVideo
   */
    private void onSelectImageVideoClick(int minCount, int maxCount, int width, int picQuality, int videoQuality, int maxDuration, boolean
            isCanSelectImageAndVideo, boolean shouldCompressVideo) {
        Intent intent = SelectImageActivity.createLauncherIntent(mContext, minCount, maxCount,
                width, picQuality, videoQuality, maxDuration, isCanSelectImageAndVideo, shouldCompressVideo);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
        //        mContext.startActivityForResult(intent, REQUEST_CODE_SELECT_VIDEO_IMAGE, null);
    }

    private void onRecordVideoClick(String code, int quality) {
        Intent intent = CaptureCodeVideoActivity.createLauncherIntent(mContext, code, quality);
        //        mContext.startActivityForResult(intent, REQUEST_CODE_RECORD_VIDEO, null);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    private void onSelectAvatarClick(int width, int quality) {
        Intent intent = SelectAvatarActivity.createLauncherIntent(mContext, width,
                quality);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
        //        mContext.startActivityForResult(intent, REQUEST_CODE_SELECT_AVATAR, null);
    }

    /**
     * 单位为秒
     *
     * @param maxDuration
     */
    private void onSelectVideoClick(int maxDuration, int quality, boolean shouldCompressVideo) {
        Intent intent = SelectVideoActivity.createLauncherIntent(mContext, maxDuration, quality, shouldCompressVideo);
        //        mContext.startActivityForResult(intent, REQUEST_CODE_SELECT_VIDEO, null);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    private void onTakePhoto(int width, int quality, boolean isFront) {
        Intent intent = CaptureVideoActivity.createImageIntent(true, width, quality, isFront);
        intent.setClass(mContext, CaptureVideoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
        //        mContext.startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO, null);
    }

    private void onCaptureVideo(int maxDuration, int videoQuality, boolean isFront) {
        Intent intent = CaptureVideoActivity.createVideoIntent(true, maxDuration, isFront, videoQuality);
        intent.setClass(mContext, CaptureVideoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
        //        mContext.startActivityForResult(intent, REQUEST_CODE_CAPTURE_VIDEO, null);
    }

    private final static String LANGUAGE_CHINA = "zh";
    private final static String LANGUAGE_ENGLISH = "en";

    private void changeLanguage(String language) {
        Resources resources = mContext.getResources();
        Configuration config = resources.getConfiguration();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        switch (language) {
            case LANGUAGE_CHINA:
                config.locale = Locale.SIMPLIFIED_CHINESE;
                config.setLayoutDirection(Locale.SIMPLIFIED_CHINESE);
                break;
            case LANGUAGE_ENGLISH:
                config.locale = Locale.ENGLISH;
                config.setLayoutDirection(Locale.ENGLISH);
                break;
            default:
                String country = Locale.getDefault().getCountry();
                Locale locale = null;
                if ("CN".equals(country)) {
                    locale = Locale.SIMPLIFIED_CHINESE;
                } else {
                    locale = Locale.ENGLISH;
                }
                config.locale = locale;
                config.setLayoutDirection(locale);
                break;
        }
        resources.updateConfiguration(config, metrics);
    }


    @ReactMethod
    public void initStringConfig(String config, String defaultLanguage) {
        ResourceConfigHelper.getInstance().initStringConfig(config, defaultLanguage);
    }

    @ReactMethod
    public void initImageConfig(String config, String defaultLanguage) {
        ResourceConfigHelper.getInstance().initImageConfig(config, defaultLanguage);
    }

    @ReactMethod
    public void initColorConfig(String config, String defaultLanguage) {
        ResourceConfigHelper.getInstance().initColorConfig(config, defaultLanguage);
    }
}

package com.yunio.videocapture.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;

import com.yunio.easypermission.BasePermissionActivity;
import com.yunio.easypermission.PermissionCallBackM;
import com.yunio.photoplugin.BuildConfig;
import com.yunio.photoplugin.PhotoPluginModule;
import com.yunio.photoplugin.R;
import com.yunio.videocapture.camera.CameraWrapper;
import com.yunio.videocapture.camera.NativeCamera;
import com.yunio.videocapture.configration.CaptureConfiguration;
import com.yunio.videocapture.configration.PredefinedCaptureConfigurations.CaptureQuality;
import com.yunio.videocapture.configration.PredefinedCaptureConfigurations.CaptureResolution;
import com.yunio.videocapture.entity.Folder;
import com.yunio.videocapture.prewiew.SensorControler;
import com.yunio.videocapture.record.VideoFile;
import com.yunio.videocapture.record.VideoRecorder;
import com.yunio.videocapture.record.VideoRecorderResultInterface;
import com.yunio.videocapture.resource.ResourceConfigHelper;
import com.yunio.videocapture.resource.entity.StringConfig;
import com.yunio.videocapture.utils.Constant;
import com.yunio.videocapture.utils.FileUtils;
import com.yunio.videocapture.utils.LogUtils;
import com.yunio.videocapture.utils.ProgressDialogUtils;
import com.yunio.videocapture.utils.UIUtils;
import com.yunio.videocapture.utils.VideoUtils;
import com.yunio.videocapture.utils.ViewUtils;
import com.yunio.videocapture.utils.WindowUtils;
import com.yunio.videocapture.view.IVideoCaptureView;
import com.yunio.videocapture.view.RecordingInterface;
import com.yunio.videocapture.view.TakePatureInterface;
import com.yunio.videocapture.view.VideoCaptureView;

import java.io.IOException;
import java.util.ArrayList;

@SuppressLint("NewApi")
public class CaptureVideoActivity extends BasePermissionActivity implements RecordingInterface,
        OnClickListener, VideoRecorderResultInterface, PermissionCallBackM, TakePatureInterface {
    private final static String TAG = "CaptureVideoActivity";
    private static final int REQUEST_CODE_READ_PERMISSION = 1001;
    public static final String EXTRA_SUPPORT_REVERSE_CAMERA = "support_reverse_camera";
    private static final String EXTRA_MAX_DURATION = "max_duration";
    private static final String EXTRA_TAKE_PICTURE = "take_picture";
    private static final String EXTRA_IS_FRONT = "is_front";
    private static final int MAX_WIDTH = 540;
    private static final int DEFAULT_MAX_DURATION = 10;
    private IVideoCaptureView mVideoCaptureView;

    private String mFlieName;
    private VideoRecorder mVideoRecorder;

    private View mVReverseCamera;
    private boolean mIsSupportReverseCamera;
    // second
    private int mMaxDuration;
    private boolean mIsTakePicture;
    protected int mWidth, mQuality;
    protected int mVideoQuality;
    protected boolean mIsFront = true;

    public static void startCaptureVideoActivity4Result(Activity activity, int requestCode, int maxDuration, boolean isFront, int quality) {
        Intent intent = createVideoIntent(true, maxDuration, isFront, quality);
        intent.setClass(activity, CaptureVideoActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void startTakePictureActivity4Result(Activity activity, int width, int quality, boolean isFront) {
        Intent intent = createImageIntent(true, width, quality, isFront);
        intent.setClass(activity, CaptureVideoActivity.class);
        activity.startActivity(intent);
    }

    public static Intent createImageIntent(boolean supportReverseCamera, int width, int quality, boolean isFront) {
        return createIntent(supportReverseCamera, DEFAULT_MAX_DURATION, true, width, quality, isFront);
    }

    public static Intent createVideoIntent(boolean supportReverseCamera, boolean isFront, int quality) {
        return createVideoIntent(supportReverseCamera, DEFAULT_MAX_DURATION, isFront, quality);
    }

    public static Intent createVideoIntent(boolean supportReverseCamera, int maxDuration, boolean isFront, int quality) {
        return createIntent(supportReverseCamera, maxDuration, false, 0, quality, isFront);
    }

    private static Intent createIntent(boolean supportReverseCamera, int maxDuration, boolean isTakePicture, int
            width, int quality, boolean isFront) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_SUPPORT_REVERSE_CAMERA, supportReverseCamera);
        intent.putExtra(EXTRA_MAX_DURATION, maxDuration);
        intent.putExtra(EXTRA_TAKE_PICTURE, isTakePicture);
        intent.putExtra(EXTRA_IS_FRONT, isFront);
        Constant.addImageParams(intent, width, quality);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowUtils.hideStatusBarShowNavBar(this, 0x33000000, 0x33000000);
        setContentView(getContentLayoutId());
        Intent intent = getIntent();
        mIsSupportReverseCamera = intent.getBooleanExtra(EXTRA_SUPPORT_REVERSE_CAMERA, true);
        mMaxDuration = intent.getIntExtra(EXTRA_MAX_DURATION, DEFAULT_MAX_DURATION);
        mIsTakePicture = intent.getBooleanExtra(EXTRA_TAKE_PICTURE, false);
        mIsFront = intent.getBooleanExtra(EXTRA_IS_FRONT, true);
        //        mIsTakePicture = true;
        if (mIsTakePicture) {
            mWidth = Constant.getWidthFromIntent(intent);
            mQuality = Constant.getQualityFromIntent(intent);
        } else {
            mVideoQuality = Constant.getQualityFromIntent(intent);
        }
        findViewById(R.id.tv_back).setOnClickListener(this);
        mVideoCaptureView = (IVideoCaptureView) findViewById(R.id.vcv_capture);
        mVideoCaptureView.setRecordingInterface(this);
        if (mVideoCaptureView instanceof VideoCaptureView) {
            VideoCaptureView captureView = (VideoCaptureView) mVideoCaptureView;
            captureView.setTakePictureInterface(this);
            captureView.setTakeCameraMode(mIsTakePicture);
            captureView.updateCameraBottomUI(mIsTakePicture);
        }

        mVReverseCamera = findViewById(R.id.iv_reverse_camera);
        if (mIsSupportReverseCamera) {
            mVReverseCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mVideoRecorder.reverseCamera();
                }
            });
        }
        ViewUtils.showView(mVReverseCamera, mIsSupportReverseCamera);
        requestPermission(REQUEST_CODE_READ_PERMISSION,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                getString(R.string.need_permission_tips), this);
    }

    protected int getContentLayoutId() {
        return R.layout.activity_capture_video;
    }

    private void initVideoRecorder() {
        Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        int cameraId = mIsFront ? NativeCamera.CAMERA_FACING_FRONT : NativeCamera.CAMERA_FACING_BACK;
        mVideoRecorder = new VideoRecorder(this,
                new CaptureConfiguration(CaptureResolution.RES_720P, CaptureQuality.HIGH, mMaxDuration,
                        CaptureConfiguration.NO_FILESIZE_LIMIT),
                new CameraWrapper(new NativeCamera(), display.getRotation()), mVideoCaptureView,
                new VideoFile(mFlieName), cameraId, mIsTakePicture);
        ViewUtils.setVisibility((View) mVideoCaptureView, View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        releaseRecord();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mVideoRecorder != null) {
            LogUtils.d(TAG, "mVideoRecorder onStart");
            mVideoRecorder.onStart();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mVideoRecorder != null) {
            LogUtils.d(TAG, "mVideoRecorder onStop");
            mVideoRecorder.onStop();
        }
    }

    @Override
    public void onBackPressed() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        super.onBackPressed();
    }

    private void releaseRecord() {
        if (mVideoRecorder != null) {
            mVideoRecorder.stopRecording(true, false, null);
            mVideoRecorder.releaseAllResources();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_back) {
            finish();
        }
    }

    @Override
    public boolean didRecordStart() {
        return mVideoRecorder.startRecording();
    }

    @Override
    public void didRecordStop(boolean isRemove, boolean isCancel) {
        mVideoRecorder.stopRecording(isRemove, isCancel, null);
        if (mVideoCaptureView instanceof VideoCaptureView) {
            VideoCaptureView videoCaptureView = (VideoCaptureView) mVideoCaptureView;
            videoCaptureView.setRecordBtmClickable(false);
        }
    }


    @Override
    public void onRecordingSuccess(final String path) {
        LogUtils.d(TAG, "path : " + path);
        onRecordComplete(path);
        if (mVideoCaptureView instanceof VideoCaptureView) {
            final VideoCaptureView videoCaptureView = (VideoCaptureView) mVideoCaptureView;
            videoCaptureView.updateRecordButtonBg(); // 由于自动结束没有更新按钮状态所以自动结束后需要更新录像按钮的背景
            videoCaptureView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    videoCaptureView.setRecordBtmClickable(true);
                }
            }, 500);
        }
    }

    /**
     * 成功完成录像
     *
     * @param path
     */
    protected void onRecordComplete(String path) {
        startPreviewResult(true, path);
        //        finishAndDistributeResult(path);
    }

    private void startPreviewResult(boolean isVideo, String path) {
        Folder.Media media = new Folder.Media();
        media.setPath(path);
        media.setVideo(isVideo);
        if (isVideo) {
            media.setCreateDate(System.currentTimeMillis());
            long duration = FileUtils.getRecordDuration(path);
            if (duration <= 0) {
                duration = 0;
            }
            media.setDuration(duration);
        }
        //        SelectImageScaleImageActivity.startActivity(this, medias, medias, 1);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        ScaleSingleImageVideoActivity.startActivity(this, media);
    }

    protected int getMaxWidth() {
        int halfScreenWidth = UIUtils.getWidthPixels() / 2;
        return halfScreenWidth > MAX_WIDTH ? MAX_WIDTH : halfScreenWidth;
    }

    @Override
    public void onRecordingFailed(String message) {
        LogUtils.d(TAG, message);
        if (mVideoCaptureView instanceof VideoCaptureView) {
            final VideoCaptureView videoCaptureView = (VideoCaptureView) mVideoCaptureView;
            videoCaptureView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    videoCaptureView.setRecordBtmClickable(true);
                }
            }, 500);
        }
    }

    @Override
    public void onPermissionGrantedM(int requestCode, String... perms) {
        if (requestCode == REQUEST_CODE_READ_PERMISSION) {
            initVideoRecorder();
        }
    }

    @Override
    public void onPermissionDeniedM(int requestCode, String... perms) {

    }

    @Override
    public void didTakePicture() {
        LogUtils.e(TAG, "didTakePicture");
        mVideoRecorder.startTakePicture(new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                SensorControler.getInstance().unlockFocus();
                Bitmap b = null;
                if (null != data) {
                    b = BitmapFactory.decodeByteArray(data, 0, data.length);//data是字节数据，将其解析成位图
                    String path = FileUtils.saveBitmap(b);
                    b.recycle();
                    try {
                        ExifInterface exifInterface = new ExifInterface(path);
                        int rotation = mVideoRecorder.isFaceBack() ? ExifInterface.ORIENTATION_ROTATE_90 :
                                ExifInterface.ORIENTATION_TRANSVERSE;
                        exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, Integer.toString(rotation));
                        exifInterface.saveAttributes();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    startPreviewResult(false, path);
                }
                //                //保存图片到sdcard
                //                if (null != b) {
                //                    Bitmap rotaBitmap = FileUtils.getRotateBitmap(b, mVideoRecorder.isFaceBack() ?
                // 90f : -90.0f, mVideoRecorder.isFaceBack());
                //                    String path = FileUtils.saveBitmap(rotaBitmap);
                //                    startPreviewResult(false, path);
                //                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mVideoCaptureView instanceof VideoCaptureView) {
            ((VideoCaptureView) mVideoCaptureView).setIsdoingSavePicture(false);//将正在保存照片设为false
        }
        if (resultCode == RESULT_OK) {
            if (requestCode == ScaleSingleImageVideoActivity.REQUEST_CODE) {
                boolean result = data.getBooleanExtra(ScaleSingleImageVideoActivity.EXTRA_RESULT,
                        false);
                if (result) {
                    Folder.Media media = data.getParcelableExtra(ScaleSingleImageVideoActivity.EXTRA_MEDIA);
                    performMediaSelectComplete(media);
                } else {
                    if (BuildConfig.DEBUG) {
                        try {
                            mVideoRecorder.startPreview();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }


    private void performMediaSelectComplete(Folder.Media media) {
        ArrayList<Folder.Media> medias = new ArrayList<>();
        medias.add(media);

        final Activity activity = this;
        VideoUtils.OnCompressCompleteListener onCompressCompleteListener = new VideoUtils.OnCompressCompleteListener() {
            @Override
            public void onCompressComplete(ArrayList<String> images, ArrayList<Folder.Media> videos) {
                ProgressDialogUtils.dismissProgressDialog();
                Intent dataIntent = new Intent();
                if (images != null && images.size() > 0) {
                    dataIntent.putExtra(Constant.EXTRA_IMAGE_PATH, images.get(0));
                }
                if (videos != null && videos.size() > 0) {
                    dataIntent.putExtra(Constant.EXTRA_VIDEO_PATH, videos.get(0));
                }
                //                activity.setResult(Activity.RESULT_OK, dataIntent);
                PhotoPluginModule.onActivityResult(mIsTakePicture ? PhotoPluginModule.REQUEST_CODE_TAKE_PHOTO :
                        PhotoPluginModule.REQUEST_CODE_CAPTURE_VIDEO, RESULT_OK, dataIntent);
                activity.finish();
            }
        };
        String wait_compress_image_video = getString(R.string.wait_compress_image_video);
        StringConfig stringConfig = ResourceConfigHelper.getInstance().getStringConfig();
        if (stringConfig != null && !TextUtils.isEmpty(stringConfig.getWaitCompressImageVideo())) {
            wait_compress_image_video = stringConfig.getWaitCompressImageVideo();
        }
        ProgressDialogUtils.showProgressDialog(activity,
                R.string.wait_compress_image_video, false, false);
        VideoUtils.performMediasSelectedComplete(CaptureVideoActivity.this, medias, mMaxDuration, mWidth, mQuality, mVideoQuality, true, onCompressCompleteListener);
    }
}

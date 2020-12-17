/**
 * Copyright 2014 Jeroen Mols
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yunio.videocapture.record;

import android.content.Context;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.View;

import com.yunio.photoplugin.R;
import com.yunio.videocapture.BaseInfoManager;
import com.yunio.videocapture.camera.CameraWrapper;
import com.yunio.videocapture.camera.OpenCameraException;
import com.yunio.videocapture.camera.PrepareCameraException;
import com.yunio.videocapture.camera.RecordingSize;
import com.yunio.videocapture.configration.CaptureConfiguration;
import com.yunio.videocapture.prewiew.CapturePreview;
import com.yunio.videocapture.prewiew.CapturePreviewInterface;
import com.yunio.videocapture.prewiew.IActivityLifiCycle;
import com.yunio.videocapture.prewiew.SensorControler;
import com.yunio.videocapture.resource.ResourceConfigHelper;
import com.yunio.videocapture.resource.entity.StringConfig;
import com.yunio.videocapture.utils.FileUtils;
import com.yunio.videocapture.utils.LogUtils;
import com.yunio.videocapture.utils.ToastUtils;
import com.yunio.videocapture.view.IVideoCaptureView;

import java.io.IOException;

public class VideoRecorder implements OnInfoListener, CapturePreviewInterface, IActivityLifiCycle {
    private final static String TAG = "VideoRecorder";
    private CameraWrapper mCameraWrapper;
    // private final Surface mPreviewSurface;
    private CapturePreview mVideoCapturePreview;

    private final CaptureConfiguration mCaptureConfiguration;
    private VideoFile mVideoFile;
    private boolean mIsTakePicture;

    private MediaRecorder mRecorder;
    private boolean mRecording = false;
    private final VideoRecorderStateInterface mRecorderStateInterface;
    private final VideoRecorderResultInterface mRecorderResultInterface;

    public VideoRecorder(VideoRecorderResultInterface recorderResultInterface,
                         CaptureConfiguration captureConfiguration, CameraWrapper cameraWrapper,
                         IVideoCaptureView captureView, VideoFile videoFile, int cameraId, boolean isTakePicture) {
        mCaptureConfiguration = captureConfiguration;
        mRecorderResultInterface = recorderResultInterface;
        mRecorderStateInterface = captureView;
        mVideoFile = videoFile;
        mCameraWrapper = cameraWrapper;
        SurfaceHolder previewHolder = captureView.getPreviewSurfaceHolder();
        // mPreviewSurface = captureView.getPreviewSurfaceHolder().getSurface();
        captureView.setMax(mCaptureConfiguration.getMaxCaptureDuration());
        initializeCameraAndPreview(previewHolder, cameraId, captureView.getSurfaceParent(), isTakePicture);
    }

    public void initializeCameraAndPreview(SurfaceHolder previewHolder, int cameraId, View surfaceParent, boolean isTakePicture) {
        try {
            mCameraWrapper.openCamera(cameraId);
        } catch (final OpenCameraException e) {
            Context context = BaseInfoManager.getInstance().getContext();
            if (context != null) {
                String no_permission_for_camera = context.getString(R.string.no_permission_for_camera);
                StringConfig stringConfig = ResourceConfigHelper.getInstance().getStringConfig();
                if (stringConfig != null && !TextUtils.isEmpty(stringConfig.getNoPermissionForCamera())) {
                    no_permission_for_camera = stringConfig.getNoPermissionForCamera();
                }
                ToastUtils.showToast(no_permission_for_camera);
            } else {
                ToastUtils.showToast(R.string.no_permission_for_camera);
            }
            e.printStackTrace();
            mRecorderResultInterface.onRecordingFailed(e.getMessage());
            return;
        }

        mVideoCapturePreview = new CapturePreview(this, mCameraWrapper, previewHolder, cameraId, surfaceParent, isTakePicture);
    }

    public void reverseCamera() {
        mVideoCapturePreview.reverseCamera();
    }

    public boolean isFaceBack() {
        return mVideoCapturePreview.isFaceBack();
    }

    public void startPreview() {
        mVideoCapturePreview.startPreview();
    }

    public void toggleRecording() throws AlreadyUsedException {
        if (mCameraWrapper == null) {
            throw new AlreadyUsedException();
        }

        if (isRecording()) {
            stopRecording(false, false, null);
        } else {
            startRecording();
        }
    }

    public void startTakePicture(Camera.PictureCallback pictureCallback) {
        mCameraWrapper.TakePicture(pictureCallback);
    }

    public boolean startRecording() {
        mRecording = false;
        // mVideoFile = new VideoFile("");
        if (!initRecorder())
            return false;
        if (!prepareRecorder())
            return false;
        if (!startRecorder())
            return false;
        mRecording = true;
        mRecorderStateInterface.onRecordingStarted();
        LogUtils.d(TAG, "Successfully started recording - outputfile: " + mVideoFile.getFullPath());
        return mRecording;
    }

    public void stopRecording(boolean isRemove, boolean isCancel, String message) {
        if (!isRecording())
            return;
        try {
            getMediaRecorder().stop();
            if (isRemove) {
                mVideoFile.cancelRecord();
                mRecorderResultInterface.onRecordingFailed("cancel record video");
            } else {
                long duration = FileUtils.getRecordDuration(mVideoFile.getFullPath());
                if (duration < 1000) {
                    mVideoFile.cancelRecord();
                    mRecorderResultInterface.onRecordingFailed("cancel record video");
                } else {
                    mRecorderResultInterface.onRecordingSuccess(mVideoFile.getFullPath());
                    LogUtils.d(TAG, "Successfully stopped recording - outputfile: "
                            + mVideoFile.getFullPath());
                    LogUtils.d(TAG, "Successfully message: " + message);
                }
            }
        } catch (final RuntimeException e) {
            mVideoFile.cancelRecord();
            if (!isCancel) {
                Context context = BaseInfoManager.getInstance().getContext();
                if (context != null) {
                    String no_permission_for_record_2 = context.getString(R.string.no_permission_for_record_2);
                    StringConfig stringConfig = ResourceConfigHelper.getInstance().getStringConfig();
                    if (stringConfig != null && !TextUtils.isEmpty(stringConfig.getNoPermissionForRecord2())) {
                        no_permission_for_record_2 = stringConfig.getNoPermissionForRecord2();
                    }
                    ToastUtils.showToast(no_permission_for_record_2);
                } else {
                    ToastUtils.showToast(R.string.no_permission_for_record_2);
                }
            }
            LogUtils.d(TAG, "Failed to stop recording");
            mRecorderResultInterface.onRecordingFailed("Failed to stop recording"); // 加这个是为了解决重复点击录像按钮引起的问题
        }

        mRecording = false;
        mRecorderStateInterface.onRecordingStopped(message);
    }

    private boolean initRecorder() {
        try {
            mCameraWrapper.prepareCameraForRecording();
        } catch (final PrepareCameraException e) {
            e.printStackTrace();
            mRecorderResultInterface.onRecordingFailed("Unable to record video");
            LogUtils.e(TAG, "Failed to initialize recorder - " + e.toString());
            return false;
        }
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.setOnInfoListener(this);
        } else {
            mRecorder.reset();
        }
        configureMediaRecorder(getMediaRecorder(), mCameraWrapper.getCamera());

        LogUtils.d(TAG, "MediaRecorder successfully initialized");
        return true;
    }

    @SuppressWarnings("deprecation")
    protected void configureMediaRecorder(final MediaRecorder recorder,
                                          android.hardware.Camera camera) throws IllegalStateException, IllegalArgumentException {
        recorder.setCamera(camera);
        recorder.setAudioSource(mCaptureConfiguration.getAudioSource());
        recorder.setVideoSource(mCaptureConfiguration.getVideoSource());

        CamcorderProfile baseProfile = mCameraWrapper.getBaseRecordingProfile();
        baseProfile.fileFormat = mCaptureConfiguration.getOutputFormat();

        RecordingSize size = mCameraWrapper.getSupportedRecordingSize(
                mCaptureConfiguration.getVideoWidth(), mCaptureConfiguration.getVideoHeight());
        baseProfile.videoFrameWidth = size.width;
        baseProfile.videoFrameHeight = size.height;
        baseProfile.videoBitRate = mCaptureConfiguration.getVideoBitrate();
        baseProfile.audioCodec = mCaptureConfiguration.getAudioEncoder();
        baseProfile.videoCodec = mCaptureConfiguration.getVideoEncoder();

        recorder.setProfile(baseProfile);
        recorder.setMaxDuration(mCaptureConfiguration.getMaxCaptureDuration());
        recorder.setOutputFile(mVideoFile.getFullPath());
        recorder.setOrientationHint(mCameraWrapper.getRecordRotationCorrection());

        try {
            recorder.setMaxFileSize(mCaptureConfiguration.getMaxCaptureFileSize());
        } catch (IllegalArgumentException e) {
            LogUtils.e(TAG, "Failed to set max filesize - illegal argument: "
                    + mCaptureConfiguration.getMaxCaptureFileSize());
        } catch (RuntimeException e2) {
            LogUtils.e(TAG, "Failed to set max filesize - runtime exception");
        }
    }

    private boolean prepareRecorder() {
        try {
            getMediaRecorder().prepare();
            LogUtils.d(TAG, "MediaRecorder successfully prepared");
            return true;
        } catch (final IllegalStateException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "MediaRecorder preparation failed - " + e.toString());
            return false;
        } catch (final IOException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "MediaRecorder preparation failed - " + e.toString());
            return false;
        }
    }

    private boolean startRecorder() {
        try {
            getMediaRecorder().start();
            LogUtils.d(TAG, "MediaRecorder successfully started");
            return true;
        } catch (final IllegalStateException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "MediaRecorder start failed - " + e.toString());
            return false;
        } catch (final RuntimeException e2) {
            Context context = BaseInfoManager.getInstance().getContext();
            if (context != null) {
                String no_permission_for_record = context.getString(R.string.no_permission_for_record);
                StringConfig stringConfig = ResourceConfigHelper.getInstance().getStringConfig();
                if (stringConfig != null && !TextUtils.isEmpty(stringConfig.getNoPermissionForRecord())) {
                    no_permission_for_record = stringConfig.getNoPermissionForRecord();
                }
                ToastUtils.showToast(no_permission_for_record);
            } else {
                ToastUtils.showToast(R.string.no_permission_for_record);
            }
            e2.printStackTrace();
            LogUtils.e(TAG, "MediaRecorder start failed - " + e2.toString());
            mRecorderResultInterface
                    .onRecordingFailed("Unable to record video with given settings");
            return false;
        }
    }

    protected boolean isRecording() {
        return mRecording;
    }

    protected MediaRecorder getMediaRecorder() {
        return mRecorder;
    }

    private void releaseRecorderResources() {
        MediaRecorder recorder = getMediaRecorder();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
    }

    public void releaseAllResources() {
        if (mVideoCapturePreview != null) {
            mVideoCapturePreview.releasePreviewResources();
        }
        if (mCameraWrapper != null) {
            mCameraWrapper.releaseCamera();
            mCameraWrapper = null;
        }
        releaseRecorderResources();
        LogUtils.d(TAG, "Released all resources");
    }

    @Override
    public void onCapturePreviewFailed() {
        mRecorderResultInterface.onRecordingFailed("Unable to show camera preview");
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        switch (what) {
            case MediaRecorder.MEDIA_RECORDER_INFO_UNKNOWN:
                // NOP
                break;
            case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
                LogUtils.d(TAG, "MediaRecorder max duration reached");
                stopRecording(false, false, "Capture stopped - Max duration reached");
                break;
            case MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED:
                LogUtils.d(TAG, "MediaRecorder max filesize reached");
                stopRecording(false, false, "Capture stopped - Max file size reached");
                break;
            default:
                break;
        }
    }

    @Override
    public void onStart() {
        mVideoCapturePreview.onStart();
    }

    @Override
    public void onStop() {
        mVideoCapturePreview.onStop();
    }
}
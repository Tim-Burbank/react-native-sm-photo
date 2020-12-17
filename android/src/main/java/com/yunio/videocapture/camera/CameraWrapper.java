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

package com.yunio.videocapture.camera;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

import com.yunio.videocapture.camera.OpenCameraException.OpenType;
import com.yunio.videocapture.prewiew.SensorControler;
import com.yunio.videocapture.utils.LogUtils;
import com.yunio.videocapture.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class CameraWrapper {
    private final static String TAG = "CameraWrapper";
    private final static int MAX_TIME = 3;
    private final int mDisplayRotation;
    private NativeCamera mNativeCamera = null;
    private Parameters mParameters = null;
    private CameraSize mDefaultSize = new CameraSize(1920, 1080);
    private int mFailedTime = 0;//次数
    private boolean cameraConfigured = false;//是否配置成功
    private CameraSize successCameraSize; //成功匹配的尺寸
    private SensorControler mSensorControler;


    public CameraWrapper(NativeCamera nativeCamera, int displayRotation) {
        mNativeCamera = nativeCamera;
        mDisplayRotation = displayRotation;
        mSensorControler = SensorControler.getInstance();
    }

    public Camera getCamera() {
        return mNativeCamera.getNativeCamera();
    }

    public void openCamera() throws OpenCameraException {
        openCamera(mNativeCamera.getCurrentFacingCameraId());
    }


    public void openCamera(int cameraId) throws OpenCameraException {
        try {
            mNativeCamera.openNativeCamera(cameraId);
            mSensorControler.restFoucs();
        } catch (final RuntimeException e) {
            e.printStackTrace();
            throw new OpenCameraException(OpenType.INUSE);
        }

        if (mNativeCamera.getNativeCamera() == null)
            throw new OpenCameraException(OpenType.NOCAMERA);
    }

    public void prepareCameraForRecording() throws PrepareCameraException {
        try {
            mNativeCamera.unlockNativeCamera();
        } catch (final RuntimeException e) {
            e.printStackTrace();
            throw new PrepareCameraException();
        }
    }

    public void releaseCamera() {
        if (getCamera() == null)
            return;
        mNativeCamera.releaseNativeCamera();
    }

    public void startPreview(final SurfaceHolder holder) throws IOException {
        mNativeCamera.setNativePreviewDisplay(holder);
        mNativeCamera.startNativePreview();
    }

    public void stopPreview() throws Exception {
        mNativeCamera.stopNativePreview();
        mNativeCamera.clearNativePreviewCallback();
    }

    public boolean TakePicture(Camera.PictureCallback pictureCallback) {
        try {
            mSensorControler.lockFocus();
            mNativeCamera.takePacture(pictureCallback);
        } catch (Exception e) {
            e.printStackTrace();
            mSensorControler.unlockFocus();
            return false;
        }
        return true;
    }

    public RecordingSize getSupportedRecordingSize(int width, int height) {
        CameraSize recordingSize = getOptimalSize(getSupportedVideoSizes(VERSION.SDK_INT), width,
                height);
        if (recordingSize == null) {
            LogUtils.e(TAG, "Failed to find supported recording size - falling back to requested: "
                    + width + "x" + height);
            return new RecordingSize(width, height);
        }
        LogUtils.d(TAG,
                "Recording size: " + recordingSize.getWidth() + "x" + recordingSize.getHeight());
        return new RecordingSize(recordingSize.getWidth(), recordingSize.getHeight());
    }

    public CamcorderProfile getBaseRecordingProfile() {
        CamcorderProfile returnProfile;
        if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {
            returnProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        } else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_720P)) {
            returnProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
        } else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_480P)) {
            returnProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
        } else {
            returnProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        }
        return returnProfile;
    }

    public void setFailedTimes(int time) {
        this.mFailedTime = time;
    }

    public void raiseFailedTime() {
        this.mFailedTime++;
    }

    public void configureSuccess(CameraSize successCameraSize) {
        this.cameraConfigured = true;
        this.successCameraSize = successCameraSize;
    }

    public boolean getConfigureSuccess() {
        return cameraConfigured;
    }

    public void setCameraConfigured(boolean cameraConfigured) {
        this.cameraConfigured = cameraConfigured;
    }

    public CameraSize configureForPreview(int viewWidth, int viewHeight) {
        //        final Parameters params = mNativeCamera.getNativeCameraParameters();
        LogUtils.d(TAG, "viewWidth-- >" + viewWidth + " viewHeight-- >" + viewHeight);
        mParameters = mNativeCamera.getNativeCameraParameters();
        mParameters.setPreviewFormat(ImageFormat.NV21);
        LogUtils.d(TAG, " cameraConfigured-- >" + cameraConfigured);
        CameraSize previewSize = null;
        if (!cameraConfigured) {
            LogUtils.d(TAG, " mFailedTime-- >" + mFailedTime);
            if (mFailedTime < MAX_TIME) {
                List<Size> tempSizes = null;
                switch (mFailedTime) {
                    case 0:
                        tempSizes = mParameters.getSupportedPreviewSizes();
                        break;
                    case 1:
                        tempSizes = mParameters.getSupportedPictureSizes();
                        break;
                    case 2:
                        tempSizes = mParameters.getSupportedVideoSizes();
                        break;
                }
                previewSize = getCloselyPreSize(true, viewWidth, viewHeight, tempSizes);
                //                previewSize = new CameraSize(viewWidth,viewHeight);
                LogUtils.d(TAG, "getCloselyPreSize previewSize width -- >" + previewSize.getWidth() + " height-- >" +
                        previewSize.getHeight());
            } else if (mFailedTime >= MAX_TIME) {
                previewSize = mDefaultSize;
            }
            if (previewSize == null) {
                Camera.Size size = mParameters.getPreviewSize();
                previewSize = new CameraSize(size.width, size.height);
            }
        } else {
            previewSize = successCameraSize;
        }
        if (previewSize != null) {
            LogUtils.d(TAG, "result  previewSize width -- >" + previewSize.getWidth() + " height-- >" +
                    previewSize.getHeight());
            if (getRotationCorrection() % 180 == 0) {
                mParameters.setPreviewSize(previewSize.getHeight(), previewSize.getWidth());
                mParameters.setPictureSize(previewSize.getHeight(), previewSize.getWidth());
            } else {
                mParameters.setPreviewSize(previewSize.getWidth(), previewSize.getHeight());
                mParameters.setPictureSize(previewSize.getWidth(), previewSize.getHeight());
            }
        }
        mNativeCamera.setDisplayOrientation(getRotationCorrection());
        mNativeCamera.updateNativeCameraParameters(mParameters);
        return previewSize;

        //        mParameters = mNativeCamera.getNativeCameraParameters();
        //        List<Size> sizes = mParameters.getSupportedPreviewSizes();
        //        Size systemDefaultSize = mParameters.getPreviewSize();
        //        Size systemPicSize = mParameters.getPreviewSize();
        //        int systemDefaultMaxWidth = Math.max(systemDefaultSize.width, systemDefaultSize.height);
        //        int systemDefaultMinWidth = Math.min(systemDefaultSize.width, systemDefaultSize.height);
        //        LogUtils.d(TAG, "systemDefaultSize width -- >" + systemDefaultSize.width + "height-- >" + systemDefaultSize.height);
        //        LogUtils.d(TAG, "systemPicSize width -- >" + systemPicSize.width + "height-- >" + systemPicSize.height);
        //        CameraSize previewSize = null;
        //        for (Size size : sizes) {
        //            LogUtils.d(TAG, "size width -- >" + size.width + "height-- >" +
        //                    size.height);
        //            if ((size.width == viewWidth && size.height == viewHeight) || (size.width == viewHeight && size.height == viewWidth)) {
        //                previewSize = new CameraSize(size.width, size.height);
        //                previewSize = compareWithDefault(previewSize);
        //                break;
        //            }
        //        }
        //        if (previewSize == null) {
        //            previewSize = getOptimalSize(mParameters.getSupportedPreviewSizes(),
        //                    viewWidth, viewHeight);
        //            LogUtils.e(TAG, "getOptimalSize previewSize width -- >" + previewSize.getWidth() + "height-- >" +
        //                    previewSize.getHeight());
        //            previewSize = compareWithDefault(previewSize);
        //        }
        //        if (systemDefaultMaxWidth == viewHeight && systemDefaultMinWidth == viewWidth) {
        //            previewSize = new CameraSize(systemDefaultSize.width, systemDefaultSize.height);
        //        }
        //        if (isFaild) {
        //            previewSize = mDefaultSize;
        //        }
        //        LogUtils.e(TAG, "previewSize width -- >" + previewSize.getWidth() + "height-- >" +
        //                previewSize.getHeight());
        //        if (getRotationCorrection() % 180 == 0) {
        //            mParameters.setPreviewSize(previewSize.getHeight(), previewSize.getWidth());
        //            mParameters.setPictureSize(previewSize.getHeight(), previewSize.getWidth());
        //        } else {
        //            mParameters.setPreviewSize(previewSize.getWidth(), previewSize.getHeight());
        //            mParameters.setPictureSize(previewSize.getWidth(), previewSize.getHeight());
        //        }
        //        //        mParameters.setPreviewSize(1920, 1080);
        //        //        mParameters.setPreviewSize(1920, 1080);
        //        mParameters.setPreviewFormat(ImageFormat.NV21);
        //        mNativeCamera.updateNativeCameraParameters(mParameters);
        //        mNativeCamera.setDisplayOrientation(getRotationCorrection());
        //        LogUtils.d(TAG, "Preview size: " + previewSize.getWidth() + "x" + previewSize.getHeight());
    }

    private CameraSize compareWithDefault(CameraSize previewSize) {
        CameraSize result;
        if (previewSize.getWidth() == mDefaultSize.getWidth() || previewSize.getHeight() == mDefaultSize.getHeight()) {
            result = mDefaultSize;
        } else {
            int preview = Math.min(previewSize.getWidth(), previewSize.getHeight());
            int default0 = Math.min(mDefaultSize.getWidth(), mDefaultSize.getHeight());
            int previewMaxWidth = Math.max(previewSize.getWidth(), previewSize.getHeight()); // 支持预览的最长的边
            int previewDefaultMaxWidth = Math.max(mDefaultSize.getWidth(), mDefaultSize.getHeight()); //默认支持的最长的边
            if (preview >= default0) {
                result = mDefaultSize;
                if (previewDefaultMaxWidth < previewMaxWidth) {
                    result = previewSize;
                }
            } else {
                result = previewSize;
            }
        }
        return result;
    }

    public boolean enableAutoFocus(String focusMode) {
        // final Parameters params = mNativeCamera.getNativeCameraParameters();
        List<String> focusModes = mParameters.getSupportedFocusModes();
        for (String mode : focusModes) {
            LogUtils.d(TAG, "focusMode : " + mode);
        }
        if (!Utils.isEmpty(focusModes)) {
            if (focusModes.contains(focusMode)) {
                mParameters.setFocusMode(focusMode);
                mNativeCamera.updateNativeCameraParameters(mParameters);
                return true;
            }
        }
        return false;
    }

    public void autoFocus(Camera.AutoFocusCallback autoFocusCallback) {
        mNativeCamera.autoFocus(autoFocusCallback);
    }

    /**
     * 手动聚焦
     *
     * @param point 触屏坐标
     */
    public boolean onFocus(Point point, Camera.AutoFocusCallback callback) {
        if (mNativeCamera == null) {
            return false;
        }
        //不支持设置自定义聚焦，则使用自动聚焦，返回
        if (Build.VERSION.SDK_INT >= 14) {

            if (mParameters.getMaxNumFocusAreas() <= 0) {
                return focus(callback);
            }

            Log.i(TAG, "onCameraFocus:" + point.x + "," + point.y);

            List<Camera.Area> areas = new ArrayList<Camera.Area>();
            int left = point.x - 300;
            int top = point.y - 300;
            int right = point.x + 300;
            int bottom = point.y + 300;
            left = left < -1000 ? -1000 : left;
            top = top < -1000 ? -1000 : top;
            right = right > 1000 ? 1000 : right;
            bottom = bottom > 1000 ? 1000 : bottom;
            areas.add(new Camera.Area(new Rect(left, top, right, bottom), 100));
            mParameters.setFocusAreas(areas);
            try {
                //本人使用的小米手机在设置聚焦区域的时候经常会出异常，看日志发现是框架层的字符串转int的时候出错了，
                //目测是小米修改了框架层代码导致，在此try掉，对实际聚焦效果没影响
                mNativeCamera.updateNativeCameraParameters(mParameters);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
                return false;
            }
        }
        return focus(callback);
    }

    private boolean focus(Camera.AutoFocusCallback callback) {
        try {
            mNativeCamera.autoFocus(callback);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public int getRotationCorrection() {
        int displayRotation = mDisplayRotation * 90;
        int result;
        if (mNativeCamera.getCurrentFacingCameraId() == NativeCamera.CAMERA_FACING_BACK) {
            result = (mNativeCamera.getCameraOrientation() - displayRotation + 360) % 360;
        } else {
            result = (mNativeCamera.getCameraOrientation() + displayRotation) % 360;
            result = (360 - result) % 360;
        }
        return result;
    }

    public int getRecordRotationCorrection() {
        int result;
        int rotation = getRotationCorrection();
        if (mNativeCamera.getCurrentFacingCameraId() == NativeCamera.CAMERA_FACING_BACK) {
            if (rotation == 180) {
                result = 180;
            } else {
                result = 0;
            }
        } else {
            if (rotation == 270 || rotation == 90 || rotation == 180) {
                result = 180;
            } else {
                result = 0;
            }
        }
        return (result + rotation) % 360;
    }

    @TargetApi(VERSION_CODES.HONEYCOMB)
    protected List<Size> getSupportedVideoSizes(int currentSdkInt) {
        // Parameters params = mNativeCamera.getNativeCameraParameters();

        List<Size> supportedVideoSizes;
        if (currentSdkInt < Build.VERSION_CODES.HONEYCOMB) {
            LogUtils.e(TAG,
                    "Using supportedPreviewSizes iso supportedVideoSizes due to API restriction");
            supportedVideoSizes = mParameters.getSupportedPreviewSizes();
        } else if (mParameters.getSupportedVideoSizes() == null) {
            LogUtils.e(TAG, "Using supportedPreviewSizes because supportedVideoSizes is null");
            supportedVideoSizes = mParameters.getSupportedPreviewSizes();
        } else {
            supportedVideoSizes = mParameters.getSupportedVideoSizes();
        }

        return supportedVideoSizes;
    }

    /**
     * Copyright (C) 2013 The Android Open Source Project
     * <p/>
     * Licensed under the Apache License, Version 2.0 (the "License"); you may
     * not use this file except in compliance with the License. You may obtain a
     * copy of the License at
     * <p/>
     * http://www.apache.org/licenses/LICENSE-2.0
     * <p/>
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
     * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
     * License for the specific language governing permissions and limitations
     * under the License.
     */
    public CameraSize getOptimalSize(List<Size> sizes, int w, int h) {
        // Use a very small tolerance because we want an exact match.
        final double ASPECT_TOLERANCE = 0.1;
        final double targetRatio;
        if (w > h) {
            targetRatio = (double) w / h;
        } else {
            targetRatio = (double) h / w;
        }

        if (sizes == null)
            return null;

        Size optimalSize = null;

        // Start with max value and refine as we iterate over available preview
        // sizes. This is the
        // minimum difference between view and camera height.
        double minDiff = Double.MAX_VALUE;

        // Target view height
        final int targetHeight = h;

        // Try to find a preview size that matches aspect ratio and the target
        // view size.
        // Iterate over all available sizes and pick the largest size that can
        // fit in the view and
        // still maintain the aspect ratio.
        for (final Size size : sizes) {
            final double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                continue;
            }
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find preview size that matches the aspect ratio, ignore the
        // requirement
        if (optimalSize == null) {
            for (final Size size : sizes) {
                if (h == size.height || w == size.height) {
                    optimalSize = size;
                }
            }
            if (optimalSize == null) {
                minDiff = Double.MAX_VALUE;
                for (final Size size : sizes) {
                    if (Math.abs(size.height - targetHeight) < minDiff) {
                        optimalSize = size;
                        minDiff = Math.abs(size.height - targetHeight);
                    }
                }
            }
        }
        return new CameraSize(optimalSize.width, optimalSize.height);
    }


    /**
     * 通过对比得到与宽高比最接近的预览尺寸（如果有相同尺寸，优先选择）
     *
     * @param isPortrait    是否竖屏
     * @param surfaceWidth  需要被进行对比的原宽
     * @param surfaceHeight 需要被进行对比的原高
     * @param preSizeList   需要对比的预览尺寸列表
     * @return 得到与原宽高比例最接近的尺寸
     */
    public static CameraSize getCloselyPreSize(boolean isPortrait, int surfaceWidth, int surfaceHeight, List<Size> preSizeList) {
        int reqTmpWidth;
        int reqTmpHeight;
        // 当屏幕为垂直的时候需要把宽高值进行调换，保证宽大于高
        if (isPortrait) {
            reqTmpWidth = surfaceHeight;
            reqTmpHeight = surfaceWidth;
        } else {
            reqTmpWidth = surfaceWidth;
            reqTmpHeight = surfaceHeight;
        }
        for (Size size : preSizeList) {
            LogUtils.d(TAG, "size width : " + size.width + " height : " + size.height);
        }
        //先查找preview中是否存在与surfaceview相同宽高的尺寸
        for (Size size : preSizeList) {
            if ((size.width == reqTmpWidth) && (size.height == reqTmpHeight)) {
                return new CameraSize(size.width, size.height);
            }
        }

        // 得到与传入的宽高比最接近的size
        float reqRatio = ((float) reqTmpWidth) / reqTmpHeight;
        float curRatio, deltaRatio;
        float deltaRatioMin = Float.MAX_VALUE;
        Size retSize = null;
        for (Size size : preSizeList) {
            curRatio = ((float) size.width) / size.height;
            deltaRatio = Math.abs(reqRatio - curRatio);
            if (deltaRatio < deltaRatioMin) {
                if (size.width > 3000) {
                    continue;
                }
                deltaRatioMin = deltaRatio;
                retSize = size;
            }
        }

        return new CameraSize(retSize.width, retSize.height);

    }

    public int getCurrentFacingCameraId() {
        return mNativeCamera.getCurrentFacingCameraId();
    }
}

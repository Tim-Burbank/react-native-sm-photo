/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yunio.videocapture.prewiew;

import android.graphics.Point;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.yunio.videocapture.camera.CameraSize;
import com.yunio.videocapture.camera.CameraWrapper;
import com.yunio.videocapture.camera.NativeCamera;
import com.yunio.videocapture.camera.OpenCameraException;
import com.yunio.videocapture.utils.LogUtils;
import com.yunio.videocapture.utils.UIUtils;

import java.io.IOException;

public class CapturePreview implements SurfaceHolder.Callback, IActivityLifiCycle {
    private final static String TAG = "CapturePreview";
    private static final int MSG_AUTOFUCS = 1001;
    private int CurrentCameraId = NativeCamera.CAMERA_FACING_FRONT;
    private boolean mPreviewRunning = false;
    private final CapturePreviewInterface mInterface;
    public final CameraWrapper mCameraWrapper;
    private SurfaceHolder mSurfaceHolder;
    private View mSurfaceParent;
    private Handler handler;
    private SensorControler mSensorControler;
    private Camera.AutoFocusCallback autoFocusCallback;
    private boolean mIsTakePickture;

    public CapturePreview(CapturePreviewInterface capturePreviewInterface,
                          CameraWrapper cameraWrapper, SurfaceHolder holder, int cameraId, View surfaceParent, boolean isTakePicture) {
        mInterface = capturePreviewInterface;
        mCameraWrapper = cameraWrapper;
        mSurfaceHolder = holder;
        CurrentCameraId = cameraId;
        mSurfaceParent = surfaceParent;
        mIsTakePickture = isTakePicture;
        initalizeSurfaceHolder(holder);
        handler = new Handler();
        mSensorControler = SensorControler.getInstance();
        mSensorControler.setCameraFocusListener(new SensorControler.CameraFocusListener() {
            @Override
            public void onFocus() {
                onCameraFocusDefault();
            }
        });
        autoFocusCallback = new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                LogUtils.d(TAG, "auto focus : " + success);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //一秒之后才能再次对焦
                        mSensorControler.unlockFocus();
                    }
                }, 1000);
            }
        };
        mSurfaceParent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                LogUtils.d(TAG, "mSurfaceParent onTouch");
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_UP:
                        LogUtils.d(TAG, "ACTION_UP ACTION_UP");
                        Point point = new Point((int) event.getX(), (int) event.getY());
                        onCameraFocus(point);
                        break;
                }
                return true;
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void initalizeSurfaceHolder(final SurfaceHolder surfaceHolder) {
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // Necessary
        // for
        // older
        // API's
    }

    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
        // NOP
        LogUtils.d(TAG, "---------surfaceCreated-----------");
        checkToResumeCamera();
    }

    private int mWidth, mHeight;

    @Override
    public void surfaceChanged(final SurfaceHolder holder, final int format, final int width,
                               final int height) {
        LogUtils.d(TAG, "surfaceChanged width and height: %d*%d", width, height);
        mWidth = width;
        mHeight = height;
        resetConfigurePreview(false);
        startPreview();
    }

    @Override
    public void surfaceDestroyed(final SurfaceHolder holder) {
        // NOP
        releasePreviewResources();
        releaseCamera();
        LogUtils.d(TAG, "---------------- surfaceDestroyed ------------------");
    }

    private void releaseCamera() {
        if (mCameraWrapper != null) {
            mCameraWrapper.releaseCamera();
        }
    }

    public void releasePreviewResources() {
        if (mPreviewRunning) {
            try {
                mCameraWrapper.stopPreview();
                setPreviewRunning(false);
                handler.removeMessages(MSG_AUTOFUCS);
            } catch (final Exception e) {
                e.printStackTrace();
                LogUtils.e(TAG, "Failed to clean up preview resources");
            }
        }
    }

    protected void setPreviewRunning(boolean running) {
        mPreviewRunning = running;
    }

    private void resetConfigurePreview(boolean reverse) {
        mCameraWrapper.setFailedTimes(0);
        if (reverse) {
            mCameraWrapper.setCameraConfigured(false);// 前置摄像头成功预览的尺寸后置摄像头不一定支持
        }
    }

    public void reverseCamera() {
        releasePreviewResources();
        mCameraWrapper.releaseCamera();
        int currentCameraId = mCameraWrapper.getCurrentFacingCameraId();
        int cameraId = (NativeCamera.CAMERA_FACING_BACK == currentCameraId ? NativeCamera.CAMERA_FACING_FRONT : NativeCamera.CAMERA_FACING_BACK);
        try {
            mCameraWrapper.openCamera(cameraId);
            CurrentCameraId = cameraId;
            resetConfigurePreview(true);
            startPreview();
        } catch (OpenCameraException e) {
            e.printStackTrace();
        }
    }

    public boolean isFaceBack() {
        return NativeCamera.CAMERA_FACING_BACK == mCameraWrapper.getCurrentFacingCameraId();
    }


    public void startPreview() {
        if (mPreviewRunning) {
            try {
                mCameraWrapper.stopPreview();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        try {
            int width = mWidth;
            int height = mHeight;
            LogUtils.d(TAG,
                    "Configured camera for preview in surface of " + width + " by " + height);
            CameraSize cameraSize = mCameraWrapper.configureForPreview(width, height);
            LogUtils.d(TAG, "configureForPreview success");
            mCameraWrapper.setFailedTimes(0);
            if (!mCameraWrapper.getConfigureSuccess()) {
                configureSurfaceParent(cameraSize);
            }
        } catch (final RuntimeException e) {
            e.printStackTrace();
            LogUtils.d(TAG, "Failed to show preview - invalid parameters set to camera preview");
            mInterface.onCapturePreviewFailed();
            mCameraWrapper.raiseFailedTime();
            startPreview();
            return;
        }
        boolean foucusable = true;
        try {
            if (mIsTakePickture) {
                mCameraWrapper.enableAutoFocus(Camera.Parameters.FOCUS_MODE_AUTO);
            } else {
                boolean flag = mCameraWrapper.enableAutoFocus(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                if (flag) {
                    foucusable = false;
                    mSensorControler.lockFocus();
                } else {
                    mSensorControler.unlockFocus();
                    mCameraWrapper.enableAutoFocus(Camera.Parameters.FOCUS_MODE_AUTO);
                }
            }
        } catch (final RuntimeException e) {
            e.printStackTrace();
            LogUtils.d(TAG, "AutoFocus not available for preview");
        }

        try {
            mCameraWrapper.startPreview(mSurfaceHolder);
            setPreviewRunning(true);
            if (foucusable) {
                onCameraFocusDefault();
            }
            //            if (!set) {
            //                mCameraWrapper.autoFocus(autoFocusCallback);
            //            }
        } catch (final IOException e) {
            e.printStackTrace();
            LogUtils.d(TAG,
                    "Failed to show preview - unable to connect camera to preview (IOException)");
            mInterface.onCapturePreviewFailed();
        } catch (final RuntimeException e) {
            e.printStackTrace();
            LogUtils.d(TAG,
                    "Failed to show preview - unable to start camera preview (RuntimeException)");
            mInterface.onCapturePreviewFailed();
        }
    }

    private void configureSurfaceParent(CameraSize size) {
        mCameraWrapper.configureSuccess(size);
        if (mSurfaceParent instanceof RelativeLayout) {
            RelativeLayout parent = (RelativeLayout) mSurfaceParent;
            double ratio = size.getHeight() / (double) size.getWidth();
            LogUtils.d(TAG, "ratio : " + ratio);
            //            int[] wh = WindowUtils.getScreenWH(mSurfaceParent.getContext());
            LogUtils.d(TAG, "W : " + mWidth + " H : " + mHeight);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) parent.getLayoutParams();
            params.height = mHeight;
            int width = (int) (mHeight * ratio);
            if (width <= mWidth) {
                params.width = mWidth;
            } else {
                params.width = width;
            }
            //            params.width = (int) (mHeight * ratio);
            parent.setLayoutParams(params);
            int deslocationX = (int) (params.width / 2.0 - mWidth / 2.0);
            LogUtils.d(TAG, "deslocationX : " + deslocationX);
            parent.animate().translationX(-deslocationX);
        }
    }

    private void checkToResumeCamera() {
        if (mCameraWrapper != null && mCameraWrapper.getCamera() == null) {
            try {
                mCameraWrapper.openCamera(CurrentCameraId);
            } catch (OpenCameraException e) {
                e.printStackTrace();
            }
        }
    }

    private void onCameraFocusDefault() {
        Point point = new Point(UIUtils.getWidthPixels() / 2, UIUtils.getHeightPixels() / 2);
        onCameraFocus(point, false);
    }

    /**
     * 相机对焦  默认不需要延时
     *
     * @param point
     */
    private void onCameraFocus(final Point point) {
        onCameraFocus(point, false);
    }

    /**
     * 相机对焦
     *
     * @param point
     * @param needDelay 是否需要延时
     */
    public void onCameraFocus(final Point point, boolean needDelay) {
        long delayDuration = needDelay ? 300 : 0;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mSensorControler.isFocusLocked()) {
                    if (mCameraWrapper.onFocus(point, autoFocusCallback)) {
                        mSensorControler.lockFocus();
                    }
                }
            }
        }, delayDuration);
    }

    @Override
    public void onStart() {
        mSensorControler.onStart();
    }

    @Override
    public void onStop() {
        mSensorControler.onStop();
    }
}
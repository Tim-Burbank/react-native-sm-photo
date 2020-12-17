package com.yunio.videocapture.camera;

import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.view.SurfaceHolder;

import java.io.IOException;

/**
 * Wrapper around the native camera class so all camera access can easily be
 * mocked.
 * <p/>
 */
public class NativeCamera {
    public final static int CAMERA_FACING_BACK = CameraInfo.CAMERA_FACING_BACK;
    public final static int CAMERA_FACING_FRONT = CameraInfo.CAMERA_FACING_FRONT;
    private Camera camera = null;

    public static final int DEFAULT_CAMERA_ID = CAMERA_FACING_FRONT;
    private int mCurrentCameraId = DEFAULT_CAMERA_ID;

    public Camera getNativeCamera() {
        return camera;
    }

    public void openNativeCamera() throws RuntimeException {
        openNativeCamera(CAMERA_FACING_FRONT);
    }

    public void openNativeCamera(int cameraId) throws RuntimeException {
        cameraId = checkCameraId(cameraId);
        camera = Camera.open(cameraId);
        mCurrentCameraId = cameraId;
    }

    public void unlockNativeCamera() {
        camera.unlock();
    }

    public void releaseNativeCamera() {
        camera.release();
        camera = null;
    }

    public void setNativePreviewDisplay(SurfaceHolder holder) throws IOException {
        camera.setPreviewDisplay(holder);
    }

    public void startNativePreview() {
        camera.startPreview();
    }

    public void stopNativePreview() {
        camera.stopPreview();
    }

    public void clearNativePreviewCallback() {
        camera.setPreviewCallback(null);
    }

    public Parameters getNativeCameraParameters() {
        return camera.getParameters();
    }

    public void updateNativeCameraParameters(Parameters params) {
        camera.setParameters(params);
    }

    public void setDisplayOrientation(int degrees) {
        camera.setDisplayOrientation(degrees);
    }

    public int getCameraOrientation() {
        CameraInfo camInfo = new CameraInfo();
        Camera.getCameraInfo(mCurrentCameraId, camInfo);
        return camInfo.orientation;
    }

    public void takePacture(Camera.PictureCallback pictureCallback) {
        camera.takePicture(null, null, pictureCallback);
    }

    public void autoFocus(Camera.AutoFocusCallback autoFocusCallback) {
        camera.autoFocus(autoFocusCallback);
    }

    /**
     * 检查是否存在指定摄像头，如果不存在则使用默认摄像头
     *
     * @param cameraId
     * @return
     */
    private int checkCameraId(int cameraId) {
        int resultCameraId = -1;
        CameraInfo info = new CameraInfo();
        int defaultCameraId = -1;
        for (int i = 0, numberOfCameras = Camera.getNumberOfCameras(); i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, info);
            if (info.facing == cameraId) {
                resultCameraId = cameraId;
                break;
            }
            if (info.facing == DEFAULT_CAMERA_ID) {
                defaultCameraId = DEFAULT_CAMERA_ID;
            } else if (defaultCameraId != DEFAULT_CAMERA_ID) {
                defaultCameraId = info.facing;
            }
        }
        return resultCameraId >= 0 ? resultCameraId : defaultCameraId;
    }

    public int getCurrentFacingCameraId() {
        return mCurrentCameraId;
    }
}

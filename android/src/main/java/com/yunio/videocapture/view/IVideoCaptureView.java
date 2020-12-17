package com.yunio.videocapture.view;

import android.content.res.Resources;
import android.view.SurfaceHolder;
import android.view.View;

import com.yunio.videocapture.record.VideoRecorderStateInterface;

/**
 * Created by PeterZhang on 2018/6/4.
 */

public interface IVideoCaptureView extends VideoRecorderStateInterface {

    public Resources getResources();

    public SurfaceHolder getPreviewSurfaceHolder();

    public void setMax(int max);

    public void setRecordingInterface(RecordingInterface recordingInterface);

    public View getSurfaceParent();
}

package com.yunio.videocapture.view;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.yunio.photoplugin.R;

public abstract class VideoCaptureBaseView extends FrameLayout implements IVideoCaptureView {
    private final static String TAG = "VideoCaptureBaseView";
    protected SurfaceView mSurfaceView;
    protected RecordingInterface mRecordingInterface;
    protected RelativeLayout mFrameView;

    protected Context mContext;
    protected boolean mIsRecording;
    protected int mMax;

    public VideoCaptureBaseView(Context context) {
        super(context);
        initialize(context);
    }

    public VideoCaptureBaseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    private void initialize(Context context) {
        mContext = context;
        View rootView = View.inflate(context, getContentLayoutId(), this);
        mSurfaceView = (SurfaceView) rootView.findViewById(R.id.sv_preview);
        mFrameView = rootView.findViewById(R.id.inner_frame);
        onInitView(rootView);
    }

    protected void onInitView(View rootView) {

    }

    protected abstract int getContentLayoutId();

    public Resources getResources() {
        return mContext.getResources();
    }

    public SurfaceHolder getPreviewSurfaceHolder() {
        return mSurfaceView.getHolder();
    }

    @Override
    public View getSurfaceParent() {
        return mFrameView;
    }

    public void setMax(int max) {
        mMax = max;
    }

    public void setRecordingInterface(RecordingInterface recordingInterface) {
        this.mRecordingInterface = recordingInterface;
    }

    @Override
    public void onRecordingStopped(String message) {
        mIsRecording = false;
    }

    @Override
    public void onRecordingStarted() {
        mIsRecording = true;
    }

    protected boolean isRecording() {
        return mIsRecording;
    }
}

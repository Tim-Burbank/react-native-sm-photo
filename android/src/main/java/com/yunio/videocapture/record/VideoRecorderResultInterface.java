package com.yunio.videocapture.record;

public interface VideoRecorderResultInterface {
    public abstract void onRecordingSuccess(String path);

    public abstract void onRecordingFailed(String message);
}

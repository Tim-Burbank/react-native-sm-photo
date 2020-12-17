package com.yunio.videocapture.resource.entity;

import com.google.gson.annotations.SerializedName;

public class ColorConfig implements IConfig {
    @SerializedName("theme_color")
    private String themeColor;
    @SerializedName("recording_color_doing")
    private String recordingColorDoing;
    @SerializedName("recording_color_cancel")
    private String recordingColorCancel;
    @SerializedName("camera_pressed")
    private String cameraPressed;


    public String getThemeColor() {
        return themeColor;
    }

    public void setThemeColor(String themeColor) {
        this.themeColor = themeColor;
    }

    public String getRecordingColorDoing() {
        return recordingColorDoing;
    }

    public void setRecordingColorDoing(String recordingColorDoing) {
        this.recordingColorDoing = recordingColorDoing;
    }

    public String getRecordingColorCancel() {
        return recordingColorCancel;
    }

    public void setRecordingColorCancel(String recordingColorCancel) {
        this.recordingColorCancel = recordingColorCancel;
    }

    public String getCameraPressed() {
        return cameraPressed;
    }

    public void setCameraPressed(String cameraPressed) {
        this.cameraPressed = cameraPressed;
    }
}

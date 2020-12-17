package com.yunio.videocapture.entity;

public class Video {
    private String path;
    private long duration;

    public Video(String path, long duration) {
        this.path = path;
        this.duration = duration;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

}

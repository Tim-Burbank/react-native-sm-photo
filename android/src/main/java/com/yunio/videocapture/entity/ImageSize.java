package com.yunio.videocapture.entity;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ImageSize implements Serializable {
    public static final String TAG = "ImageSize";

    private int mWidth;
    private int mHeight;

    public ImageSize(ImageSize size) {
        this.mWidth = size.mWidth;
        this.mHeight = size.mHeight;
    }

    public ImageSize(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    public void setWidth(int width) {
        this.mWidth = width;
    }

    public int getWidth() {
        return this.mWidth;
    }

    public void setHeight(int height) {
        this.mHeight = height;
    }

    public int getHeight() {
        return this.mHeight;
    }

    @Override
    public String toString() {
        return mWidth + "x" + mHeight;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + mHeight;
        result = prime * result + mWidth;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ImageSize other = (ImageSize) obj;
        if (mHeight != other.mHeight) return false;
        if (mWidth != other.mWidth) return false;
        return true;
    }

    public static final boolean valid(ImageSize size) {
        if (size != null && size.getWidth() > 0 && size.getHeight() > 0) {
            return true;
        } else {
            return false;
        }
    }
}
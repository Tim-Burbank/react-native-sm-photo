package com.yunio.videocapture.view;

import android.content.Context;
import android.util.AttributeSet;

import com.yunio.videocapture.utils.LogUtils;

/**
 * Created by Henry on 2018/3/1.
 */

public class AppVideoView extends VideoView {
    private final static String TAG = "AppVideoView";
    private int resizeWidth, resizeHeight;

    public AppVideoView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public AppVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public AppVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    public void setMeasured(int width, int height) {
        this.resizeWidth = width;
        this.resizeHeight = height;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getWidth() != 0 && getHeight() != 0 && getMeasuredWidth() < getMeasuredHeight()) {
            int width = getDefaultSize(getWidth(), widthMeasureSpec);
            int height = getDefaultSize(getHeight(), heightMeasureSpec);
            if (resizeWidth > 0 && resizeHeight > 0) {
                width = resizeWidth;
                height = resizeHeight;
            }
            LogUtils.d(TAG, "width : " + width + " height : " + height);
            setMeasuredDimension(width, height);
        }
    }
}

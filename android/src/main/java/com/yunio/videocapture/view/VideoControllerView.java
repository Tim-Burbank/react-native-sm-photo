package com.yunio.videocapture.view;

import android.content.Context;
import android.util.AttributeSet;

import com.yunio.photoplugin.R;

/**
 * Created by PeterZhang on 2018/6/1.
 */

public class VideoControllerView extends MediaController {
    public VideoControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoControllerView(Context context, boolean useFastForward) {
        super(context, useFastForward);
    }

    public VideoControllerView(Context context) {
        super(context);
    }

    @Override
    protected int getControllerLayoutId() {
        return R.layout.video_controller_layout;
    }
}

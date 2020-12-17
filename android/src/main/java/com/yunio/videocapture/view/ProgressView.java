package com.yunio.videocapture.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.yunio.photoplugin.R;
import com.yunio.videocapture.resource.ResourceConfigHelper;
import com.yunio.videocapture.resource.entity.ColorConfig;

public class ProgressView extends View {
    private Paint mProgressPaint;
    private Paint mRemovePaint;

    private int mMax;
    private int mProgress;
    private boolean isRemove;

    public ProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ProgressView(Context context) {
        super(context);
    }

    private void init() {
        ColorConfig config = ResourceConfigHelper.getInstance().getColorConfig();
        setBackgroundColor(getResources().getColor(android.R.color.transparent));
        mProgressPaint = new Paint();
        mProgressPaint.setColor(Color.GREEN);
        if(config!=null&&!TextUtils.isEmpty(config.getRecordingColorDoing())){
            mProgressPaint.setColor(Color.parseColor(config.getRecordingColorDoing()));
        }
        mProgressPaint.setStyle(Paint.Style.FILL);
        mRemovePaint = new Paint();
        mRemovePaint.setColor(getResources().getColor(R.color.text_yellow2));
        if(config!=null&&!TextUtils.isEmpty(config.getRecordingColorCancel())){
            mRemovePaint.setColor(Color.parseColor(config.getRecordingColorCancel()));
        }
        mRemovePaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int progressLength = (int) ((mProgress / (mMax * 1.0f)) * (width / 2));
        canvas.drawRect(progressLength, 0, width - progressLength, height,
                isRemove ? mRemovePaint : mProgressPaint);
        canvas.restore();
    }

    public void setMax(int max) {
        this.mMax = max;
    }

    public void setProgress(int progress) {
        this.mProgress = progress;
    }

    public void setIsRemove(boolean isRemove) {
        this.isRemove = isRemove;
    }

}

package com.yunio.videocapture.view;

import com.yunio.videocapture.utils.UIUtils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 在控件的左下角显示一个小三角
 *
 *
 *
 */
public class DropView extends TextView {

    private int mSize;
    private int mPadding;
    private Paint mPaint;

    public DropView(Context context) {
        this(context, null);
    }

    public DropView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DropView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (mPaint == null) {
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setStyle(Style.FILL);
            mPaint.setColor(getCurrentTextColor());

            // 小角标的宽度
            mSize = (int) (getTextSize() * 2 / 3);

            // 角标与文本的距离
            mPadding = UIUtils.dip2px(2);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int w = getMeasuredWidth() + mSize;

        setMeasuredDimension(w, getMeasuredHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int h = getMeasuredHeight();

        String text = getText().toString();
        int px = (int) getPaint().measureText(text) + mPadding;
        int py = (int) (h / 2 + getTextSize() / 2);

        Path path = new Path();
        path.moveTo(px, py);
        path.lineTo(px + mSize, py);
        path.lineTo(px + mSize, py - mSize);
        path.close();

        canvas.drawPath(path, mPaint);
    }
}

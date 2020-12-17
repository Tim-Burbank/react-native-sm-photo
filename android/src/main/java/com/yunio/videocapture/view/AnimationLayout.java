package com.yunio.videocapture.view;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.RelativeLayout;

public class AnimationLayout extends RelativeLayout implements AnimatorListener {

    private final static int DURATION = 300;

    private View bgView;
    private View innerView;
    private ViewPropertyAnimator bgAnimator;
    private ViewPropertyAnimator innerAnimator;

    private int value;
    private boolean isRunning;

    public AnimationLayout(Context context) {
        this(context, null);
    }

    public AnimationLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimationLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void init() {

        // 必须设置子view
        if (getChildCount() < 1) {
            throw new RuntimeException("the child view is required");
        }
        innerView = getChildAt(0);
        innerAnimator = innerView.animate();
        innerAnimator.setListener(this);

        // 初始化子view的位置
        initLocation();

        // 创建用于支持背景渐变的view
        bgView = new View(getContext());
        bgView.setLayoutParams(
                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        bgView.setBackgroundColor(0xb0000000);
        bgView.setAlpha(0);
        bgAnimator = bgView.animate();
        addView(bgView, 0);
    }

    private void initLocation() {
        value = innerView.getHeight();
        innerView.setY(-value);
        innerView.setVisibility(View.VISIBLE);
    }

    public boolean isShowing() {
        if (innerView == null) {
            return false;
        }
        return innerView.getY() == 0;
    }

    public void show() {
        if (innerView == null) {
            init();
        }
        if (isRunning) {
            return;
        }
        if (isShowing()) {
            dismiss();
            return;
        }
        bgAnimator.alpha(1).setDuration(DURATION).start();
        innerAnimator.y(0).setDuration(DURATION).start();
        setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isShowing()) {
                    dismiss();
                }
            }
        });
    }

    public void dismiss() {
        if (innerView == null) {
            return;
        }
        if (isRunning) {
            return;
        }
        bgAnimator.alpha(0).setDuration(DURATION).start();
        innerAnimator.y(-value).setDuration(DURATION).start();
        setClickable(false);
    }

    @Override
    public void onAnimationStart(Animator animation) {
        isRunning = true;
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        isRunning = false;
    }

    @Override
    public void onAnimationCancel(Animator animation) {
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
    }

}

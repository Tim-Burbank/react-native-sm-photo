package com.yunio.videocapture.utils;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

public class ViewUtils {

    public static boolean removeFromParent(View v) {
        try {
            ViewParent vp = v.getParent();
            if (vp instanceof ViewGroup) {
                ((ViewGroup) vp).removeView(v);
                return true;
            }
			
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 设置View的显示属性, Visible or Gone
     * 
     * @param view
     * @param show
     */
    public static void showView(View view, boolean show) {
        setVisibility(view, show ? View.VISIBLE : View.GONE);
    }

    /**
     * 设置view的visibile属性, 先判断是否一致
     * 
     * @param view
     * @param visible
     */
    public static void setVisibility(View view, int visible) {
        if (view != null && view.getVisibility() != visible) {
            view.setVisibility(visible);
        }
    }

    /**
     * 设置TextView的padding drawable
     * 
     * @param textView
     * @param resId
     * @param position
     */
    public static void setPaddingDrawable(TextView textView, int resId, int position) {
        setPaddingDrawable(textView, resId, position, 0);
    }

    /**
     * 设置TextView的padding drawable
     * 
     * @param textView
     * @param resId
     * @param position
     */
    public static void setPaddingDrawable(TextView textView, int resId, int position, int padding) {
        Drawable drawable = null;
        if (resId > 0) {
            try {
                drawable = textView.getResources().getDrawable(resId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        setPaddingDrawable(textView, drawable, position, true, padding);
    }

    /**
     * 根据宽高设置TextView的PaddingDrawable
     * 
     * @param view
     * @param resId
     * @param position
     * @param width
     * @param height
     */
    public final static void setPaddingDrawable(TextView view, int resId, int position, int width,
            int height) {
        BitmapDrawable drawable = (BitmapDrawable) view.getResources().getDrawable(resId);
        drawable.setBounds(0, 0, width, height);
        setPaddingDrawable(view, drawable, position, false, 0);
    }

    private static void setPaddingDrawable(TextView view, Drawable drawable, int position,
            boolean useIntrinsic, int padding) {
        Drawable left = null;
        Drawable top = null;
        Drawable right = null;
        Drawable bottom = null;
        switch (position) {
        case Gravity.LEFT:
            left = drawable;
            break;
        case Gravity.TOP:
            top = drawable;
            break;
        case Gravity.RIGHT:
            right = drawable;
            break;
        case Gravity.BOTTOM:
            bottom = drawable;
            break;
        default:
            throw new IllegalArgumentException("position is invalid");
        }
        if (useIntrinsic) {
            view.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
        } else {
            view.setCompoundDrawables(left, top, right, bottom);
        }
        if (padding > 0) {
            view.setCompoundDrawablePadding(padding);
        }
    }

    /**
     * get the width of child in gridview
     * 
     * @time Jan 13, 2014
     * 
     * @param numColums
     *            numColums of GRIDVIEW
     * @param spacing
     *            Horizontal spacing of GRIDVIEW
     * @param width
     *            WIDTH of GRIDVIEW (padding left & padding right is not
     *            included of GRIDVIEW)
     * @return
     */
    public final static int getAdapterItemSize(int numColums, int spacing, int width) {
        if (width < 0) {
            return 0;
        }
        if (numColums < 1 || spacing < 0) {
            return width;
        }
        int exSpace = (numColums + 1) * spacing;
        int trueWidth = width - exSpace;
        return (int) trueWidth / numColums;
    }

    public final static int getSmallSize() {
        return UIUtils.getWidthPixels() / 5;
    }

    public final static int getNormalSize() {
        return UIUtils.getWidthPixels() / 3;
    }

    public final static int getBigSize() {
        return UIUtils.getWidthPixels() / 2;
    }
}

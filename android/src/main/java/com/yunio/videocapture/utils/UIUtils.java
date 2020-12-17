package com.yunio.videocapture.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Looper;
import android.util.DisplayMetrics;

import java.lang.reflect.Field;

/**
 * 与UI相关的帮助类, 使用之前�?先调用{@link #init(Context)} 方法
 *
 * @author PeterZhang
 */
public class UIUtils {
    private static final String ANDROID_INTERNAL_DIME = "com.android.internal.R$dimen";
    private static float ROUND_DIFFERENCE = 0.5f;
    private static DisplayMetrics sDisplayMetrics;
    private static int sStatusBarHeight;

    /**
     * 初始化设备UI显示信息
     *
     * @param context
     */
    public static void init(Context context) {
        sDisplayMetrics = context.getResources().getDisplayMetrics();
    }

    /**
     * 获取屏幕宽度 单位：像�?
     *
     * @return 屏幕宽度
     */
    public static int getWidthPixels() {
        return sDisplayMetrics.widthPixels;
    }

    /**
     * 获取屏幕高度 单位：像�?
     *
     * @return 屏幕高度
     */
    public static int getHeightPixels() {
        return sDisplayMetrics.heightPixels;
    }

    /**
     * 获取屏幕宽度 单位：像�?
     *
     * @return 屏幕宽度
     */
    public static float getDensity() {
        return sDisplayMetrics.density;
    }

    /**
     * dp �? px
     *
     * @param dp dp�?
     * @return 转换后的像素�?
     */
    public static int dip2px(int dp) {
        return (int) (dp * sDisplayMetrics.density + ROUND_DIFFERENCE);
    }

    /**
     * px �? dp
     *
     * @param px px�?
     * @return 转换后的dp�?
     */
    public static int px2dip(int px) {
        return (int) (px / sDisplayMetrics.density + ROUND_DIFFERENCE);
    }

    public static boolean isUIThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    /**
     * 是否横屏
     *
     * @param context
     * @return
     */
    public static boolean isLandscape(Context context) {
        return context.getResources()
                .getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    /**
     * 设置状�?�栏的高度， 在�?�当的时候初始化
     *
     * @param statusBarHeight
     */
    public static void setStatusBarHeight(int statusBarHeight) {
        sStatusBarHeight = statusBarHeight;
    }

    /**
     * 获取状�?�栏的高�?
     *
     * @return
     */
    public static int getStatusBarHeight() {
        return sStatusBarHeight;
    }
    public static int getStatusBarHeight(Context mContext) {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, sbar = 0;
        try {
            c = Class.forName(ANDROID_INTERNAL_DIME);
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            sbar = mContext.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return sbar;
    }
    /**
     * 获取屏幕长宽比
     * @return
     */
    public static float getScreenRate() {
        float H = UIUtils.getHeightPixels();
        float W = UIUtils.getWidthPixels();
        return (H / W);
    }
}

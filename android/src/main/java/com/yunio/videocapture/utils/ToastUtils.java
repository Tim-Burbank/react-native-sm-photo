package com.yunio.videocapture.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {

	private static Context sContext; // Application 的context
	private static Toast sToast;

	/**
	 * 初始化
	 * 
	 * @param context
	 *            context
	 */
	public static void init(Context context) {
		sContext = context;
		sToast = Toast.makeText(sContext, "", Toast.LENGTH_SHORT);
	}

	/**
	 * 显示一个Toast提示
	 * 
	 * @param message
	 *            提示信息
	 * @param duration
	 *            显示时间
	 */
	public static void showToast(String message, int duration) {
		sToast.setText(message);
		sToast.setDuration(duration);
		sToast.show();
	}

	public static void showToast(int resId, int duration) {
		showToast(sContext.getString(resId), duration);
	}

	public static void showToast(int resId) {
		showToast(sContext.getString(resId), Toast.LENGTH_SHORT);
	}

	public static void showToast(String message) {
		showToast(message, Toast.LENGTH_SHORT);
	}
}

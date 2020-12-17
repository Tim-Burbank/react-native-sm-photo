package com.yunio.videocapture.utils;

import com.yunio.photoplugin.R;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * ProgressDialog帮助类
 * 
 * @author PeterZhang
 * 
 */
public class ProgressDialogUtils {
    private static Dialog sProgressDialog;
    private static TextView mTvMessage;
    private static boolean mIsCancelable;
    private static boolean mIsOutsideCancelable;
    private static OnTapCancelListener sOnTapCancelListener;

    /**
     * 点击对话框时取消对话框的listener
     * 
     * @author PeterZhang
     * 
     */
    public static interface OnTapCancelListener {
        /**
         * 点击对话框时dismiss前调用
         */
        public void onTapCancel();
    }

    /**
     * 显示一个进度对话框
     * 
     * @param context
     *            上下文对象
     * @param resId
     *            提示信息资源ID
     */
    public static synchronized void showProgressDialog(Context context, int resId) {
        showProgressDialog(context, context.getResources().getString(resId));
    }

    /**
     * 显示一个进度对话框
     * 
     * @param context
     *            上下文对象
     * @param message
     *            提示信息
     */
    public static synchronized void showProgressDialog(Context context, String message) {
        showProgressDialog(context, message, true, true);
    }

    /**
     * 显示一个进度对话框
     * 
     * @param context
     *            上下文对象
     * @param resId
     *            提示信息资源ID
     * @param canceledOnTouchOutsideEnable
     *            是否允许触摸对话框以外的地方，关闭对话框
     * @param cancelable
     *            点击back键时，是否关闭对话框
     */
    public static synchronized void showProgressDialog(Context context, int resId,
            boolean canceledOnTouchOutsideEnable, boolean cancelable) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null!!");
        }
        showProgressDialog(context, context.getString(resId), canceledOnTouchOutsideEnable,
                cancelable);
    }

    /**
     * 显示一个进度对话框
     * 
     * @param context
     * @param message
     * @param canceledOnTouchOutsideEnable
     * @param cancelable
     */
    public static synchronized void showProgressDialog(Context context, String message,
            boolean canceledOnTouchOutsideEnable, boolean cancelable) {
        showProgressDialog(context, message, canceledOnTouchOutsideEnable, cancelable, null);
    }

    /**
     * 显示一个进度对话框, 只能点击对话框对容才能取消
     * 
     * @param context
     * @param msgResId
     * @param onTapCancelListener
     */
    public static synchronized void showProgressDialog(Context context, int msgResId,
            OnTapCancelListener onTapCancelListener) {
        showProgressDialog(context, context.getString(msgResId), onTapCancelListener);
    }

    /**
     * 显示一个进度对话框, 只能点击对话框对容才能取消
     * 
     * @param context
     * @param message
     * @param onTapCancelListener
     */
    public static synchronized void showProgressDialog(Context context, String message,
            OnTapCancelListener onTapCancelListener) {
        showProgressDialog(context, message, false, false, onTapCancelListener);
    }

    /**
     * 显示一个进度对话框
     * 
     * @param context
     *            上下文对象
     * @param message
     *            提示信息
     * @param canceledOnTouchOutsideEnable
     *            是否允许触摸对话框以外的地方，关闭对话框
     * @param cancelable
     *            点击back键时，是否关闭对话框
     */
    public static synchronized void showProgressDialog(Context context, String message,
            boolean canceledOnTouchOutsideEnable, boolean cancelable,
            OnTapCancelListener onTapCancelListener) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null!!");
        }
        sOnTapCancelListener = onTapCancelListener;
        if (sProgressDialog != null) {
            if (sProgressDialog.isShowing() && mIsCancelable == cancelable
                    && mIsOutsideCancelable == canceledOnTouchOutsideEnable) {
                setMessage(message);
                return;
            }
            dismissDialog();
        }

        sProgressDialog = createProgressDialog(context);
        sProgressDialog.setCanceledOnTouchOutside(canceledOnTouchOutsideEnable);
        sProgressDialog.setCancelable(cancelable);
        setMessage(message);
        try {
            // 如果传过来的activity此时已被销毁则可能抛出异常
            sProgressDialog.show();
        } catch (Exception ex) {
            dismissProgressDialog();
            ex.printStackTrace();
        }

        mIsCancelable = cancelable;
        mIsOutsideCancelable = canceledOnTouchOutsideEnable;
    }

    private static Dialog createProgressDialog(Context context) {
        View contentView = LayoutInflater.from(context).inflate(R.layout.view_loading_dialog, null);
        mTvMessage = (TextView) contentView.findViewById(R.id.tv_msg_yprocess_dialog);
        Dialog dialog = new Dialog(context, R.style.custom_dialog);
        dialog.setContentView(contentView);
        contentView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (sOnTapCancelListener != null) {
                    sOnTapCancelListener.onTapCancel();
                    dismissProgressDialog();
                }
            }
        });
        return dialog;
    }

    /**
     * 设置被取消时的回调
     * 
     * @param listener
     */
    public synchronized static void setOnCancelListener(DialogInterface.OnCancelListener listener) {
        if (sProgressDialog != null) {
            sProgressDialog.setOnCancelListener(listener);
        }
    }

    /**
     * 关闭对话框
     */
    public static void dismissProgressDialog() {
        dismissDialog();
        sOnTapCancelListener = null;
    }

    /**
     * 捕获异常的dimiss方法
     */
    private static void dismissDialog() {
        if (sProgressDialog != null) {
            try {
                sProgressDialog.dismiss();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            sProgressDialog = null;
        }
    }

    /**
     * 进度对话框是否在显示中
     * 
     * @return true - 显示中
     */
    public static boolean isProgressDialogShowing() {
        return sProgressDialog != null && sProgressDialog.isShowing();
    }

    private static void setMessage(String message) {
        if (mTvMessage != null) {
            mTvMessage.setText(message);
        }
    }
}

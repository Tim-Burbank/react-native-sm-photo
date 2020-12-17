package com.yunio.videocapture.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParserException;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Class containing some static utility methods.
 */
public class Utils {
    private final static String TAG = "Utils";

    public static int sizeOf(Collection<?> collection) {
        if (collection == null) {
            return 0;
        }
        return collection.size();
    }

    public static void showIME(Context mContext) {
        InputMethodManager imm = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static void showIME(Context mContext, View v) {
        InputMethodManager imm = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        v.setFocusable(true);
        v.requestFocus();
        imm.showSoftInput(v, 0);
    }

    public static void hideIME(Context mContext, View v) {
        InputMethodManager imm = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean result = imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        LogUtils.e(TAG, "result : " + result);
    }

    public static boolean existsEmpty(String... text) {
        if (text == null || text.length == 0) {
            return true;
        } else {
            for (String str : text) {
                if (isEmpty(str)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean existsNotEmpty(String... text) {
        if (text == null || text.length == 0) {
            return false;
        } else {
            for (String str : text) {
                if (!isEmpty(str)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isEmpty(String text) {
        if (text == null || text.length() == 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isEmpty(Collection<?> collection) {
        return sizeOf(collection) == 0;
    }

    public static String encodeURL(String url) {
        if (isEmpty(url)) {
            return url;
        }

        String result = "";
        String[] temp = url.split("/");
        int length = temp.length;
        for (int index = 0; index < length; index++) {
            try {
                temp[index] = URLEncoder.encode(temp[index], "UTF-8");
                temp[index] = temp[index].replace("+", "%20");
            } catch (Exception e) {
                e.printStackTrace();
                return url;
            }
            result += temp[index];
            if (index < (length - 1)) {
                result += "/";
            }
        }
        return result;
    }

    public static int getRoundedFloat(float data) {
        BigDecimal decimal = new BigDecimal(data);
        return decimal.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
    }

    public static String resetDuration(long duration) {
        if (duration < 1) {
            duration = 1;
        }
        StringBuilder builder = new StringBuilder();
        long ms = duration / 60;// 分
        if (ms < 10) {
            builder.append("0");
        }
        builder.append(ms).append(":");
        long ss = duration % 60;// 秒
        if (ss < 10) {
            builder.append("0");
        }
        builder.append(ss);
        return builder.toString();
    }
}

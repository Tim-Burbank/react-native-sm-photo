package com.yunio.videocapture.resource;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.yunio.photoplugin.R;
import com.yunio.videocapture.resource.entity.ColorConfig;
import com.yunio.videocapture.utils.UIUtils;

public class ResourceUtils {

    public static int getThemeColor() {
        ColorConfig config = ResourceConfigHelper.getInstance().getColorConfig();
        if (config != null && !TextUtils.isEmpty(config.getThemeColor())) {
            return Color.parseColor(config.getThemeColor());
        }
        return Color.parseColor("#d8b434");
    }

    public static int getAlphaThemeColor() {
        ColorConfig config = ResourceConfigHelper.getInstance().getColorConfig();
        if (config != null && !TextUtils.isEmpty(config.getThemeColor())) {
            //            return Color.parseColor(config.getThemeColor());
            return Color.parseColor("#80" + config.getThemeColor().substring(1));
        }

        return Color.parseColor("#80d8b434");
    }

    public static int getCameraPressedColor() {
        ColorConfig config = ResourceConfigHelper.getInstance().getColorConfig();
        if (config != null && !TextUtils.isEmpty(config.getCameraPressed())) {
            return Color.parseColor(config.getCameraPressed());
        }
        return Color.parseColor("#55d8b434");
    }

    public static GradientDrawable getCameraNormalDrawable() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(Color.TRANSPARENT);
        drawable.setStroke(UIUtils.dip2px(2), ResourceUtils.getThemeColor());
        return drawable;
    }

    public static GradientDrawable getCameraPressedDrawable() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(Color.TRANSPARENT);
        drawable.setStroke(UIUtils.dip2px(2), ResourceUtils.getCameraPressedColor());
        return drawable;
    }

    public static GradientDrawable getThemeDrawable() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(ResourceUtils.getThemeColor());
        drawable.setStroke(UIUtils.dip2px(2), ResourceUtils.getThemeColor());
        return drawable;
    }

    public static void initTitleBack(View contentView){
        ImageView ivBack =  contentView.findViewById(R.id.title_left_img);
        Drawable back = ivBack.getDrawable();
        if(back !=null){
            back.setColorFilter(ResourceUtils.getThemeColor(),PorterDuff.Mode.SRC_IN);
        }
    }
}

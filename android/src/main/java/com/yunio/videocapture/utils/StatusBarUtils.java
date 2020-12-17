package com.yunio.videocapture.utils;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

public class StatusBarUtils {
  public static void setTranslucent(Activity activity, boolean translucent) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      View decorView = activity.getWindow().getDecorView();
      if (translucent) {
        decorView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
          @Override
          public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
            WindowInsets defaultInsets = view.onApplyWindowInsets(windowInsets);
            return defaultInsets.replaceSystemWindowInsets(
              defaultInsets.getSystemWindowInsetLeft(),
              0,
              defaultInsets.getSystemWindowInsetRight(),
              defaultInsets.getSystemWindowInsetBottom());
          }
        });
      } else {
        decorView.setOnApplyWindowInsetsListener(null);
      }
      ViewCompat.requestApplyInsets(decorView);
    }
  }

  public static void setStyle(Activity activity, @Nullable final String style) {
    View decorView = activity.getWindow().getDecorView();
    int systemUiVisibilityFlags = decorView.getSystemUiVisibility();
    if ("dark-content".equals(style)) {
      systemUiVisibilityFlags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
    } else {
      systemUiVisibilityFlags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
    }
    decorView.setSystemUiVisibility(systemUiVisibilityFlags);
  }

  public static void setColor(final Activity activity, int color, boolean animated) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
      if (animated) {
        int curColor = activity.getWindow().getStatusBarColor();
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), curColor, color);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
          @Override
          public void onAnimationUpdate(ValueAnimator animator) {
            activity.getWindow().setStatusBarColor((Integer) animator.getAnimatedValue());
          }
        });
        colorAnimation.setDuration(300).setStartDelay(0);
        colorAnimation.start();
      } else {
        activity.getWindow().setStatusBarColor(color);
      }
    }
  }

}

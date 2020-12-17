package com.yunio.videocapture.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.DisplayCutout;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 相关API
 * Window#setFlags
 * View#setSystemUiVisibility (Android 3.0开始提供)
 * 相关Flag
 * WindowManager.LayoutParams.FLAG_FULLSCREEN
 * 隐藏状态栏
 * View.SYSTEM_UI_FLAG_VISIBLE                   API 14
 * 默认标记
 * View.SYSTEM_UI_FLAG_LOW_PROFILE                  API 14
 * 低调模式, 会隐藏不重要的状态栏图标
 * View.SYSTEM_UI_FLAG_LAYOUT_STABLE                  API 16
 * 保持整个View稳定, 常和控制System UI悬浮, 隐藏的Flags共用, 使View不会因为System UI的变化而重新layout
 * View.SYSTEM_UI_FLAG_FULLSCREEN                     API 16
 * 状态栏隐藏，效果同设置WindowManager.LayoutParams.FLAG_FULLSCREEN
 * View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN             API 16
 * 视图延伸至状态栏区域，状态栏上浮于视图之上
 * View.SYSTEM_UI_FLAG_HIDE_NAVIGATION                  API 14
 * 隐藏导航栏
 * View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION            API 16
 * 视图延伸至导航栏区域，导航栏上浮于视图之上
 * View.SYSTEM_UI_FLAG_IMMERSIVE                                 API 19
 * 沉浸模式, 隐藏状态栏和导航栏, 并且在第一次会弹泡提醒, 并且在状态栏区域滑动可以呼出状态栏（这样会系统会清楚之前设置的View.SYSTEM_UI_FLAG_FULLSCREEN或View.SYSTEM_UI_FLAG_HIDE_NAVIGATION标志）。使之生效，需要和View.SYSTEM_UI_FLAG_FULLSCREEN，View.SYSTEM_UI_FLAG_HIDE_NAVIGATION中的一个或两个同时设置。
 * View.SYSTEM_UI_FLAG_IMMERSIVE_STIKY                      API 19
 * 与上面唯一的区别是, 呼出隐藏的状态栏后不会清除之前设置的View.SYSTEM_UI_FLAG_FULLSCREEN或View.SYSTEM_UI_FLAG_HIDE_NAVIGATION标志，在一段时间后将再次隐藏系统栏）
 * <p>
 * 作者：Lollo
 * 链接：https://www.jianshu.com/p/11a2b780fd9b
 * 来源：简书
 * 简书著作权归作者所有，任何形式的转载都请联系作者获得授权并注明出处。
 */

public class WindowUtils {

  private final static String TAG = "WindowUtils";

  public static void showBar(Activity context) {
    int uiOptions = context.getWindow().getDecorView().getSystemUiVisibility();
    int newUiOptions = uiOptions;
    boolean isImmersiveModeEnabled =
      ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
    if (isImmersiveModeEnabled) {
      LogUtils.d(TAG, "Turning immersive mode mode off. ");
      //先取 非 后再 与， 把对应位置的1 置成0，原本为0的还是0
      if (Build.VERSION.SDK_INT >= 14) {
        newUiOptions &= ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
      }
      if (Build.VERSION.SDK_INT >= 16) {
        newUiOptions &= ~View.SYSTEM_UI_FLAG_FULLSCREEN;
      }
      if (Build.VERSION.SDK_INT >= 18) {
        newUiOptions &= ~View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
      }
      context.getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
    }
  }

  public static void hideBar(Activity activity) {
    // The UI options currently enabled are represented by a bitfield.
    // getSystemUiVisibility() gives us that bitfield.
    int uiOptions = activity.getWindow().getDecorView().getSystemUiVisibility();
    int newUiOptions = uiOptions;
    boolean isImmersiveModeEnabled =
      ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
    if (!isImmersiveModeEnabled) {
      LogUtils.d(TAG, "Turning immersive mode mode on. ");
      if (Build.VERSION.SDK_INT >= 14) {
        newUiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
      }
      if (Build.VERSION.SDK_INT >= 16) {
        newUiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;
      }
      if (Build.VERSION.SDK_INT >= 18) {
        newUiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
      }
      activity.getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
    }
  }

  public void toggleHideyBar(Activity activity) {

    // BEGIN_INCLUDE (get_current_ui_flags)
    // The UI options currently enabled are represented by a bitfield.
    // getSystemUiVisibility() gives us that bitfield.
    int uiOptions = activity.getWindow().getDecorView().getSystemUiVisibility();
    int newUiOptions = uiOptions;
    // END_INCLUDE (get_current_ui_flags)
    // BEGIN_INCLUDE (toggle_ui_flags)
    boolean isImmersiveModeEnabled =
      ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
    if (isImmersiveModeEnabled) {
      LogUtils.d(TAG, "Turning immersive mode mode off. ");
    } else {
      LogUtils.d(TAG, "Turning immersive mode mode on.");
    }

    // Navigation bar hiding:  Backwards compatible to ICS.
    if (Build.VERSION.SDK_INT >= 14) {
      newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
    }

    // Status bar hiding: Backwards compatible to Jellybean
    if (Build.VERSION.SDK_INT >= 16) {
      newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
    }

    // Immersive mode: Backward compatible to KitKat.
    // Note that this flag doesn't do anything by itself, it only augments the behavior
    // of HIDE_NAVIGATION and FLAG_FULLSCREEN.  For the purposes of this sample
    // all three flags are being toggled together.
    // Note that there are two immersive mode UI flags, one of which is referred to as "sticky".
    // Sticky immersive mode differs in that it makes the navigation and status bars
    // semi-transparent, and the UI flag does not get cleared when the user interacts with
    // the screen.
    if (Build.VERSION.SDK_INT >= 18) {
      newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    }

    activity.getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
    //END_INCLUDE (set_ui_flags)
  }

  /**
   * 状态栏、导航栏全透明去阴影（5.0以上）
   *
   * @param activity
   * @param color_status
   * @param color_nav
   */
  public static void setStatusNavBarColor(Activity activity, int color_status, int color_nav) {
    Window window = activity.getWindow();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
      window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
      window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
      window.setStatusBarColor(color_status);
      window.setNavigationBarColor(color_nav);
      return;
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

      window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
    }
  }

  /**
   * 是否存在虚拟按键
   *
   * @param activity
   * @return
   */
  public static boolean isHaveSoftKey(Activity activity) {
    Display d = activity.getWindowManager().getDefaultDisplay();
    DisplayMetrics realDisplayMetrics = new DisplayMetrics();
    d.getRealMetrics(realDisplayMetrics);
    int realHeight = realDisplayMetrics.heightPixels;
    int realWidth = realDisplayMetrics.widthPixels;
    DisplayMetrics displayMetrics = new DisplayMetrics();
    d.getMetrics(displayMetrics);
    int displayHeight = displayMetrics.heightPixels;
    int displayWidth = displayMetrics.widthPixels;
    return (realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0;
  }

  /**
   * 底部虚拟按键的高度
   *
   * @param activity
   * @return
   */
  public static int getBottomSoftKeysHeight(Activity activity) {
    Display d = activity.getWindowManager().getDefaultDisplay();
    DisplayMetrics realDisplayMetrics = new DisplayMetrics();
    d.getRealMetrics(realDisplayMetrics);
    int realHeight = realDisplayMetrics.heightPixels;
    DisplayMetrics displayMetrics = new DisplayMetrics();
    d.getMetrics(displayMetrics);
    int displayHeight = displayMetrics.heightPixels;
    return (realHeight - displayHeight);
  }

  public static void hideStatusBarShowNavBar(Activity activity, int color_status, int color_nav) {
    Window window = activity.getWindow();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
      //      window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
      window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
      window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        window.setAttributes(lp);
      }
      window.setStatusBarColor(color_status);
      window.setNavigationBarColor(color_nav);
      return;
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
      window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
    }
  }

  public static int getStatusBarHeight(Context context) {
    Resources resources = context.getResources();
    int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
    LogUtils.d(TAG, "getNavigationHeight : " + resources.getDimensionPixelSize(resourceId));
    return resources.getDimensionPixelSize(resourceId);
  }

  public static int getNavigationBarHeight(Context context) {
    Resources resources = context.getResources();
    int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
    return resources.getDimensionPixelSize(resourceId);
  }

  public static int[] getScreenWH(Context context) {
    DisplayMetrics displayMetrics = new DisplayMetrics();
    WindowManager wm = (WindowManager) context
      .getSystemService(Context.WINDOW_SERVICE);
    wm.getDefaultDisplay().getMetrics(displayMetrics);
    return new int[]{displayMetrics.widthPixels, displayMetrics.heightPixels};
  }

  /**
   * 将px值转换为dip或dp值，保证尺寸大小不变
   *
   * @param pxValue
   * @param scale   （DisplayMetrics类中属性density）
   * @return
   */
  public static int px2dp(Context context, float pxValue) {
    final float scale = context.getResources().getDisplayMetrics().density;
    return (int) (pxValue / scale + 0.5f);
  }

  /**
   * 将dip或dp值转换为px值，保证尺寸大小不变
   *
   * @param dipValue
   * @param scale    （DisplayMetrics类中属性density）
   * @return
   */
  public static int dpInt2px(Context context, int dipValue) {
    final float scale = context.getResources().getDisplayMetrics().density;
    return (int) (dipValue * scale + 0.5f);
  }

  public static int dp2px(Context context, int dimen_resID) {
    final float scale = context.getResources().getDisplayMetrics().density;
    return (int) (context.getResources().getDimension(dimen_resID) * scale + 0.5f);
  }

  /**
   * 将px值转换为sp值，保证文字大小不变
   *
   * @param pxValue
   * @param fontScale （DisplayMetrics类中属性scaledDensity）
   * @return
   */
  public static int px2sp(Context context, float pxValue) {
    final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
    return (int) (pxValue / fontScale + 0.5f);
  }

  /**
   * 将sp值转换为px值，保证文字大小不变
   *
   * @param spValue
   * @param fontScale （DisplayMetrics类中属性scaledDensity）
   * @return
   */
  public static int sp2px(Context context, float spValue) {
    final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
    return (int) (spValue * fontScale + 0.5f);
  }

  /**
   * 计算指定的 View 在屏幕中的坐标。
   */
  public static RectF calcViewScreenLocation(View view) {
    int[] location = new int[2];
    // 获取控件在屏幕中的位置，返回的数组分别为控件左顶点的 x、y 的值
    view.getLocationOnScreen(location);
    return new RectF(location[0], location[1], location[0] + view.getWidth(),
      location[1] + view.getHeight());
  }

  /**
   * 判断触摸点是否在控件内
   */
  public static boolean isInViewRange(View view, MotionEvent event) {

    // MotionEvent event;
    // event.getX(); 获取相对于控件自身左上角的 x 坐标值
    // event.getY(); 获取相对于控件自身左上角的 y 坐标值
    float x = event.getRawX(); // 获取相对于屏幕左上角的 x 坐标值
    float y = event.getRawY(); // 获取相对于屏幕左上角的 y 坐标值

    // View view;
    RectF rect = calcViewScreenLocation(view);
    return rect.contains(x, y);
  }


  /**
   * 判断是否是刘海屏
   *
   * @return
   */
  public static boolean hasNotchScreen(Activity activity) {
    if (getInt("ro.miui.notch", activity) == 1 || hasNotchAtHuawei(activity) || hasNotchAtOPPO(activity)
      || hasNotchAtVivo(activity) || isAndroidP(activity) != null) { //TODO 各种品牌
      return true;
    }

    return false;
  }


  /**
   * Android P 刘海屏判断
   *
   * @param activity
   * @return
   */
  public static DisplayCutout isAndroidP(Activity activity) {
    View decorView = activity.getWindow().getDecorView();
    if (decorView != null && android.os.Build.VERSION.SDK_INT >= 28) {
      WindowInsets windowInsets = decorView.getRootWindowInsets();
      if (windowInsets != null)
        return windowInsets.getDisplayCutout();
    }
    return null;
  }

  /**
   * 小米刘海屏判断.
   *
   * @return 0 if it is not notch ; return 1 means notch
   * @throws IllegalArgumentException if the key exceeds 32 characters
   */
  public static int getInt(String key, Activity activity) {
    int result = 0;
    if (isXiaomi()) {
      try {
        ClassLoader classLoader = activity.getClassLoader();
        @SuppressWarnings("rawtypes")
        Class SystemProperties = classLoader.loadClass("android.os.SystemProperties");
        //参数类型
        @SuppressWarnings("rawtypes")
        Class[] paramTypes = new Class[2];
        paramTypes[0] = String.class;
        paramTypes[1] = int.class;
        Method getInt = SystemProperties.getMethod("getInt", paramTypes);
        //参数
        Object[] params = new Object[2];
        params[0] = new String(key);
        params[1] = new Integer(0);
        result = (Integer) getInt.invoke(SystemProperties, params);

      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (IllegalArgumentException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    }
    return result;
  }

  // 是否是小米手机
  public static boolean isXiaomi() {
    return "Xiaomi".equals(Build.MANUFACTURER);
  }

  /**
   * 华为刘海屏判断
   *
   * @return
   */
  public static boolean hasNotchAtHuawei(Context context) {
    boolean ret = false;
    try {
      ClassLoader classLoader = context.getClassLoader();
      Class HwNotchSizeUtil = classLoader.loadClass("com.huawei.android.util.HwNotchSizeUtil");
      Method get = HwNotchSizeUtil.getMethod("hasNotchInScreen");
      ret = (boolean) get.invoke(HwNotchSizeUtil);
    } catch (ClassNotFoundException e) {
      LogUtils.e(TAG, "hasNotchAtHuawei ClassNotFoundException");
    } catch (NoSuchMethodException e) {
      LogUtils.e(TAG, "hasNotchAtHuawei NoSuchMethodException");
    } catch (Exception e) {
      LogUtils.e(TAG, "hasNotchAtHuawei Exception");
    } finally {
      return ret;
    }
  }

  public static final int VIVO_NOTCH = 0x00000020;//是否有刘海
  public static final int VIVO_FILLET = 0x00000008;//是否有圆角

  /**
   * VIVO刘海屏判断
   *
   * @return
   */
  public static boolean hasNotchAtVivo(Context context) {
    boolean ret = false;
    try {
      ClassLoader classLoader = context.getClassLoader();
      Class FtFeature = classLoader.loadClass("android.util.FtFeature");
      Method method = FtFeature.getMethod("isFeatureSupport", int.class);
      ret = (boolean) method.invoke(FtFeature, VIVO_NOTCH);
    } catch (ClassNotFoundException e) {
      LogUtils.e(TAG, "hasNotchAtVivo ClassNotFoundException");
    } catch (NoSuchMethodException e) {
      LogUtils.e(TAG, "hasNotchAtVivo NoSuchMethodException");
    } catch (Exception e) {
      LogUtils.e(TAG, "hasNotchAtVivo Exception");
    } finally {
      return ret;
    }
  }

  /**
   * OPPO刘海屏判断
   *
   * @return
   */
  public static boolean hasNotchAtOPPO(Context context) {
    return context.getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism");
  }

  //获取是否存在NavigationBar
  public static boolean checkDeviceHasNavigationBar(Context context) {
    boolean hasNavigationBar = false;
    Resources rs = context.getResources();
    int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
    if (id > 0) {
      hasNavigationBar = rs.getBoolean(id);
    }
    try {
      Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
      Method m = systemPropertiesClass.getMethod("get", String.class);
      String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
      if ("1".equals(navBarOverride)) {
        hasNavigationBar = false;
      } else if ("0".equals(navBarOverride)) {
        hasNavigationBar = true;
      }
    } catch (Exception e) {

    }
    return hasNavigationBar;

  }

  public static int getNavigationHeight(Context activity) {
    if (activity == null) {
      return 0;
    }
    Resources resources = activity.getResources();
    int resourceId = resources.getIdentifier("navigation_bar_height",
      "dimen", "android");
    int height = 0;
    if (resourceId > 0) {
      //获取NavigationBar的高度
      height = resources.getDimensionPixelSize(resourceId);
    }
    return height;
  }

  public interface OnNavigationStateListener {
    void onNavigationState(boolean isShowing, int height);
  }

  public static void isNavigationBarExist(Activity activity, final OnNavigationStateListener onNavigationStateListener) {
    if (activity == null) {
      return;
    }
    final int height = getNavigationHeight(activity);
    LogUtils.d(TAG, "getNavigationHeight : " + height);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
      activity.getWindow().getDecorView().setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
        @Override
        public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
          boolean isShowing = false;
          int b = 0;
          if (windowInsets != null) {
            b = windowInsets.getSystemWindowInsetBottom();
            isShowing = (b == height);
          }

          LogUtils.d(TAG, "windowInsets.getSystemWindowInsetBottom() : " + b);
          LogUtils.d(TAG, "windowInsets.getSystemWindowInsetTop() : " + windowInsets.getSystemWindowInsetTop());
          if (onNavigationStateListener != null && b <= height) {
            onNavigationStateListener.onNavigationState(isShowing, b);
          }
          return windowInsets;
        }
      });
    }
  }


  /**
   * 获取虚拟功能键高度
   *
   * @param context
   * @return
   */
  public static int getVirtualBarHeigh(Context context) {
    int vh = 0;
    WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    Display display = windowManager.getDefaultDisplay();
    DisplayMetrics dm = new DisplayMetrics();
    try {
      @SuppressWarnings("rawtypes")
      Class c = Class.forName("android.view.Display");
      @SuppressWarnings("unchecked")
      Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
      method.invoke(display, dm);
      vh = dm.heightPixels - windowManager.getDefaultDisplay().getHeight();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return vh;
  }
}

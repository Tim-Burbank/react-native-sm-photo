package com.yunio.easypermission;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

final class PermissionUtils {
    private PermissionUtils() {
    }

    static boolean isOverMarshmallow() {
        return Build.VERSION.SDK_INT >= 23;
    }

    static List<String> findDeniedPermissions(Activity activity, String... permission) {
        if (activity == null) {
            return null;
        }
        List<String> denyPermissions = new ArrayList<String>();
        if (Build.VERSION.SDK_INT >= 23) {
            for (String value : permission) {
                if (activity.checkSelfPermission(value) != PackageManager.PERMISSION_GRANTED) {
                    denyPermissions.add(value);
                }
            }
        }
        return denyPermissions;
    }

    static boolean shouldShowRequestPermissionRationale(Object object, String perm) {
        if (object instanceof Activity) {
            if (isOverMarshmallow()) {
                return ((Activity) object).shouldShowRequestPermissionRationale(perm);
            }
            return false;
        } else if (object instanceof Fragment) {
            // return ((Fragment)
            // object).shouldShowRequestPermissionRationale(perm);
            return false;
        } else {
            return object instanceof android.app.Fragment && Build.VERSION.SDK_INT >= 23
                    && ((android.app.Fragment) object).shouldShowRequestPermissionRationale(perm);
        }
    }

    static Activity getActivity(Object object) {
        if (object instanceof Activity) {
            return ((Activity) object);
        } else if (object instanceof Fragment) {
            return ((Fragment) object).getActivity();
        } else if (object instanceof android.app.Fragment) {
            return ((android.app.Fragment) object).getActivity();
        } else {
            return null;
        }
    }

    static boolean isEmpty(List list) {
        return list == null || list.isEmpty();
    }

}

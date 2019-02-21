package com.github.ipcjs.screenshottile;

import android.text.TextUtils;
import android.util.Log;

public class Utils {

    public static void p(Object... args) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        p(TextUtils.join(", ", args));
    }

    public static void p(String msg) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        Log.i("Screenshot", msg);
    }

    public static void p(String format, Object... args) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        p(String.format(format, args));
    }

}

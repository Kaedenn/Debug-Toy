package net.kaedenn.debugtoy.util;

import android.util.Log;

@SuppressWarnings("unused")
public class Logf {
    public static void v(String tag, String format, Object... args) {
        Log.v(tag, String.format(format, args));
    }
    public static void d(String tag, String format, Object... args) {
        Log.d(tag, String.format(format, args));
    }
    public static void i(String tag, String format, Object... args) {
        Log.i(tag, String.format(format, args));
    }
    public static void w(String tag, String format, Object... args) {
        Log.w(tag, String.format(format, args));
    }
    public static void e(String tag, String format, Object... args) {
        Log.e(tag, String.format(format, args));
    }
}

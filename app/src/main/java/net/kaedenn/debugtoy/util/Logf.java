package net.kaedenn.debugtoy.util;

import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.HashMap;

/** Formatted logging utility class.
 *
 * This class provides several useful APIs.
 *
 * The static single-character methods wrap the {@code Log} methods of the same
 * name, but also provide {@code String#format(...)} arguments for logging a
 * formatted string.
 *
 *
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Logf {
    /* Start of Logf singleton pattern */

    private static WeakReference<Logf> log = new WeakReference<>(new Logf());

    /** Get the {@code Logf} singleton.
     *
     * @return The {@code Logf} singleton.
     */
    public static Logf getInstance() {
        return log.get();
    }

    private Logf() {
    }

    /* Start of Logf configuration area */

    /** LogConfig class definition.
     *
     * This class wraps all of a class's logging configuration.
     */
    @SuppressWarnings("WeakerAccess")
    private class LogConfig {
        public String mClass;
        public String mTag;
        public boolean mEnable;
        LogConfig(String cls, String tag) {
            this(cls, tag, true);
        }
        LogConfig(String cls, String tag, boolean enable) {
            mClass = cls;
            mTag = tag;
            mEnable = enable;
        }
    }

    private HashMap<String, LogConfig> mConfig = new HashMap<>();

    /** Enable logging for the specified class.
     *
     * @param cls The class to configure.
     * @return True if the class configuration was updated, false if the class
     * was not found in the configuration map.
     */
    public boolean enable(Class<?> cls) {
        LogConfig cfg = mConfig.remove(cls.getName());
        if (cfg != null) {
            cfg.mEnable = true;
            mConfig.put(cls.getName(), cfg);
            return true;
        }
        return false;
    }

    /** Disable logging for the specified class.
     *
     * @param cls The class to configure.
     * @return True if the class configuration was updated, false if the class
     * was not found in the configuration map.
     */
    public boolean disable(Class<?> cls) {
        LogConfig cfg = mConfig.remove(cls.getName());
        if (cfg != null) {
            cfg.mEnable = false;
            mConfig.put(cls.getName(), cfg);
            return true;
        }
        return false;
    }

    /** Register configuration for a class.
     *
     * Classes are registered by name, using the {@code Class<T>#getName()}
     * method.
     *
     * @param cls The class to register.
     * @param tag The logging tag to use for the given class.
     */
    public void add(Class<?> cls, String tag) {
        mConfig.put(cls.getName(), new LogConfig(cls.getName(), tag));
    }

    /** Deduce the calling stack frame.
     *
     * @return The stack frame corresponding to whoever called a {@code Logf}
     * method.
     */
    private static StackTraceElement getCaller() {
        Thread currentThread = Thread.currentThread();
        StackTraceElement[] stack = currentThread.getStackTrace();
        int frame = 0;
        /* Skip methods relevant to getStackTrace */
        while (frame < stack.length && !stack[frame].getClassName().equals(Logf.class.getName())) {
            ++frame;
        }
        /* Skip methods relevant to Logf */
        while (frame < stack.length && stack[frame].getClassName().equals(Logf.class.getName())) {
            ++frame;
        }
        if (frame < stack.length) {
            return stack[frame];
        } else {
            return null;
        }
    }

    private String getTag(String cls) {
        LogConfig cfg = mConfig.get(cls);
        if (cfg != null) {
            return cfg.mTag;
        }
        /* Failed to get caller's tag... perhaps it's a nested class? */
        if (cls.contains("$")) {
            return getTag(cls.substring(0, cls.indexOf("$")));
        }
        Logf.e("Logf", "Unable to find tag for caller \"%s\"", cls);
        return cls;
    }

    private static String getCallerTag() {
        StackTraceElement caller = getCaller();
        if (caller != null) {
            Logf instance = getInstance();
            if (instance != null) {
                return instance.getTag(caller.getClassName());
            } else {
                /* We're more than likely shutting down; use the class name */
                return caller.getClassName();
            }
        }
        Log.e("Logf", "Unable to find caller; malformed stack? Call from native code?");
        return "<unknown>";
    }

    /* Start of static Logf methods */

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

    public static void vc(String format, Object... args) {
        v(getCallerTag(), format, args);
    }
    public static void dc(String format, Object... args) {
        d(getCallerTag(), format, args);
    }
    public static void ic(String format, Object... args) {
        i(getCallerTag(), format, args);
    }
    public static void wc(String format, Object... args) {
        w(getCallerTag(), format, args);
    }
    public static void ec(String format, Object... args) {
        e(getCallerTag(), format, args);
    }
}

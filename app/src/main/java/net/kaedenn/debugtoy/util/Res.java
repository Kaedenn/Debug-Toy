package net.kaedenn.debugtoy.util;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;

import net.kaedenn.debugtoy.MainActivity;

import static java.lang.Float.NaN;

@SuppressWarnings("unused")
public final class Res {
    private static MainActivity getActivity() {
        return MainActivity.getInstance();
    }

    public static Resources getResources() {
        return getActivity().getResources();
    }

    public static int getColor(int id) {
        return getResources().getColor(id, null);
    }

    public static int getColor(int id, Resources.Theme theme) {
        return getResources().getColor(id, theme);
    }

    public static float getDimension(int id) {
        return getResources().getDimension(id);
    }

    public static Drawable getDrawable(int id) {
        return getResources().getDrawable(id, null);
    }

    public static Drawable getDrawable(int id, Resources.Theme theme) {
        return getResources().getDrawable(id, theme);
    }

    public static Drawable getDrawableForDensity(int id, int density) {
        return getResources().getDrawableForDensity(id, density, null);
    }

    public static float getFloat(int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return getResources().getFloat(id);
        }
        return NaN;
    }

    public static Typeface getFont(int id) {
        return getResources().getFont(id);
    }

    public static float getFraction(int id, int base, int pbase) {
        return getResources().getFraction(id, base, pbase);
    }

    public static int[] getIntArray(int id) {
        return getResources().getIntArray(id);
    }

    public static int getInteger(int id) {
        return getResources().getInteger(id);
    }

    public static XmlResourceParser getLayout(int id) {
        return getResources().getLayout(id);
    }

    public static String getQuantityString(int id, int quantity) {
        return getResources().getQuantityString(id, quantity);
    }

    public static String getQuantityString(int id, int quantity, Object... formatArgs) {
        return getResources().getQuantityString(id, quantity, formatArgs);
    }

    public static CharSequence getQuantityText(int id, int quantity) {
        return getResources().getQuantityText(id, quantity);
    }

    public static String getString(int id) {
        return getResources().getString(id);
    }

    public static String getString(int id, Object... formatArgs) {
        return getResources().getString(id, formatArgs);
    }

    public static String[] getStringArray(int id) {
        return getResources().getStringArray(id);
    }

    public static CharSequence getText(int id) {
        return getResources().getText(id);
    }

    public static CharSequence getText(int id, CharSequence def) {
        return getResources().getText(id, def);
    }

    public static CharSequence[] getTextArray(int id) {
        return getResources().getTextArray(id);
    }

    public static XmlResourceParser getXml(int id) {
        return getResources().getXml(id);
    }

    /* Unimplemented:
    void getValue(int, TypedValue, boolean)
    void getValue(String, TypedValue, boolean)
    void getValueForDensity(int, int, TypedValue, boolean)

    getIdentifier(String, String, String) -> int
    getResourceEntryName(int id) -> String
    getResourceName(int id) -> String
    getResourcePackageName(int id) -> String
    getResourceTypeName(int id) -> String
     */
}

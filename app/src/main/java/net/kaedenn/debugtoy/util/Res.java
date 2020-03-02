package net.kaedenn.debugtoy.util;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.TypedValue;

import net.kaedenn.debugtoy.MainActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Float.NaN;

/** Helper class to simplify accessing resources from anywhere.
 *
 * This class replaces {@code MainActivity.getInstance().getResources()} calls
 * with the word {@code Res}. Note that this class has slight differences in
 * behavior from the native {@link android.content.res.Resources} class. The
 * differences are documented where present.
 */
@SuppressWarnings("unused")
public final class Res {

    /** {@code Resources#getColor(int)} replacement.
     *
     * API difference: this method does not call the {@code Resources} method
     * of the same signature. Instead, this method wraps the
     * {@code Resources#getColor(int, Resources.Theme)} method and passes a
     * {@code null} theme.
     *
     * @param id The resource {@code R.color.*} ID.
     * @return The resource color as an integer.
     */
    public static int getColor(int id) {
        return MainActivity.getInstance().getResources().getColor(id, null);
    }

    /** Direct wrapper */
    public static int getColor(int id, Resources.Theme theme) {
        return MainActivity.getInstance().getResources().getColor(id, theme);
    }

    /** Direct wrapper */
    public static float getDimension(int id) {
        return MainActivity.getInstance().getResources().getDimension(id);
    }

    /** Direct wrapper */
    public static Drawable getDrawable(int id) {
        return MainActivity.getInstance().getResources().getDrawable(id, null);
    }

    /** Direct wrapper */
    public static Drawable getDrawable(int id, Resources.Theme theme) {
        return MainActivity.getInstance().getResources().getDrawable(id, theme);
    }

    /** Direct wrapper */
    public static Drawable getDrawableForDensity(int id, int density) {
        return MainActivity.getInstance().getResources().getDrawableForDensity(id, density, null);
    }

    /** {@code Resources#getFloat(int)} replacement.
     *
     * This method ensures the API is available before calling it. If the API
     * isn't available, then {@code NaN} is returned.
     *
     * @param id The resource ID.
     * @return The resource float.
     */
    public static float getFloat(int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return MainActivity.getInstance().getResources().getFloat(id);
        }
        return NaN;
    }

    /** Direct wrapper */
    public static Typeface getFont(int id) {
        return MainActivity.getInstance().getResources().getFont(id);
    }

    /** Direct wrapper */
    public static float getFraction(int id, int base, int pbase) {
        return MainActivity.getInstance().getResources().getFraction(id, base, pbase);
    }

    /** Direct wrapper */
    public static int[] getIntArray(int id) {
        return MainActivity.getInstance().getResources().getIntArray(id);
    }

    /** Wrapper of {@code getIntArray}, returning an array of {@code Integer}s.
     *
     * This method is identical to the {@code getIntArray} method, but returns
     * an array of {@code Integer} objects rather than native {@code int}s.
     *
     * @param id The {@code <int-array>} resource ID.
     * @return The resource array.
     */
    public static Integer[] getIntegerArray(int id) {
        int[] res = getIntArray(id);
        Integer[] iRes = new Integer[res.length];
        for (int i = 0; i < res.length; ++i) {
            iRes[i] = res[i];
        }
        return iRes;
    }

    /** Wrapper of {@code getIntArray}, returning a {@code List} of {@code Integer}s.
     *
     * This method is identical to the {@code getIntegerArray} method, but
     * returns a {@code List} of {@code Integer}s.
     *
     * @param id The {@code <int-array>} resource ID.
     * @return The resource array as a {@code List<Integer>}.
     */
    public static List<Integer> getIntegerList(int id) {
        return Arrays.asList(getIntegerArray(id));
    }

    public static ArrayList<Integer> getIntegerArrayList(int id) {
        return new ArrayList<>(getIntegerList(id));
    }

    /** Direct wrapper */
    public static int getInteger(int id) {
        return MainActivity.getInstance().getResources().getInteger(id);
    }

    /** Direct wrapper */
    public static XmlResourceParser getLayout(int id) {
        return MainActivity.getInstance().getResources().getLayout(id);
    }

    /** Direct wrapper */
    public static String getQuantityString(int id, int quantity) {
        return MainActivity.getInstance().getResources().getQuantityString(id, quantity);
    }

    /** Direct wrapper */
    public static String getQuantityString(int id, int quantity, Object... formatArgs) {
        return MainActivity.getInstance().getResources().getQuantityString(id, quantity, formatArgs);
    }

    /** Direct wrapper */
    public static CharSequence getQuantityText(int id, int quantity) {
        return MainActivity.getInstance().getResources().getQuantityText(id, quantity);
    }

    /** Direct wrapper */
    public static String getString(int id) {
        return MainActivity.getInstance().getResources().getString(id);
    }

    /** Direct wrapper */
    public static String getString(int id, Object... formatArgs) {
        return MainActivity.getInstance().getResources().getString(id, formatArgs);
    }

    /** Direct wrapper */
    public static String[] getStringArray(int id) {
        return MainActivity.getInstance().getResources().getStringArray(id);
    }

    /** Wrapper of {@code getStringArray}, returning a {@code List}.
     *
     * @param id The {@code <string-array>} resource ID.
     * @return The resource array as a {@code List} of {@code String}s.
     */
    public static List<String> getStringList(int id) {
        return Arrays.asList(getStringArray(id));
    }

    public static ArrayList<String> getStringArrayList(int id) {
        return new ArrayList<>(getStringList(id));
    }

    /** Direct wrapper */
    public static CharSequence getText(int id) {
        return MainActivity.getInstance().getResources().getText(id);
    }

    /** Direct wrapper */
    public static CharSequence getText(int id, CharSequence def) {
        return MainActivity.getInstance().getResources().getText(id, def);
    }

    /** Direct wrapper */
    public static CharSequence[] getTextArray(int id) {
        return MainActivity.getInstance().getResources().getTextArray(id);
    }

    /** Wrapper of {@code getTextArray}, returning a {@code List}.
     *
     * @param id The {@code <array>} resource ID.
     * @return The resource array as a {@code List} of {@code CharSequence}s.
     */
    public static List<CharSequence> getTextList(int id) {
        return Arrays.asList(getTextArray(id));
    }

    public static ArrayList<CharSequence> getTextArrayList(int id) {
        return new ArrayList<>(getTextList(id));
    }

    /** Direct wrapper */
    public static XmlResourceParser getXml(int id) {
        return MainActivity.getInstance().getResources().getXml(id);
    }

    /** Direct wrapper */
    public static void getValue(int id, TypedValue outValue, boolean resolveRefs) {
        MainActivity.getInstance().getResources().getValue(id, outValue, resolveRefs);
    }

    /** Direct wrapper */
    public void getValue(String name, TypedValue outValue, boolean resolveRefs) {
        MainActivity.getInstance().getResources().getValue(name, outValue, resolveRefs);
    }

    /** Direct wrapper */
    public void getValueForDensity(int id, int density, TypedValue outValue, boolean resolveRefs) {
        MainActivity.getInstance().getResources().getValueForDensity(id, density, outValue, resolveRefs);
    }

    /* Unimplemented:
    getIdentifier(String, String, String) -> int
    getResourceEntryName(int id) -> String
    getResourceName(int id) -> String
    getResourcePackageName(int id) -> String
    getResourceTypeName(int id) -> String
    */
}

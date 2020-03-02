package net.kaedenn.debugtoy.util;

import android.annotation.SuppressLint;
import android.text.Html;
import android.text.Spanned;
import android.view.Gravity;

@SuppressLint("DefaultLocale")
@SuppressWarnings({"unused", "WeakerAccess"})
public final class Str {

    /** Repeat a string.
     *
     * Note that the output consists of {@param s} repeated exactly {@param n}
     * times. Passing 0 for {@code n} will always yield an empty string.
     *
     * @param s The string to repeat
     * @param n The number of times to repeat it
     * @return The repeated string
     */
    public static String repeat(String s, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; ++i) {
            sb.append(s);
        }
        return sb.toString();
    }

    /** Escape a single character.
     *
     * XXX: Do not use for input sanitation.
     *
     * @param c The character to escape
     * @return The escape sequence for the character, if one is needed
     */
    public static String escape(char c) {
        if (c == '\0') {
            return "\\0";
        } else if (c == '\r') {
            return "\\r";
        } else if (c == '\n') {
            return "\\n";
        } else if (c == '\t') {
            return "\\t";
        } else if (c == '\b') {
            return "\b";
        } else if (c == '"') {
            return "\"";
        } else if (c < 8) { /* octal */
            return String.format("\\%d", (int)c);
        } else if (c < 0x20 || c >= 0x80) { /* control or non-ASCII */
            return String.format("\\u%04d", (int)c);
        } else { /* something else (should be unused) */
            return String.valueOf(c);
        }
    }

    /** Return a string with the characters of {@param s}, escaped if needed.
     *
     * XXX: Do not use for input sanitation. Using this for input sanitation
     * will lead to buffer overrun problems and likely arbitrary code execution.
     *
     * @param s The string to escape
     * @return The new string
     */
    public static String escape(String s) {
        StringBuilder sb = new StringBuilder();
        sb.append("\"");
        for (char c : s.toCharArray()) {
            sb.append(escape(c));
        }
        sb.append("\"");
        return sb.toString();
    }

    /** Convert a key/value pair to HTML.
     *
     * @param k The key.
     * @param v The value.
     * @return A {@code Spanned} string {@code "k - v"} where the key is bold.
     */
    public static Spanned kvToHtml(String k, String v) {
        return kvToHtml(k, v, null);
    }

    /** Convert a key/value pair to HTML with the given prefix.
     *
     * If the {@code prefix} is present, then a space is inserted after the
     * prefix and before the key.
     *
     * @param k The key
     * @param v The value
     * @param prefix The prefix string
     * @return A {@code Spanned} string {@code "p k - v"} where the prefix is
     * italicized and the key is bold.
     */
    public static Spanned kvToHtml(String k, String v, String prefix) {
        return kvToHtml(k, v, prefix, "-");
    }

    /** Convert a key/value pair to HTML with the given prefix and separator.
     *
     * If the {@code prefix} is present, then a space is inserted after the
     * prefix and before the key.
     *
     * @param k The key.
     * @param v The value.
     * @param prefix The prefix string.
     * @param sep The separator string.
     * @return A {@code Spanned} string {@code "p k s v"} where the prefix is
     * italicized and the key is bold.
     */
    public static Spanned kvToHtml(String k, String v, String prefix, String sep) {
        String ph = "";
        if (prefix != null) {
            ph = "<i>" + Html.escapeHtml(prefix) + "</i> ";
        }
        String kh = "<b>" + Html.escapeHtml(k) + "</b>";
        String sh = " " + Html.escapeHtml(sep) + " ";
        String vh = Html.escapeHtml(v);
        return Html.fromHtml(String.format("%s%s %s %s", ph, kh, sh, vh), 0);
    }

    /** Convert a Gravity bitmask to a String.
     *
     * This is a public version of the {@code Gravity.toString()} method, which
     * although being {@code public}, is unavailable to use.
     *
     * @param gravity The gravity mask to convert.
     * @return A string describing the gravity bitmask.
     */
    public static String gravityToString(int gravity) {
        final StringBuilder result = new StringBuilder();
        if ((gravity & Gravity.FILL) == Gravity.FILL) {
            result.append("FILL").append(' ');
        } else {
            if ((gravity & Gravity.FILL_VERTICAL) == Gravity.FILL_VERTICAL) {
                result.append("FILL_VERTICAL").append(' ');
            } else {
                if ((gravity & Gravity.TOP) == Gravity.TOP) {
                    result.append("TOP").append(' ');
                }
                if ((gravity & Gravity.BOTTOM) == Gravity.BOTTOM) {
                    result.append("BOTTOM").append(' ');
                }
            }
            if ((gravity & Gravity.FILL_HORIZONTAL) == Gravity.FILL_HORIZONTAL) {
                result.append("FILL_HORIZONTAL").append(' ');
            } else {
                if ((gravity & Gravity.START) == Gravity.START) {
                    result.append("START").append(' ');
                } else if ((gravity & Gravity.LEFT) == Gravity.LEFT) {
                    result.append("LEFT").append(' ');
                }
                if ((gravity & Gravity.END) == Gravity.END) {
                    result.append("END").append(' ');
                } else if ((gravity & Gravity.RIGHT) == Gravity.RIGHT) {
                    result.append("RIGHT").append(' ');
                }
            }
        }
        if ((gravity & Gravity.CENTER) == Gravity.CENTER) {
            result.append("CENTER").append(' ');
        } else {
            if ((gravity & Gravity.CENTER_VERTICAL) == Gravity.CENTER_VERTICAL) {
                result.append("CENTER_VERTICAL").append(' ');
            }
            if ((gravity & Gravity.CENTER_HORIZONTAL) == Gravity.CENTER_HORIZONTAL) {
                result.append("CENTER_HORIZONTAL").append(' ');
            }
        }
        if (result.length() == 0) {
            result.append("NO GRAVITY").append(' ');
        }
        if ((gravity & Gravity.DISPLAY_CLIP_VERTICAL) == Gravity.DISPLAY_CLIP_VERTICAL) {
            result.append("DISPLAY_CLIP_VERTICAL").append(' ');
        }
        if ((gravity & Gravity.DISPLAY_CLIP_HORIZONTAL) == Gravity.DISPLAY_CLIP_HORIZONTAL) {
            result.append("DISPLAY_CLIP_HORIZONTAL").append(' ');
        }
        result.deleteCharAt(result.length() - 1);
        return result.toString();
    }

    /** Try to parse the string as an Integer with the given radix.
     *
     * @param s The string to parse.
     * @param radix The radix to use.
     * @return An {@code Integer} or {@code null}.
     * @see Integer#parseInt(String, int)
     */
    public static Integer tryParseInteger(String s, int radix) {
        try {
            return Integer.parseInt(s, radix);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Try to parse the string as a base-10 Integer.
     *
     * This method is a wrapper for {@code tryParseInteger} with {@code radix}
     * set to 10.
     *
     * @param s The string to parse.
     * @return An {@code Integer} or {@code null}.
     */
    public static Integer tryParseInteger(String s) {
        return tryParseInteger(s, 10);
    }

    /** Try to parse the string as a Float.
     * 
     * @param s The string to parse.
     * @return A {@code Float} or {@code null}.
     * @see Float#parseFloat(String)
     */
    public static Float tryParseFloat(String s) {
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Try to parse the string as a Double.
     * 
     * @param s The string to parse.
     * @return A {@code Double} or {@code null}.
     * @see Double#parseDouble(String) 
     */
    public static Double tryParseDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /* TODO: Find a use or remove altogether.
    public static class HTML {
        private static Spanned wrap(String s, boolean escape, String tagStart, String tagEnd) {
            String text = escape ? Html.escapeHtml(s) : s;
            return Html.fromHtml(tagStart + text + tagEnd, 0);
        }
        public static Spanned bold(String s, boolean escape) {
            return wrap(s, escape, "<b>", "</b>");
        }
        public static Spanned bold(String s) {
            return wrap(s, true, "<b>", "</b>");
        }
        public static Spanned italic(String s, boolean escape) {
            return wrap(s, escape, "<i>", "</i>");
        }
        public static Spanned italic(String s) {
            return wrap(s, true, "<i>", "</i>");
        }
        public static Spanned underline(String s, boolean escape) {
            return wrap(s, escape, "<u>", "</u>");
        }
        public static Spanned underline(String s) {
            return wrap(s, true, "<u>", "</u>");
        }
        public static Spanned strikethrough(String s, boolean escape) {
            return wrap(s, escape, "<s>", "</s>");
        }
        public static Spanned strikethrough(String s) {
            return wrap(s, true, "<s>", "</s>");
        }
        public static Spanned subscript(String s, boolean escape) {
            return wrap(s, escape, "<sub>", "</sub>");
        }
        public static Spanned subscript(String s) {
            return wrap(s, true, "<sub>", "</sub>");
        }
        public static Spanned superscript(String s, boolean escape) {
            return wrap(s, escape, "<sup>", "</sup>");
        }
        public static Spanned superscript(String s) {
            return wrap(s, true, "<sup>", "</sup>");
        }
    }
    */
}

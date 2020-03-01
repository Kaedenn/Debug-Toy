package net.kaedenn.debugtoy.util;

import android.annotation.SuppressLint;

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

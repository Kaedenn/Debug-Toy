package net.kaedenn.debugtoy.util;

import android.annotation.SuppressLint;

@SuppressLint("DefaultLocale")
@SuppressWarnings({"unused", "WeakerAccess"})
public final class Strings {

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
     * @param c The character to escape
     * @return The escape sequence for the character, if one is needed
     */
    public static String escapeChar(char c) {
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
        } else if (c < 8) {
            return String.format("\\%d", (int)c);
        } else if (c < 0x20 || c >= 0x80) {
            return String.format("\\u%04d", (int)c);
        } else {
            return String.valueOf(c);
        }
    }

    /** Return a string with the characters of {@param s}, escaped if needed.
     *
     * @param s The string to escape
     * @return The new string
     */
    public static String escape(String s) {
        StringBuilder sb = new StringBuilder();
        sb.append("\"");
        for (char c : s.toCharArray()) {
            sb.append(escapeChar(c));
        }
        sb.append("\"");
        return sb.toString();
    }

}

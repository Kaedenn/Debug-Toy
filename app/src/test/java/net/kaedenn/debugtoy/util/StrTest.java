package net.kaedenn.debugtoy.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class StrTest {

    @Test
    public void repeat() {
        assertEquals("", Str.repeat("text", 0));
        assertEquals("text", Str.repeat("text", 1));
        assertEquals("texttext", Str.repeat("text", 2));
        assertEquals("     ", Str.repeat(" ", 5));
    }

    @Test
    public void escapeChar() {
        assertEquals("\\r", Str.escape('\r'));
        assertEquals("\\n", Str.escape('\n'));
        assertEquals("\\t", Str.escape('\t'));
        assertEquals("\\0", Str.escape('\0'));
        assertEquals("\\1", Str.escape('\1'));
        assertEquals(" ", Str.escape((char)0x20));
        assertEquals("\"", Str.escape('"'));
    }

    @Test
    public void escape() {
        assertEquals("\"Hi mom!\"", Str.escape("Hi mom!"));
        assertEquals("\"Hi\\nmom!\"", Str.escape("Hi\nmom!"));
    }
}

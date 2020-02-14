package net.kaedenn.debugtoy.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class StringsTest {

    @Test
    public void repeat() {
        assertEquals(Strings.repeat("text", 0), "");
        assertEquals(Strings.repeat("text", 1), "text");
        assertEquals(Strings.repeat("text", 2), "texttext");
    }

    @Test
    public void escapeChar() {
        assertEquals("\\r", Strings.escapeChar('\r'));
        assertEquals("\\n", Strings.escapeChar('\n'));
        assertEquals("\\t", Strings.escapeChar('\t'));
        assertEquals("\\0", Strings.escapeChar('\0'));
        assertEquals("\\1", Strings.escapeChar('\1'));
        assertEquals(" ", Strings.escapeChar((char)0x20));
        assertEquals("\"", Strings.escapeChar('"'));
    }

    @Test
    public void escape() {
        assertEquals("\"Hi mom!\"", Strings.escape("Hi mom!"));
        assertEquals("\"Hi\\nmom!\"", Strings.escape("Hi\nmom!"));
    }
}
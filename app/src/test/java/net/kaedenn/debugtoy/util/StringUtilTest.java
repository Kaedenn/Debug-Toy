package net.kaedenn.debugtoy.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class StringUtilTest {

    @Test
    public void repeat() {
        assertEquals(StringUtil.repeat("text", 0), "");
        assertEquals(StringUtil.repeat("text", 1), "text");
        assertEquals(StringUtil.repeat("text", 2), "texttext");
    }

    @Test
    public void escapeChar() {
        assertEquals("\\r", StringUtil.escapeChar('\r'));
        assertEquals("\\n", StringUtil.escapeChar('\n'));
        assertEquals("\\t", StringUtil.escapeChar('\t'));
        assertEquals("\\0", StringUtil.escapeChar('\0'));
        assertEquals("\\1", StringUtil.escapeChar('\1'));
        assertEquals(" ", StringUtil.escapeChar((char)0x20));
        assertEquals("\"", StringUtil.escapeChar('"'));
    }

    @Test
    public void escape() {
        assertEquals("\"Hi mom!\"", StringUtil.escape("Hi mom!"));
        assertEquals("\"Hi\\nmom!\"", StringUtil.escape("Hi\nmom!"));
    }
}

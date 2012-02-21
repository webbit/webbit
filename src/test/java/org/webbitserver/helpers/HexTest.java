package org.webbitserver.helpers;

import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertEquals;

public class HexTest {
    @Test
    public void testEncodesByteArray() throws UnsupportedEncodingException {
        String input = "hello world";
        String expected = "68656C6C6F20776F726C64";
        assertEquals(expected, Hex.toHex(input.getBytes("UTF-8")));
        assertEquals(input, new String(Hex.fromHex(expected), "UTF-8"));
    }

    @Test
    public void testEncodesMultilineByteArray() throws UnsupportedEncodingException {
        String input = "the quick\r\nbrown fox\n\njumps over the\nlazy dog";
        String expected = "74686520717569636B0D0A62726F776E20666F78" +
                "0A0A6A756D7073206F766572207468650A6C617A7920646F67";
        assertEquals(expected, Hex.toHex(input.getBytes("UTF-8")));
        assertEquals(input, new String(Hex.fromHex(expected), "UTF-8"));
    }
}
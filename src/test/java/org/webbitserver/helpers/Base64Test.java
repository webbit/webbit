package org.webbitserver.helpers;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * This code originally came from the XStream http://xstream.codehaus.org project by Joe Walnes. Relicensed to Webbit.
 */
public class Base64Test {

    @Test
    public void testEncodesEntireByteArrayAsString() {
        byte input[] = "hello world".getBytes();
        String expected = "aGVsbG8gd29ybGQ=";
        assertEquals(expected, Base64.encode(input));
        assertByteArrayEquals(input, Base64.decode(expected));
    }

    @Test
    public void wrapsLinesAt76Chars() {
        byte input[] = ("hello world. hello world. hello world. hello world. hello world. hello world. hello world. "
                + "hello world. hello world. hello world. hello world. hello world. hello world. hello world. ").getBytes();
        String expected = "aGVsbG8gd29ybGQuIGhlbGxvIHdvcmxkLiBoZWxsbyB3b3JsZC4gaGVsbG8gd29ybGQuIGhlbGxv\n"
                + "IHdvcmxkLiBoZWxsbyB3b3JsZC4gaGVsbG8gd29ybGQuIGhlbGxvIHdvcmxkLiBoZWxsbyB3b3Js\n"
                + "ZC4gaGVsbG8gd29ybGQuIGhlbGxvIHdvcmxkLiBoZWxsbyB3b3JsZC4gaGVsbG8gd29ybGQuIGhl\n"
                + "bGxvIHdvcmxkLiA=";
        assertEquals(expected, Base64.encode(input));
        assertByteArrayEquals(input, Base64.decode(expected));
    }

    @Test
    public void padsSingleMissingByteWhenNotMultipleOfThree() {
        byte input[] = {1, 2, 3, 4, 5};
        String expected = "AQIDBAU=";
        assertEquals(expected, Base64.encode(input));
        assertByteArrayEquals(input, Base64.decode(expected));
    }

    @Test
    public void padsDoubleMissingByteWhenNotMultipleOfThree() {
        byte input[] = {1, 2, 3, 4};
        String expected = "AQIDBA==";
        assertEquals(expected, Base64.encode(input));
        assertByteArrayEquals(input, Base64.decode(expected));
    }

    @Test
    public void doesNotPadWhenMultipleOfThree() {
        byte input[] = {1, 2, 3, 4, 5, 6};
        String expected = "AQIDBAUG";
        assertEquals(expected, Base64.encode(input));
        assertByteArrayEquals(input, Base64.decode(expected));
    }

    @Test
    public void handlesAllPositiveBytes() {
        byte input[] = new byte[127];
        for (int i = 0; i < 126; i++) input[i] = (byte) (i + 1);
        String expected = "AQIDBAUGBwgJCgsMDQ4PEBESExQVFhcYGRobHB0eHyAhIiMkJSYnKCkqKywtLi8wMTIzNDU2Nzg5\n"
                + "Ojs8PT4/QEFCQ0RFRkdISUpLTE1OT1BRUlNUVVZXWFlaW1xdXl9gYWJjZGVmZ2hpamtsbW5vcHFy\n"
                + "c3R1dnd4eXp7fH1+AA==";
        assertEquals(expected, Base64.encode(input));
        assertByteArrayEquals(input, Base64.decode(expected));
    }

    @Test
    public void handlesAllNegativeBytes() {
        byte input[] = new byte[128];
        for (int i = 0; i < 127; i++) input[i] = (byte) (-1 - i);
        String expected = "//79/Pv6+fj39vX08/Lx8O/u7ezr6uno5+bl5OPi4eDf3t3c29rZ2NfW1dTT0tHQz87NzMvKycjH\n"
                + "xsXEw8LBwL++vby7urm4t7a1tLOysbCvrq2sq6qpqKempaSjoqGgn56dnJuamZiXlpWUk5KRkI+O\n"
                + "jYyLiomIh4aFhIOCgQA=";
        assertEquals(expected, Base64.encode(input));
        assertByteArrayEquals(input, Base64.decode(expected));
    }

    @Test
    public void handlesZeroByte() {
        byte input[] = {0, 0, 0, 0};
        String expected = "AAAAAA==";
        assertEquals(expected, Base64.encode(input));
        assertByteArrayEquals(input, Base64.decode(expected));
    }

    @Test
    public void producesEmptyStringWhenNoBytesGiven() {
        byte input[] = new byte[0];
        String expected = "";
        assertEquals(expected, Base64.encode(input));
        assertByteArrayEquals(input, Base64.decode(expected));
    }

    protected void assertByteArrayEquals(byte expected[], byte actual[]) {
        assertEquals(dumpBytes(expected), dumpBytes(actual));
    }

    private String dumpBytes(byte bytes[]) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            result.append(bytes[i]).append(' ');
            if (bytes[i] < 100) result.append(' ');
            if (bytes[i] < 10) result.append(' ');
            if (bytes[i] >= 0) result.append(' ');
            if (i % 16 == 15) result.append('\n');
        }
        return result.toString();
    }
}

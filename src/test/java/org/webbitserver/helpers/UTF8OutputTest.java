package org.webbitserver.helpers;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class UTF8OutputTest {
    private static UTF8Output utf8Output = new UTF8Output();

    @Test
    public void decodesLatin() throws IOException {
        assertUtf8("Hellesøy");
    }

    @Test
    public void decodesChinese() throws IOException {
        assertUtf8("我希望有人告诉我数字相加的结果");
    }

    @Test
    public void decodesManyTimes() throws IOException {
        assertUtf8("Hellesøy 我希望有人告诉我数字相加的结果");
        assertUtf8("Hellesøy");
        assertUtf8("我希望有人告诉我数字相加的结果");
    }

    @Test(expected = UTF8Exception.class)
    public void throwsOnNonUTF8Bytes() {
        int[] bad = new int[]{
                0xCE, 0xBA, 0xE1, 0xBD, 0xB9, 0xCF, 0x83, 0xCE, 0xBC, 0xCE, 0xB5, 0xED, 0xA0, 0x80, 0x65, 0x64, 0x69, 0x74, 0x65, 0x64
        };
        for (int i : bad) {
            utf8Output.write(i);
        }
    }

    private void assertUtf8(String s) throws IOException {
        utf8Output.write(s.getBytes("UTF-8"));
        assertEquals(s, utf8Output.getStringAndRecycle());
    }
}

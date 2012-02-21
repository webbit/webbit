package org.webbitserver.helpers;

public class Hex {
    public static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString().toUpperCase();
    }

    public static byte[] fromHex(String string) {
        byte[] result = new byte[string.length() / 2];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) Integer.parseInt(string.substring(i * 2, (i * 2) + 2), 16);
        }
        return result;
    }
}

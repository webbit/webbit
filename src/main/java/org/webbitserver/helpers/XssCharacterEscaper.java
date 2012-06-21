package org.webbitserver.helpers;

public class XssCharacterEscaper {
    /**
     * Replaces characters in input which may open up cross-site scripting (XSS) attacks with XSS-safe equivalents.
     *
     * Follows escaping rules from
     * <a href="https://www.owasp.org/index.php/XSS_(Cross_Site_Scripting)_Prevention_Cheat_Sheet#RULE_.231_-_HTML_Escape_Before_Inserting_Untrusted_Data_into_HTML_Element_Content">the OWASP</a>.
     *
     * @param input String to sanitize.
     * @return XSS-safe version of input.
     */
    public static String escape(String input) {
        StringBuilder builder = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); ++i) {
            char original = input.charAt(i);
            switch (original) {
                case '&':
                    builder.append("&amp;");
                    break;
                case '<':
                    builder.append("&lt;");
                    break;
                case '>':
                    builder.append("&gt;");
                    break;
                case '"':
                    builder.append("&quot;");
                    break;
                case '\'':
                    builder.append("&#x27;");
                    break;
                case '/':
                    builder.append("&#x2F;");
                    break;
                default:
                    builder.append(original);
                    break;
            }
        }
        return builder.toString();
    }
}
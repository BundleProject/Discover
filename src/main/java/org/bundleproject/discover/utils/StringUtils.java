package org.bundleproject.discover.utils;

public class StringUtils {

    public static String wrapText(String text, int charLength) {
        StringBuilder sb = new StringBuilder();
        int lineLength = 0;
        boolean needsLineBreak = false;
        for (char c : text.toCharArray()) {
            lineLength += 1;
            if (c == '\n') lineLength = 0;
            if (lineLength > charLength) {
                needsLineBreak = true;
            }
            if (needsLineBreak && c == ' ') {
                lineLength = 0;
                sb.append('\n');
                needsLineBreak = false;
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

}

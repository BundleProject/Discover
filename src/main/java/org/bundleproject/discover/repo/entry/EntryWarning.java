package org.bundleproject.discover.repo.entry;

import java.util.Arrays;
import java.util.List;

public class EntryWarning {

    private final List<String> message;

    public EntryWarning(String[] lines) {
        message = Arrays.asList(lines);
    }

    public EntryWarning(List<String> lines) {
        message = lines;
    }

    public String getMessageHtml() {
        StringBuilder sb = new StringBuilder("<html><div style='text-align: center;'>");
        for (String line : message) {
            sb.append(line).append("<br>");
        }
        sb.append("</div></html>");
        return sb.toString();
    }

}

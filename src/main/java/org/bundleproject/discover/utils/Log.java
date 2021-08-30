package org.bundleproject.discover.utils;

public class Log {

    private static int indentCount = 0;

    public static void startSection(String message) {
        sendSectionInfo(message);
        indentCount++;
    }

    public static void endSection() {
        indentCount = Math.max(0, indentCount - 1);
    }

    public static void endSection(String message) {
        sendSectionInfo(message);
        endSection();
    }

    public static void sendSectionInfo(String message) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indentCount; i++) {
            sb.append("  ");
        }
        info(sb.append(message).toString());
    }

    public static void resetSections() {
        indentCount = 0;
    }

    public static void info(String msg) {
        System.out.println("INFO: " + msg);
    }

    public static void err(String msg) {
        System.out.println("ERROR: " + msg);
    }

    public static void warn(String msg) {
        System.out.println("WARN: " + msg);
    }

}

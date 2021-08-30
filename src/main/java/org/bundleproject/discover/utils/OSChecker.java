package org.bundleproject.discover.utils;

public class OSChecker {

    public enum OSType {
        WINDOWS, OS_X, LINUX, UNKNOWN
    }

    protected static OSType detectedOS;

    public static OSType getOperatingSystemType() {
        if (detectedOS == null) {
            String OS = System.getProperty("os.name", "generic").toLowerCase();
            if ((OS.contains("mac")) || (OS.contains("darwin"))) {
                detectedOS = OSType.OS_X;
            } else if (OS.contains("win")) {
                detectedOS = OSType.WINDOWS;
            } else if (OS.contains("nux")) {
                detectedOS = OSType.LINUX;
            } else {
                detectedOS = OSType.UNKNOWN;
            }
        }
        return detectedOS;
    }

}

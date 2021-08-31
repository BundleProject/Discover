package org.bundleproject.discover.utils

import java.util.*

object OSChecker {
    private var detectedOS: OSType? = null
    val operatingSystemType: OSType?
        get() {
            if (detectedOS == null) {
                val OS = System.getProperty("os.name", "generic").lowercase(Locale.getDefault())
                if (OS.contains("mac") || OS.contains("darwin")) {
                    detectedOS = OSType.OS_X
                } else if (OS.contains("win")) {
                    detectedOS = OSType.WINDOWS
                } else if (OS.contains("nux")) {
                    detectedOS = OSType.LINUX
                } else {
                    detectedOS = OSType.UNKNOWN
                }
            }
            return detectedOS
        }

    enum class OSType {
        WINDOWS, OS_X, LINUX, UNKNOWN
    }
}

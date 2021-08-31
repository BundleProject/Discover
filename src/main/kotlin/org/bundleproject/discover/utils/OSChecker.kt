package org.bundleproject.discover.utils

import java.util.*

object OSChecker {
    private var detectedOS: OSType? = null
    val operatingSystemType: OSType?
        get() {
            if (detectedOS == null) { //The user's computer can't change operating systems without restarting
                val os = System.getProperty("os.name", "generic").lowercase(Locale.getDefault())
                detectedOS = if (os.contains("mac") || os.contains("darwin")) {
                    OSType.OS_X
                } else if (os.contains("win")) {
                    OSType.WINDOWS
                } else if (os.contains("nux")) {
                    OSType.LINUX
                } else {
                    OSType.UNKNOWN
                }
            }
            return detectedOS
        }

    enum class OSType {
        WINDOWS, OS_X, LINUX, UNKNOWN
    }
}

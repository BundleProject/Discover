package org.bundleproject.discover.utils

object Log {
    private var indentCount = 0
    fun startSection(message: String?) {
        sendSectionInfo(message)
        indentCount++
    }

    fun endSection() {
        indentCount = 0.coerceAtLeast(indentCount - 1)
    }

    fun endSection(message: String?) {
        sendSectionInfo(message)
        endSection()
    }

    fun sendSectionInfo(message: String?) {
        val sb = StringBuilder()
        for (i in 0 until indentCount) {
            sb.append("  ")
        }
        info(sb.append(message).toString())
    }

    fun resetSections() {
        indentCount = 0
    }

    fun info(msg: String) {
        println("INFO: $msg")
    }

    fun err(msg: String) {
        println("ERROR: $msg")
    }

    fun warn(msg: String) {
        println("WARN: $msg")
    }
}

package org.bundleproject.discover.utils

object StringUtils {
    fun wrapText(text: String, charLength: Int): String {
        val sb = StringBuilder()
        var lineLength = 0
        var needsLineBreak = false
        for (c in text.toCharArray()) {
            lineLength += 1
            if (c == '\n') lineLength = 0
            if (lineLength > charLength) {
                needsLineBreak = true
            }
            if (needsLineBreak && c == ' ') {
                lineLength = 0
                sb.append('\n')
                needsLineBreak = false
            } else {
                sb.append(c)
            }
        }
        return sb.toString()
    }
}

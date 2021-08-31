package org.bundleproject.discover.repo.entry

import java.util.*


class EntryWarning {
    private val message: List<String?>

    constructor(lines: Array<String?>) {
        message = lines.toList()
    }

    constructor(lines: List<String>) {
        message = lines
    }

    val messageHtml: String
        get() {
            val sb = StringBuilder("<html><div style='text-align: center;'>")
            for (line in message) {
                sb.append(line).append("<br>")
            }
            sb.append("</div></html>")
            return sb.toString()
        }
}

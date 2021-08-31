package org.bundleproject.discover.start

import com.formdev.flatlaf.FlatDarkLaf
import org.bundleproject.discover.Discover.Companion.instance
import java.io.PrintStream
import javax.swing.UIManager

@Throws(Exception::class)
fun main() {
    val fileOut = PrintStream("./discover.log")
    System.setOut(fileOut)

    println("Setting LAF...")
    try {
        UIManager.setLookAndFeel(FlatDarkLaf())
    } catch (e: Exception) {
        e.printStackTrace()
    }

    instance
}
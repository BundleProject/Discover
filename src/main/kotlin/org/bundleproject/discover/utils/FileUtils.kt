package org.bundleproject.discover.utils

import java.awt.Image
import java.io.File
import java.io.IOException
import java.nio.file.CopyOption
import java.nio.file.Files
import javax.imageio.ImageIO

object FileUtils {
    // https://stackoverflow.com/a/13379744/15301449
    fun exportResource(resourceName: String, output: File, vararg options: CopyOption?) {
        try {
            FileUtils::class.java.getResourceAsStream(resourceName).use { `is` ->
                if (`is` == null) {
                    throw IOException("Cannot get resource \"$resourceName\"")
                }
                Files.copy(`is`, output.toPath(), *options)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getResourceImage(resourceName: String): Image? {
        try {
            FileUtils::class.java.getResourceAsStream(resourceName).use { `is` ->
                if (`is` == null) {
                    throw IOException("Cannot get resource \"$resourceName\"")
                }
                return ImageIO.read(`is`)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun deleteDirectory(directoryToBeDeleted: File): Boolean {
        val allContents = directoryToBeDeleted.listFiles()
        if (allContents != null) {
            for (file in allContents) {
                deleteDirectory(file)
            }
        }
        return directoryToBeDeleted.delete()
    }

    fun removeFileExtension(name: String): String {
        return name.substring(0, name.lastIndexOf('.'))
    }
}

package org.bundleproject.discover.utils

import java.awt.Image
import java.awt.RenderingHints
import java.awt.image.BufferedImage


object ImageUtils {
    fun getScaledImage(srcImg: Image?, w: Int, h: Int, iconScaling: String): BufferedImage {
        var renderingHint = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        if (iconScaling.equals("smooth", ignoreCase = true)) renderingHint = RenderingHints.VALUE_INTERPOLATION_BICUBIC
        val resizedImg = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
        val g = resizedImg.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, renderingHint)
        g.drawImage(srcImg, 0, 0, w, h, null)
        g.dispose()
        return resizedImg
    }
}
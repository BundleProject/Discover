package org.bundleproject.discover.utils;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageUtils {

    public static BufferedImage getScaledImage(Image srcImg, int w, int h, String iconScaling) {
        Object renderingHint = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
        if (iconScaling.equalsIgnoreCase("pixel")) renderingHint = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
        else if (iconScaling.equalsIgnoreCase("smooth")) renderingHint = RenderingHints.VALUE_INTERPOLATION_BICUBIC;

        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resizedImg.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, renderingHint);
        g.drawImage(srcImg, 0, 0, w, h, null);
        g.dispose();

        return resizedImg;
    }

}

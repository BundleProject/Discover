package org.bundleproject.discover.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;

public class FileUtils {

    // https://stackoverflow.com/a/13379744/15301449
    public static void exportResource(String resourceName, File output, CopyOption... options) {
        try (InputStream is = FileUtils.class.getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new IOException("Cannot get resource \"" + resourceName + "\"");
            }

            Files.copy(is, output.toPath(), options);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Image getResourceImage(String resourceName) {
        try (InputStream is = FileUtils.class.getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new IOException("Cannot get resource \"" + resourceName + "\"");
            }

            return ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    public static String removeFileExtension(String name) {
        return name.substring(0, name.lastIndexOf('.'));
    }

}

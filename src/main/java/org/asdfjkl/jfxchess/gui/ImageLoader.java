package org.asdfjkl.jfxchess.gui;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class ImageLoader {

    public static BufferedImage loadImage(String path) throws IOException {
        InputStream is = ImageLoader.class
                .getClassLoader()
                .getResourceAsStream(path);

        if (is == null) {
            throw new IOException("Resource not found: " + path);
        }

        return ImageIO.read(is);
    }
}
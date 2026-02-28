package org.asdfjkl.jfxchess.gui;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class HighDPIHelper {

    public static double getUIScaleFactor() {
        GraphicsConfiguration gc =
                GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .getDefaultScreenDevice()
                        .getDefaultConfiguration();

        AffineTransform tx = gc.getDefaultTransform();
        return tx.getScaleX(); // Usually 1.0, 1.25, 1.3, 1.5, etc.
    }
}

package org.asdfjkl.jfxchess.gui;

import java.awt.*;
import java.awt.datatransfer.*;

public class ImageSelection implements Transferable {

    private final Image image;

    public ImageSelection(Image image) {
        this.image = image;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DataFlavor.imageFlavor};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return DataFlavor.imageFlavor.equals(flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException {

        if (!isDataFlavorSupported(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }

        return image;
    }
}
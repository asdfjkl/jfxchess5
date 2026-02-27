package org.asdfjkl.jfxchess.gui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Model_JFXChess {

    private final PropertyChangeSupport pcs =
            new PropertyChangeSupport(this);

    private String laf;

    public void addListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public void setLaf(String laf) {
        this.laf = laf;
        pcs.firePropertyChange("switchLaf", "stuff", this.laf);
    }

    public String getLaf() {
        return this.laf;
    }



}

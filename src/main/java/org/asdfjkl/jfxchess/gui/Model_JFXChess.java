package org.asdfjkl.jfxchess.gui;

import org.asdfjkl.jfxchess.lib.Board;
import org.asdfjkl.jfxchess.lib.Game;
import org.asdfjkl.jfxchess.lib.GameNode;

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


    public Game getGame() {
        Game g = new Game();
        Board b = new Board(true);
        GameNode start = new GameNode();
        start.setBoard(b);
        g.setRoot(start);
        g.setCurrent(start);
        return g;
    }

    public boolean getFlipBoard() {
        return true;
    }
}

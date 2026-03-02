package org.asdfjkl.jfxchess.gui;

import org.asdfjkl.jfxchess.lib.Arrow;
import org.asdfjkl.jfxchess.lib.ColoredField;
import org.asdfjkl.jfxchess.lib.Move;

public class Controller_Board {

    private final Model_JFXChess model;

    public Controller_Board(Model_JFXChess model) {
        this.model = model;
    }

    public void applyMove(Move m) {
        model.applyMove(m);
    }

    public void addOrRemoveArrow(Arrow a) {
        model.getGame().getCurrentNode().addOrRemoveArrow(a);
    }

    public void addOrRemoveColoredField(ColoredField c) {
        model.getGame().getCurrentNode().addOrRemoveColoredField(c);
    }

}

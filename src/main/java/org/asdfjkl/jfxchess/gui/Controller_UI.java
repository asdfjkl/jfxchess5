package org.asdfjkl.jfxchess.gui;

import java.awt.event.ActionListener;

public class Controller_UI {

    private final Model_JFXChess model;

    public Controller_UI(Model_JFXChess model) {
        this.model = model;
    }

    public ActionListener switchLaf(String laf) {
        return e -> { model.setLaf(laf); };
    }


}

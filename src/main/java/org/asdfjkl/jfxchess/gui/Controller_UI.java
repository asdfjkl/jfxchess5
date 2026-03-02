package org.asdfjkl.jfxchess.gui;

import org.asdfjkl.jfxchess.lib.Move;

import java.awt.event.ActionListener;

public class Controller_UI {

    private final Model_JFXChess model;

    public Controller_UI(Model_JFXChess model) {
        this.model = model;
    }

    public ActionListener switchLaf(String laf) {
        return e -> { model.setLaf(laf); };
    }

    public ActionListener showAbout() {
        return e -> {
            DialogAbout dlg = new DialogAbout(model.mainFrameRef, model.getVersion());
            dlg.setVisible(true);
        };
    }


    public ActionListener switchBoardColor(int bColor) {
        return e -> {
            model.setBoardColor(bColor);
        };
    }

    public ActionListener switchPieceStyle(int pStyle) {
        return e -> {
            System.out.println("switch piece style in controller: "+pStyle);
            model.setPieceStyle(pStyle);
        };
    }

    public ActionListener resetWindowLayout() {
        return e -> {
            // todo: window reset implementation
        };
    }
}

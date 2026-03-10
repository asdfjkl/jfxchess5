package org.asdfjkl.jfxchess.gui;

import org.asdfjkl.jfxchess.lib.Arrow;
import org.asdfjkl.jfxchess.lib.ColoredField;
import org.asdfjkl.jfxchess.lib.Move;

import java.awt.event.ActionListener;

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

    public ActionListener moveForward() {
        return e -> {
            /*
            ArrayList<GameNode> variations = this.gameModel.getGame().getCurrentNode().getVariations();
            if(variations.size() > 1) {
                ArrayList<String> nextMoves = new ArrayList<>();
                for(GameNode varI : variations) {
                    nextMoves.add(varI.getSan());
                }
                int variationIdx = DialogNextMove.show(gameModel.getStageRef(), nextMoves);
                if(variationIdx >= 0) {
                    this.gameModel.getGame().goToChild(variationIdx);
                    this.gameModel.triggerStateChange();
                }
            } else {
                this.gameModel.getGame().goToMainLineChild();
                this.gameModel.triggerStateChange();
            }
             */
            model.goToChild(0);
        };
    }

    public ActionListener moveBack() {
        return e -> {
            model.goToParent();
        };
    }

    public ActionListener seekToEnd() {
        return e -> {
            model.seekToEnd();
        };
    }

    public ActionListener seekToBeginning() {
        return e -> {
            model.seekToBeginning();
        };
    }

    public void goToNode(int node) {
        model.goToNode(node);
    }

}

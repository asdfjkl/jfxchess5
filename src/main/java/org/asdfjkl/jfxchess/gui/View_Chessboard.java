package org.asdfjkl.jfxchess.gui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.asdfjkl.jfxchess.lib.Arrow;

public class View_Chessboard extends JPanel
        implements PropertyChangeListener {

    private final Model_JFXChess model;

    BoardStyle boardStyle;
    //final double outputScaleX;
    boolean flipBoard = true;

    int innerXOffset;
    int innerYOffset;
    int squareSize;

    //final PieceImageProvider pieceImageProvider;

    Point moveSource;
    final GrabbedPiece grabbedPiece = new GrabbedPiece();
    boolean drawGrabbedPiece = false;

    Point colorClickSource;

    Arrow grabbedArrow;
    boolean drawGrabbedArrow = false;

    Color lastMoveColor;
    Color arrowColor;
    Color arrowGrabColor;
    Color coloredFieldColor;

    public View_Chessboard(Model_JFXChess model) {
        this.model = model;
        this.model.addListener(this);

        setPreferredSize(new Dimension(500, 500));
        setBackground(Color.WHITE);

        Border border = BorderFactory.createLineBorder(Color.BLACK);
        setBorder(border);
        // initUI();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Placeholder rectangle
        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(Color.LIGHT_GRAY);
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setColor(Color.BLACK);
        g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

        // Centered text
        String text = "Chessboard (placeholder)";

        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        int y = (getHeight() + fm.getAscent()) / 2;

        g2.drawString(text, x, y);
    }


    @Override
    public void propertyChange(PropertyChangeEvent evt) {

    }
}

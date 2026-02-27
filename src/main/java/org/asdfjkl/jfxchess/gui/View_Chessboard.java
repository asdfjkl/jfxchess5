package org.asdfjkl.jfxchess.gui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import org.asdfjkl.jfxchess.lib.Arrow;
import org.asdfjkl.jfxchess.lib.Board;
import org.asdfjkl.jfxchess.lib.ColoredField;
import org.asdfjkl.jfxchess.lib.Move;

import static org.asdfjkl.jfxchess.lib.CONSTANTS.*;

public class View_Chessboard extends JPanel
        implements PropertyChangeListener {

    private final Model_JFXChess model;

    BoardStyle boardStyle = new BoardStyle();
    final double outputScaleX = 1.0;
    boolean flipBoard = true;

    int innerXOffset;
    int innerYOffset;
    int squareSize;

    final PieceImageProvider pieceImageProvider = new PieceImageProvider();

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
    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);

        // fill background
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(boardStyle.getDarkSquareColor());
        g2.fillRect(0, 0, getWidth(), getHeight());

        // setup font size
        //FontMetrics fm = g2.getFontMetrics();
        //int x = (getWidth() - fm.stringWidth(text)) / 2;
        //int y = (getHeight() + fm.getAscent()) / 2;

        // size of real board incl. corner
        double height = this.getHeight();
        double width = this.getWidth();
        double minWidthHeight = Math.min(height, width);

        // spare 2 percent left and right
        int outerMargin = (int) (minWidthHeight * 0.05);
        int boardSize = (int) (minWidthHeight - (2*outerMargin));

        int xOffset = outerMargin;
        if(width > height) {
            int surplus = (int) (width - height);
            xOffset += surplus/2;
        }

        int borderMargin = 18; // (int) (minWidthHeight * 0.03);
        squareSize = ((boardSize - (2* borderMargin)) / 8);
        innerXOffset = (xOffset + borderMargin);
        innerYOffset = (outerMargin + borderMargin);

        // paint board inc. margin with letters & numbers
        g2.setColor(boardStyle.getBorderColor());
        g2.fillRect(xOffset, outerMargin, (squareSize*8)+(borderMargin*2), (squareSize*8)+(borderMargin*2));

        // get the from and to field of the last move
        // to highlight those squares
        Point lastMoveFrom = null;
        Point lastMoveTo = null;
        if(model.getGame().getCurrentNode().getMove() != null) {
            Move m = model.getGame().getCurrentNode().getMove();
            lastMoveFrom = Board.internalToXY(m.getMoveSourceSquare());
            lastMoveTo = Board.internalToXY(m.getMoveTargetSquare());
        }

        // paint squares
        Color fieldColor;
        for(int i=0;i<8;i++) {
            for(int j=0;j<8;j++) {
                if((j%2 == 0 && i%2==1) || (j%2 == 1 && i%2==0)) {
                    if(!flipBoard) {
                        fieldColor = boardStyle.getLightSquareColor();
                    } else {
                        fieldColor = boardStyle.getDarkSquareColor();
                    }
                } else {
                    if(!flipBoard) {
                        fieldColor = boardStyle.getDarkSquareColor();
                    } else {
                        fieldColor = boardStyle.getLightSquareColor();
                    }
                }
                int x = (innerXOffset) + (i*squareSize);
                if(flipBoard) {
                    x = innerXOffset+((7-i)*squareSize);
                }
                int y = (innerYOffset) + ((7-j)*squareSize);

                g2.setColor(fieldColor);
                g2.fillRect(x,y,squareSize,squareSize);

                if(lastMoveFrom != null && lastMoveTo != null) {
                    boolean markField = false;
                    if(!flipBoard) {
                        if ((lastMoveFrom.getX() == i && lastMoveFrom.getY() == j) ||
                                (lastMoveTo.getX() == i && lastMoveTo.getY() == j)) {
                            markField = true;
                        }
                    }
                    if(flipBoard) {
                        if ((lastMoveFrom.getX() == i && lastMoveFrom.getY() == 7 - j) ||
                                (lastMoveTo.getX() == i && lastMoveTo.getY() == 7 - j)) {
                            markField = true;
                        }
                    }
                    if(markField) {
                        g2.setColor(lastMoveColor);
                        g2.fillRect(x, y, squareSize, squareSize);
                    }
                }
            }
        }

        // paint colored fields
        for(ColoredField coloredField : model.getGame().getCurrentNode().getColoredFields()) {

            int i = coloredField.x;
            int j = coloredField.y;

            int x = (innerXOffset) + (i*squareSize);
            int y = (innerYOffset) + ((7-j)*squareSize);
            if(flipBoard) {
                x = innerXOffset+((7-i)*squareSize);
                y = (innerYOffset) + (j*squareSize);
            }

            g2.setColor(coloredFieldColor);
            g2.fillRect(x,y,squareSize,squareSize);
        }

        // draw the board coordinates
        g2.setColor(boardStyle.getCoordinateColor());
        for(int i=0;i<8;i++) {
            if(flipBoard){
                char ch = (char) (65 + (7 - i));
                String idx = Character.toString(ch);
                String num = Integer.toString(i + 1);
                //g2.drawString(text, x, y);
                g2.drawString(idx, innerXOffset + (i * squareSize) + (squareSize / 2) - 4,
                        (int) (innerYOffset + (8 * squareSize) + (borderMargin * 0.8)));
                g2.drawString(num, xOffset + 5, innerYOffset + (i * squareSize) + (squareSize / 2) + 4);
            } else{
                char ch = (char) (65 + i);
                String idx = Character.toString(ch);
                String num = Integer.toString(8 - i);
                g2.drawString(idx, innerXOffset + (i * squareSize) + (squareSize / 2) - 4,
                        (int) (innerYOffset + (8 * squareSize) + (borderMargin * 0.8)));
                g2.drawString(num, xOffset + 5, innerYOffset + (i * squareSize) + (squareSize / 2) + 4);
            }
        }

        // draw pieces
        Board b = model.getGame().getCurrentNode().getBoard();
        for(int i=0;i<8;i++) {
            for (int j = 0; j < 8; j++) {
                int x;
                if(flipBoard) {
                    x = innerXOffset+((7-i)*squareSize);
                } else {
                    x = innerXOffset+(i*squareSize);
                }
                // drawing coordinates are from top left
                // whereas chess coords are from bottom left
                int y = innerYOffset+((7-j)*squareSize);
                int piece = 0;
                if(flipBoard) {
                    piece = b.getPieceAt(i, 7-j);
                } else {
                    piece = b.getPieceAt(i, j);
                }
                if(piece != EMPTY && piece != FRINGE) {
                    if(!flipBoard) {
                        if (!(drawGrabbedPiece && i == moveSource.x && j == moveSource.y)) {
                            Image pieceImage = pieceImageProvider.getImage(piece, (int) (squareSize * this.outputScaleX),
                                    boardStyle.getPieceStyle());
                            g2.drawImage(pieceImage, x, y, null);
                            //gc.drawImage(pieceImage, x, y, squareSize, squareSize);
                        }
                    } else {
                        if (!(drawGrabbedPiece && i == moveSource.x && (7-j) == moveSource.y)) {
                            Image pieceImage = pieceImageProvider.getImage(piece, (int) (squareSize * this.outputScaleX),
                                    boardStyle.getPieceStyle());
                            //gc.drawImage(pieceImage, x, y, squareSize, squareSize);
                            g2.drawImage(pieceImage, x, y, null);
                        }
                    }
                }
            }
        }

        // mark side to move
        int x_side_to_move = innerXOffset + 8 * squareSize + 6;
        int y_side_to_move = innerYOffset + 8 * squareSize + 6;
        if(b.turn == WHITE) {
            if(model.getFlipBoard()) {
                y_side_to_move = innerYOffset - 11;
            }
        }
        if(b.turn == BLACK) {
            if(!model.getFlipBoard()) {
                y_side_to_move = innerYOffset - 11;
            }
        }
        g2.setColor(boardStyle.getLightSquareColor());
        g2.fillRect(x_side_to_move, y_side_to_move, 4,4);
        // }

        // draw grabbed piece
        if(drawGrabbedPiece) {
            int offset = squareSize / 2;
            Image pieceImage = pieceImageProvider.getImage(grabbedPiece.getPiece(),
                    (int) (squareSize * this.outputScaleX), boardStyle.getPieceStyle());
            g2.drawImage(pieceImage, (int) (grabbedPiece.getCurrentXLocation() - offset),
                    (int) (grabbedPiece.getCurrentYLocation() - offset), null);
        }

        // draw arrows
        ArrayList<Arrow> arrows = model.getGame().getCurrentNode().getArrows();
        if(arrows != null) {
            for (Arrow ai : model.getGame().getCurrentNode().getArrows()) {
                drawArrow(ai, arrowColor, innerXOffset, innerYOffset);
            }
        }

        // draw currently grabbed arrow
        if(drawGrabbedArrow
                && grabbedArrow.xFrom != -1 && grabbedArrow.yFrom != -1
                && grabbedArrow.xTo != -1 && grabbedArrow.yTo != -1
                && ((grabbedArrow.xFrom != grabbedArrow.xTo) || (grabbedArrow.yFrom != grabbedArrow.yTo))) {
            drawArrow(grabbedArrow, arrowGrabColor, innerXOffset, innerYOffset);
        }
    }

    private void drawArrow(Arrow arrow, Color color, int boardOffsetX, int boardOffsetY) {
    }

    /*
        GraphicsContext gc = this.getGraphicsContext2D();

        int xFrom = 0;
        int xTo = 0;
        int yFrom = 0;
        int yTo = 0;
        if(this.flipBoard) {
            xFrom = boardOffsetX+((7-arrow.xFrom)*squareSize) + (squareSize/2);
            xTo = boardOffsetX+((7-arrow.xTo)*squareSize) + (squareSize/2);
            yFrom = boardOffsetY+(arrow.yFrom*squareSize)+ (squareSize/2);
            yTo = boardOffsetY+(arrow.yTo*squareSize)+ (squareSize/2);
        } else {
            xFrom = boardOffsetX+(arrow.xFrom*squareSize)+ (squareSize/2);
            xTo = boardOffsetX+(arrow.xTo*squareSize)+ (squareSize/2);
            yFrom = boardOffsetY+((7-arrow.yFrom)*squareSize)+ (squareSize/2);
            yTo = boardOffsetY+((7-arrow.yTo)*squareSize)+ (squareSize/2);
        }

        // incredible annoying calculation to get arrow head
        Point fromPoint = new Point(xFrom, yFrom);
        Point toPoint = new Point(xTo, yTo);

        // added to toPoint to place arrow head
        // somewhere in the center
        double vx = -toPoint.getX() + fromPoint.getX();
        double vy = -toPoint.getY() + fromPoint.getY();

        // vectors correspond to the arrows
        double dx = toPoint.getX() - fromPoint.getX();
        double dy = toPoint.getY() - fromPoint.getY();

        double length = Math.sqrt(dx * dx + dy * dy);

        double unitDx = dx / length;
        double unitDy = dy / length;

        // adjusted according to arrow length
        vx = vx * ((double) squareSize /6 /length);
        vy = vy * ((double) squareSize /6 /length);

        toPoint = new Point((int) (toPoint.getX() - vx), (int) (toPoint.getY() - vy));

        int arrowHeadBoxSize = squareSize/4;
        Point arrowPoint1 = new Point(
                (int) (toPoint.getX() - unitDx * arrowHeadBoxSize - unitDy * arrowHeadBoxSize),
                (int) (toPoint.getY() - unitDy * arrowHeadBoxSize + unitDx * arrowHeadBoxSize));

        Point arrowPoint2 = new Point(
                (int) (toPoint.getX() - unitDx * arrowHeadBoxSize + unitDy * arrowHeadBoxSize),
                (int) (toPoint.getY() - unitDy * arrowHeadBoxSize - unitDx * arrowHeadBoxSize));

        gc.setFill(color);
        gc.fillPolygon( new double[] { toPoint.getX(), arrowPoint1.getX(), arrowPoint2.getX() },
                new double[] { toPoint.getY(), arrowPoint1.getY(), arrowPoint2.getY() },
                3);

        // take the old center coordinates to draw the
        // line to, so that the line does not
        // cover the arrow head due to the line's thickness
        Point to = new Point(xTo, yTo);
        double currentLineWidth = gc.getLineWidth();
        javafx.scene.paint.Paint currentPaint = gc.getStroke();
        gc.setLineWidth(squareSize/6);
        gc.setStroke(color);
        gc.strokeLine(fromPoint.getX(), fromPoint.getY(),to.getX(), to.getY());
        gc.setLineWidth(currentLineWidth);
        gc.setStroke(currentPaint);
    }
    */

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

    }
}

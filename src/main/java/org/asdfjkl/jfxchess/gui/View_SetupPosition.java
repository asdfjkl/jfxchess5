package org.asdfjkl.jfxchess.gui;

import org.asdfjkl.jfxchess.lib.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import static java.lang.Math.min;
import static org.asdfjkl.jfxchess.lib.CONSTANTS.*;
import static org.asdfjkl.jfxchess.lib.CONSTANTS.BLACK;

public class View_SetupPosition extends JPanel {

    Model_JFXChess model;
    private final Board board;
    private final GrabbedPiece grabbedPiece = new GrabbedPiece();
    PieceImageProvider pieceImageProvider = new PieceImageProvider();
    final double outputScaleX = HighDPIHelper.getUIScaleFactor();
    final int[][] pickupPieces = {
            { CONSTANTS.WHITE_PAWN, CONSTANTS.BLACK_PAWN },
            { CONSTANTS.WHITE_KNIGHT, CONSTANTS.BLACK_KNIGHT },
            { CONSTANTS.WHITE_BISHOP, CONSTANTS.BLACK_BISHOP },
            { CONSTANTS.WHITE_ROOK, CONSTANTS.BLACK_ROOK },
            { CONSTANTS.WHITE_QUEEN, CONSTANTS.BLACK_QUEEN },
            { CONSTANTS.WHITE_KING, CONSTANTS.BLACK_KING }
    };

    public View_SetupPosition(Model_JFXChess model) {

        this.model = model;
        board = model.getGame().getCurrentNode().getBoard().makeCopy();

    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);

        // fill background
        Graphics2D g2 = (Graphics2D) g;
        //g2.scale(outputScaleX, outputScaleX);
        g2.setColor(model.getBoardStyle().getDarkSquareColor());
        g2.fillRect(0, 0, getWidth(), getHeight());

        // size of real board incl. corner
        double height = this.getHeight();
        double width = this.getWidth();
        double minWidthHeight = Math.min(height, width);

        // spare 2 percent left and right
        int outerMargin = (int) (minWidthHeight * 0.05);
        int boardSize = (int) (minWidthHeight - (2*outerMargin));

        int xOffset = outerMargin;

        int borderMargin = 18;

        int squareSize = ((boardSize - (2* borderMargin)) / 8);

        if(width > height) {
            int widthBoardincPieceSel = (((boardSize - (4* borderMargin)) / 8))*12;
            int surplus = (int) (widthBoardincPieceSel - height);
            xOffset += (surplus/2)+1;
        }

        int innerXOffset = (xOffset + borderMargin);
        int innerYOffset = (outerMargin + borderMargin);

        // paint board inc. margin with letters & numbers
        g2.setColor(model.getBoardStyle().getBorderColor());
        g2.fillRect(xOffset, outerMargin, (squareSize*8)+(borderMargin*2), (squareSize*8)+(borderMargin*2));

        // paint squares
        Color fieldColor;
        for(int i=0;i<8;i++) {
            for(int j=0;j<8;j++) {
                if((j%2 == 0 && i%2==1) || (j%2 == 1 && i%2==0)) {
                    if(!model.getFlipBoard()) {
                        fieldColor = model.getBoardStyle().getLightSquareColor();
                    } else {
                        fieldColor = model.getBoardStyle().getDarkSquareColor();
                    }
                } else {
                    if(!model.getFlipBoard()) {
                        fieldColor = model.getBoardStyle().getDarkSquareColor();
                    } else {
                        fieldColor = model.getBoardStyle().getLightSquareColor();
                    }
                }
                int x = (innerXOffset) + (i*squareSize);
                if(model.getFlipBoard()) {
                    x = innerXOffset +((7-i)*squareSize);
                }
                int y = (innerYOffset) + ((7-j)*squareSize);

                g2.setColor(fieldColor);
                g2.fillRect(x,y,squareSize,squareSize);
            } // for j
        } // for i

        // draw the board coordinates
        g2.setColor(model.getBoardStyle().getCoordinateColor());
        for(int i=0;i<8;i++) {
            if(model.getFlipBoard()){
                char ch = (char) (65 + (7 - i));
                String idx = Character.toString(ch);
                String num = Integer.toString(i + 1);
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
        for(int i=0;i<8;i++) {
            for (int j = 0; j < 8; j++) {
                int x;
                if(model.getFlipBoard()) {
                    x = innerXOffset +((7-i)*squareSize);
                } else {
                    x = innerXOffset +(i*squareSize);
                }
                // drawing coordinates are from top left
                // whereas chess coords are from bottom left
                int y = innerYOffset+((7-j)*squareSize);
                int piece = 0;
                if(model.getFlipBoard()) {
                    piece = board.getPieceAt(i, 7-j);
                } else {
                    piece = board.getPieceAt(i, j);
                }
                if(piece != EMPTY && piece != FRINGE) {
                    if(!model.getFlipBoard()) {
                        if (!(grabbedPiece.getDrawImage() && i == grabbedPiece.sourceSquareX() &&
                                j == grabbedPiece.sourceSquareY())) {
                            Image pieceImage = pieceImageProvider.getImage(piece, (int) (squareSize * this.outputScaleX),
                                    model.getBoardStyle().getPieceStyle());
                            g2.drawImage(pieceImage, x, y, squareSize, squareSize, null);

                        }
                    } else if (!(grabbedPiece.getDrawImage() && i == grabbedPiece.sourceSquareX() &&
                            (7 - j) == grabbedPiece.sourceSquareY())) {
                        Image pieceImage = pieceImageProvider.getImage(piece, (int) (squareSize * this.outputScaleX),
                                model.getBoardStyle().getPieceStyle());
                        g2.drawImage(pieceImage, x, y, squareSize, squareSize, null);
                    }
                }
            }
        }

        // draw rect for piece selection. reset to border color
        g2.setColor(model.getBoardStyle().getBorderColor());
        g2.fillRect((xOffset + 9*squareSize) + (borderMargin*2),
                outerMargin,
                (squareSize*2)+(borderMargin*2),
                (squareSize*6)+(borderMargin*2));

        // Mark the selected piece in the pieceSelector-area with DarkSquareColor.
        // In dragNDropMode we only want to do this if the user actually pressed
        // or clicked the mouse-button in that area when the piece was grabbed.
        for(int i=0;i<6;i++) {
            for(int j=0;j<2;j++) {
                // draw pickup squares
                g2.setColor(model.getBoardStyle().getLightSquareColor());
                g2.fillRect((xOffset + (9+j)*squareSize) + (borderMargin*3),
                        outerMargin + (i*squareSize) + borderMargin,
                        squareSize, squareSize);


                // draw pickup piece image
                int pieceType =  pickupPieces[i][j];
                Image pieceImage = pieceImageProvider.getImage(pieceType, (int) (squareSize * this.outputScaleX),
                        model.getBoardStyle().getPieceStyle());
                int x = (xOffset + (9+j)*squareSize) + (borderMargin*3);
                int y = outerMargin + (i*squareSize) + borderMargin;
                g2.drawImage(pieceImage, x, y, squareSize, squareSize, null);
            }
        }

        // draw grabbed piece
        if (grabbedPiece.getDrawImage()) {
            int offset = squareSize / 2;
            Image pieceImage = pieceImageProvider.getImage(grabbedPiece.getPiece(), (int) (squareSize * this.outputScaleX),
                    model.getBoardStyle().getPieceStyle());
            g2.drawImage(pieceImage, (int) (grabbedPiece.getCurrentXLocation() - offset),
                    (int) (grabbedPiece.getCurrentYLocation() - offset), squareSize, squareSize, null);
        }

    }

}

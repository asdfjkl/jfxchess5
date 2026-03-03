package org.asdfjkl.jfxchess.gui;

import org.asdfjkl.jfxchess.lib.Board;
import org.asdfjkl.jfxchess.lib.Move;
import org.asdfjkl.jfxchess.lib.PgnPrinter;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import static org.asdfjkl.jfxchess.lib.CONSTANTS.*;
import static org.asdfjkl.jfxchess.lib.CONSTANTS.BLACK;

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

    public ActionListener copyFenToClipboard() {
        return e -> {
            String fen = model.getGame().getCurrentNode().getBoard().fen();
            StringSelection stringSelection = new StringSelection(fen);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
        };
    }

    public ActionListener copyPgnToClipboard() {
        return e -> {
            PgnPrinter pgnPrinter = new PgnPrinter();
            String pgn = pgnPrinter.printGame(model.getGame());
            StringSelection stringSelection = new StringSelection(pgn);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
        };
    }


    private BufferedImage renderToImage(int width, int height) {

        PieceImageProvider pieceImageProvider = new PieceImageProvider();

        BufferedImage image =
                new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = image.createGraphics();

        // set colors for rendering
        // light square color: white
        // dark square color: black
        Color darkSquareColor = new Color(192, 192, 192, 255);
        Color lightSquareColor = new Color(255, 255, 255, 255);
        Color borderColor = new Color(100, 100, 100, 255);

        // Better quality
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        // fill background
        g2.setColor(darkSquareColor);
        g2.fillRect(0, 0, width, height);

        double minWidthHeight = Math.min(width, height);

        int outerMargin = 0;
        int boardSize = (int) (minWidthHeight - (2 * outerMargin));

        int xOffset = outerMargin;
        if (width > height) {
            int surplus = width - height;
            xOffset += surplus / 2;
        }

        int borderMargin = 18;

        int squareSize = ((boardSize - (2 * borderMargin)) / 8);
        int innerXOffset = (xOffset + borderMargin);
        int innerYOffset = (outerMargin + borderMargin);

        boolean flipBoard = model.getFlipBoard();



        // paint board border
        g2.setColor(borderColor);
        g2.fillRect(
                xOffset,
                outerMargin,
                (squareSize * 8) + (borderMargin * 2),
                (squareSize * 8) + (borderMargin * 2)
        );

        // paint squares
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {

                Color fieldColor;

                if ((j % 2 == 0 && i % 2 == 1) || (j % 2 == 1 && i % 2 == 0)) {
                    fieldColor = flipBoard
                            ? darkSquareColor
                            : lightSquareColor;
                } else {
                    fieldColor = flipBoard
                            ? lightSquareColor
                            : darkSquareColor;
                }

                int x = innerXOffset + (i * squareSize);
                if (flipBoard) {
                    x = innerXOffset + ((7 - i) * squareSize);
                }

                int y = innerYOffset + ((7 - j) * squareSize);

                g2.setColor(fieldColor);
                g2.fillRect(x, y, squareSize, squareSize);
            }
        }

        // draw coordinates
        g2.setColor(model.getBoardStyle().getCoordinateColor());

        for (int i = 0; i < 8; i++) {

            if (flipBoard) {
                char ch = (char) (65 + (7 - i));
                String idx = Character.toString(ch);
                String num = Integer.toString(i + 1);

                g2.drawString(
                        idx,
                        innerXOffset + (i * squareSize) + (squareSize / 2) - 4,
                        (int) (innerYOffset + (8 * squareSize) + (borderMargin * 0.8))
                );

                g2.drawString(
                        num,
                        xOffset + 5,
                        innerYOffset + (i * squareSize) + (squareSize / 2) + 4
                );

            } else {

                char ch = (char) (65 + i);
                String idx = Character.toString(ch);
                String num = Integer.toString(8 - i);

                g2.drawString(
                        idx,
                        innerXOffset + (i * squareSize) + (squareSize / 2) - 4,
                        (int) (innerYOffset + (8 * squareSize) + (borderMargin * 0.8))
                );

                g2.drawString(
                        num,
                        xOffset + 5,
                        innerYOffset + (i * squareSize) + (squareSize / 2) + 4
                );
            }
        }

        // draw pieces
        Board b = model.getGame().getCurrentNode().getBoard();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {

                int x = flipBoard
                        ? innerXOffset + ((7 - i) * squareSize)
                        : innerXOffset + (i * squareSize);

                int y = innerYOffset + ((7 - j) * squareSize);

                int piece = flipBoard
                        ? b.getPieceAt(i, 7 - j)
                        : b.getPieceAt(i, j);

                if (piece != EMPTY && piece != FRINGE) {

                    Image pieceImage =
                            pieceImageProvider.getImage(
                                    piece,
                                    (int) (squareSize),
                                    model.getBoardStyle().getPieceStyle()
                            );

                    g2.drawImage(pieceImage, x, y, squareSize, squareSize, null);
                }
            }
        }

        // side to move marker
        int xSide = innerXOffset + 8 * squareSize + 6;
        int ySide = innerYOffset + 8 * squareSize + 6;

        if (b.turn == WHITE && model.getFlipBoard()) {
            ySide = innerYOffset - 11;
        }

        if (b.turn == BLACK && !model.getFlipBoard()) {
            ySide = innerYOffset - 11;
        }

        g2.setColor(lightSquareColor);
        g2.fillRect(xSide, ySide, 4, 4);

        g2.dispose();

        return image;
    }


    public ActionListener copyBitmapToClipboard() {
        return e -> {
            BufferedImage image = renderToImage(500,500);
            ImageSelection selection = new ImageSelection(image);
            Clipboard clipboard =
                    Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, null);
        };
    }

    public ActionListener editGameData() {
        return e -> {
            DialogEditGameData dialog =
                    new DialogEditGameData(model.mainFrameRef, model.getGame().getPgnHeaders());
            dialog.setVisible(true);
        };
    }
}

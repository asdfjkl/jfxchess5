package org.asdfjkl.jfxchess.gui;

import org.asdfjkl.jfxchess.lib.CONSTANTS;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class DialogPromotion extends JDialog {

    PieceImageProvider pieceImageProvider = new PieceImageProvider();
    int selectedPiece = CONSTANTS.EMPTY;

    public DialogPromotion(
            Frame parent,
            String title,
            boolean playerColor,
            int pieceStyle
    ) {
        super(parent, title, true);

        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        double scale = HighDPIHelper.getUIScaleFactor();
        int squareSize = 64;
        int imgSize = (int) (squareSize * scale);

        BufferedImage imgQueen;
        BufferedImage imgRook;
        BufferedImage imgBishop;
        BufferedImage imgKnight;

        if(playerColor == CONSTANTS.WHITE) {
            imgQueen = pieceImageProvider.getImage(CONSTANTS.WHITE_QUEEN, imgSize, pieceStyle);
            imgRook = pieceImageProvider.getImage(CONSTANTS.WHITE_ROOK, imgSize, pieceStyle);
            imgBishop = pieceImageProvider.getImage(CONSTANTS.WHITE_BISHOP, imgSize, pieceStyle);
            imgKnight = pieceImageProvider.getImage(CONSTANTS.WHITE_KNIGHT, imgSize, pieceStyle);
        } else {
            imgQueen = pieceImageProvider.getImage(CONSTANTS.BLACK_QUEEN, imgSize, pieceStyle);
            imgRook = pieceImageProvider.getImage(CONSTANTS.BLACK_ROOK, imgSize, pieceStyle);
            imgBishop = pieceImageProvider.getImage(CONSTANTS.BLACK_BISHOP, imgSize, pieceStyle);
            imgKnight = pieceImageProvider.getImage(CONSTANTS.BLACK_KNIGHT, imgSize, pieceStyle);
        }

        JButton btnQueen = new JButton(new ImageIcon(imgQueen));
        btnQueen.setBorderPainted(false);
        btnQueen.setContentAreaFilled(false);
        btnQueen.setFocusPainted(false);

        btnQueen.addActionListener(e -> {
             selectedPiece = CONSTANTS.QUEEN;
             dispose();
        });
        add(btnQueen);

        JButton btnRook = new JButton(new ImageIcon(imgRook));
        btnRook.setBorderPainted(false);
        btnRook.setContentAreaFilled(false);
        btnRook.setFocusPainted(false);

        btnRook.addActionListener(e -> {
            selectedPiece = CONSTANTS.ROOK;
            dispose();
        });
        add(btnRook);

        JButton btnBishop = new JButton(new ImageIcon(imgBishop));
        btnBishop.setBorderPainted(false);
        btnBishop.setContentAreaFilled(false);
        btnBishop.setFocusPainted(false);

        btnBishop.addActionListener(e -> {
            selectedPiece = CONSTANTS.BISHOP;
            dispose();
        });
        add(btnBishop);

        JButton btnKnight = new JButton(new ImageIcon(imgKnight));
        btnKnight.setBorderPainted(false);
        btnKnight.setContentAreaFilled(false);
        btnKnight.setFocusPainted(false);

        btnKnight.addActionListener(e -> {
            selectedPiece = CONSTANTS.KNIGHT;
            dispose();
        });
        add(btnKnight);

        pack();
        setLocationRelativeTo(parent);
    }

    public int getSelectedPiece() {
        return selectedPiece;
    }


}



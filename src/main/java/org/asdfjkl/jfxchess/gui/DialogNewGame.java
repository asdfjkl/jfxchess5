package org.asdfjkl.jfxchess.gui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.asdfjkl.jfxchess.lib.CONSTANTS;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class DialogNewGame extends JDialog {

    public static int ENTER_ANALYSE = 0;
    public static int PLAY_BOT = 1;
    public static int PLAY_UCI = 2;

    private int selection = -1;

    public DialogNewGame(JFrame parent, String title) {
        super(parent, title, true);

        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        double scale = HighDPIHelper.getUIScaleFactor();
        int squareSize = 64;
        int imgSize = (int) (squareSize * scale);

        JButton btnEnterAnalyse = new JButton();
        btnEnterAnalyse.setIcon(new FlatSVGIcon("icons/edit_square.svg"));
        btnEnterAnalyse.setToolTipText("Enter & Analyse");
        btnEnterAnalyse.setText("Enter & Analyse");
        btnEnterAnalyse.setHorizontalTextPosition(SwingConstants.CENTER);
        btnEnterAnalyse.setVerticalTextPosition(SwingConstants.BOTTOM);
        //btnEnterAnalyse.putClientProperty("JButton.buttonType", "toolBarButton");
        btnEnterAnalyse.setFocusable(false);

        JButton btnPlayBot = new JButton();
        btnPlayBot.setIcon(new FlatSVGIcon("icons/smart_toy.svg"));
        btnPlayBot.setToolTipText("Play Bot");
        btnPlayBot.setText("Play Bot");
        btnPlayBot.setHorizontalTextPosition(SwingConstants.CENTER);
        btnPlayBot.setVerticalTextPosition(SwingConstants.BOTTOM);
        //btnPlayBot.putClientProperty("JButton.buttonType", "toolBarButton");
        btnPlayBot.setFocusable(false);

        JButton btnPlayUci = new JButton();
        btnPlayUci.setIcon(new FlatSVGIcon("icons/memory.svg"));
        btnPlayUci.setToolTipText("Play Engine");
        btnPlayUci.setText("Play Engine");
        btnPlayUci.setHorizontalTextPosition(SwingConstants.CENTER);
        btnPlayUci.setVerticalTextPosition(SwingConstants.BOTTOM);
        //btnPlayUci.putClientProperty("JButton.buttonType", "toolBarButton");
        btnPlayUci.setFocusable(false);

        // set width of all buttons to that one of
        // enter & analyse (widest one)
        Dimension d = btnEnterAnalyse.getPreferredSize();
        btnPlayBot.setPreferredSize(d);
        btnPlayUci.setPreferredSize(d);

        btnEnterAnalyse.addActionListener(e -> {
            selection = ENTER_ANALYSE;
            dispose();
        });
        add(btnEnterAnalyse);

        btnPlayBot.addActionListener(e -> {
            selection = PLAY_BOT;
            dispose();
        });
        add(btnPlayBot);

        btnPlayUci.addActionListener(e -> {
            selection = PLAY_UCI;
            dispose();
        });
        add(btnPlayUci);

        pack();
        setLocationRelativeTo(parent);

    }

    public int getSelection() {
        return selection;
    }

}



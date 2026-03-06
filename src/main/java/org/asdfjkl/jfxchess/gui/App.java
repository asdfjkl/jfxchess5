package org.asdfjkl.jfxchess.gui;

import org.asdfjkl.jfxchess.lib.Game;
import org.asdfjkl.jfxchess.lib.PgnReader;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

//import com.formdev.flatlaf.FlatDarkLaf;
//import com.formdev.flatlaf.FlatIntelliJLaf;


public class App {

    private static JFrame frame;

    public static void main(String[] args) {

        // set to false, for platform independent jar
        //System.setProperty("flatlaf.useWindowDecorations", "false");
        //System.setProperty("flatlaf.menuBarEmbedded", "false");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Error setting L&F: " + e);
        }

        /*
        try {
        for (UIManager.LookAndFeelInfo info :
                UIManager.getInstalledLookAndFeels()) {

                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } */


        /*
        try {
            FlatLightLaf.setup();
            FlatDarkLaf.setup();
            FlatIntelliJLaf.setup();
            FlatDarculaLaf.setup();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
         */

        SwingUtilities.invokeLater(() -> {
            new App().start();
        });
    }

    private void start() {

        // todo: reconstruct from settings file
        Model_JFXChess model = new Model_JFXChess();
        ScreenGeometry g = model.restoreScreenGeometry();
        View_MainFrame frame = new View_MainFrame(model);
        model.mainFrameRef = frame;
        PgnReader pgnReader = new PgnReader();
        Game game = pgnReader.readGame("[Event \"Casual Game\"]\n" +
                "[Site \"Berlin GER\"]\n" +
                "[Date \"1852.??.??\"]\n" +
                "[EventDate \"?\"]\n" +
                "[Round \"?\"]\n" +
                "[Result \"1-0\"]\n" +
                "[White \"Adolf Anderssen\"]\n" +
                "[Black \"Jean Dufresne\"]\n" +
                "[ECO \"C52\"]\n" +
                "[WhiteElo \"?\"]\n" +
                "[BlackElo \"?\"]\n" +
                "[PlyCount \"47\"]\n" +
                "\n" +
                "1.e4 e5 2.Nf3 Nc6 3.Bc4 Bc5 4.b4 Bxb4 5.c3 Ba5 6.d4 exd4 7.O-O\n" +
                "d3 8.Qb3 Qf6 9.e5 Qg6 10.Re1 Nge7 11.Ba3 b5 12.Qxb5 Rb8 13.Qa4\n" +
                "Bb6 14.Nbd2 Bb7 15.Ne4 Qf5 16.Bxd3 Qh5 17.Nf6+ gxf6 18.exf6\n" +
                "Rg8 19.Rad1 Qxf3 20.Rxe7+ Nxe7 21.Qxd7+ Kxd7 22.Bf5+ Ke8\n" +
                "23.Bd7+ Kf8 24.Bxe7# 1-0");
        game.setCurrent(game.getRootNode());
        model.setGame(game);

        frame.setGeometry(g);
        frame.setVisible(true);

    }







    // ----------------------------------------------------
    // Placeholder Chessboard Panel
    // ----------------------------------------------------

    static class BoardPanel extends JPanel {

        public BoardPanel() {
            setPreferredSize(new Dimension(500, 500));
            setBackground(Color.WHITE);

            Border border = BorderFactory.createLineBorder(Color.BLACK);
            setBorder(border);
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
    }


}
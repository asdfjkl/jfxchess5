package org.asdfjkl.jfxchess.gui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

//import com.formdev.flatlaf.FlatDarkLaf;
//import com.formdev.flatlaf.FlatIntelliJLaf;


public class App {

    private static JFrame frame;

    public static void main(String[] args) {

        // set to false, for platform independent jar
        System.setProperty("flatlaf.useWindowDecorations", "false");
        System.setProperty("flatlaf.menuBarEmbedded", "false");

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
        View_MainFrame frame = new View_MainFrame(model);
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
package org.asdfjkl.jfxchess.gui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DialogSetupPosition extends JDialog {

    // --- Stub chessboard widget ---
    static class ChessboardWidget extends JPanel {
        public ChessboardWidget() {
            setBorder(BorderFactory.createLineBorder(Color.BLACK));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawString("Chessboard Stub", 10, 20);
        }
    }

    Model_JFXChess model;

    public DialogSetupPosition(Frame parent, Model_JFXChess model) {
        super(parent, "Enter Position", true);

        this.model = model;

        setLayout(new BorderLayout(10,10));

        add(createMainPanel(), BorderLayout.CENTER);
        add(createBottomButtons(), BorderLayout.SOUTH);

        setSize(800, 500);
        setLocationRelativeTo(parent);
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        // --- Chessboard (grows) ---
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(5,5,5,5);
        View_SetupPosition viewSetupPosition = new View_SetupPosition(model);
        //panel.add(new ChessboardWidget(), c);
        panel.add(viewSetupPosition, c);

        // --- Options panel (fixed width) ---
        c.gridx = 1;
        c.weightx = 0;
        c.weighty = 1;
        c.fill = GridBagConstraints.VERTICAL;
        panel.add(createOptionsPanel(), c);

        return panel;
    }

    private JPanel createOptionsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(new Dimension(220, 0));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5,5,5,5);

        int y = 0;

        c.gridy = y++;
        panel.add(createCastlingPanel(), c);

        c.gridy = y++;
        panel.add(createEnPassantPanel(), c);

        c.gridy = y++;
        panel.add(createTurnPanel(), c);

        c.gridy = y++;
        panel.add(createActionButtons(), c);

        c.gridy = y++;
        c.weighty = 1;
        panel.add(Box.createVerticalGlue(), c);

        return panel;
    }

    private JPanel createCastlingPanel() {
        JPanel panel = new JPanel(new GridLayout(4,1));
        panel.setBorder(new TitledBorder("Castling Rights"));

        panel.add(new JCheckBox("White 0-0"));
        panel.add(new JCheckBox("White 0-0-0"));
        panel.add(new JCheckBox("Black 0-0"));
        panel.add(new JCheckBox("Black 0-0-0"));

        return panel;
    }

    private JPanel createEnPassantPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(new TitledBorder("En Passant Square"));

        JComboBox<String> combo = new JComboBox<>(createEnPassantOptions());
        panel.add(combo);

        return panel;
    }

    private String[] createEnPassantOptions() {
        List<String> squares = new ArrayList<>();
        squares.add("-");

        for (char f='a'; f<='h'; f++)
            squares.add(f + "3");

        for (char f='a'; f<='h'; f++)
            squares.add(f + "6");

        return squares.toArray(new String[0]);
    }

    private JPanel createTurnPanel() {
        JPanel panel = new JPanel(new GridLayout(2,1));
        panel.setBorder(new TitledBorder("Turn"));

        JRadioButton white = new JRadioButton("White", true);
        JRadioButton black = new JRadioButton("Black");

        ButtonGroup group = new ButtonGroup();
        group.add(white);
        group.add(black);

        panel.add(white);
        panel.add(black);

        return panel;
    }

    private JPanel createActionButtons() {
        JPanel panel = new JPanel(new GridLayout(4,1,0,5));

        panel.add(new JButton("Flip Board"));
        panel.add(new JButton("Initial Position"));
        panel.add(new JButton("Clear Board"));
        panel.add(new JButton("Current Position"));

        return panel;
    }

    private JPanel createBottomButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        panel.add(new JButton("OK"));
        panel.add(new JButton("Cancel"));

        return panel;
    }

}
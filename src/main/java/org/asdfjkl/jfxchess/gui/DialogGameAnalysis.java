package org.asdfjkl.jfxchess.gui;

import org.asdfjkl.jfxchess.lib.CONSTANTS;

import javax.swing.*;
import java.awt.*;

public class DialogGameAnalysis extends JDialog {

    private JSpinner secPerMoveSpinner;
    private JSpinner thresholdSpinner;

    private JRadioButton bothBtn;
    private JRadioButton whiteBtn;
    private JRadioButton blackBtn;

    private boolean confirmed = false;

    public DialogGameAnalysis(Frame parent) {
        super(parent, "Game Analysis", true);

        JPanel main = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 1: Sec(s) per Move
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        main.add(new JLabel("Sec(s) per Move"), gbc);

        secPerMoveSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 999, 1));
        gbc.gridx = 1;
        gbc.weightx = 1;
        main.add(secPerMoveSpinner, gbc);

        // Row 2: Threshold
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        main.add(new JLabel("Threshold (in pawns)"), gbc);

        thresholdSpinner = new JSpinner(new SpinnerNumberModel(0.6, 0.0, 10.0, 0.1));
        gbc.gridx = 1;
        gbc.weightx = 1;
        main.add(thresholdSpinner, gbc);

        // Row 3: Analyse Players (spans both columns)
        bothBtn = new JRadioButton("Both", true);
        whiteBtn = new JRadioButton("White");
        blackBtn = new JRadioButton("Black");

        ButtonGroup group = new ButtonGroup();
        group.add(bothBtn);
        group.add(whiteBtn);
        group.add(blackBtn);

        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        radioPanel.add(new JLabel("Analyse Players:"));
        radioPanel.add(bothBtn);
        radioPanel.add(whiteBtn);
        radioPanel.add(blackBtn);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        main.add(radioPanel, gbc);

        // Buttons panel
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(ok);
        buttons.add(cancel);

        ok.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        cancel.addActionListener(e -> dispose());

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        main.add(buttons, gbc);

        setContentPane(main);
        pack();
        setLocationRelativeTo(parent);
    }

    public int getSecondsPerMove() {
        return (Integer) secPerMoveSpinner.getValue();
    }

    public double getThreshold() {
        return (Double) thresholdSpinner.getValue();
    }

    public int getSelectedPlayer() {
        if (whiteBtn.isSelected()) return CONSTANTS.IWHITE;
        if (blackBtn.isSelected()) return CONSTANTS.IBLACK;
        return Model_JFXChess.BOTH_PLAYERS;
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
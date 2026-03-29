package org.asdfjkl.jfxchess.gui;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class DialogSave extends JDialog {

    public static int CANCEL = 0;
    public static int SAVE_NEW = 1;
    public static int APPEND_CURRENT = 2;
    public static int REPLACE_CURRENT = 3;
    public static int APPEND_OTHER = 4;

    private int result = CANCEL;

    public DialogSave(Frame parent,
                     boolean appendCurrentEnabled,
                     boolean replaceCurrentEnabled) {
        super(parent, "Choose Action", true);

        setLayout(new GridLayout(5, 1, 5, 5));
        ((JComponent) getContentPane()).setBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );

        JButton btnSaveNew = new JButton("Save as new PGN");
        JButton btnAppendCurrent = new JButton("Append to current PGN");
        JButton btnReplaceCurrent = new JButton("Replace current Game");
        JButton btnAppendOther = new JButton("Append to other PGN");
        JButton btnCancel = new JButton("Cancel");

        // Enable/disable based on parameters
        btnAppendCurrent.setEnabled(appendCurrentEnabled);
        btnReplaceCurrent.setEnabled(replaceCurrentEnabled);

        // Make all buttons same width
        Dimension maxSize = getMaxButtonSize(
                btnSaveNew,
                btnAppendCurrent,
                btnReplaceCurrent,
                btnAppendOther,
                btnCancel
        );

        for (JButton b : new JButton[]{
                btnSaveNew, btnAppendCurrent, btnReplaceCurrent,
                btnAppendOther, btnCancel
        }) {
            b.setPreferredSize(maxSize);
        }

        // Action listeners
        btnSaveNew.addActionListener(e -> { result = SAVE_NEW; dispose(); });
        btnAppendCurrent.addActionListener(e -> { result = APPEND_CURRENT; dispose(); });
        btnReplaceCurrent.addActionListener(e -> { result = REPLACE_CURRENT; dispose(); });
        btnAppendOther.addActionListener(e -> { result = APPEND_OTHER; dispose(); });
        btnCancel.addActionListener(e -> { result = CANCEL; dispose(); });

        // Add buttons
        add(btnSaveNew);
        add(btnAppendCurrent);
        add(btnReplaceCurrent);
        add(btnAppendOther);
        add(btnCancel);

        pack();
        setLocationRelativeTo(parent);
    }

    public int getResult() {
        return result;
    }

    private Dimension getMaxButtonSize(JButton... buttons) {
        int maxWidth = 0;
        int maxHeight = 0;

        for (JButton b : buttons) {
            Dimension d = b.getPreferredSize();
            maxWidth = Math.max(maxWidth, d.width);
            maxHeight = Math.max(maxHeight, d.height);
        }

        return new Dimension(maxWidth, maxHeight);
    }

}
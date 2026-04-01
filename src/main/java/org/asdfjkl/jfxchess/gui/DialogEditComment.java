package org.asdfjkl.jfxchess.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DialogEditComment extends JDialog {

    private JTextArea textArea;
    private boolean confirmed = false;

    public DialogEditComment(Frame parent, String title) {
        super(parent, title, true); // modal dialog
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // Text area (multiline, plain text)
        textArea = new JTextArea(10, 40);
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        // Buttons panel (right-aligned)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Button actions
        okButton.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        // Handle window close (treat as Cancel)
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmed = false;
            }
        });

        pack();
        setLocationRelativeTo(getParent());
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getText() {
        return textArea.getText();
    }

    public void setText(String text) {
        textArea.setText(text);
    }

}

package org.asdfjkl.jfxchess.gui;

import javax.swing.*;
import java.awt.*;

public class DialogProgress extends JDialog {

    private final JProgressBar progressBar = new JProgressBar(0, 100);
    private final JButton cancelButton = new JButton("Cancel");
    //private final JLabel messageLabel = new JLabel("Processing...");

    public DialogProgress(JFrame parent, SwingWorker worker, String windowTitle) {
        super(parent, windowTitle, true);

        initUI();
        bindWorker(worker);

        cancelButton.addActionListener(e -> {
            worker.cancel(true);
            dispose();
        });
    }

    public DialogProgress(Window parent, SwingWorker worker, String windowTitle) {
        super(parent, windowTitle, ModalityType.APPLICATION_MODAL);

        initUI();
        bindWorker(worker);

        cancelButton.addActionListener(e -> {
            worker.cancel(true);
            dispose();
        });
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // ---- Main content panel with padding ----
        JPanel content = new JPanel();
        content.setLayout(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        // ---- Message label ----
        //messageLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        // ---- Progress bar ----
        progressBar.setStringPainted(true);
        progressBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));

        // Stack label + progress bar
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        //centerPanel.add(messageLabel, BorderLayout.NORTH);
        centerPanel.add(progressBar, BorderLayout.CENTER);

        content.add(centerPanel, BorderLayout.CENTER);

        // ---- Button panel (Windows-style centered button) ----
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        cancelButton.setPreferredSize(new Dimension(90, 26)); // small fixed width
        buttonPanel.add(cancelButton);


        // subtle top separator like native dialogs
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(10, 0, 5, 0)
        ));

        add(content, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        //setSize(320, 130);
        pack();
        setResizable(false);
        setLocationRelativeTo(getParent());
    }

    private void bindWorker(SwingWorker worker) {
        worker.addPropertyChangeListener(evt -> {
            switch (evt.getPropertyName()) {
                case "progress":
                    progressBar.setValue((Integer) evt.getNewValue());
                    break;

                case "state":
                    if (evt.getNewValue() == SwingWorker.StateValue.DONE) {
                        dispose();
                    }
                    break;
            }
        });
    }
}
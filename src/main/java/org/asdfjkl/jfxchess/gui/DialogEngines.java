package org.asdfjkl.jfxchess.gui;

import javax.swing.*;
import java.awt.*;

public class DialogEngines extends JDialog {

    public DialogEngines(Frame parent) {
        super(parent, "Chess Engines", true);
        initUI();
        setSize(300, 350);
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // ===== CENTER PANEL (2 columns) =====
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        add(centerPanel, BorderLayout.CENTER);

        // LEFT: List
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> engineList = new JList<>(listModel);
        JScrollPane listScroll = new JScrollPane(engineList);
        centerPanel.add(listScroll, BorderLayout.CENTER);

        // RIGHT: Buttons column
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        centerPanel.add(rightPanel, BorderLayout.EAST);

        JButton btnAdd = new JButton("Add");
        JButton btnRemove = new JButton("Remove");
        JButton btnEdit = new JButton("Edit Parameters");
        JButton btnReset = new JButton("Reset Parameters");

        // Determine max width/height
        Dimension maxSize = btnReset.getPreferredSize();

        maxSize = new Dimension(
                Math.max(Math.max(btnAdd.getPreferredSize().width, btnRemove.getPreferredSize().width),
                        Math.max(btnEdit.getPreferredSize().width, btnReset.getPreferredSize().width)),
                maxSize.height
        );

        // Apply same size to all buttons
        for (JButton b : new JButton[]{btnAdd, btnRemove, btnEdit, btnReset}) {
            b.setPreferredSize(maxSize);
            b.setMaximumSize(maxSize);
            b.setMinimumSize(maxSize);
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
        }


        rightPanel.add(btnAdd);
        rightPanel.add(Box.createVerticalStrut(5));
        rightPanel.add(btnRemove);

        // Expanding space
        rightPanel.add(Box.createVerticalGlue());

        rightPanel.add(btnEdit);
        rightPanel.add(Box.createVerticalStrut(5));
        rightPanel.add(btnReset);

        // ===== BOTTOM: OK / Cancel =====
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOK = new JButton("OK");
        JButton btnCancel = new JButton("Cancel");

        bottomPanel.add(btnOK);
        bottomPanel.add(btnCancel);

        add(bottomPanel, BorderLayout.SOUTH);

        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
    }

}
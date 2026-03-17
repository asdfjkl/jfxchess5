package org.asdfjkl.jfxchess.gui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class DialogEngines extends JDialog {

    DefaultListModel<Engine> engineListModel;

    public DialogEngines(Frame parent, ArrayList<Engine> engines, int idxActiveEngine) {
        super(parent, "Chess Engines", true);

        engineListModel = new DefaultListModel<>();
        for (Engine e : engines) {
            engineListModel.addElement(e.makeCopy());
        }

        initUI(idxActiveEngine);
        setSize(300, 350);
        setLocationRelativeTo(parent);
    }

    private void initUI(int idxActiveEngine) {
        setLayout(new BorderLayout(10, 10));

        // ===== CENTER PANEL (2 columns) =====
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        add(centerPanel, BorderLayout.CENTER);

        // LEFT: List
        JList<Engine> engineList = new JList<>(engineListModel);
        JScrollPane listScroll = new JScrollPane(engineList);
        centerPanel.add(listScroll, BorderLayout.CENTER);
        engineList.setSelectedIndex(idxActiveEngine);

        // RIGHT: Buttons column
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        centerPanel.add(rightPanel, BorderLayout.EAST);

        JButton btnAdd = new JButton("Add");
        btnAdd.addActionListener(e -> {
            // todo: implement
            DialogEngineOptions dlg = new DialogEngineOptions(this, engineListModel.get(0).options);
            dlg.setVisible(true);
        });
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

    public ArrayList<Engine> getEngines() {
        ArrayList<Engine> engines = new ArrayList<>();
        for (int i = 0; i < engineListModel.size(); i++) {
            engines.add(engineListModel.get(i));
        }
        return engines;
    }

}
package org.asdfjkl.jfxchess.gui;

import javax.swing.*;
import java.awt.*;

public class DialogSearchGames extends JDialog {

    private static final int INPUT_WIDTH = 260;

    public DialogSearchGames(Window parent) {
        super(parent, "Search Chess Games", ModalityType.APPLICATION_MODAL);
        setSize(430, 400);
        setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // padding

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        int y = 0;

        // --- Text fields ---
        addAlignedField(panel, gbc, y++, "White:");
        addAlignedField(panel, gbc, y++, "Black:");

        // Ignore Colors
        gbc.gridx = 1;
        gbc.gridy = y++;
        panel.add(new JCheckBox("Ignore Colors"), gbc);

        addAlignedField(panel, gbc, y++, "Event:");
        addAlignedField(panel, gbc, y++, "Site:");

        // --- Year ---
        gbc.gridx = 0;
        gbc.gridy = y;
        panel.add(new JCheckBox("Year:"), gbc);

        gbc.gridx = 1;
        panel.add(createYearPanel(), gbc);
        y++;

        // --- ECO ---
        gbc.gridx = 0;
        gbc.gridy = y;
        panel.add(new JCheckBox("ECO:"), gbc);

        gbc.gridx = 1;
        panel.add(createEcoPanel(), gbc);
        y++;

        // --- Elo ---
        gbc.gridx = 0;
        gbc.gridy = y;
        panel.add(new JLabel("Elo:"), gbc);

        gbc.gridx = 1;
        panel.add(createEloRangePanel(), gbc);
        y++;

        // Elo radio buttons
        gbc.gridx = 1;
        gbc.gridy = y++;
        panel.add(createRadioGroup(), gbc);

        // --- Result ---
        gbc.gridx = 0;
        gbc.gridy = y;
        panel.add(new JLabel("Result:"), gbc);

        gbc.gridx = 1;
        panel.add(createResultPanel(), gbc);
        y++;

        // --- Buttons ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 4, 4, 4);

        panel.add(buttonPanel, gbc);

        // Reset defaults
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(4, 4, 4, 4);

        // Button behavior
        okButton.addActionListener(e -> dispose());
        cancelButton.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(okButton);

        add(panel);
    }

    // ---------- Helper Methods ----------

    private void addAlignedField(JPanel panel, GridBagConstraints gbc, int y, String labelText) {
        gbc.gridx = 0;
        gbc.gridy = y;
        panel.add(new JLabel(labelText), gbc);

        JTextField field = new JTextField(20);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        wrapper.setPreferredSize(new Dimension(INPUT_WIDTH, field.getPreferredSize().height));
        wrapper.add(field);

        gbc.gridx = 1;
        panel.add(wrapper, gbc);
    }

    private JPanel createYearPanel() {
        JPanel inner = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        inner.add(createSpinner(500, 0, 3000));
        inner.add(new JLabel(" to "));
        inner.add(createSpinner(2100, 0, 3000));

        return wrap(inner);
    }

    private JPanel createEcoPanel() {
        JPanel inner = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        JTextField from = new JTextField("A00", 4);
        JTextField to = new JTextField("E99", 4);

        inner.add(from);
        inner.add(new JLabel(" to "));
        inner.add(to);

        return wrap(inner);
    }

    private JPanel createEloRangePanel() {
        JPanel inner = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        inner.add(createSpinner(1000, 0, 4000));
        inner.add(new JLabel(" to "));
        inner.add(createSpinner(3000, 0, 4000));

        return wrap(inner);
    }

    private JPanel wrap(JPanel inner) {
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        wrapper.setPreferredSize(new Dimension(INPUT_WIDTH, inner.getPreferredSize().height));
        wrapper.add(inner);
        return wrapper;
    }

    private JPanel createRadioGroup() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

        JRadioButton ignore = new JRadioButton("Ignore", true);
        JRadioButton one = new JRadioButton("One");
        JRadioButton both = new JRadioButton("Both");
        JRadioButton avg = new JRadioButton("Average");

        ButtonGroup group = new ButtonGroup();
        group.add(ignore);
        group.add(one);
        group.add(both);
        group.add(avg);

        panel.add(ignore);
        panel.add(one);
        panel.add(both);
        panel.add(avg);

        return panel;
    }

    private JPanel createResultPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        panel.add(new JCheckBox("1-0", true));
        panel.add(new JCheckBox("0-1", true));
        panel.add(new JCheckBox("*", true));
        panel.add(new JCheckBox("1/2-1/2", true));

        return panel;
    }

    private JSpinner createSpinner(int value, int min, int max) {
        return new JSpinner(new SpinnerNumberModel(value, min, max, 1));
    }


}
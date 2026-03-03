package org.asdfjkl.jfxchess.gui;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

public class DialogEditGameData extends JDialog {

    private boolean confirmed = false;

    // Text fields
    private JTextField whiteFirstName = new JTextField(15);
    private JTextField whiteSurname = new JTextField(15);
    private JTextField blackFirstName = new JTextField(15);
    private JTextField blackSurname = new JTextField(15);
    private JTextField site = new JTextField(15);
    private JTextField event = new JTextField(15);

    // Spinners
    private JSpinner yearSpinner;
    private JSpinner monthSpinner;
    private JSpinner daySpinner;
    private JSpinner roundSpinner;
    private JSpinner eloWhiteSpinner;
    private JSpinner eloBlackSpinner;

    // Result buttons
    private JRadioButton winWhite = new JRadioButton("1-0");
    private JRadioButton winBlack = new JRadioButton("0-1");
    private JRadioButton draw = new JRadioButton("1/2-1/2");
    private JRadioButton unknown = new JRadioButton("*");

    HashMap<String, String> pgnHeaders = new HashMap<>();

    public DialogEditGameData(Frame parent,  HashMap<String, String> pgnHeaders) {

        super(parent, "Game Information", true);

        for (Map.Entry<String, String> entry : pgnHeaders.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            this.pgnHeaders.put(key,value);
        }

        initSpinners();
        initUI();

        pack();

        setLocationRelativeTo(parent);
    }

    private void initSpinners() {

        yearSpinner = new JSpinner(
                new SpinnerNumberModel(2024, 1000, 9999, 1));

        installPlainNumberEditor(yearSpinner, "0000");


        eloWhiteSpinner = new JSpinner(
                new SpinnerNumberModel(2000, 0, 9999, 1));

        installPlainNumberEditor(eloWhiteSpinner, "0000");


        eloBlackSpinner = new JSpinner(
                new SpinnerNumberModel(2000, 0, 9999, 1));

        installPlainNumberEditor(eloBlackSpinner, "0000");


        monthSpinner = new JSpinner(
                new SpinnerNumberModel(1, 1, 12, 1));

        installPlainNumberEditor(monthSpinner, "0");


        daySpinner = new JSpinner(
                new SpinnerNumberModel(1, 1, 31, 1));

        installPlainNumberEditor(daySpinner, "0");


        roundSpinner = new JSpinner(
                new SpinnerNumberModel(1, 1, 99, 1));

        installPlainNumberEditor(roundSpinner, "0");

        // ---- Set widths (pixels) ----
        /*
        int w = 60;
        setSpinnerWidth(yearSpinner, w);
        setSpinnerWidth(monthSpinner, w);
        setSpinnerWidth(daySpinner, w);
        setSpinnerWidth(roundSpinner, w);
        setSpinnerWidth(eloWhiteSpinner, w);
        setSpinnerWidth(eloBlackSpinner, w);

         */
        fixSpinnerWidth(yearSpinner, 80);
        fixSpinnerWidth(monthSpinner, 80);
        fixSpinnerWidth(daySpinner, 80);
        fixSpinnerWidth(roundSpinner, 80);
        fixSpinnerWidth(eloWhiteSpinner, 80);
        fixSpinnerWidth(eloBlackSpinner, 80);

        /*
        removeSpinnerGrouping(yearSpinner);
        removeSpinnerGrouping(eloWhiteSpinner);
        removeSpinnerGrouping(eloBlackSpinner);
        removeSpinnerGrouping(roundSpinner);
        removeSpinnerGrouping(monthSpinner);
        removeSpinnerGrouping(daySpinner);

         */
    }

    private void initUI() {

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(4, 6, 4, 6);
        //gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Add rows
        addRow(formPanel, gbc, row++, "White Firstname:", whiteFirstName);
        addRow(formPanel, gbc, row++, "White Surname:", whiteSurname);
        addRow(formPanel, gbc, row++, "Black Firstname:", blackFirstName);
        addRow(formPanel, gbc, row++, "Black Surname:", blackSurname);
        addRow(formPanel, gbc, row++, "Site:", site);
        addRow(formPanel, gbc, row++, "Event:", event);

        addRow(formPanel, gbc, row++, "Year:", yearSpinner);
        addRow(formPanel, gbc, row++, "Month:", monthSpinner);
        addRow(formPanel, gbc, row++, "Day:", daySpinner);
        addRow(formPanel, gbc, row++, "Round:", roundSpinner);

        addRow(formPanel, gbc, row++, "Elo White:", eloWhiteSpinner);
        addRow(formPanel, gbc, row++, "Elo Black:", eloBlackSpinner);

        // Result row
        JPanel resultPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

        ButtonGroup group = new ButtonGroup();
        group.add(winWhite);
        group.add(winBlack);
        group.add(draw);
        group.add(unknown);

        unknown.setSelected(true);

        resultPanel.add(winWhite);
        resultPanel.add(winBlack);
        resultPanel.add(draw);
        resultPanel.add(unknown);

        addRow(formPanel, gbc, row++, "Result:", resultPanel);

        // Buttons
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        okButton.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        cancelButton.addActionListener(e -> dispose());

        JPanel buttonPanel =
                new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        // Layout
        setLayout(new BorderLayout());
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void removeSpinnerGrouping(JSpinner spinner) {

        if (spinner.getEditor() instanceof JSpinner.NumberEditor editor) {

            NumberFormat format = editor.getFormat();
            format.setGroupingUsed(false);

            editor.getTextField().setHorizontalAlignment(JTextField.LEFT);
        }
    }

    private void addRow(JPanel panel, GridBagConstraints gbc,
                        int row, String labelText, JComponent field) {

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;

        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;

        if (field instanceof JTextField) {

            // Text fields grow and fill
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;

        } else {

            // Spinners and others stay fixed
            gbc.weightx = 0.0;
            gbc.fill = GridBagConstraints.NONE;
        }

        panel.add(field, gbc);
    }

    // =======================
    // Getters
    // =======================

    public boolean isConfirmed() {
        return confirmed;
    }

    public Map<String, Object> getData() {

        Map<String, Object> data = new HashMap<>();

        data.put("whiteFirstName", whiteFirstName.getText());
        data.put("whiteSurname", whiteSurname.getText());
        data.put("blackFirstName", blackFirstName.getText());
        data.put("blackSurname", blackSurname.getText());
        data.put("site", site.getText());
        data.put("event", event.getText());

        data.put("year", yearSpinner.getValue());
        data.put("month", monthSpinner.getValue());
        data.put("day", daySpinner.getValue());
        data.put("round", roundSpinner.getValue());

        data.put("eloWhite", eloWhiteSpinner.getValue());
        data.put("eloBlack", eloBlackSpinner.getValue());

        data.put("result", getResult());

        return data;
    }

    private void installPlainNumberEditor(JSpinner spinner, String pattern) {

        JSpinner.NumberEditor editor =
                new JSpinner.NumberEditor(spinner, pattern);

        NumberFormat format = editor.getFormat();
        format.setGroupingUsed(false);

        spinner.setEditor(editor);
    }

    private void setSpinnerWidth(JSpinner spinner, int width) {

        JComponent editor = spinner.getEditor();

        if (editor instanceof JSpinner.DefaultEditor) {

            JFormattedTextField tf =
                    ((JSpinner.DefaultEditor) editor).getTextField();

            Dimension d = tf.getPreferredSize();
            d.width = width;

            tf.setPreferredSize(d);
            tf.setMinimumSize(d);
        }
    }

    private void fixSpinnerWidth(JSpinner spinner, int width) {

        Dimension d = spinner.getPreferredSize();

        d = new Dimension(width, d.height);

        spinner.setPreferredSize(d);
        spinner.setMinimumSize(d);
        spinner.setMaximumSize(d);
    }

    private String getResult() {

        if (winWhite.isSelected()) return "1-0";
        if (winBlack.isSelected()) return "0-1";
        if (draw.isSelected()) return "1/2-1/2";
        return "*";
    }
}
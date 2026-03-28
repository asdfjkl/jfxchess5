package org.asdfjkl.jfxchess.gui;

import org.asdfjkl.jfxchess.lib.PolyglotExtEntry;

import javax.swing.*;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class View_Book extends JTable {

    private ExtPolyglotTableModel tableModel;

    public View_Book(List<PolyglotExtEntry> data) {
        super();

        // Set model
        tableModel = new ExtPolyglotTableModel(data);
        setModel(tableModel);

        // Disable sorting
        setRowSorter(null);
        getTableHeader().setReorderingAllowed(false);

        // Appearance
        setFillsViewportHeight(true);
        setRowHeight(24);

        // Optional: hand cursor
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // Add click handling
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e);
            }
        });
    }

    private void handleClick(MouseEvent e) {
        // Double-click (change to 1 if you prefer single-click)
        if (e.getClickCount() == 2) {

            int row = getSelectedRow();
            if (row >= 0) {

                int modelRow = convertRowIndexToModel(row);
                PolyglotExtEntry entry = tableModel.getEntry(modelRow);

                applyMove(entry.getMove());
            }
        }
    }

    // Dummy method (you will implement later)
    private void applyMove(String move) {
        System.out.println("Applying move: " + move);
    }

    // Optional: update data dynamically
    public void setData(List<PolyglotExtEntry> data) {
        tableModel.setData(data);
    }

    // Optional: access selected entry
    public PolyglotExtEntry getSelectedEntry() {
        int row = getSelectedRow();
        if (row >= 0) {
            return tableModel.getEntry(convertRowIndexToModel(row));
        }
        return null;
    }
}
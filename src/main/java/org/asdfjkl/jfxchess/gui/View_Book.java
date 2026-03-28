package org.asdfjkl.jfxchess.gui;

import org.asdfjkl.jfxchess.lib.Move;
import org.asdfjkl.jfxchess.lib.PolyglotExtEntry;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class View_Book extends JTable implements PropertyChangeListener {

    private final ExtPolyglotTableModel tableModel;
    private final Model_JFXChess model;
    private final Controller_Board controller_Board;

    public View_Book(Model_JFXChess model, Controller_Board controller_Board) {
        super();

        this.model = model;
        this.controller_Board = controller_Board;
        ArrayList<PolyglotExtEntry> data = new ArrayList<>();
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

        getColumnModel().getColumn(2).setCellRenderer(new WinDrawLossRenderer());
        setRowHeight(28);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        // total count is centered
        getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        getColumnModel().getColumn(1).setHeaderRenderer(centerRenderer);
        // average elo centered
        getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        getColumnModel().getColumn(3).setHeaderRenderer(centerRenderer);

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
        Move m = new Move(move);
        controller_Board.applyMove(m);
    }

    // Optional: access selected entry
    public PolyglotExtEntry getSelectedEntry() {
        int row = getSelectedRow();
        if (row >= 0) {
            return tableModel.getEntry(convertRowIndexToModel(row));
        }
        return null;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("currentGameNodeChanged".equals(evt.getPropertyName())
                || "gameChanged".equals(evt.getPropertyName()))
        {
            ArrayList<PolyglotExtEntry> moves = model.extBook.findEntries(model.getGame().getCurrentNode().getBoard());
            System.out.println("moves len: "+moves.size());
            tableModel.setData(moves);

        }
    }
}
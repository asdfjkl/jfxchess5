package org.asdfjkl.jfxchess.gui;

import org.asdfjkl.jfxchess.lib.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DialogDatabase extends JDialog {

    private JTable table;
    private GameTableModel tableModel;

    private JButton btnSearch;
    private JButton btnReset;
    private JButton btnDelete;
    private JButton btnOpen;
    private JButton btnCancel;

    private Controller_Pgn controller_Pgn;
    private Model_JFXChess  model_JFXChess;

    private boolean isConfirmed = false;

    private SearchPattern pattern = new SearchPattern();

    public DialogDatabase(Frame owner,
                          Model_JFXChess model_JFXChess,
                          Controller_Pgn controller) {
        super(owner, "Database", true);

        this.model_JFXChess = model_JFXChess;
        this.controller_Pgn = controller;
        this.tableModel = new GameTableModel(model_JFXChess.getPgnDatabase());
        initUI();

        setSize(900, 600);
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // ===== TABLE =====
        table = new JTable(tableModel);
        table.setAutoCreateRowSorter(false);
        table.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // ===== BUTTONS =====
        JPanel bottomPanel = new JPanel(new BorderLayout());

        // Left buttons
        JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnSearch = new JButton("Search");
        btnReset = new JButton("Reset Search");
        btnDelete = new JButton("Delete Game");

        leftButtons.add(btnSearch);
        leftButtons.add(btnReset);
        leftButtons.add(btnDelete);

        // Right buttons
        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnOpen = new JButton("Open Game");
        btnCancel = new JButton("Cancel");

        rightButtons.add(btnOpen);
        rightButtons.add(btnCancel);

        bottomPanel.add(leftButtons, BorderLayout.WEST);
        bottomPanel.add(rightButtons, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dispose());
        btnOpen.addActionListener(e -> {
            isConfirmed = true;
            dispose();
        });
        btnDelete.addActionListener(e -> { deleteSelectedGame(); });
        btnReset.addActionListener(e -> { resetSearch(); });
        btnSearch.addActionListener(e -> { onBtnSearch(); });
    }

    // ===== TABLE MODEL =====
    private static class GameTableModel extends AbstractTableModel {

        private final String[] columns = {
                "No", "White", "Black", "Event", "Date", "Result"
        };

        private List<PgnGameInfo> games;

        public GameTableModel(List<PgnGameInfo> games) {
            this.games = games; // NO COPY -> fast init
        }

        @Override
        public int getRowCount() {
            return games.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int col) {
            return columns[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            PgnGameInfo g = games.get(row);

            switch (col) {
                case 0: return row + 1;
                case 1: return g.getWhite();
                case 2: return g.getBlack();
                case 3: return g.getEvent();
                case 4: return g.getDate();
                case 5: return g.getResult();
                default: return "";
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return Integer.class;
                default:
                    return String.class;
            }
        }

        public PgnGameInfo getGameAt(int row) {
            return games.get(row);
        }

        public void removeRow(int row) {
            games.remove(row);
            fireTableRowsDeleted(row, row);
        }

        public void setData(ArrayList<PgnGameInfo> newEntries) {
            this.games = newEntries;
            fireTableDataChanged();
        }
    }

    // ===== ACCESS HELPERS =====
    public PgnGameInfo getSelectedGame() {
        int row = table.getSelectedRow();
        if (row < 0) return null;

        // convert if sorting is active
        int modelRow = table.convertRowIndexToModel(row);
        return tableModel.getGameAt(modelRow);
    }

    public void deleteSelectedGame() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        int modelRow = table.convertRowIndexToModel(row);
        PgnGameInfo gameInfo = tableModel.getGameAt(modelRow);
        long startOffset = gameInfo.getOffset();
        // check for confirmation
        int result = JOptionPane.showConfirmDialog(this,
                "Deleting '" +
                        gameInfo.getWhite() + " vs. " +  gameInfo.getBlack() +
                        "', please confirm",
                "Confirm Deletion",
                JOptionPane.OK_CANCEL_OPTION
        );
        if (result == JOptionPane.OK_OPTION) {
            if(modelRow + 1 < tableModel.getRowCount()) {
                long nextGameOffset = tableModel.getGameAt(modelRow + 1).getOffset();
                controller_Pgn.deleteGame(model_JFXChess.getFnPgnDatabase(), startOffset, nextGameOffset);
                tableModel.removeRow(modelRow);
            } else { // last game - delete until end
                controller_Pgn.deleteGame(model_JFXChess.getFnPgnDatabase(), startOffset);
                tableModel.removeRow(modelRow);
            }
        }
    }

    private void onBtnSearch() {
        System.out.println("onBtnSearch start");
        DialogSearchGames dlgSearch = new DialogSearchGames(this, pattern);
        dlgSearch.setVisible(true);
        pattern = dlgSearch.getSearchPattern();
        if(dlgSearch.isConfirmed()) {
            System.out.println("onBtnSearch confirmed");
            searchGames(pattern);
        }
        System.out.println("onBtnSearch end");
    }

    private void searchGames(SearchPattern pattern) {

        System.out.println("searchGames start");
        PgnSearchWorker worker = new PgnSearchWorker(model_JFXChess.getPgnDatabase(),
                pattern,
                new PgnReader(),
                entriesFromWorker -> { tableModel.setData(entriesFromWorker); }
        );
        System.out.println("searchGames before progress dialog");
        DialogProgress dlgProgress = new DialogProgress(this, worker, "Searching Games");
        worker.execute();
        dlgProgress.setVisible(true);
        System.out.println("searchGames end");
    }

    private void resetSearch() {
        tableModel.setData(model_JFXChess.getPgnDatabase());
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }
}

/* JFXChess - A Chess Graphical User Interface
 * Copyright (C) 2020-2026 Dominik Klein
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.asdfjkl.jfxchess.gui;

import org.asdfjkl.jfxchess.lib.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DialogDatabase extends JDialog {

    private JTable table;
    private final GameTableModel tableModel;

    private final Controller_Pgn controller_Pgn;
    private final Model_JFXChess  model_JFXChess;

    private boolean isConfirmed = false;

    private SearchPattern pattern = new SearchPattern();

    public DialogDatabase(Frame owner,
                          Model_JFXChess model_JFXChess,
                          Controller_Pgn controller) {
        super(owner, model_JFXChess.getFnPgnDatabase(), true);

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
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // ===== BUTTONS =====
        JPanel bottomPanel = new JPanel(new BorderLayout());

        // Left buttons
        JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnSearch = new JButton("Search");
        JButton btnReset = new JButton("Reset Search");
        JButton btnDelete = new JButton("Delete Game");

        leftButtons.add(btnSearch);
        leftButtons.add(btnReset);
        leftButtons.add(btnDelete);

        // Right buttons
        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOpen = new JButton("Open Game");
        JButton btnCancel = new JButton("Cancel");

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
        // make sure, open is disabled if no entry in table is selected
        btnOpen.setEnabled(false);
        table.getSelectionModel().addListSelectionListener(e -> {
            // Avoid double events while adjusting
            if (!e.getValueIsAdjusting()) {
                boolean rowSelected = table.getSelectedRow() != -1;
                btnOpen.setEnabled(rowSelected);
            }
        });

        btnDelete.addActionListener(e -> { deleteSelectedGame(); });
        btnReset.addActionListener(e -> { resetSearch(); });
        btnSearch.addActionListener(e -> { onBtnSearch(); });
    }

    // ===== TABLE MODEL =====
    private static class GameTableModel extends AbstractTableModel {

        private final String[] columns = {
                "No", "White", "Elo", "Black", "Elo", "Result", "Event", "Date"
        };

        private List<PgnGameInfo> games;

        public GameTableModel(List<PgnGameInfo> games) {
            this.games = games;
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
                case 2: return g.getWhiteElo();
                case 3: return g.getBlack();
                case 4: return g.getBlackElo();
                case 5: return g.getResult();
                case 6: return g.getEvent();
                case 7: return g.getDate();
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
        DialogSearchGames dlgSearch = new DialogSearchGames(this, pattern);
        dlgSearch.setVisible(true);
        pattern = dlgSearch.getSearchPattern();
        if(dlgSearch.isConfirmed()) {
            searchGames(pattern);
        }
    }

    private void searchGames(SearchPattern pattern) {

        PgnSearchWorker worker = new PgnSearchWorker(model_JFXChess.getPgnDatabase(),
                pattern,
                new PgnReader(),
                entriesFromWorker -> { tableModel.setData(entriesFromWorker); }
        );
        DialogProgress dlgProgress = new DialogProgress(this, worker, "Searching Games");
        worker.execute();
        dlgProgress.setVisible(true);
    }

    private void resetSearch() {
        tableModel.setData(model_JFXChess.getPgnDatabase());
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }
}

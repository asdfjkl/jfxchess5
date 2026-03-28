package org.asdfjkl.jfxchess.gui;

import org.asdfjkl.jfxchess.lib.PolyglotExtEntry;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class ExtPolyglotTableModel extends AbstractTableModel {

    private final String[] columns = {
            "Move", "Count", "Win %", "Draw %", "Loss %", "Elo"
    };

    private List<PolyglotExtEntry> data;

    public ExtPolyglotTableModel(List<PolyglotExtEntry> data) {
        this.data = data;
    }

    public void setData(List<PolyglotExtEntry> data) {
        this.data = data;
        fireTableDataChanged();
    }

    public PolyglotExtEntry getEntry(int row) {
        return data.get(row);
    }

    @Override
    public int getRowCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0 -> String.class;   // Move
            case 1 -> Long.class;     // Count
            case 2, 3, 4 -> Integer.class; // percentages
            case 5 -> Integer.class;  // Elo
            default -> Object.class;
        };
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        PolyglotExtEntry entry = data.get(rowIndex);

        return switch (columnIndex) {
            case 0 -> entry.getMove();
            case 1 -> entry.getPosCount();
            case 2 -> entry.getWins();
            case 3 -> entry.getDraws();
            case 4 -> entry.getLosses();
            case 5 -> entry.getAvgELO();
            default -> null;
        };
    }
}
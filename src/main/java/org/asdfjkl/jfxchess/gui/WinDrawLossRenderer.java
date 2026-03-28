package org.asdfjkl.jfxchess.gui;

import org.asdfjkl.jfxchess.lib.PolyglotExtEntry;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class WinDrawLossRenderer extends JPanel implements TableCellRenderer {

    private int win;
    private int draw;
    private int loss;

    public WinDrawLossRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {

        if (value instanceof PolyglotExtEntry entry) {
            this.win = entry.getWins();
            this.draw = entry.getDraws();
            this.loss = entry.getLosses();
        }

        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int width = getWidth();
        int height = getHeight();

        // Calculate widths
        int winWidth = (int) (width * (win / 100.0));
        int drawWidth = (int) (width * (draw / 100.0));
        int lossWidth = width - winWidth - drawWidth;

        // Colors
        Color winColor = new Color(120, 200, 120);   // green
        Color drawColor = new Color(200, 200, 200);  // gray
        Color lossColor = new Color(200, 100, 100);  // red

        // Draw bars
        int x = 0;

        g.setColor(winColor);
        g.fillRect(x, 2, winWidth, height - 4);
        x += winWidth;

        g.setColor(drawColor);
        g.fillRect(x, 2, drawWidth, height - 4);
        x += drawWidth;

        g.setColor(lossColor);
        g.fillRect(x, 2, lossWidth, height - 4);

        // Draw text (centered in each segment)
        g.setColor(Color.BLACK);
        FontMetrics fm = g.getFontMetrics();

        drawCenteredText(g, win + "%", 0, winWidth, height, fm);
        drawCenteredText(g, draw + "%", winWidth, drawWidth, height, fm);
        drawCenteredText(g, loss + "%", winWidth + drawWidth, lossWidth, height, fm);
    }

    private void drawCenteredText(Graphics g, String text, int startX, int width, int height, FontMetrics fm) {
        if (width <= 0) return;

        int textWidth = fm.stringWidth(text);
        int x = startX + (width - textWidth) / 2;
        int y = (height + fm.getAscent() - fm.getDescent()) / 2;

        g.drawString(text, x, y);
    }
}
package org.asdfjkl.jfxchess.gui;

import org.asdfjkl.jfxchess.lib.PgnGameInfo;
import org.asdfjkl.jfxchess.lib.PgnReader;
import org.asdfjkl.jfxchess.lib.ProgressListener;
import org.asdfjkl.jfxchess.lib.SearchPattern;

import javax.swing.*;
import java.util.ArrayList;

public class PgnSearchWorker extends SwingWorker<ArrayList<PgnGameInfo>, Integer> {

    private final ArrayList<PgnGameInfo> entries;
    private final PgnReader pgnReader;
    private final PgnScanListener pgnScanListener;
    private final SearchPattern pattern;

    public PgnSearchWorker(ArrayList<PgnGameInfo> entriesToSearch,
                           SearchPattern pattern,
                           PgnReader reader,
                           PgnScanListener listener) {
        this.entries = entriesToSearch;
        this.pgnReader = reader;
        this.pgnScanListener = listener;
        this.pattern = pattern;
    }

    @Override
    protected ArrayList<PgnGameInfo> doInBackground() throws Exception {

        return pgnReader.searchPgn(entries, pattern,
                new ProgressListener() {
                    @Override
                    public void onProgress(int percent) {
                        setProgress(percent);
                    }

                    @Override
                    public boolean isCancelled() {
                        return PgnSearchWorker.this.isCancelled();
                    }
                });
    }

    @Override
    protected void done() {
        try {
            if (!isCancelled()) {
                ArrayList<PgnGameInfo> result = get();
                pgnScanListener.onScanFinished(result);
                System.out.println("Done: " + result.size());
            } else {
                System.out.println("Cancelled");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
package org.asdfjkl.jfxchess.gui;

import org.asdfjkl.jfxchess.lib.PgnGameInfo;
import org.asdfjkl.jfxchess.lib.PgnReader;
import org.asdfjkl.jfxchess.lib.ProgressListener;

import javax.swing.*;
import java.util.ArrayList;

public class PgnSaveWorker extends SwingWorker<ArrayList<PgnGameInfo>, Integer> {

    private final String filename;
    private final PgnReader pgnReader;
    private final PgnScanListener pgnScanListener;

    public PgnSaveWorker(String filename, PgnReader lib, PgnScanListener listener) {
        this.filename = filename;
        this.pgnReader = lib;
        this.pgnScanListener = listener;
    }

    @Override
    protected ArrayList<PgnGameInfo> doInBackground() throws Exception {

        return pgnReader.scanPgn(filename, new ProgressListener() {
            @Override
            public void onProgress(int percent) {
                setProgress(percent); // SwingWorker built-in support
            }

            @Override
            public boolean isCancelled() {
                return PgnSaveWorker.this.isCancelled();
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
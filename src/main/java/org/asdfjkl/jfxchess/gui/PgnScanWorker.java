package org.asdfjkl.jfxchess.gui;

import org.asdfjkl.jfxchess.lib.PgnGameInfo;
import org.asdfjkl.jfxchess.lib.PgnReader;
import org.asdfjkl.jfxchess.lib.ProgressListener;

import javax.swing.*;
import java.util.ArrayList;

public class PgnScanWorker extends SwingWorker<ArrayList<PgnGameInfo>, Integer> {

    private final String filename;
    private final PgnReader pgnReader;

    public PgnScanWorker(String filename, PgnReader lib) {
        this.filename = filename;
        this.pgnReader = lib;
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
                return PgnScanWorker.this.isCancelled();
            }
        });
    }

    @Override
    protected void done() {
        try {
            if (!isCancelled()) {
                ArrayList<PgnGameInfo> result = get();
                System.out.println("Done: " + result.size());
            } else {
                System.out.println("Cancelled");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
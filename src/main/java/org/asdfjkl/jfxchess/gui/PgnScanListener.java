package org.asdfjkl.jfxchess.gui;

import org.asdfjkl.jfxchess.lib.PgnGameInfo;

import java.util.ArrayList;

public interface PgnScanListener {
    void onScanFinished(ArrayList<PgnGameInfo> result);
}
package org.asdfjkl.jfxchess.gui;

import org.asdfjkl.jfxchess.lib.*;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.prefs.Preferences;

public class Model_JFXChess {

    // start old
    public static final int MAX_PV = 64;
    public static final int MAX_N_ENGINES = 10;
    public static final int MODE_ENTER_MOVES = 0;
    public static final int MODE_ANALYSIS = 1;
    public static final int MODE_PLAY_WHITE = 2;
    public static final int MODE_PLAY_BLACK = 3;
    public static final int MODE_GAME_ANALYSIS = 4;
    public static final int BOTH_PLAYERS = 5;
    public static final int MODE_PLAYOUT_POSITION = 7;
    Game game;
    private int currentMode;
    private boolean multiPvChanged = false;
    private boolean flipBoard = false;
    private boolean humanPlayerColor = CONSTANTS.WHITE;
    public boolean wasSaved = false;
    private int engineStrength = 2400;
    private int engineThinkTimeSecs = 3;

    ArrayList<Engine> engines = new ArrayList<>();
    Engine activeEngine = null;
    Engine selectedPlayEngine = null;
    Engine selectedAnalysisEngine = null;

    ArrayList<BotEngine> botEngines = new ArrayList<>();

    private int gameAnalysisForPlayer = BOTH_PLAYERS;
    private double gameAnalysisThreshold = 0.5; // pawns
    private int gameAnalysisThinkTimeSecs = 3;  // seconds

    private boolean gameAnalysisJustStarted = false;

    // to make sure that if the user play against the computer/bot
    // he is not able to/does not accidentally move the computer's pieces
    // on the computer's turn
    public boolean blockGUI = false;

    public String currentBestPv = "";
    public int currentBestEval = 0;
    public int currentMateInMoves = -1;
    public boolean currentIsMate = false;

    public String childBestPv = "";
    public int childBestEval = 0;
    public int childMateInMoves = -1;
    public boolean childIsMate = false;

    public PolyglotExt extBook;

    private Preferences prefs;

    private static final int modelVersion = 460;

    //private final PgnDatabase pgnDatabase;
    public int currentPgnDatabaseIdx = -1;
    public File lastOpenedDirPath = null;
    public File lastSaveDirPath = null;

    public boolean openDatabaseOnNextDialog = false;

    public String extBookPath;

    public int maxCpus = 1;

    //private SearchPattern searchPattern;
    BoardStyle boardStyle;

    // end old

    private final PropertyChangeSupport pcs =
            new PropertyChangeSupport(this);

    private String laf;
    public JFrame mainFrameRef;

    public void addListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public void setLaf(String laf) {
        this.laf = laf;
        pcs.firePropertyChange("switchLaf", "stuff", this.laf);
    }

    public String getLaf() {
        return this.laf;
    }


    public Game getGame() {
        Game g = new Game();
        Board b = new Board(true);
        GameNode start = new GameNode();
        start.setBoard(b);
        g.setRoot(start);
        g.setCurrent(start);
        return g;
    }

    public boolean getFlipBoard() {
        return true;
    }
}

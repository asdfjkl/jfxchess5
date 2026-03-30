package org.asdfjkl.jfxchess.gui;

import org.asdfjkl.jfxchess.lib.*;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.prefs.Preferences;

public class Model_JFXChess {

    private static final int modelVersion = 500;

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

    //private boolean gameAnalysisJustStarted = false;

    // to make sure that if the user play against the computer/bot
    // he is not able to/does not accidentally move the computer's pieces
    // on the computer's turn
    private boolean blockGUI = false;

    public String currentBestPv = "";
    public int currentBestEval = 0;
    public int currentMateInMoves = -1;
    public boolean currentIsMate = false;

    public String childBestPv = "";
    public int childBestEval = 0;
    public int childMateInMoves = -1;
    public boolean childIsMate = false;

    public PolyglotExt extBook = new PolyglotExt();

    private Preferences prefs;

    private ArrayList<PgnGameInfo> pgnDatabase = new ArrayList<>();
    private String fnPgnDatabase = "";
    private int indexOfCurrentGameInPgn = -1;
    private File lastOpenedDirPath = null;
    private File lastSaveDirPath = null;

    public String extBookPath;
    public int maxCpus = 1;
    BoardStyle boardStyle;
    // end old

    private final PropertyChangeSupport pcs =
            new PropertyChangeSupport(this);

    private String laf;
    public View_MainFrame mainFrameRef;
    private String latestEngineInfo = "";

    public Model_JFXChess() {
        game = new Game();
        Board b = new Board(true);

        //pgnDatabase = new PgnDatabase();
        //searchPattern = new SearchPattern();
        //searchPattern.setSearchForHeader(true);
        boardStyle = new BoardStyle();
        laf = "system.default";

        game.getRootNode().setBoard(b);
        currentMode = MODE_ENTER_MOVES;

        String stockfishPath = getStockfishPath();
        Engine stockfish = new Engine();
        stockfish.setName(CONSTANTS.INTERNAL_ENGINE_NAME);
        if(stockfishPath != null) {
            stockfish.setPath(stockfishPath);
        }
        stockfish.setInternal(true);
        engines.add(stockfish);
        selectedAnalysisEngine = stockfish;
        activeEngine = stockfish;

        // manually add for internal stockfish up to 4 mpv
        EngineOption internalMPV = new EngineOption();
        internalMPV.name = "MultiPV";
        internalMPV.spinMin = 1;
        internalMPV.spinMax = 4;
        internalMPV.spinDefault = 1;
        internalMPV.spinValue = 1;
        internalMPV.type = EngineOption.EN_OPT_TYPE_SPIN;

        EngineOption internalElo = new EngineOption();
        internalElo.name = "UCI_Elo";
        internalElo.spinMin = 1320;
        internalElo.spinMax = 3190;
        internalElo.spinDefault = 1320;
        internalElo.spinValue = 3190;
        internalElo.type = EngineOption.EN_OPT_TYPE_SPIN;

        EngineOption internalLimitStrength = new EngineOption();
        internalLimitStrength.name = "UCI_LimitStrength";
        internalLimitStrength.type = EngineOption.EN_OPT_TYPE_CHECK;
        internalLimitStrength.checkStatusDefault = false;
        internalLimitStrength.checkStatusValue = false;

        EngineOption internalThreads = new EngineOption();
        internalThreads.name = "Threads";
        internalThreads.type = EngineOption.EN_OPT_TYPE_SPIN;
        internalThreads.spinMin = 1;
        internalThreads.spinMax = 1024;
        internalThreads.spinDefault = 1;
        internalThreads.spinValue = 1;

        activeEngine.options.add(internalElo);
        activeEngine.options.add(internalMPV);
        activeEngine.options.add(internalLimitStrength);
        activeEngine.options.add(internalThreads);

        // add bots
        String botPath = getBotEnginePath();
        botEngines = BotEngines.createEngines(botPath);
        selectedPlayEngine = botEngines.get(0); // set benny as default; todo: remember last selected bot

        /*
        temp
         */
        File file = new File("C:\\MyFiles\\workspace\\jfxchess5\\target\\book\\extbook.bin");
        extBook.loadBook(file);

    }



    public void addListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }



    public Path getJarPath() {
        try {
            Path jarPath = Paths.get(App.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path jarDir = jarPath.getParent();
            return jarDir;
        } catch (URISyntaxException e) {
            System.err.println("[ERROR] Failed to resolve JAR location: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("[ERROR] Unexpected error locating engine binary: " + e.getMessage());
            return null;
        }
    }

    private String getStockfishPath() {

        // currently redundant, but let's keep it here if we
        // have different packaging requirements in the future
        Path jarDir = getJarPath();
        String os = System.getProperty("os.name").toLowerCase();
        if(os.contains("win")) {
            if (jarDir != null) {
                Path engineBinary = jarDir.resolve("engine").resolve("stockfish.exe");
                return engineBinary.toString();
            }
        }
        if(os.contains("linux")) {
            Path engineBinary = null;
            if (jarDir != null) {
                engineBinary = jarDir.resolve("engine").resolve("stockfish_x64");
                return engineBinary.toString();
            }
        }
        return null;
    }

    private String getBotEnginePath() {

        // currently redundant, but let's keep it here if we
        // have different packaging requirements in the future
        Path jarDir = getJarPath();
        String os = System.getProperty("os.name").toLowerCase();
        if(os.contains("win")) {
            if (jarDir != null) {
                Path engineBinary = jarDir.resolve("engine").resolve("stockfish5.exe");
                return engineBinary.toString();
            }
        }
        if(os.contains("linux")) {
            Path engineBinary = null;
            if (jarDir != null) {
                engineBinary = jarDir.resolve("engine").resolve("stockfish5_x64");
                return engineBinary.toString();
            }
        }
        return null;

    }

    public String getExtBookPath() {

        Path jarDir = getJarPath();
        String os = System.getProperty("os.name").toLowerCase();
        if(os.contains("win")) {
            Path extBookBinary = null;
            if (jarDir != null) {
                extBookBinary = jarDir.resolve("book").resolve("extbook.bin");
                return extBookBinary.toString();
            }
        }
        if(os.contains("linux")) {
            Path extBookBinary = null;
            if (jarDir != null) {
                extBookBinary = jarDir.resolve("book").resolve("extbook.bin");
                return extBookBinary.toString();
            }
        }
        return null;
    }

    public void setLaf(String laf) {
        this.laf = laf;
        pcs.firePropertyChange("switchLaf", "stuff", this.laf);
    }

    public String getLaf() {
        return this.laf;
    }


    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
        pcs.firePropertyChange("gameChanged", null, null);
    }

    public int getGameAnalysisForPlayer() { return gameAnalysisForPlayer; }

    public void setGameAnalysisForPlayer(int player) { gameAnalysisForPlayer = player; }

    public double getGameAnalysisThreshold() { return gameAnalysisThreshold; }

    public void setGameAnalysisThreshold(double threshold) { gameAnalysisThreshold = threshold; }

    public void setGameAnalysisThinkTimeSecs(int thinktimeSecs) { gameAnalysisThinkTimeSecs = thinktimeSecs; }

    public int getGameAnalysisThinkTimeSecs() { return gameAnalysisThinkTimeSecs; }

    public void setMode(int mode) {
        this.currentMode = mode;
        pcs.firePropertyChange("modeChanged", null, null);
    }

    public void setComputerThinkTimeSecs(int secs) {
        engineThinkTimeSecs = secs;
    }

    public int getComputerThinkTimeSecs() {
        return engineThinkTimeSecs;
    }

    public int getEngineStrength() { return engineStrength; }

    public void setEngineStrength(int strength) { engineStrength = strength; }

    public int getMode() {
        return currentMode;
    }

    public int getMultiPv() {
        return activeEngine.getMultiPV();
    }

    public void setFlipBoard(boolean flipBoard) {
        this.flipBoard = flipBoard;
        pcs.firePropertyChange("boardFlipped", null, null);
    }

    public boolean getFlipBoard() {
        return flipBoard;
    }

    public void setHumanPlayerColor(boolean humanPlayerColor) {
        this.humanPlayerColor = humanPlayerColor;
    }

    public boolean getHumanPlayerColor() {
        return humanPlayerColor;
    }

    //public boolean getGameAnalysisJustStarted() { return gameAnalysisJustStarted; }

    //public void setGameAnalysisJustStarted(boolean val) { gameAnalysisJustStarted = val; }

    /*
    public PgnDatabase getPgnDatabase() {
        return pgnDatabase;
    } */

    /*
    public SearchPattern getSearchPattern() {
        return searchPattern;
    } */

    /*
    public void setSearchPattern(SearchPattern searchPattern) {
        this.searchPattern = searchPattern;
    } */

    public void setMultiPv(int multiPv) {
        if(multiPv >= 1 && multiPv <= activeEngine.getMaxMultiPV() && multiPv <= MAX_PV) {
            activeEngine.setMultiPV(multiPv);
            this.multiPvChanged = true;
        }
    }

    public boolean wasMultiPvChanged() {
        return this.multiPvChanged;
    }

    public void setMultiPvChange(boolean b) {
        this.multiPvChanged = b;
    }

    public void saveModel() {

        prefs = Preferences.userRoot().node(this.getClass().getName());

        prefs.putInt("modelVersion",modelVersion);

        PgnPrinter printer = new PgnPrinter();
        String pgn = printer.printGame(getGame());
        prefs.put("currentGame", pgn);
    }

    public void savePaths() {
        prefs = Preferences.userRoot().node(this.getClass().getName());
        if(lastOpenedDirPath != null) {
            prefs.put("lastOpenDir", lastOpenedDirPath.toString());
        }
        if(lastSaveDirPath != null) {
            prefs.put("lastSaveDir", lastSaveDirPath.toString());
        }
    }

    public void saveBoardStyle() {
        prefs = Preferences.userRoot().node(this.getClass().getName());
        prefs.putInt("COLOR_STYLE", boardStyle.getColorStyle());
        prefs.putInt("PIECE_STYLE", boardStyle.getPieceStyle());
    }

    public void saveEngines() {

        prefs = Preferences.userRoot().node(this.getClass().getName());
        // Clean up preferences. Preferences for engines
        // which have been removed may still be there.
        for(int i=0;i<MAX_N_ENGINES;i++) {
            prefs.remove("ENGINE"+i);
        }
        for(int i=1;i<engines.size();i++) {
            Engine engine = engines.get(i);
            String engineString = engine.writeToString();
            prefs.put("ENGINE"+i, engineString);
        }
        int activeEngineIdx = engines.indexOf(activeEngine);
        if(activeEngineIdx > 0) {
            prefs.putInt("ACTIVE_ENGINE_IDX", engines.indexOf(activeEngine));
        } else {
            prefs.putInt("ACTIVE_ENGINE_IDX", 0);
        }
    }

    public void saveExtBookPath() {
        prefs = Preferences.userRoot().node(this.getClass().getName());
        if(!extBookPath.isEmpty()) {
            prefs.put("EXT_BOOK_PATH_FILE", extBookPath);
        }
    }

    public void restoreExtBookPath() {

        prefs = Preferences.userRoot().node(this.getClass().getName());
        int mVersion = prefs.getInt("modelVersion", 0);

        String bookPath = getExtBookPath();
        if(mVersion == modelVersion) {
            bookPath = prefs.get("EXT_BOOK_PATH_FILE", bookPath);
        }
        extBookPath = bookPath;
    }

    public void saveGameAnalysisThresholds() {
        prefs = Preferences.userRoot().node(this.getClass().getName());
        prefs.putInt("GAME_ANALYSIS_SECS", getGameAnalysisThinkTimeSecs());
        prefs.putDouble("GAME_ANALYSIS_THRESHOLD", getGameAnalysisThreshold());
    }

    public void restoreGameAnalysisThresholds() {

        prefs = Preferences.userRoot().node(this.getClass().getName());
        int mVersion = prefs.getInt("modelVersion", 0);

        if(mVersion == modelVersion) {
            int gameAnalysisSecs = prefs.getInt("GAME_ANALYSIS_SECS", 3);
            double gameAnalysisThreshold = prefs.getDouble("GAME_ANALYSIS_THRESHOLD", 0.5);
            setGameAnalysisThinkTimeSecs(gameAnalysisSecs);
            setGameAnalysisThreshold(gameAnalysisThreshold);
        }
    }

    public void saveNewGameSettings() {
        prefs = Preferences.userRoot().node(this.getClass().getName());
        prefs.putInt("COMPUTER_THINK_TIME_SECS", getComputerThinkTimeSecs());
        prefs.putDouble("COMPUTER_STRENGTH", getEngineStrength());
    }

    public void restoreNewGameSettings() {

        prefs = Preferences.userRoot().node(this.getClass().getName());
        int mVersion = prefs.getInt("modelVersion", 0);

        if(mVersion == modelVersion) {
            int secs = prefs.getInt("COMPUTER_THINK_TIME_SECS", 3);
            int strength = prefs.getInt("COMPUTER_STRENGTH", 20);
            setComputerThinkTimeSecs(secs);
            setEngineStrength(strength);
        }
    }

    public void restoreBoardStyle() {

        BoardStyle style = new BoardStyle();
        prefs = Preferences.userRoot().node(this.getClass().getName());
        int mVersion = prefs.getInt("modelVersion", 0);

        if(mVersion == modelVersion) {
            int colorStyle = prefs.getInt("COLOR_STYLE", BoardStyle.STYLE_BLUE);
            int pieceStyle = prefs.getInt("PIECE_STYLE", BoardStyle.PIECE_STYLE_MERIDA);
            style.setPieceStyle(pieceStyle);
            style.setColorStyle(colorStyle);
        }
        boardStyle = style;
    }

    public void restoreEngines() {

        prefs = Preferences.userRoot().node(this.getClass().getName());
        int mVersion = prefs.getInt("modelVersion", 0);
        if (mVersion == modelVersion) {
            // don't restore engine with idx 0 (that's stockfish internal)
            for (int i = 1; i < MAX_N_ENGINES; i++) {
                String engineString = prefs.get("ENGINE" + i, "");
                if (!engineString.isEmpty()) {
                    Engine engine;
                    //if (i == 0) {
                    //    // engine 0 is Stockfish internal
                    //    // engine = engines.get(0);
                    //    // engine.restoreFromString(engineString);
                    //} else {
                    engine = new Engine();
                    engine.restoreFromString(engineString);
                    engines.add(engine);
                    //}
                }
            }
            int activeIdx = prefs.getInt("ACTIVE_ENGINE_IDX", 0);
            if(activeIdx < engines.size()) {
                activeEngine = engines.get(activeIdx);
                selectedAnalysisEngine = engines.get(activeIdx);
            } else {
                activeEngine = engines.get(0);
                selectedAnalysisEngine = engines.get(0);
            }
        }
    }

    public void saveScreenGeometry() {

        Preferences prefs = Preferences.userRoot().node(this.getClass().getName());

        Rectangle bounds = mainFrameRef.getBounds();

        prefs.putInt("WINDOW_WIDTH" , bounds.width);
        prefs.putInt("WINDOW_HEIGHT" , bounds.height);
        prefs.putInt("WINDOW_POSITION_X" , bounds.x);
        prefs.putInt("WINDOW_POSITION_Y" , bounds.y);

        boolean maximized =
                (mainFrameRef.getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0;
        prefs.putBoolean("WINDOW_MAXIMIZED", maximized);

        prefs.putInt("DIVIDER_HORIZONTAL", mainFrameRef.horizontalSplit.getDividerLocation());
        prefs.putInt("DIVIDER_VERTICAL", mainFrameRef.verticalSplit.getDividerLocation());

    }

    public ScreenGeometry restoreScreenGeometry() {

        prefs = Preferences.userRoot().node(this.getClass().getName());
        int mVersion = prefs.getInt("modelVersion", 0);

        ScreenGeometry g = new ScreenGeometry();

        if(mVersion == modelVersion) {

            g.height = prefs.getInt("WINDOW_HEIGHT", g.height);
            g.width = prefs.getInt("WINDOW_WIDTH", g.width);
            g.posX = prefs.getInt("WINDOW_POSITION_X", g.posX);
            g.posY = prefs.getInt("WINDOW_POSITION_Y", g.posY);

            g.isMaximized = prefs.getBoolean("WINDOW_MAXIMIZED", false);

            g.dividerHorizontal = prefs.getInt("DIVIDER_HORIZONTAL", g.dividerHorizontal);
            g.dividerVertical = prefs.getInt("DIVIDER_VERTICAL", g.dividerVertical);

        }
        return g;

    }

    public void saveTheme() {
        Preferences prefs = Preferences.userRoot().node(this.getClass().getName());
        prefs.put("LAF", laf);
    }

    public void restoreTheme() {
        Preferences prefs = Preferences.userRoot().node(this.getClass().getName());
        laf = prefs.get("LAF", "com.formdev.flatlaf.FlatDarculaLaf");
    }

    public void restorePaths() {
        prefs = Preferences.userRoot().node(this.getClass().getName());
        String lastOpenDir = prefs.get("lastOpenDir", "");
        String lastSaveDir = prefs.get("lastSaveDir", "");
        if (!lastOpenDir.isEmpty()) {
            lastOpenedDirPath = new File(lastOpenDir);
            if (!lastOpenedDirPath.exists()) {
                lastOpenedDirPath = null;
            }
        }
        if (!lastSaveDir.isEmpty()) {
            lastSaveDirPath = new File(lastSaveDir);
            if (!lastSaveDirPath.exists()) {
                lastSaveDirPath = null;
            }
        }
    }

    public void restoreModel() {

        prefs = Preferences.userRoot().node(this.getClass().getName());
        int mVersion = prefs.getInt("modelVersion", 0);

        if(mVersion == modelVersion) {
            PgnReader reader = new PgnReader();

            String pgn = prefs.get("currentGame", "");

            if(!pgn.isEmpty()) {
                Game g = reader.readGame(pgn);
                PgnPrinter p = new PgnPrinter();
                if (g.getRootNode().getBoard().isConsistent()) {
                    setGame(g);
                    g.setTreeWasChanged(true);
                }
            }
        }
    }

    public String getVersion() {

        int mainVersion = modelVersion / 100;
        int subVersion = (modelVersion % 100) / 10;
        int subSubVersion = ((modelVersion % 100) % 10);

        return mainVersion + "." + subVersion + "." + subSubVersion;

    }

    public boolean isBlockGUI() {
        return blockGUI;
    }

    public void setBlockGUI(boolean blockGUI) {
        boolean tmp = this.blockGUI;
        this.blockGUI = blockGUI;
        pcs.firePropertyChange("blockGUI", tmp, this.blockGUI);
    }

    public void applyMove(Move m) {
        // after applying a move, we block the GUI
        // when we are playing against the computer
        boolean treeWasChanged = getGame().applyMove(m);
        if(treeWasChanged) {
            pcs.firePropertyChange("treeChanged", null, null);
        }
        pcs.firePropertyChange("currentGameNodeChanged", null, null);
    }

    public void goToChild(int idx) {
        game.goToChild(idx);
        pcs.firePropertyChange("currentGameNodeChanged", null, null);
    }

    public void goToParent() {
        game.goToParent();
        pcs.firePropertyChange("currentGameNodeChanged", null, null);
    }

    public void setBoardColor(int style) {
        boardStyle.setColorStyle(style);
        pcs.firePropertyChange("boardColor", null, style);
    }

    public void setPieceStyle(int style) {
        boardStyle.setPieceStyle(style);
        pcs.firePropertyChange("pieceStyle", null, style);
    }

    public BoardStyle getBoardStyle() {
        return boardStyle;
    }

    public void setBoardStyle(BoardStyle boardStyle) {
        this.boardStyle = boardStyle;
    }

    public void setPgnHeaders(HashMap<String, String> data) {
        game.setPgnHeaders(data);
        pcs.firePropertyChange("pgnHeadersChanged", null, null);
    }

    public void goToNode(int id) {
        try {
            GameNode node = game.findNodeById(id);
            game.setCurrent(node);
            pcs.firePropertyChange("currentGameNodeChanged", null, null);
        } catch (IllegalArgumentException e) {
            // silently fail
        }
    }

    public void seekToEnd() {
        game.goToLeaf();
        pcs.firePropertyChange("currentGameNodeChanged", null, null);
    }

    public void seekToBeginning() {
        game.goToRoot();
        pcs.firePropertyChange("currentGameNodeChanged", null, null);
    }

    public void setComment(int nodeId, String s) {
        try {
            GameNode node = game.findNodeById(nodeId);
            node.setComment(s);
            pcs.firePropertyChange("treeChanged", null, null);
        } catch(IllegalArgumentException ignored) {

        }
    }

    public void addNag(int nodeId, int nag) {
        try {
            GameNode node = game.findNodeById(nodeId);
            node.addNag(nag);
            pcs.firePropertyChange("treeChanged", null, null);
        } catch(IllegalArgumentException ignored) {

        }
    }

    public void removeMoveAnnotations(int nodeId) {
        try {
            GameNode selectedNode = game.findNodeById(nodeId);
            selectedNode.removeNagsInRange(0, CONSTANTS.MOVE_ANNOTATION_UPPER_LIMIT);
            pcs.firePropertyChange("treeChanged", null, null);
        } catch(IllegalArgumentException ignored) {

        }
    }

    public void removePosAnnotations(int nodeId) {
        try {
            GameNode selectedNode = game.findNodeById(nodeId);
            selectedNode.removeNagsInRange(CONSTANTS.POSITION_ANNOTATION_LOWER_LIMIT,
                    CONSTANTS.POSITION_ANNOTATION_UPPER_LIMIT);
            pcs.firePropertyChange("treeChanged", null, null);
        } catch(IllegalArgumentException ignored) {

        }
    }

    public void removeMoveAndPosAnnotation(int nodeId) {
        try {
            GameNode selectedNode = game.findNodeById(nodeId);
            selectedNode.removeNagsInRange(0, CONSTANTS.POSITION_ANNOTATION_UPPER_LIMIT);
            pcs.firePropertyChange("treeChanged", null, null);
        } catch(IllegalArgumentException ignored) {

        }
    }

    public void moveVariantUp(int nodeId) {
        try {
            GameNode selectedNode = game.findNodeById(nodeId);
            game.moveUp(selectedNode);
            pcs.firePropertyChange("treeChanged", null, null);
        } catch(IllegalArgumentException ignored) {

        }
    }

    public void moveVariantDown(int nodeId) {
        try {
            GameNode selectedNode = game.findNodeById(nodeId);
            game.moveDown(selectedNode);
            pcs.firePropertyChange("treeChanged", null, null);
        } catch(IllegalArgumentException ignored) {

        }
    }

    public void deleteVariant(int nodeId) {
        try {
            GameNode selectedNode = game.findNodeById(nodeId);
            game.delVariant(selectedNode);
            pcs.firePropertyChange("treeChanged", null, null);
        } catch(IllegalArgumentException ignored) {

        }
    }

    public void deleteFromHere(int nodeId) {
        try {
            GameNode selectedNode = game.findNodeById(nodeId);
            game.delBelow(selectedNode);
            pcs.firePropertyChange("treeChanged", null, null);
        } catch(IllegalArgumentException ignored) {

        }
    }

    public void deleteAllComments() {
        try {
            game.removeAllComments();
            pcs.firePropertyChange("treeChanged", null, null);
        } catch(IllegalArgumentException ignored) {

        }
    }

    public void deleteAllVariants() {
        game.removeAllVariants();
        pcs.firePropertyChange("treeChanged", null, null);
    }

    public void markTreeChange() {
        pcs.firePropertyChange("treeChanged", null, null);
    }

    public String getCurrentEngineInfo() {
        return latestEngineInfo;
    }

    public void setCurrentEngineInfo(String info) {
        latestEngineInfo = info;
        //System.out.println("model: set currentEngineInfo: " + info);
        pcs.firePropertyChange("engineInfo", null, null);
    }

    public ArrayList<PgnGameInfo> getPgnDatabase() {
        return pgnDatabase;
    }

    public void setPgnDatabase(ArrayList<PgnGameInfo> pgnDatabase) {
        this.pgnDatabase = pgnDatabase;
    }

    public String getFnPgnDatabase() {
        return fnPgnDatabase;
    }

    public void setFnPgnDatabase(String fnPgnDatabase) {
        this.fnPgnDatabase = fnPgnDatabase;
    }

    public File getLastOpenedDirPath() {
        return lastOpenedDirPath;
    }

    public void setLastOpenedDirPath(File lastOpenedDirPath) {
        this.lastOpenedDirPath = lastOpenedDirPath;
    }

    public File getLastSaveDirPath() {
        return lastSaveDirPath;
    }

    public void setLastSaveDirPath(File lastSaveDirPath) {
        this.lastSaveDirPath = lastSaveDirPath;
    }

    public int getIndexOfCurrentGameInPgn() {
        return indexOfCurrentGameInPgn;
    }

    public void setIndexOfCurrentGameInPgn(int indexOfCurrentGameInPgn) {
        this.indexOfCurrentGameInPgn = indexOfCurrentGameInPgn;
    }


}

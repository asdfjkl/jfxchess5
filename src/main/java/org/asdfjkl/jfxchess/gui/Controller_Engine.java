package org.asdfjkl.jfxchess.gui;

import org.asdfjkl.jfxchess.lib.Board;
import org.asdfjkl.jfxchess.lib.CONSTANTS;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

public class Controller_Engine implements PropertyChangeListener {

    private final Model_JFXChess model;
    final EngineThread engineThread;
    final BlockingQueue<String> cmdQueue = new LinkedBlockingQueue<String>();
    private boolean inGoInfinite = false;
    Engine currentEngine = null;

    public Controller_Engine(Model_JFXChess model) {
        this.model = model;
        model.addListener(this);

        final AtomicReference<String> count = new AtomicReference<>();
        engineThread = new EngineThread(cmdQueue);
        engineThread.addPropertyChangeListener(this);
        engineThread.start();
    }

    public void sendCommand(String cmd) {
        if (cmd.equals("go infinite")) {
            inGoInfinite = true;
        } else {
            if (inGoInfinite && (!cmd.equals("stop"))
                    && (!cmd.equals("quit"))) {
                //try {
                //    System.out.println("controller engine: in go infinite, thus sending stop");
                    //cmdQueue.put("stop");
                //} catch (InterruptedException e) {
                //    e.printStackTrace();
                //}
            }
            inGoInfinite = false;
        }
        try {
            cmdQueue.put(cmd);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stopEngine() {
        sendCommand("stop");
        sendCommand("quit");
        do {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (engineThread.engineIsOn());
    }

    public void restartEngine(Engine activeEngine) {
        currentEngine = activeEngine;
        // It's OK to send stop and quit even if the engine process
        // inside the engine thread is not running. These commands will
        // just be consumed by the engine thread in that case.
        stopEngine();
        // Restart engine.
        sendCommand("start " + activeEngine.getPath());
        int countMs = 0;
        do {
            try {
                Thread.sleep(10);
                countMs += 10;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (!engineThread.engineIsOn() && countMs < 1500);


        // Since the engine is either internal, or we have
        // been able to set up the engine in the
        // Mode->Engines... dialog, we know it's a UCI-engine.
        // But since some engines won't accept commands prior
        // to uci, so:
        sendCommand("uci");
        // Here the engine thread will wait for uciok from engine,
        // which is according to the UCI-protocol, but meanwhile we
        // continue with pumping setoption commands into the cmdQueue.
        for (EngineOption enOpt : activeEngine.options) {
            if (enOpt.isNotDefault()) {
                sendCommand(enOpt.toUciCommand());
            }
        }
        // The engine thread requires an isready
        // before any other commands can be sent,
        // except for uci, setoption and quit.
        sendCommand("isready");
        sendCommand("ucinewgame");
        // An isready should be sent after ucinewgame.
        sendCommand("isready");

        //System.out.println("controller engine: engine should be ready");
    }

    public void setMultiPV(int n) {
        if (currentEngine != null && currentEngine.supportsMultiPV()) {
            sendCommand("setoption name MultiPV value " + n);
        }
        // (I had a problem with adding and removing PV-lines
        // in the outputview before the enginestart after edit-engines.)
        // So, in case the engine hasn't been started yet:
        engineThread.engineInfoSetPVLines(n);
    }

    public void setNrThreads(int n) {
        if (currentEngine != null && currentEngine.supportsMultiThread()) {
            sendCommand("setoption name Threads value " + n);
        }
    }

    public ActionListener incMultiPV() {
        return e -> {
            int currentMultiPv = model.getMultiPv();
            if (currentMultiPv < model.activeEngine.getMaxMultiPV() &&
                    currentMultiPv < Model_JFXChess.MAX_PV) {
                currentMultiPv++;
                model.setMultiPv(currentMultiPv);
                sendCommand("stop");
                sendCommand("setoption name MultiPV value " + currentMultiPv);
                sendCommand("go infinite");
            }
        };
    }

    public ActionListener decMultiPV() {
        return e -> {
            int currentMultiPv = model.getMultiPv();
            if (currentMultiPv > 1) {
                currentMultiPv--;
                model.setMultiPv(currentMultiPv);
                sendCommand("setoption name MultiPV value " + currentMultiPv);
            }
        };
    }

    public void sendNewPosition(String s) {
        sendCommand("stop");
        sendCommand(s);
    }

    public void uciGoMoveTime(int milliseconds) {
        sendCommand("go movetime " + milliseconds);
    }

    public void uciGoInfinite() {
        sendCommand("go infinite");
    }

    public void activateAnalysisMode() {
        //System.out.println("controller engine: activate analysis mode");
        stopEngine();
        model.activeEngine = model.selectedAnalysisEngine;
        if (model.activeEngine.supportsUciLimitStrength()) {
            model.activeEngine.setUciLimitStrength(false);
        }
        //setEngineNameAndInfoToOuptput();
        restartEngine(model.activeEngine);
        setMultiPV(model.getMultiPv());
        model.setBlockGUI(false);
        model.setMode(Model_JFXChess.MODE_ANALYSIS);
        String fen = model.getGame().getUciPositionString();
        sendNewPosition(fen);
        uciGoInfinite();
    }

    public ActionListener startAnalysisMode() {
        return e -> {
            activateAnalysisMode();
        };
    }

    public void activateEnterMovesMode() {
        System.out.println("controller engine: activateEnterMovesMode");
        stopEngine();
        model.activeEngine = model.selectedAnalysisEngine;
        if (model.activeEngine.supportsUciLimitStrength()) {
            model.activeEngine.setUciLimitStrength(false);
        }
        //setEngineNameAndInfoToOuptput();
        model.setBlockGUI(false);
        model.setMode(Model_JFXChess.MODE_ENTER_MOVES);
    }

    public ActionListener startEnterMovesMode() {
        return e -> {
            activateEnterMovesMode();
        };
    }

    private void handleNewBoardPositionModeAnalysis() {
        String fen = model.getGame().getUciPositionString();
        System.out.println("controller engine: handleNewBoardPositionModeAnalysis, sending fen");
        //sendCommand("stop");
        sendNewPosition(fen);
        uciGoInfinite();
    }

    private void handleNewBoardPositionModeGameAnalysis() {

        boolean continueAnalysis = true;

        boolean parentIsRoot = (model.getGame().getCurrentNode().getParent() == model.getGame().getRootNode());
        if (!parentIsRoot) {
            // if the current position is in the opening book,
            // we stop the analysis
            long zobrist = model.getGame().getCurrentNode().getBoard().getZobrist();
            if (model.extBook.inBook(zobrist)) {
                model.getGame().getCurrentNode().setComment("last book move");
                continueAnalysis = false;
            } else {
                // otherwise continue the analysis
                if (model.getGameAnalysisJustStarted()) {
                    model.setGameAnalysisJustStarted(false);
                } else {
                    model.getGame().goToParent();
                }
                String fen = model.getGame().getUciPositionString();
                sendNewPosition(fen);
                uciGoMoveTime(model.getGameAnalysisThinkTimeSecs() * 1000);
            }
        } else {
            continueAnalysis = false;
        }

        if (!continueAnalysis) {
            // we are at the root or found a book move
            // should fire an event
            activateEnterMovesMode();
            JOptionPane.showMessageDialog(model.mainFrameRef, "Game Analysis Finished");
        }
    }

    public void handleNewBoardPositionModePlayout() {

        String fen = model.getGame().getUciPositionString();
        sendNewPosition(fen);
        uciGoMoveTime(model.getComputerThinkTimeSecs()*1000);
    }

    public void handleNewBoardPositionModePlay() {

    }

    public void activatePlayWhiteMode() {
        // restart engine
        model.activeEngine = model.selectedPlayEngine;
        if (!(model.activeEngine instanceof BotEngine)) {
            if (model.activeEngine.supportsUciLimitStrength()) {
                model.activeEngine.setUciLimitStrength(true);
            }
        }
        // restart
        restartEngine(model.activeEngine);

        // change game mode, trigger statechange
        //setEngineNameAndInfoToOuptput();
        model.setFlipBoard(false);
        model.setHumanPlayerColor(CONSTANTS.WHITE);
        model.setMode(Model_JFXChess.MODE_PLAY_WHITE);
    }

    public void activatePlayBlackMode() {
        // restart engine
        model.activeEngine = model.selectedPlayEngine;
        if (!(model.activeEngine instanceof BotEngine)) {
            if (model.activeEngine.supportsUciLimitStrength()) {
                model.activeEngine.setUciLimitStrength(true);
            }
        }
        // restart
        restartEngine(model.activeEngine);
        //setEngineNameAndInfoToOuptput();
        model.setFlipBoard(true);
        model.setHumanPlayerColor(CONSTANTS.BLACK);
        model.setMode(Model_JFXChess.MODE_PLAY_BLACK);
    }

    public void activatePlayoutPositionMode() {
        // first change gamestate and reset engine
        model.activeEngine = model.selectedAnalysisEngine;
        if (model.activeEngine.supportsUciLimitStrength()) {
            model.activeEngine.setUciLimitStrength(false);
        }
        restartEngine(model.activeEngine);
        //setEngineNameAndInfoToOuptput();
        model.setFlipBoard(false);
        model.setMode(Model_JFXChess.MODE_PLAYOUT_POSITION);
    }

    public void activateGameAnalysisMode() {

        model.getGame().removeAllComments();
        model.getGame().removeAllVariants();
        model.getGame().removeAllAnnotations();

        model.activeEngine = model.selectedAnalysisEngine;
        if (model.activeEngine.supportsUciLimitStrength()) {
            model.activeEngine.setUciLimitStrength(false);
        }
        if (model.activeEngine.supportsMultiPV()) {
            model.activeEngine.setMultiPV(1);
        }
        //setEngineNameAndInfoToOuptput();
        restartEngine(model.activeEngine);

        model.setFlipBoard(false);
        model.getGame().goToRoot();
        model.getGame().goToLeaf();
        if (model.getGame().getCurrentNode().getBoard().isCheckmate()) {
            model.currentIsMate = true;
            model.currentMateInMoves = 0;
        }
        model.setGameAnalysisJustStarted(true);
    }

    public ActionListener startGameAnalysisMode() {
        return e -> {
            DialogGameAnalysis dlg = new DialogGameAnalysis(model.mainFrameRef);
            dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dlg.setVisible(true);

            if(dlg.isConfirmed()) {
                model.setGameAnalysisForPlayer(dlg.getSelectedPlayer());
                model.setGameAnalysisThinkTimeSecs(dlg.getSecondsPerMove());
                model.setGameAnalysisThreshold(dlg.getThreshold());
                activateGameAnalysisMode();
            }
        };
    }

    /*
    public void handleStateChangePlayWhiteOrBlack() {

        // first check if we can apply a bookmove
        long zobrist = gameModel.getGame().getCurrentNode().getBoard().getZobrist();
        boolean maxDepthReached = false;
        int currentDepth = gameModel.getGame().getCurrentNode().getDepth();
        int currentElo = gameModel.activeEngine.getUciElo();
        boolean limitElo = gameModel.activeEngine.getUciLimitStrength();
        // engine supports setting ELO
        // let's limit book knowledge according to ELO
        if(limitElo && (currentElo > 0)) {
            if(currentElo <= 1200 && currentDepth > 10) {
                maxDepthReached = true;
            }
            if(currentElo > 1200 && currentElo < 1400 && currentDepth > 10) {
                maxDepthReached = true;
            }
            if(currentElo > 1400 && currentElo < 1600 && currentDepth > 12) {
                maxDepthReached = true;
            }
            if(currentElo > 1600 && currentElo < 1800 && currentDepth > 14) {
                maxDepthReached = true;
            }
            if(currentElo > 1800 && currentElo < 2000 && currentDepth > 16) {
                maxDepthReached = true;
            }
            if(currentElo > 2000 && currentElo < 2200 && currentDepth > 18) {
                maxDepthReached = true;
            }
        }
        String bookMove = gameModel.extBook.getRandomMove(zobrist);
        if((bookMove != null) && (!maxDepthReached)) {
            // don't execute the book move immediately; we want to
            // create the illusion of the computer thinking about his move
            // inside your event handler or wherever:
            PauseTransition delay = new PauseTransition(Duration.seconds(gameModel.getComputerThinkTimeSecs()));
            delay.setOnFinished(event -> {
                handleBestMove("BESTMOVE|"+bookMove+"|"+zobrist);
            });
            delay.play();
        } else {
            String fen = gameModel.getGame().getUciPositionString();
            engineController.sendNewPosition(fen);
            engineController.uciGoMoveTime(gameModel.getComputerThinkTimeSecs()*1000);
        }
    }

    public void handleStateChangePlayoutPosition() {

        String fen = gameModel.getGame().getUciPositionString();
        engineController.sendNewPosition(fen);
        engineController.uciGoMoveTime(gameModel.getComputerThinkTimeSecs()*1000);
    }

     */

    /*
    private void addBestPv(String[] uciMoves) {
        GameNode currentNode = gameModel.getGame().getCurrentNode();

        for (String uciMove : uciMoves) {
            try {
                GameNode next = new GameNode();
                Board board = currentNode.getBoard().makeCopy();
                Move m = new Move(uciMove);
                if(!board.isLegal(m)) {
                    break;
                }
                board.apply(m);
                next.setMove(m);
                next.setBoard(board);
                next.setParent(currentNode);
                // to avoid bugs when incoherent information is
                // given/received by the engine, do not add lines that already exist
                if (currentNode.getVariations().size() > 0) {
                    String mUciChild0 = currentNode.getVariation(0).getMove().getUci();
                    if (mUciChild0.equals(uciMove)) {
                        break;
                    }
                }
                currentNode.addVariation(next);
                currentNode = next;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }*/

    // This is a method to directly after editing engines or at startup of
    // the program, show in the outputview the engineID, the Elo strength
    // (if UCILimitStrength option is true) and the empty Pv-lines of the active engine.
    // When the engine starts, the elo will be shown in the normal way
    // via the EngineThread and EngineInfo when the corresponding commands
    // are being sent to the engine.
    /*
    public void setEngineNameAndInfoToOuptput() {

        // | id (Level MAX) | zobrist  |  nps | hashfull | tbhits | current Move + depth | eval+line pv1 | .. pv2 | ...pv3 | ...pv4 | ... | ...pv64 |
        String newInfo = "|||||||||||";
        engineOutputView.setId(gameModel.activeEngine.getNameWithElo());
        engineOutputView.setText("|||||||||||");

    }*/

    /*
    public void editEngines() {

        // The following call stops the engine-process, set the ENTER_MOVES_MODE
        // and calls GameModel.triggerChangeState(). Previously the engine was
        // not stopped here.
        activateEnterMovesMode();
        DialogEngines dlg = new DialogEngines();
        ArrayList<Engine> enginesCopy = new ArrayList<>();
        for(Engine engine : gameModel.engines) {
            enginesCopy.add(engine);
        }
        int selectedIdx = gameModel.engines.indexOf(gameModel.activeEngine);
        if(selectedIdx < 0) {
            selectedIdx = 0;
        }
        boolean accepted = dlg.show(gameModel.getStageRef(), enginesCopy, selectedIdx);
        if(accepted) {
            ArrayList<Engine> engineList = new ArrayList<>(dlg.engineList);
            Engine selectedEngine = dlg.engineList.get(dlg.selectedIndex);
            gameModel.engines = engineList;
            gameModel.activeEngine = selectedEngine;
            gameModel.selectedAnalysisEngine = selectedEngine;
            // Change the engine-info in the bottom panel immediately on OK
            // being pressed. Previously it didn't change until we started
            // playing.
            setEngineNameAndInfoToOuptput();
            // // reset pv line to 1 for new engine
            // gameModel.setMultiPv(1);

            gameModel.setMultiPvChange(true); // Not important anymore.
            gameModel.triggerStateChange(); // Important.
        }
    }*/

    /*
    public void handleBestMove(String bestmove) {

        int mode = gameModel.getMode();

        if(mode == GameModel.MODE_ENTER_MOVES) {
            return;
        }

        String[] bestmoveItems = bestmove.split("\\|");

        String zobristString = bestmoveItems[bestmoveItems.length-1];
        long zobrist = Long.parseLong(zobristString);

        if(zobrist != gameModel.getGame().getCurrentNode().getBoard().getZobrist()) {
            // if this bestmove is for a different position than the current,
            // it is a relict from thread/gui synchronisation mismatch; we just dismiss it
            return;
        } else {
            // If not, this is a bestmove from either playing the engine
            // Then we need to unlock the GUI as
            // the user wants to potentially react to that
            gameModel.blockGUI = false;
        }

        if (mode == GameModel.MODE_PLAY_WHITE || mode == GameModel.MODE_PLAY_BLACK  || mode == GameModel.MODE_PLAYOUT_POSITION) {

            // todo: catch Exceptions!
            String uci = bestmoveItems[1].split(" ")[0];
            Move m = new Move(uci);
            Board b = gameModel.getGame().getCurrentNode().getBoard();
            if (b.isLegal(m)) {
                if(mode == GameModel.MODE_PLAY_WHITE && b.turn == CONSTANTS.BLACK) {
                    gameModel.getGame().applyMove(m);
                    notifyUserDuringPlay();
                    gameModel.triggerStateChange();
                }
                if(mode == GameModel.MODE_PLAY_BLACK && b.turn == CONSTANTS.WHITE) {
                    gameModel.getGame().applyMove(m);
                    notifyUserDuringPlay();
                    gameModel.triggerStateChange();
                }
                if(mode == GameModel.MODE_PLAYOUT_POSITION) {
                    gameModel.getGame().applyMove(m);
                    gameModel.triggerStateChange();
                }
            }
        }

        if(mode == GameModel.MODE_GAME_ANALYSIS) {

            // first update information for current node
            gameModel.childBestPv = gameModel.currentBestPv;
            gameModel.childBestEval = gameModel.currentBestEval;
            gameModel.childIsMate = gameModel.currentIsMate;
            gameModel.childMateInMoves = gameModel.currentMateInMoves;

            gameModel.currentBestPv = bestmoveItems[3];
            gameModel.currentBestEval = Integer.parseInt(bestmoveItems[2]);
            gameModel.currentIsMate = bestmoveItems[4].equals("true");
            gameModel.currentMateInMoves = Integer.parseInt(bestmoveItems[5]);

            // double check, since some engines misreport / report not quickly enough
            if(gameModel.getGame().getCurrentNode().getBoard().isCheckmate()){
                gameModel.currentIsMate = true;
            }

            // ignore leafs (game ended here)
            if(!gameModel.getGame().getCurrentNode().isLeaf()) {

                // completely skip analysis for black or white, if
                // that option was chosen
                boolean turn = gameModel.getGame().getCurrentNode().getBoard().turn;
                if ((gameModel.getGameAnalysisForPlayer() == GameModel.BOTH_PLAYERS)
                        || (gameModel.getGameAnalysisForPlayer() == CONSTANTS.IWHITE && turn == CONSTANTS.WHITE)
                        || (gameModel.getGameAnalysisForPlayer() == CONSTANTS.IBLACK && turn == CONSTANTS.BLACK)) {

                    int centiPawnThreshold = (int) (gameModel.getGameAnalysisThreshold() * 100.0);
                    // first case if there was simply a better move; i.e. no checkmate overseen or
                    // moved into a checkmate
                    if (!gameModel.currentIsMate && !gameModel.childIsMate) {
                        boolean wMistake = turn == CONSTANTS.WHITE && ((gameModel.currentBestEval - gameModel.childBestEval) >= centiPawnThreshold);
                        boolean bMistake = turn == CONSTANTS.BLACK && ((gameModel.currentBestEval - gameModel.childBestEval) <= -(centiPawnThreshold));

                        if (wMistake || bMistake) {
                            String uci = bestmoveItems[1].split(" ")[0];
                            String nextMove = gameModel.getGame().getCurrentNode().getVariation(0).getMove().getUci();
                            String[] pvMoves = gameModel.currentBestPv.split(" ");
                            // if the bestmove returned by the engine is different
                            // from the best suggested pv line, it means that e.g. the
                            // engine took a book move, but did not give a pv evaluation
                            // or there was some major async error between engine and gui
                            // in such a case, do not add the best pv line, as it is probably
                            // not a valid pv line for the current node
                            // we also do not want to add the same line as the child
                            if (!uci.equals(nextMove) && pvMoves.length > 0 && pvMoves[0].equals(uci)) {

                                addBestPv(pvMoves);

                                NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
                                DecimalFormat decim = (DecimalFormat) nf;
                                decim.applyPattern("0.00");
                                String sCurrentBest = decim.format(gameModel.currentBestEval / 100.0);
                                String sChildBest = decim.format(gameModel.childBestEval / 100.0);

                                ArrayList<GameNode> vars = gameModel.getGame().getCurrentNode().getVariations();
                                if (vars != null && vars.size() > 1) {
                                    GameNode child0 = gameModel.getGame().getCurrentNode().getVariation(0);
                                    child0.setComment(sChildBest);
                                    GameNode child1 = gameModel.getGame().getCurrentNode().getVariation(1);
                                    child1.setComment(sCurrentBest);
                                }
                            }
                        }
                    }

                    if (gameModel.currentIsMate && !gameModel.childIsMate) {
                        // the current player missed a mate
                        String[] pvMoves = gameModel.currentBestPv.split(" ");
                        addBestPv(pvMoves);

                        NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
                        DecimalFormat decim = (DecimalFormat) nf;
                        decim.applyPattern("0.00");
                        String sChildBest = decim.format(gameModel.childBestEval / 100.0);

                        String sCurrentBest = "";
                        if (turn == CONSTANTS.WHITE) {
                            sCurrentBest = "#" + (Math.abs(gameModel.currentMateInMoves));
                        } else {
                            sCurrentBest = "#-" + (Math.abs(gameModel.currentMateInMoves));
                        }

                        ArrayList<GameNode> vars = gameModel.getGame().getCurrentNode().getVariations();
                        if (vars != null && vars.size() > 1) {
                            GameNode child0 = gameModel.getGame().getCurrentNode().getVariation(0);
                            child0.setComment(sChildBest);
                            GameNode child1 = gameModel.getGame().getCurrentNode().getVariation(1);
                            child1.setComment(sCurrentBest);
                        }
                    }

                    if (!gameModel.currentIsMate && gameModel.childIsMate) {
                        // the current player  moved into a mate
                        String[] pvMoves = gameModel.currentBestPv.split(" ");
                        addBestPv(pvMoves);

                        NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
                        DecimalFormat decim = (DecimalFormat) nf;
                        decim.applyPattern("0.00");
                        String sCurrentBest = decim.format(gameModel.currentBestEval / 100.0);

                        String sChildBest = "";
                        if (turn == CONSTANTS.WHITE) {
                            sChildBest = "#-" + (Math.abs(gameModel.childMateInMoves));
                        } else {
                            sChildBest = "#" + (Math.abs(gameModel.childMateInMoves));
                        }

                        ArrayList<GameNode> vars = gameModel.getGame().getCurrentNode().getVariations();
                        if (vars != null && vars.size() > 1) {
                            GameNode child0 = gameModel.getGame().getCurrentNode().getVariation(0);
                            child0.setComment(sChildBest);
                            GameNode child1 = gameModel.getGame().getCurrentNode().getVariation(1);
                            child1.setComment(sCurrentBest);
                        }
                    }

                    if (gameModel.currentIsMate && gameModel.childIsMate) {
                        // the current player had a mate, but instead of executing it, he moved into a mate,
                        // but we also want to skip the situation where the board position is checkmate
                        if ((gameModel.currentMateInMoves >= 0 && gameModel.childMateInMoves >= 0) &&
                                gameModel.childMateInMoves != 0) {

                            String[] pvMoves = gameModel.currentBestPv.split(" ");
                            addBestPv(pvMoves);

                            String sCurrentBest = "";
                            String sChildBest = "";
                            if (turn == CONSTANTS.WHITE) {
                                sCurrentBest = "#" + (Math.abs(gameModel.currentMateInMoves));
                                sChildBest = "#-" + (Math.abs(gameModel.childMateInMoves));
                            } else {
                                sCurrentBest = "#-" + (Math.abs(gameModel.currentMateInMoves));
                                sChildBest = "#" + (Math.abs(gameModel.childMateInMoves));
                            }

                            ArrayList<GameNode> vars = gameModel.getGame().getCurrentNode().getVariations();
                            if (vars != null && vars.size() > 1) {
                                GameNode child0 = gameModel.getGame().getCurrentNode().getVariation(0);
                                child0.setComment(sChildBest);
                                GameNode child1 = gameModel.getGame().getCurrentNode().getVariation(1);
                                child1.setComment(sCurrentBest);
                            }
                        }
                    }
                }
            }
            gameModel.getGame().setTreeWasChanged(true);
            gameModel.triggerStateChange();
        }

    }

     */

    /*
    @Override
    public void stateChange() {
        int mode = gameModel.getMode();
        Board board = gameModel.getGame().getCurrentNode().getBoard();
        boolean turn = board.turn;

        boolean isCheckmate = board.isCheckmate();
        boolean isStalemate = board.isStalemate();
        boolean isThreefoldRepetition = gameModel.getGame().isThreefoldRepetition();
        boolean isInsufficientMaterial = gameModel.getGame().isInsufficientMaterial();

        boolean abort = false;

        // first we check if the game is finished due to checkmate, stalemate, three-fold repetition
        // or insufficient material
        // if that is the case
        //      we check if we play against the computer or are in analysis or playoutposition mode
        //         if so, we are going to abort the game and switch to entermovesmode
        //      then we check if we play against the computer
        //         if so, we are going to inform the user

        // if we change from e.g. play white to enter moves, the state change would trigger
        // the notification again in enter moves mode after the state change. thus,
        // also check if
        if ((isCheckmate || isStalemate || isThreefoldRepetition || isInsufficientMaterial)) {
            if (mode == GameModel.MODE_PLAY_WHITE || mode == GameModel.MODE_PLAY_BLACK || mode == GameModel.MODE_PLAYOUT_POSITION) {
                abort = true;
            }

            if (mode == GameModel.MODE_PLAY_WHITE || mode == GameModel.MODE_PLAY_BLACK) {

                String message = "";
                if (isCheckmate) {
                    message = "     Checkmate.     ";
                }
                if (isStalemate) {
                    message = "     Stalemate.     ";
                }
                if (isThreefoldRepetition) {
                    message = "Draw (Threefold Repetition)";
                }
                if (isInsufficientMaterial) {
                    message = "Draw (Insufficient material for checkmate)";
                }
                String finalMessage = message;
                Platform.runLater(() -> {
                    DialogSimpleAlert dlgAlert = new DialogSimpleAlert(
                            gameModel.getStageRef(), Alert.AlertType.INFORMATION,
                            "Game Finished", finalMessage);
                    dlgAlert.showAndWait();
                });
            }
        }

        if(abort) {
            activateEnterMovesMode();
        } else {
            if (mode == GameModel.MODE_ANALYSIS) {
                handleStateChangeAnalysis();
            }
            if (mode == GameModel.MODE_GAME_ANALYSIS) {
                handleStateChangeGameAnalysis();
            }
            if (mode == GameModel.MODE_PLAYOUT_POSITION) {
                handleStateChangePlayoutPosition();
            }
            if ((mode == GameModel.MODE_PLAY_WHITE || mode == GameModel.MODE_PLAY_BLACK)
                    && turn != gameModel.getHumanPlayerColor()) {
                handleStateChangePlayWhiteOrBlack();
            }
        }
    }

     */

    public void handleNewEngineInfo(String s) {
        //System.out.println("controller: received info from thread");
        // String s = evt.getNewValue().toString();
        // we show info only during analysis, not when playing
        // against the bots/engine
        if (s.startsWith("INFO")) {
            if ((model.getMode() != Model_JFXChess.MODE_PLAY_BLACK) &&
                    (model.getMode() != Model_JFXChess.MODE_PLAY_WHITE)) {
                model.setCurrentEngineInfo(s);
            }
        }
        // if we get info from the engine but currently play against it,
        // just show some dummy info
        if ((model.getMode() == Model_JFXChess.MODE_PLAY_WHITE) || (model.getMode() == Model_JFXChess.MODE_PLAY_BLACK)) {
            if (model.getMode() == Model_JFXChess.MODE_PLAY_WHITE) {
                if (model.getGame().getCurrentNode().getBoard().turn == CONSTANTS.WHITE) {
                    model.setCurrentEngineInfo("|||||||Your turn - White to move");
                } else {
                    model.setCurrentEngineInfo("|||||||...thinking...");
                }
            }
            if (model.getMode() == Model_JFXChess.MODE_PLAY_BLACK) {
                if (model.getGame().getCurrentNode().getBoard().turn == CONSTANTS.BLACK) {
                    model.setCurrentEngineInfo("|||||||Your turn - Black to move");
                } else {
                    model.setCurrentEngineInfo("|||||||...thinking...");
                }
            }
        }
        if (s.startsWith("BESTMOVE")) {
            // handleBestMove(s);
        }
    }

    public void handleNewBoardPosition() {
        int mode = model.getMode();
        Board board = model.getGame().getCurrentNode().getBoard();
        boolean turn = board.turn;

        boolean isCheckmate = board.isCheckmate();
        boolean isStalemate = board.isStalemate();
        boolean isThreefoldRepetition = model.getGame().isThreefoldRepetition();
        boolean isInsufficientMaterial = model.getGame().isInsufficientMaterial();

        boolean abort = false;

        // first we check if the game is finished due to checkmate, stalemate, three-fold repetition
        // or insufficient material
        // if that is the case
        //      we check if we play against the computer or are in MODE_PLAYOUT_POSITION
        //         if so, we are going to abort the game and switch to MODE_ENTER_MOVES
        //      then we check if we play against the computer
        //         if so, we are going to inform the user

        if ((isCheckmate || isStalemate || isThreefoldRepetition || isInsufficientMaterial)) {
            if (mode == Model_JFXChess.MODE_PLAY_WHITE || mode == Model_JFXChess.MODE_PLAY_BLACK || mode == Model_JFXChess.MODE_PLAYOUT_POSITION) {
                abort = true;
            }
            if (mode == Model_JFXChess.MODE_PLAY_WHITE || mode == Model_JFXChess.MODE_PLAY_BLACK) {
                String message = "";
                if (isCheckmate) {
                    message = "Checkmate";
                }
                if (isStalemate) {
                    message = "Stalemate";
                }
                if (isThreefoldRepetition) {
                    message = "Draw (Threefold Repetition)";
                }
                if (isInsufficientMaterial) {
                    message = "Draw (Insufficient material for checkmate)";
                }
                JOptionPane.showMessageDialog(model.mainFrameRef, message);
            }
        }

        if(abort) {
            activateEnterMovesMode();
        } else {
            if (mode == Model_JFXChess.MODE_ANALYSIS) {
                handleNewBoardPositionModeAnalysis();
            }
            if (mode == Model_JFXChess.MODE_GAME_ANALYSIS) {
                handleNewBoardPositionModeGameAnalysis();
            }
            if (mode == Model_JFXChess.MODE_PLAYOUT_POSITION) {
                handleNewBoardPositionModePlayout();
            }
            if ((mode == Model_JFXChess.MODE_PLAY_WHITE || mode == Model_JFXChess.MODE_PLAY_BLACK)
                    && turn != model.getHumanPlayerColor()) {
                handleNewBoardPositionModePlay();
            }
        }
    }


    // this is to get the engine info thread safe as a String
    // via the evt new value
    // but also to listen to position changes that occur in the model
    @Override
    public void propertyChange(PropertyChangeEvent evt) {

        if (evt.getPropertyName().equals("engineInfoFromThread")) {
            handleNewEngineInfo((String) evt.getNewValue());
        } else {
            //System.out.println("controller engine: pcs received "+evt.getPropertyName());
        }
        if (evt.getPropertyName().equals("currentGameNodeChanged")) {
        //|| evt.getPropertyName().equals("gameTreeChanged")) {
            handleNewBoardPosition();
        }
        if (evt.getPropertyName().equals("treeChanged")) {
            //|| evt.getPropertyName().equals("gameTreeChanged")) {
            handleNewBoardPosition();
        }

    }
}

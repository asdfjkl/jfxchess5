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

//import javafx.beans.property.SimpleStringProperty;
// import javafx.beans.property.StringProperty;

import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class EngineThread extends Thread {

    static final Pattern REG_MOVES = Pattern.compile("\\s[a-z]\\d[a-z]\\d([a-z]{0,1})");
    static final Pattern REG_BESTMOVE = Pattern.compile("bestmove\\s([a-z]\\d[a-z]\\d[a-z]{0,1})");
    static final Pattern REG_STRENGTH = Pattern.compile("UCI_Elo value \\d+");

    // private final StringProperty stringProperty;

    private final BlockingQueue<String> cmdQueue;
    Process engineProcess;
    BufferedReader engineOutput;
    BufferedWriter engineInput;
    private volatile boolean running = true;
    private long lastInfoUpdate = 0;
    private long lastBestmoveUpdate = 0;

    private final EngineInfo engineInfo;

    private boolean readyok = false;
    private boolean uciok = false;

    private String sharedString;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    public EngineThread(BlockingQueue<String> cmdQueue) {
        this.engineInfo = new EngineInfo();
        this.cmdQueue = cmdQueue;
        //stringProperty = new SimpleStringProperty(this, "String", "");
        lastInfoUpdate = System.currentTimeMillis();
        lastBestmoveUpdate = System.currentTimeMillis();
        setDaemon(true);
    }

    public synchronized boolean engineIsOn() {
        //System.out.println("engine Process != null: " + (engineProcess != null));
        if(engineProcess != null) {
        //System.out.println("is alive: "+engineProcess.isAlive());
        }
        return (engineProcess != null && engineProcess.isAlive());
    }

    public void setSharedString(String newValue) {
        String oldValue = sharedString;
        sharedString = newValue;
        support.firePropertyChange("engineInfoFromThread", oldValue, newValue);
    }

    public String getSharedString() {
        return sharedString;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    /*
    // bestmove result comes from the engine via this method.
    public String getString() {
        return stringProperty.get();
    }

    public StringProperty stringProperty() {
        return stringProperty;
    }
    */

    public void engineInfoSetPVLines(int n)
    {
        engineInfo.nrPvLines = n;
    }

    private void take_write_and_flush(String cmd) {
        try {
            cmdQueue.take();
            System.out.println("TO ENGINE>>"+cmd);
            engineInput.write(cmd + "\n");
            engineInput.flush();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace(System.out);
        }
    }

    @Override
    public void run() {
        int savedElo = -1;
        while (running) {
            //System.out.println("RUNNING");
            // Set the thread to loop at about 1000 times per second.
            // It Keeps CPU-load down and is probably more than enough.
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (this.isInterrupted()) {
                System.out.println("INTERRUPTED");
                // here: delete process if it exists
                if (engineIsOn()) {
                    try {
                        // Try to close down the engine the normal way.
                        engineInput.write("stop\n");
                        engineInput.flush();
                        engineInput.write("quit\n");
                        engineInput.flush();
                        boolean finished = engineProcess.waitFor(500, TimeUnit.MILLISECONDS);
                        if (!finished) {
                            System.out.println("DESTROY");
                            engineProcess.destroy();
                        }
                    } catch(IOException | InterruptedException e) {
                        e.printStackTrace(System.out);
                    }
                }
                // Stop this thread.
                running = false;
                continue;
            }
            // Process engine output
            if (engineOutput != null) {
                //System.out.println("OUTPUT");
                int linesRead = 0;
                try {
                    while (engineOutput.ready() && linesRead < 100) {
                        String line = engineOutput.readLine();
                        //System.out.println("FROM ENGINE<<"+line);

                        if (line.contains("readyok")) {
                            readyok = true;
                            continue;
                        }
                        if (line.contains("uciok")) {
                            uciok = true;
                            continue;
                        }
                        if (!line.isEmpty()) {
                            // todo: instead of directly setting bestmove,
                            // try updating engine info
                            if (line.startsWith("bestmove")) {
                                engineInfo.bestmove = "BESTMOVE|"
                                        + line.substring(9)
                                        +"|"+engineInfo.score.get(0)
                                        +"|"+String.join(" ", engineInfo.pvList)
                                        +"|"+engineInfo.seesMate.get(0)
                                        +"|"+engineInfo.mate.get(0)
                                        +"|"+engineInfo.zobrist;
                                linesRead++;
                                continue;
                            }
                            // Update engine info with other ouput-lines
                            engineInfo.update(line);
                            //System.out.println("thread: engine line: "+line);
                        }
                        linesRead++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // send update
            long currentMs = System.currentTimeMillis();
            if ((currentMs - lastInfoUpdate) > 100) {
                //setSharedString("INFO " + engineInfo.toString());
                setSharedString("INFO " + engineInfo.toHtml());
                //System.out.println("engine thread: set shared string 1 " + engineInfo.toString() );
                lastInfoUpdate = currentMs;
            }
            // we need to constantly send "bestmove". If we only send it once,
            // and the user keeps flooding the GUI with events, i.e. by frequently resizing
            // the window or other inputs, the GUI might skip to handle (the only one)
            // bestmove info. Instead, the GUI will receive bestmove frequently
            // but ignore the info, if already processed.
            if ((currentMs - lastBestmoveUpdate) > 800) {
                setSharedString(engineInfo.bestmove);
                //System.out.println("engine thread: set shared string 2 " + engineInfo.bestmove );
                lastBestmoveUpdate = currentMs;
            }
            if (!engineIsOn()) {
                //System.out.println("ENGINE NOT RUNNING");
                // engine not running
                if (!cmdQueue.isEmpty()) {
                    try {
                        // Here we dispose of (or consume) the next command
                        // sent to a dead engine, or start a new engine process
                        // if we find a start command.
                        // This makes it OK for the engineController to
                        // always send stop and quit first, when restarting
                        // an engine, without first checking if the engine is on.
                        String cmd = (String)cmdQueue.take();
                        System.out.println("thread: cmd !engineIsOn "+cmd);
                        if (cmd.startsWith("start")) {
                            // reset engine info if we start
                            engineInfo.clear();
                            String engineCmd = cmd.substring(6);
                            try {
                                this.engineProcess = new ProcessBuilder(engineCmd).start();
                                this.engineInput = new BufferedWriter(new OutputStreamWriter(engineProcess.getOutputStream()));
                                this.engineOutput = new BufferedReader(new InputStreamReader(engineProcess.getInputStream()));
                                // Reset some "state" variables.
                                readyok = false;
                                uciok = false;
                            } catch (IOException e) {
                                e.printStackTrace(System.out);
                            }
                            this.engineInfo.strength = -1;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace(System.out);
                    }
                }
                continue;
            }
            //System.out.println("ENGINE RUNNING?!");
            // When we have come this far in the while-loop
            // we know that the process is alive -> engine is running.
            // The commands uci, quit, setoption and isready are
            // treated in special ways. We are not expecting any
            // other commands from the engine controller until isready
            // has been sent at least once and we have received readyok
            // from the engine.
            if (!cmdQueue.isEmpty()) {
                // The problem of sending stop first if we are in "go infinite"-mode
                // is handled in EngineController.
                // Don't remove from queue until we know which command it is.
                // It could be some other command just waiting for us to
                // pass the readyok check below, now or in the next loop, maybe.
                String cmd = (String) cmdQueue.peek();
                System.out.println("process alive, peeking cmd "+cmd);
                if (cmd == null) {
                    // What to do here?
                    // Better luck next loop!
                    continue;
                }

                // I noticed that even after stop, quit and waiting for
                // the process to die, the process.isAlive() method took time
                // before it answered false. So when restarting, the start
                // command was being sent to the dead engine as a normal
                // command below. The following statement prevents that.
                if (cmd.startsWith("start")) {
                    continue;
                }

                // The command uci must be sent immediately after startup.
                // Some engines will not report readyok on isready directly
                // after startup (like e.g. arasan). thus we require of the
                // engine controller to always send 'uci' after starting an
                // engine process by the start command.
                if (cmd.equals("uci")) {
                    take_write_and_flush(cmd);
                    // Set the uciok flag to false.
                    // This thread won't send any other commands
                    // until uciok has been received.
                    uciok = false;
                    continue;
                }
                if (!uciok) {
                    // Go no further until we have received uciok.
                    continue;
                }
                // When we have reached this point in the while-loop
                // we know that the engine is ready to receive other
                // commands than uci. We could always be ready to send
                // the quit command if it appears here, (even before isready).
                if (cmd.equals("quit")) {
                    // reset engine info if we quit
                    engineInfo.clear();
                    // In case elo has been sent but not limit-strength
                    // we must clear savedElo here as well
                    savedElo = -1;
                    take_write_and_flush(cmd);
                    try {
                        System.out.println("thread: quit send, waiting");
                        // and wait for engine process to die.
                        boolean finished = engineProcess.waitFor(500, TimeUnit.MILLISECONDS);
                        System.out.println("thread: quit send, finished");
                        if (!finished) {
                            System.out.println("thread: quit send fail, destroying");
                            engineProcess.destroy();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace(System.out);
                    }
                    continue;
                }

                // We can (and maybe should, according to the UCI-protocol),
                // be ready to send the setoption commands directly after uciok
                // has been received.
                if (cmd.startsWith("setoption")) {
                    if (cmd.startsWith("setoption name UCI_Elo")) {
                        // We must save the elo value if elo is set before limit-strength
                        // The value is only valid in connection with limitedStrength == true
                        Matcher matchExpressionStrength = REG_STRENGTH.matcher(cmd);
                        if (matchExpressionStrength.find()) {
                            savedElo = Integer.parseInt(matchExpressionStrength.group().substring(14));
                        }
                        if (engineInfo.limitedStrength == true) {
                            engineInfo.strength = savedElo;
                            savedElo = -1;
                        }
                    }
                    if (cmd.startsWith(("setoption name UCI_LimitStrength value"))) {
                        String isActive = cmd.substring(38).strip();
                        if (isActive.equals("true")) {
                            engineInfo.limitedStrength = true;
                            // If there is a saved elo value show it in the outputview
                            // (in that case elo has been set before limitedStrength).
                            if (savedElo > -1) {
                                engineInfo.strength = savedElo;
                            }
                        } else {
                            engineInfo.limitedStrength = false;
                        }
                        // Clear savedElo (for next start of the engine)
                        savedElo = -1; // also done at quit, so maybe it's redundant.
                    }
                    if (cmd.startsWith("setoption name MultiPV value")) {
                        engineInfo.nrPvLines = Integer.parseInt(cmd.substring(29));
                        //System.out.println("MultiPV value set to: " + cmd);
                    }
                    //cmd = "stop";
                    take_write_and_flush(cmd);
                    continue;
                }

                // Always be ready to send stop
                if (cmd.equals("stop")) {
                    take_write_and_flush(cmd);
                    continue;
                }

                if (cmd.equals("isready")) {
                    // We wish to be able to send isready more than
                    // once during the lifetime of an engineprocess,
                    // so the next line is important.
                    readyok = false;
                    take_write_and_flush(cmd);
                    continue;
                }

                if (!readyok) {
                    // Wait for readyok before proceeding to
                    // the sending of other commands below.
                    continue;
                }

                // if the command is "position fen moves", first count the
                // number of moves so far to generate move numbers in engine info
                // todo: needed???
                if (cmd.startsWith("position")) {
                    Matcher matchMoves = REG_MOVES.matcher(cmd);
                    int cnt = 0;
                    while (matchMoves.find()) {
                        cnt++;
                    }
                    if (cnt > 0) {
                        engineInfo.halfmoves = cnt;
                    }
                }
                if ((cmd.startsWith("position fen")) || (cmd.startsWith("position startpos"))) {
                    engineInfo.setFen(cmd);
                }
                // All other commands can be sent as they are,
                // without any action.

                take_write_and_flush(cmd);

            }
        }
    }

    public void terminate() {
        running = false;
    }

}
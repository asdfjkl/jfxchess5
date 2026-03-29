package org.asdfjkl.jfxchess.gui;

import org.asdfjkl.jfxchess.lib.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;

public class Controller_Pgn {

    private ArrayList<PgnGameInfo> searchResults;
    private PgnReader reader;
    String filename;

    private Model_JFXChess model;

    public Controller_Pgn(Model_JFXChess model) {
        reader = new PgnReader();
        this.model = model;
    }

    public int getNrGames() { return model.getPgnDatabase().size(); }

    public Game loadGame(int index) {

        ArrayList<PgnGameInfo> entries = model.getPgnDatabase();
        OptimizedRandomAccessFile raf = null;
        Game g = new Game();
        try {
            raf = new OptimizedRandomAccessFile(filename, "r");
            if(index < entries.size()) {
                raf.seek(entries.get(index).getOffset());
                g = reader.readGame(raf);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return g;
    }

    public ActionListener openFile() {
        return e -> {
            openAndScanPgn();
        };
    }

    private void openAndScanPgn() {

        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter pgnFilter = new FileNameExtensionFilter("PGN Files (*.pgn)", "pgn");
        chooser.setFileFilter(pgnFilter);

        chooser.setAcceptAllFileFilterUsed(true);
        // todo: set to last openend file, save in model
        //chooser.setCurrentDirectory(new File("."));
        try {
            int result = chooser.showOpenDialog(model.mainFrameRef);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                if (selectedFile != null &&
                        selectedFile.exists() &&
                        selectedFile.canRead()
                ) {
                    String pgnFilename = selectedFile.getAbsolutePath();
                    PgnScanWorker worker = new PgnScanWorker(pgnFilename,
                            reader,
                            entriesFromWorker -> { onScanPgnCompletion(pgnFilename, entriesFromWorker); }
                    );
                    DialogProgress dlgProgress = new DialogProgress(model.mainFrameRef, worker, "Scanning PGN");
                    worker.execute();
                    dlgProgress.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Error reading file.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onScanPgnCompletion(String pgnFilename,
                                     ArrayList<PgnGameInfo> entriesFromWorker) {
        model.setPgnDatabase(entriesFromWorker);
        model.setFnPgnDatabase(pgnFilename);
        if(model.getPgnDatabase().size() == 1) {
            // read game from file and show, don't display database dialog
            OptimizedRandomAccessFile raf = null;
            PgnReader reader = new PgnReader();
            try {
                raf = new OptimizedRandomAccessFile(pgnFilename, "r");
                Game g = reader.readGame(raf);
                model.setGame(g);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(model.getPgnDatabase().size() > 1) {
            DialogDatabase dlgDatabase = new DialogDatabase(model.mainFrameRef, model,this);
            dlgDatabase.setVisible(true);
            if(dlgDatabase.isConfirmed()) {
                PgnGameInfo gameInfo = dlgDatabase.getSelectedGame();
                OptimizedRandomAccessFile raf = null;
                PgnReader reader = new PgnReader();
                try {
                    raf = new OptimizedRandomAccessFile(pgnFilename, "r");
                    raf.seek(gameInfo.getOffset());
                    Game g = reader.readGame(raf);
                    model.setGame(g);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public ActionListener showDatabase() {
        return e -> {
            DialogDatabase dlgDatabase = new DialogDatabase(model.mainFrameRef, model, this);
            dlgDatabase.setVisible(true);
        };
    }

    public void deleteGame(String pgnFilename, long startOffset, long nextGameOffset) {
        reader.deleteGame(pgnFilename, startOffset, nextGameOffset);
    }

    public void deleteGame(String pgnFilename, long startOffset) {
        reader.deleteGame(pgnFilename, startOffset);
    }

    public ActionListener saveGame() {

        return e -> {

            // first check, which options are actually possible
            // if we haven't opened a pgn before, there can be
            // no replacement/append to the current database
            boolean replaceAllowed = false;
            boolean appendToCurrentAllowed = false;

            String fnPgnDatabase = model.getFnPgnDatabase();
            long fileSize = 0;
            if(fnPgnDatabase != null) {
                File f = new File(fnPgnDatabase);
                if(f.exists() && !f.isDirectory()) {
                    appendToCurrentAllowed = true;
                    fileSize = f.length();
                }
            }
            long offset1 = -1;
            int gameIdxCurrent = model.currentPgnDatabaseIdx;
            if(gameIdxCurrent >= 0 && gameIdxCurrent < model.getPgnDatabase().size()) {
                offset1 = model.getPgnDatabase().get(gameIdxCurrent).getOffset();
                replaceAllowed = true;
            }
            long offset2 = -1;
            if(gameIdxCurrent + 1 < model.getPgnDatabase().size()) {
                offset2 = model.getPgnDatabase().get(gameIdxCurrent+1).getOffset();
            } else {
                // no subsequent game, essentially append - take end of file size
                offset2 = fileSize;
            }


            // show dialog
            DialogSave dlgSave = new DialogSave(model.mainFrameRef, appendToCurrentAllowed, replaceAllowed);
            dlgSave.setVisible(true);
            int res = dlgSave.getResult();
            if (res != DialogSave.CANCEL) {
                if (res == DialogSave.SAVE_NEW) {
                    saveAsNewPGN();
                }
                if (res == DialogSave.APPEND_CURRENT) {
                    appendToCurrentPGN();
                }
                if (res == DialogSave.APPEND_OTHER) {
                    appendToOtherPGN();
                }
                if (res == DialogSave.REPLACE_CURRENT) {
                    PgnPrinter printer = new PgnPrinter();
                    String currentAsPgn = printer.printGame(model.getGame());
                    // todo: potentially long-running, put in thread
                    try {
                        replaceTextInFile(fnPgnDatabase, currentAsPgn, offset1, offset2);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null,
                                "Error replacing game in PGN.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        };
    }

    private void saveAsNewPGN() {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter pgnFilter = new FileNameExtensionFilter("PGN Files (*.pgn)", "pgn");
        chooser.setFileFilter(pgnFilter);

        chooser.setAcceptAllFileFilterUsed(true);

        try {
            int result = chooser.showSaveDialog(model.mainFrameRef);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                if (selectedFile != null &&
                        selectedFile.exists() &&
                        selectedFile.canRead()
                ) {
                    String pgnFilename = selectedFile.getAbsolutePath();
                    PgnPrinter printer = new PgnPrinter();
                    printer.writeGame(model.getGame(), pgnFilename);
                    // todo: now reload this as the current database
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Error saving PGN.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void appendToOtherPGN() {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter pgnFilter = new FileNameExtensionFilter("PGN Files (*.pgn)", "pgn");
        chooser.setFileFilter(pgnFilter);

        chooser.setAcceptAllFileFilterUsed(true);

        try {
            int result = chooser.showSaveDialog(model.mainFrameRef);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                if (selectedFile != null &&
                        selectedFile.exists() &&
                        selectedFile.canRead()
                ) {
                    BufferedWriter writer = null;
                    try {
                        PgnPrinter pgnPrinter = new PgnPrinter();
                        writer = new BufferedWriter(new FileWriter(selectedFile, true));
                        String sGame = pgnPrinter.printGame(model.getGame());
                        writer.write(0xa); // 0xa = LF = \n
                        writer.write(0xa); // 0xa = LF = \n
                        writer.write(sGame);
                        writer.write(0xa); // 0xa = LF = \n
                        writer.write(0xa); // 0xa = LF = \n
                        writer.close();
                    } catch(IOException e) {
                        System.err.println(e);
                    } finally {
                        if(writer != null) {
                            try {
                                writer.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    // todo: now reload this as the current database
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Error saving PGN.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void appendToCurrentPGN() {

        Game g = model.getGame();
        BufferedWriter writer = null;
        try {
            File file = new File(filename);
            long offset = file.length() + 2;
            PgnPrinter pgnPrinter = new PgnPrinter();
            writer = new BufferedWriter(new FileWriter(filename, true));
            String sGame = pgnPrinter.printGame(g);
            writer.write(0xa); // 0xa = LF = \n
            writer.write(0xa); // 0xa = LF = \n
            writer.write(sGame);
            writer.write(0xa); // 0xa = LF = \n
            writer.write(0xa); // 0xa = LF = \n
            PgnGameInfo newEntry = new PgnGameInfo();
            newEntry.setWhite(g.getHeader("White"));
            newEntry.setBlack(g.getHeader("Black"));
            newEntry.setDate(g.getHeader("Date"));
            newEntry.setOffset(offset);
            newEntry.setEvent(g.getHeader("Event"));
            newEntry.setEco(g.getHeader("Eco"));
            newEntry.setResult(g.getHeader("Result"));
            newEntry.setRound(g.getHeader("Round"));
            newEntry.setSite(g.getHeader("Site"));
            newEntry.setIndex(model.getPgnDatabase().size()+1);
            model.getPgnDatabase().add(newEntry);
            writer.close();
        } catch(IOException e) {
            System.err.println(e);
        } finally {
            if(writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    System.err.println(e);
                }
            }
        }
    }

    public void replaceTextInFile(String filePath, String text, long offset1, long offset2)
            throws IOException {

        Path originalPath = Paths.get(filePath);

        if (!Files.exists(originalPath)) {
            throw new FileNotFoundException("File not found: " + filePath);
        }

        long fileSize = Files.size(originalPath);

        // Validate offsets
        if (offset1 < 0 || offset2 < 0 || offset1 > offset2 || offset2 > fileSize) {
            throw new IllegalArgumentException(
                    "Invalid offsets: offset1=" + offset1 + ", offset2=" + offset2
            );
        }

        Path tempPath = Files.createTempFile(
                originalPath.getParent(),
                "replace_tmp_",
                ".tmp"
        );

        try (
                InputStream in = Files.newInputStream(originalPath);
                OutputStream out = Files.newOutputStream(tempPath, StandardOpenOption.WRITE)
        ) {
            byte[] buffer = new byte[8192];
            long bytesCopied = 0;

            // 1. Copy [0, offset1)
            while (bytesCopied < offset1) {
                int toRead = (int) Math.min(buffer.length, offset1 - bytesCopied);
                int read = in.read(buffer, 0, toRead);
                if (read == -1) break;

                out.write(buffer, 0, read);
                bytesCopied += read;
            }

            // 2. Skip [offset1, offset2)
            long bytesToSkip = offset2 - offset1;
            while (bytesToSkip > 0) {
                long skipped = in.skip(bytesToSkip);
                if (skipped <= 0) {
                    // fallback if skip fails
                    int read = in.read(buffer, 0, (int)Math.min(buffer.length, bytesToSkip));
                    if (read == -1) break;
                    skipped = read;
                }
                bytesToSkip -= skipped;
            }

            // 3. Insert new content
            String insertion = "\n\n" + text + "\n\n";
            out.write(insertion.getBytes(StandardCharsets.UTF_8));

            // 4. Copy remainder [offset2, end)
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }

        // Atomic replace
        Files.move(
                tempPath,
                originalPath,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
        );
    }


    /*
    public void saveDatabase() {

        saveDatabaseAs(filename);

    }

    public void saveDatabaseAs(String filename) {

        String tmpFilenameWoDir = Util.getRandomFilename();

        File file = new File(filename);
        File path = file.getParentFile();
        String filenameWithoutDir = file.getName();
        File tmpFile = new File(path, tmpFilenameWoDir);

        final String currentPgnFilename = this.filename;
        final String pgnFilename = filename;
        final String tmpFilename = tmpFile.getAbsolutePath();

        final boolean overwrite = pgnFilename.equals(tmpFilename);

        final ObservableList<PgnDatabaseEntry> entries = this.entries;

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);

        Label lblScanPgn = new Label("Saving PGN...");
        ProgressBar progressBar = new ProgressBar();

        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().addAll(lblScanPgn, progressBar);

        vbox.setSpacing(10);
        vbox.setPadding( new Insets(10));

        Scene scene = new Scene(vbox, 400, 200);

        stage.setScene(scene);
        stage.show();

        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {

                OptimizedRandomAccessFile rafReader = null;
                OptimizedRandomAccessFile rafWriter = null;
                BufferedWriter writer = null;

                File currentPgn = new File(currentPgnFilename);
                long fileSize = currentPgn.length();

                PgnPrinter pgnPrinter = new PgnPrinter();

                int startIndex = 0;

                long linesWritten = 0;

                try {
                    rafReader = new OptimizedRandomAccessFile(currentPgnFilename, "r");
                    writer = new BufferedWriter(
                            new OutputStreamWriter(
                                    new FileOutputStream(tmpFilename),
                                    StandardCharsets.UTF_8
                            )
                    );
                    for (int i = 0; i < entries.size(); i++) {

                        // if game was modified, always write it out
                        if(entries.get(i).wasModified()) {
                            // first write out everything unmodified up until now
                            long startOffset = entries.get(startIndex).getOffset();
                            long stopOffset = entries.get(i).getOffset();
                            rafReader.seek(startOffset);
                            while(rafReader.getFilePointer() < stopOffset) {
                                String line = rafReader.readLine();
                                line = new String(line.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                                linesWritten += 1;
                                if(linesWritten % 20000 == 0) {
                                    linesWritten = 1;
                                    updateProgress(rafReader.getFilePointer(), fileSize);
                                }
                                if(line == null) {
                                    break;
                                } else {
                                    writer.write(line);
                                    writer.write(0xa); // 0xa = LF = \n
                                }
                            }
                            // write the modified game
                            Game g = entries.get(i).getModifiedGame();
                            if(g!=null) {
                                String sGame = pgnPrinter.printGame(g);
                                writer.write(sGame);
                                writer.write(0xa); // 0xa = LF = \n
                                writer.write(0xa); // 0xa = LF = \n
                            }
                        } else {
                            // if it wasn't modified, just collect
                            // only exception: we encountered the last game
                            if(i>0 && entries.get(i-1).wasModified()) {
                                startIndex = i;
                            }
                            if(i == entries.size()-1) {
                                rafReader.seek(entries.get(startIndex).getOffset());
                                while(rafReader.getFilePointer() < fileSize) {
                                    String line = rafReader.readLine();
                                    line = new String(line.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                                    linesWritten++;
                                    if(linesWritten % 20000 == 0) {
                                        linesWritten = 1;
                                        updateProgress(rafReader.getFilePointer(), fileSize);
                                    }
                                    if(line == null) {
                                        break;
                                    } else {
                                        writer.write(line);
                                        writer.write(0xa); // 0xa = LF = \n
                                    }
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (rafReader != null) {
                        try {
                            rafReader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (rafWriter != null) {
                        try {
                            rafWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if(writer != null) {
                        try {
                            writer.flush();
                            writer.close();
                        } catch(IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                File pgn = new File(pgnFilename);
                pgn.delete();
                tmpFile.renameTo(pgn);

                return null;
            }
        };


        progressBar.progressProperty().bind(task.progressProperty());

        task.setOnSucceeded(e -> {
            unregisterRunningTask(task);
            task.getValue();
            stage.close();
            if(this.dialogDatabase != null) {
                dialogDatabase.updateTable();
            }
            this.filename = pgnFilename;
            open();
        });

        Thread thread = new Thread(task);
        thread.setDaemon(false);
        registerRunningTask(task);
        thread.start();

    }

    public void appendToCurrentPGN(Game g) {

        BufferedWriter writer = null;
        try {
            File file = new File(filename);
            long offset = file.length() + 2;
            PgnPrinter pgnPrinter = new PgnPrinter();
            writer = new BufferedWriter(new FileWriter(filename, true));
            String sGame = pgnPrinter.printGame(g);
            writer.write(0xa); // 0xa = LF = \n
            writer.write(0xa); // 0xa = LF = \n
            writer.write(sGame);
            writer.write(0xa); // 0xa = LF = \n
            writer.write(0xa); // 0xa = LF = \n
            PgnDatabaseEntry newEntry = new PgnDatabaseEntry();
            newEntry.setWhite(g.getHeader("White"));
            newEntry.setBlack(g.getHeader("Black"));
            newEntry.setDate(g.getHeader("Date"));
            newEntry.setOffset(offset);
            newEntry.setEvent(g.getHeader("Event"));
            newEntry.setEco(g.getHeader("Eco"));
            newEntry.setResult(g.getHeader("Result"));
            newEntry.setRound(g.getHeader("Round"));
            newEntry.setSite(g.getHeader("Site"));
            newEntry.setIndex(entries.size()+1);
            entries.add(newEntry);
            writer.close();
        } catch(IOException e) {
            System.err.println(e);
        } finally {
            if(writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    System.err.println(e);
                }
            }
        }
    }

    public void appendToOtherPGN(GameModel gameModel) {

        // write game to file, then re-load this file into database
        FileChooser fileChooser = new FileChooser();
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        if(gameModel.lastSaveDirPath != null && gameModel.lastSaveDirPath.exists()) {
            fileChooser.setInitialDirectory(gameModel.lastSaveDirPath);
        }
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PGN", "*.pgn")
        );
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            if(file.getParentFile() != null) {
                gameModel.lastSaveDirPath = file.getParentFile();
            }

            BufferedWriter writer = null;
            try {
                PgnPrinter pgnPrinter = new PgnPrinter();
                writer = new BufferedWriter(new FileWriter(file, true));
                String sGame = pgnPrinter.printGame(gameModel.getGame());
                writer.write(0xa); // 0xa = LF = \n
                writer.write(0xa); // 0xa = LF = \n
                writer.write(sGame);
                writer.write(0xa); // 0xa = LF = \n
                writer.write(0xa); // 0xa = LF = \n
                writer.close();
            } catch(IOException e) {
                System.err.println(e);
            } finally {
                if(writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        System.err.println(e);
                    }
                }
            }
            this.filename = file.getAbsolutePath();
            open();
        }
    }

    public void replaceCurrentGame(Game g, int currentDatabaseIndex) {
        entries.get(currentDatabaseIndex).setModifiedGame(g);
        entries.get(currentDatabaseIndex).markAsModified();
        PgnPrinter tmp = new PgnPrinter();
        String tmp_game = tmp.printGame(g);
        saveDatabase();
    }

    public void saveAsNewPGN(GameModel gameModel) {

        // write game to file, then re-load this file into database
        FileChooser fileChooser = new FileChooser();
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        if(gameModel.lastSaveDirPath != null && gameModel.lastSaveDirPath.exists()) {
            fileChooser.setInitialDirectory(gameModel.lastSaveDirPath);
        }
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PGN", "*.pgn")
        );
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            if(file.getParentFile() != null) {
                gameModel.lastSaveDirPath = file.getParentFile();
            }
            PgnPrinter printer = new PgnPrinter();
            printer.writeGame(gameModel.getGame(), file.getAbsolutePath());

            this.filename = file.getAbsolutePath();
            open();
        }

    }

    public void deleteGame(int index) {

        String tmpFilenameWoDir = Util.getRandomFilename();

        File file = new File(filename);
        File path = file.getParentFile();
        String filenameWithoutDir = file.getName();
        File tmpFile = new File(path, tmpFilenameWoDir);

        final String currentPgnFilename = this.filename;
        final String pgnFilename = filename;
        final String tmpFilename = tmpFile.getAbsolutePath();

        final boolean overwrite = pgnFilename.equals(tmpFilename);

        final ObservableList<PgnDatabaseEntry> entries = this.entries;

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);

        Label lblScanPgn = new Label("Deleting Game...");
        ProgressBar progressBar = new ProgressBar();

        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().addAll(lblScanPgn, progressBar);

        vbox.setSpacing(10);
        vbox.setPadding( new Insets(10));

        Scene scene = new Scene(vbox, 400, 200);

        stage.setScene(scene);
        stage.show();

        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {

                OptimizedRandomAccessFile rafReader = null;
                OptimizedRandomAccessFile rafWriter = null;
                BufferedWriter writer = null;

                File currentPgn = new File(currentPgnFilename);
                long fileSize = currentPgn.length();

                PgnPrinter pgnPrinter = new PgnPrinter();

                long linesWritten = 0;

                try {
                    rafReader = new OptimizedRandomAccessFile(currentPgnFilename, "r");
                    writer = new BufferedWriter(new FileWriter(tmpFilename));

                    long startOffset = entries.get(0).getOffset();
                    long stopOffset = entries.get(index).getOffset();
                    long afterStartOffset = -1;
                    if(entries.size() > (index+1)) {
                        afterStartOffset = entries.get(index+1).getOffset();
                    }
                    rafReader.seek(startOffset);
                    while(rafReader.getFilePointer() < stopOffset) {
                        String line = rafReader.readLine();
                        linesWritten += 1;
                        if(linesWritten % 20000 == 0) {
                            linesWritten = 1;
                            updateProgress(rafReader.getFilePointer(), fileSize);
                        }
                        if(line == null) {
                            break;
                        } else {
                            writer.write(line);
                            writer.write(0xa); // 0xa = LF = \n
                        }
                    }
                    if(afterStartOffset > 0) {
                        rafReader.seek(afterStartOffset);
                        while(rafReader.getFilePointer() < fileSize) {
                            String line = rafReader.readLine();
                            linesWritten++;
                            if(linesWritten % 20000 == 0) {
                                linesWritten = 1;
                                updateProgress(rafReader.getFilePointer(), fileSize);
                            }
                            if(line == null) {
                                break;
                            } else {
                                writer.write(line);
                                writer.write(0xa); // 0xa = LF = \n
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (rafReader != null) {
                        try {
                            rafReader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (rafWriter != null) {
                        try {
                            rafWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if(writer != null) {
                        try {
                            writer.flush();
                            writer.close();
                        } catch(IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                File pgn = new File(pgnFilename);
                pgn.delete();
                tmpFile.renameTo(pgn);
                updateProgress(fileSize, fileSize);
                return null;
            }
        };


        progressBar.progressProperty().bind(task.progressProperty());

        task.setOnSucceeded(e -> {
            unregisterRunningTask(task);
            task.getValue();
            stage.close();
            if(this.dialogDatabase != null) {
                dialogDatabase.updateTable();
            }
            this.filename = pgnFilename;
            open();
        });

        Thread thread = new Thread(task);
        thread.setDaemon(false);
        registerRunningTask(task);
        thread.start();

    }
*/

    /*
    public void open() {

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);

        Label lblScanPgn = new Label("Scanning PGN...");
        ProgressBar progressBar = new ProgressBar();

        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().addAll(lblScanPgn, progressBar);

        vbox.setSpacing(10);
        vbox.setPadding( new Insets(10));

        Scene scene = new Scene(vbox, 400, 200);

        stage.setScene(scene);
        stage.show();

        final String tmpFilename = this.filename;

        entries.clear();
        System.gc();

        Task<ObservableList<PgnDatabaseEntry>> task = new Task<>() {
            @Override protected ObservableList<PgnDatabaseEntry> call() throws Exception {

                ArrayList<PgnDatabaseEntry> newEntries = new ArrayList<>();

                boolean inComment = false;
                long game_pos = -1;
                PgnDatabaseEntry current = null;
                long last_pos = 0;

                // check if we actually read at least one game;
                boolean readAtLeastOneGame = false;

                String currentLine = "";
                OptimizedRandomAccessFile raf = null;

                File file = new File(tmpFilename);
                long fileSize = file.length();

                long gamesRead = 0;

                try {
                    raf = new OptimizedRandomAccessFile(tmpFilename, "r");
                    int cnt_i = 0;
                    while ((currentLine = raf.readLine()) != null) {
                        cnt_i++;
                        if (isCancelled()) {
                            break;
                        }
                        // skip comments
                        if (currentLine.startsWith("%")) {
                            continue;
                        }

                        if (!inComment && currentLine.startsWith("[")) {
                            if (game_pos == -1) {
                                game_pos = last_pos;
                                current = new PgnDatabaseEntry();
                            }
                            last_pos = raf.getFilePointer();
                            if (currentLine.length() > 4) {
                                int spaceOffset = currentLine.indexOf(' ');
                                int firstQuote = currentLine.indexOf('"');
                                int secondQuote = currentLine.indexOf('"', firstQuote + 1);
                                String tag = currentLine.substring(1, spaceOffset);
                                if(secondQuote > firstQuote) {
                                    String value = currentLine.substring(firstQuote + 1, secondQuote);
                                    String valueEncoded = new String(value.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                                    if (tag.equals("Event")) {
                                        current.setEvent(valueEncoded);
                                        current.markValid();
                                    }
                                    if (tag.equals("Site")) {
                                        current.setSite(valueEncoded);
                                        current.markValid();
                                    }
                                    if (tag.equals("Round")) {
                                        current.setRound(valueEncoded);
                                        current.markValid();
                                    }
                                    if (tag.equals("White")) {
                                        current.setWhite(valueEncoded);
                                        current.markValid();
                                    }
                                    if (tag.equals("Black")) {
                                        current.setBlack(valueEncoded);
                                        current.markValid();
                                    }
                                    if (tag.equals("Result")) {
                                        current.setResult(valueEncoded);
                                        current.markValid();
                                    }
                                    if (tag.equals("Date")) {
                                        current.setDate(valueEncoded);
                                        current.markValid();
                                    }
                                    if (tag.equals("ECO")) {
                                        current.setEco(valueEncoded);
                                        current.markValid();
                                    }
                                }
                            }
                            continue;
                        }
                        if ((!inComment && currentLine.contains("{"))
                                || (inComment && currentLine.contains("}"))) {
                            inComment = currentLine.lastIndexOf("{") > currentLine.lastIndexOf("}");
                        }

                        if (game_pos != -1) {
                            current.setOffset(game_pos);
                            current.setIndex(newEntries.size()+1);
                            gamesRead += 1;
                            if(gamesRead > 10000) {
                                // updateMessage("Iteration " + game_pos);
                                updateProgress(game_pos, fileSize);
                                gamesRead = 0;
                            }
                            if(current.isValid()) {
                                newEntries.add(current);
                            }
                            game_pos = -1;
                        }
                        last_pos = raf.getFilePointer();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (raf != null) {
                        try {
                            raf.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return FXCollections.observableArrayList(newEntries);
            }
        };


        progressBar.progressProperty().bind(task.progressProperty());

        task.setOnSucceeded(e -> {
            unregisterRunningTask(task);
            entries = task.getValue();
            stage.close();
            if(this.dialogDatabase != null) {
                dialogDatabase.updateTable();
                dialogDatabase.table.scrollTo(0);
            }
        });

        Thread thread = new Thread(task);
        thread.setDaemon(false);
        registerRunningTask(task);
        thread.start();

    }
    */

    /*
    public void search(SearchPattern pattern) {

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);

        Label lblScanPgn = new Label("Searching...");
        ProgressBar progressBar = new ProgressBar();

        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().addAll(lblScanPgn, progressBar);

        vbox.setSpacing(10);
        vbox.setPadding( new Insets(10));

        Scene scene = new Scene(vbox, 400, 200);

        stage.setScene(scene);
        stage.show();


        Task<ObservableList<PgnDatabaseEntry>> task = new Task<>() {
            @Override protected ObservableList<PgnDatabaseEntry> call() throws Exception {

                final ArrayList<Long> indices = new ArrayList<Long>();
                for(PgnDatabaseEntry entry : entries) {
                    indices.add(entry.getOffset());
                }

                PgnReader reader = new PgnReader();
                ArrayList<PgnDatabaseEntry> foundEntries = new ArrayList<>();
                OptimizedRandomAccessFile raf = null;

                final long startTime = System.currentTimeMillis();

                try {
                    raf = new OptimizedRandomAccessFile(filename, "r");
                    for(int i=0;i<indices.size();i++) {
                        if (isCancelled()) {
                            break;
                        }
                        if(i%50000 == 0) {
                            updateProgress(i, entries.size());
                        }
                        if(pattern.isSearchForHeader()) {
                            if(!pattern.matchesHeader(entries.get(i))) {
                                continue;
                            }
                        }
                        if(pattern.isSearchForPosition()) {
                            raf.seek(indices.get(i));
                            Game g = reader.readGame(raf);
                            PgnPrinter printer = new PgnPrinter();
                            if(!g.containsPosition(pattern.getPositionHash(), pattern.getMinMove(), pattern.getMaxMove())) {
                                continue;
                            }
                        }
                        foundEntries.add(entries.get(i));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (raf != null) {
                        try {
                            raf.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                long stopTime = System.currentTimeMillis();
                long timeElapsed = stopTime - startTime;
                return FXCollections.observableArrayList(foundEntries);
            }
        };

        progressBar.progressProperty().bind(task.progressProperty());

        task.setOnSucceeded(e -> {
            unregisterRunningTask(task);
            searchResults = task.getValue();
            stage.close();
            if(this.dialogDatabase != null) {
                dialogDatabase.updateTableWithSearchResults();
            }
        });

        Thread thread = new Thread(task);
        registerRunningTask(task);
        thread.setDaemon(false);
        thread.start();
    }
    */

}

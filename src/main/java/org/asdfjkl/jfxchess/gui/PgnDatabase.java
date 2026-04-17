package org.asdfjkl.jfxchess.gui;

import org.asdfjkl.jfxchess.lib.PgnGameInfo;

import java.util.ArrayList;

public class PgnDatabase {

    private ArrayList<PgnGameInfo> entries = new ArrayList<>();
    private ArrayList<PgnGameInfo> searchResult = new ArrayList<>();
    private int idxOfCurrentlyOpenedGame = - 1;
    private String absoluteFilename = "";

    private boolean isSearchActive = false;

    public ArrayList<PgnGameInfo> getEntries() {
        return entries;
    }

    public void setEntries(ArrayList<PgnGameInfo> entries) {
        this.entries = entries;
    }

    public ArrayList<PgnGameInfo> getSearchResult() {
        return searchResult;
    }

    public void setSearchResults(ArrayList<PgnGameInfo> searchResult) {
        this.searchResult = searchResult;
    }

    public int getIdxOfCurrentlyOpenedGame() {
        return idxOfCurrentlyOpenedGame;
    }

    public void setIdxOfCurrentlyOpenedGame(int idxOfCurrentlyOpenedGame) {
        this.idxOfCurrentlyOpenedGame = idxOfCurrentlyOpenedGame;
    }

    public String getAbsoluteFilename() {
        return absoluteFilename;
    }

    public void setAbsoluteFilename(String absoluteFilename) {
        this.absoluteFilename = absoluteFilename;
    }

    public boolean isSearchActive() {
        return isSearchActive;
    }

    public void setSearchActive(boolean searchActive) {
        isSearchActive = searchActive;
    }
}

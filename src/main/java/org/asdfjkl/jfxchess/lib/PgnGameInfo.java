package org.asdfjkl.jfxchess.lib;

public class PgnGameInfo {

    private long offset = 0;
    private long index = 0;
    private String event = "";
    private String site = "";
    private String date = "";
    private String round = "";
    private String white = "";
    private String black = "";
    private String result = "";
    private String eco = "";
    private boolean modifiedFlag = false;
    Game modifiedGame = null;

    private boolean foundAtLeast1Tag = false;

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getRound() {
        return round;
    }

    public void setRound(String round) {
        this.round = round;
    }

    public String getWhite() {
        return white;
    }

    public void setWhite(String white) {
        this.white = white;
    }

    public String getBlack() {
        return black;
    }

    public void setBlack(String black) {
        this.black = black;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getEco() {
        return eco;
    }

    public void setEco(String eco) {
        this.eco = eco;
    }

    public void markValid() {
        foundAtLeast1Tag = true;
    }

    public void markAsModified() {
        modifiedFlag = true;
    }

    public boolean wasModified() {
        return modifiedFlag;
    }

    public void markAsUnmodified() {
        modifiedFlag = false;
        modifiedGame = null;
    }

    public void setModifiedGame(Game game) {
        modifiedGame = game;
    }

    public Game getModifiedGame() {
        return modifiedGame;
    }

    public boolean isValid() {
        return foundAtLeast1Tag;
    }
}
package org.asdfjkl.jfxchess.gui;

public interface BackgroundTask<T> {
    T run() throws Exception;
}
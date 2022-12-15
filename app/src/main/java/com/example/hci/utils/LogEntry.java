package com.example.hci.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class LogEntry {

    public enum ContextMode {
        IN_HANDS,
        ON_SURFACE,
        WALKING;
    }

    public enum DifficultyMode {
        EASY,
        MEDIUM,
        HARD;
    }

    private ContextMode contextMode;
    private DifficultyMode difficultyMode;
    private float elapsedTime;
    private String username;

    public LogEntry(String username, ContextMode contextMode, DifficultyMode difficultyMode, float elapsedTime) {
        this.contextMode = contextMode;
        this.difficultyMode = difficultyMode;
        this.elapsedTime = elapsedTime;
        this.username = username;
    }

    @Override
    public String toString() {
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(Locale.getDefault());
        decimalFormatSymbols.setDecimalSeparator(',');
        DecimalFormat formatter = new DecimalFormat("#.####", decimalFormatSymbols);
        return username + " " + difficultyMode + " " + contextMode + " " + formatter.format(elapsedTime) + '\n';
    }
}

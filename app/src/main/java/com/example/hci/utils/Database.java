package com.example.hci.utils;

import android.content.Context;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Database {
    private static Database instance = null;
    private File logFile;
    private FileWriter fileWriter;

    public static Database getInstance(Context context) {
        if (instance == null) {
            instance = new Database(context);
        }

        return instance;
    }

    private Database(Context context) {
        logFile = new File(context.getExternalFilesDir(null), "logs.txt");
    }
    
    public void writeNewLog(LogEntry log) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile))) {
            writer.write(log.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

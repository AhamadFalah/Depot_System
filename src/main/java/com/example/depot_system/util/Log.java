package com.example.depot_system.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Log {
    private static Log instance;
    private StringBuilder logBuffer = new StringBuilder();

    private Log() {
    };

    public static Log getInstance() {
        if (instance == null) {
            instance = new Log();
        }
        return instance;
    }

    public void logEvent(String event) {
        logBuffer.append(event).append("\n");
    }

    public void saveToFile(String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(logBuffer.toString());
            System.out.println("Logs saved to file: " + fileName);
        } catch (IOException e) {
            System.err.println("Error saving logs to file: " + e.getMessage());
        }
    }

    public String getAllLogEvents() {
        return logBuffer.toString();
    }


}

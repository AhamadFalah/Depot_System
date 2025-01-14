package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Log {
    private static Log instance;
    private StringBuilder logBuffer = new StringBuilder();

    private Log() {
    }

    public static Log getInstance() {
        if (instance == null) {
            instance = new Log();
        }
        return instance;
    }

    public void logEvent(String event) {
        logBuffer.append(event).append("\n");
    }

    public synchronized void saveToFile(String fileName) {
        File logsDir = DirectoryManager.getDirectory(DirectoryManager.LOGS_DIR);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = LocalDateTime.now().format(dtf);

        String baseName = fileName;
        String extension = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex != -1) {
            baseName = fileName.substring(0, dotIndex);
            extension = fileName.substring(dotIndex);
        }

        String timestampedFileName = baseName + "_" + timestamp + extension;


        File logFile = new File(logsDir, timestampedFileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            writer.write(logBuffer.toString());
            writer.flush();
            logBuffer.setLength(0);
            System.out.println("Logs saved successfully to: " + logFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error saving log to file '" + logFile.getAbsolutePath() + "': " + e.getMessage());
        }
    }

    public String getAllLogEvents() {
        return logBuffer.toString();
    }
}


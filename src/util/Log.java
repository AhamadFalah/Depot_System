package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Log {
    private static Log instance;
    private StringBuilder logBuffer;

    // Constructor for Log
    private Log() {
        logBuffer = new StringBuilder();
    }

    // Creates a new instance
    public static synchronized Log getInstance() {
        if (instance == null) {
            instance = new Log();
        }
        return instance;
    }

    // Logs info messages
    public void logInfo(String message) {
        logEvent("[INFO]", message);
    }

    // Logs error messages
    public void logError(String message) {
        logEvent("[ERROR]", message);
    }

    // Logs the events with timestamps
    private void logEvent(String level, String message) {
        String timestamp = getTimestamp();
        String formattedMessage = String.format("%s[%s] %s", level, timestamp, message);
        logBuffer.append(formattedMessage).append("\n");
        System.out.println(formattedMessage); // Optional: Log to console
    }

    // Generates the current timestamp
    private String getTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // Saves the current log buffer to a file
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

    // Retrieves all log events currently in the log buffer
    public String getAllLogEvents() {
        return logBuffer.toString();
    }
}


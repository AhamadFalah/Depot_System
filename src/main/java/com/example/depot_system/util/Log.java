package com.example.depot_system.util;

public class Log {
    private static Log instance;
    private StringBuilder logBuffer = new StringBuilder();

    private Log() {};

    public static Log getInstance() {
        if (instance == null) {
            instance = new Log();
        }
        return instance;
    }

    public void logEvent(String event) {
        logBuffer.append(event).append("\n");
    }

//    public void saveToFile(String fileName) {};

//    public String getAllLogEvents() {
//        return
//    };



}

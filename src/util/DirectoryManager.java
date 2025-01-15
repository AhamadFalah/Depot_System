package util;

import java.io.File;

public class DirectoryManager {

    public static final String LOGS_DIR = "logs";
    public static final String RECEIPTS_DIR = "receipts";
    public static final String REPORTS_DIR = "reports";

    // Retrieves a directory by its name, creating it if it does not already exist
    public static File getDirectory(String dirName) {
        File dir = new File(dirName);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                System.out.println("Directory created: " + dir.getAbsolutePath());
            } else {
                System.err.println("Failed to create directory: " + dir.getAbsolutePath());
            }
        }
        return dir;
    }
}

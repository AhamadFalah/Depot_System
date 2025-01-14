import controller.Manager;
import util.DirectoryManager;
import view.MainUI;

import javax.swing.*;

public class DepotSystemDemo {
    public static void main(String[] args) {
        // Log application startup
        System.out.println("Starting Depot System Application...");

        // Initialize the Directories
        DirectoryManager.getDirectory(DirectoryManager.LOGS_DIR);
        DirectoryManager.getDirectory(DirectoryManager.RECEIPTS_DIR);
        DirectoryManager.getDirectory(DirectoryManager.REPORTS_DIR);

        // Initialize the Manager
        Manager manager = new Manager();

        String customerFile = "Custs (1).csv";
        String parcelFile = "Parcels.csv";
        try {
            manager.loadFiles(customerFile, parcelFile);
        } catch (Exception e) {
            System.err.println("Error loading initial files: " + e.getMessage());
        }

        // Launch the Main UI
        SwingUtilities.invokeLater(() -> new MainUI(manager));

        System.out.println("Depot System Application launched successfully.");
    }
}

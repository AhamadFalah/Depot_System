import controller.Manager;
import view.MainUI;

import javax.swing.*;

public class DepotSystemDemo {
    public static void main(String[] args) {
        // Log application startup
        System.out.println("Starting Depot System Application...");

        // Initialize the Manager
        Manager manager = new Manager();

        String customerFile = "Custs (1).csv";
        String parcelFile = "Parcels.csv";
        try {
            manager.loadFiles(customerFile, parcelFile);
        } catch (Exception e) {
            System.err.println("Error loading initial files: " + e.getMessage());
        }

        // Launch the main UI
        SwingUtilities.invokeLater(() -> new MainUI(manager));

        System.out.println("Depot System Application launched successfully.");
    }
}

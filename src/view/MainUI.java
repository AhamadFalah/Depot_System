package view;

import controller.Manager;
import util.Log;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainUI {
    private JFrame frame;
    private Manager manager;
    private Log log = Log.getInstance();

    // Constructor for MainUI
    public MainUI(Manager manager) {
        this.manager = manager;
        log.logInfo("Initializing MainUI.");
        showMainMenu();
        log.logInfo("MainUI initialized successfully.");
    }

    // Sets up and displays the main menu.
    private void showMainMenu() {
        log.logInfo("Displaying Main Menu.");
        frame = new JFrame("Depot System - Main Menu");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLayout(new GridLayout(3, 1));

        JLabel welcomeLabel = new JLabel("Welcome to Depot System", SwingConstants.CENTER);
        frame.add(welcomeLabel);

        JButton customerButton = new JButton("Customer");
        JButton workerButton = new JButton("Worker");

        frame.add(customerButton);
        frame.add(workerButton);
        customerButton.addActionListener(e -> {
            log.logInfo("Navigating to Customer UI.");
            frame.dispose();
            new CustomerUI(manager);
        });

        workerButton.addActionListener(e -> {
            log.logInfo("Worker Login initiated.");
            showWorkerLogin();
        });

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                log.logInfo("Window close event triggered.");
                confirmAndCloseApplication();
            }
        });

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        log.logInfo("Main Menu displayed.");
    }

    // Prompts the worker to log in with a password.
    private void showWorkerLogin() {
        log.logInfo("Displaying Worker Login prompt."); // Log the display of the login prompt
        String password = JOptionPane.showInputDialog(frame, "Enter Worker Password (1234):", "Login", JOptionPane.PLAIN_MESSAGE);
        if ("1234".equals(password)) {
            log.logInfo("Worker Login successful.");
            frame.dispose();
            new WorkerUI(manager);
        } else {
            log.logError("Worker Login failed: Invalid Password.");
            JOptionPane.showMessageDialog(frame, "Invalid Password!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Displays a confirmation dialog when the user attempts to close the application.
    private void confirmAndCloseApplication() {
        log.logInfo("Exit confirmation dialog triggered.");
        int choice = JOptionPane.showConfirmDialog(frame, "Are you sure you want to exit?", "Exit Confirmation", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            log.logInfo("User confirmed application exit. Saving logs.");
            manager.getLog().saveToFile("DepotSystemLogs.txt");
            System.out.println("Logs saved to file.");
            log.logInfo("Application exited successfully.");
            System.exit(0);
        } else {
            log.logInfo("User canceled application exit.");
        }
    }
}

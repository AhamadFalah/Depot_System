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

    public MainUI(Manager manager) {
        this.manager = manager;
        showMainMenu();
    }

    private void showMainMenu() {
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
            frame.dispose();
            new CustomerUI(manager);
        });

        workerButton.addActionListener(e -> showWorkerLogin());

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmAndCloseApplication();
            }
        });

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void showWorkerLogin() {
        String password = JOptionPane.showInputDialog(frame, "Enter Worker Password (1234):", "Login", JOptionPane.PLAIN_MESSAGE);
        if ("1234".equals(password)) {
            frame.dispose();
            new WorkerUI(manager);
        } else {
            JOptionPane.showMessageDialog(frame, "Invalid Password!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void confirmAndCloseApplication() {
        int choice = JOptionPane.showConfirmDialog(frame, "Are you sure you want to exit?", "Exit Confirmation", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            Log.getInstance().logEvent("Exiting Depot System...");
            manager.getLog().saveToFile("DepotSystemLogs.txt");
            System.out.println("Logs saved to file.");
            System.exit(0);
        }
    }
}


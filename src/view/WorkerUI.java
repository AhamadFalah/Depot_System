package view;

import controller.Manager;
import model.Parcel;
import util.Log;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class WorkerUI {
    private JFrame frame;
    private Manager manager;
    private ParcelsPanel parcelsPanel;
    private CustomersPanel customersPanel;
    private CurrentParcelPanel currentParcelPanel;
    private Log log = Log.getInstance();

    public WorkerUI(Manager manager) {
        this.manager = manager;
        log.logInfo("Initializing WorkerUI.");
        initUI();
    }

    // Initializes the user interface components for WorkerUI
    private void initUI() {
        log.logInfo("Setting up WorkerUI components.");

        frame = new JFrame("Worker Interface");
        frame.setSize(1500, 800);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmAndCloseApplication();
            }
        });

        JPanel mainPanel = new JPanel(new BorderLayout());
        frame.setContentPane(mainPanel);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backButton = new JButton("Back to Main Menu");
        backButton.addActionListener(e -> {
            log.logInfo("Navigating back to the Main Menu from WorkerUI.");
            frame.dispose();
            new MainUI(manager);
        });
        topPanel.add(backButton);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        parcelsPanel = new ParcelsPanel(manager, this);
        customersPanel = new CustomersPanel(manager, this);
        currentParcelPanel = new CurrentParcelPanel();

        log.logInfo("Adding panels to WorkerUI.");

        mainPanel.add(parcelsPanel, BorderLayout.CENTER);
        mainPanel.add(customersPanel, BorderLayout.EAST);
        mainPanel.add(currentParcelPanel, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        log.logInfo("WorkerUI setup completed and displayed.");
    }

    // Displays a Confirmation Window to exit the Depot Application
    private void confirmAndCloseApplication() {
        log.logInfo("User requested application exit.");
        int choice = JOptionPane.showConfirmDialog(frame, "Are you sure you want to exit?", "Exit Confirmation", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            log.logInfo("Saving logs and exiting application.");
            manager.getLog().saveToFile("Logs.txt");
            System.out.println("Logs saved to file.");
            System.exit(0);
        } else {
            log.logInfo("User canceled exit request.");
        }
    }


    // Updates the CurrentParcelPanel with the provided parecls details
    public void showCurrentParcel(Parcel parcel) {
        log.logInfo("Displaying current parcel in CurrentParcelPanel: " + parcel);
        currentParcelPanel.setCurrentParcel(parcel);
    }
}

package view;

import controller.Manager;
import model.Parcel;

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

    public WorkerUI(Manager manager) {
        this.manager = manager;
        initUI();
    }

    private void initUI() {
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
            frame.dispose();
            new MainUI(manager);
        });
        topPanel.add(backButton);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        parcelsPanel = new ParcelsPanel(manager, this);
        customersPanel = new CustomersPanel(manager, this);
        currentParcelPanel = new CurrentParcelPanel();

        // Add them to mainPanel
        mainPanel.add(parcelsPanel, BorderLayout.CENTER);
        mainPanel.add(customersPanel, BorderLayout.EAST);
        mainPanel.add(currentParcelPanel, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void confirmAndCloseApplication() {
        int choice = JOptionPane.showConfirmDialog(frame, "Are you sure you want to exit?", "Exit Confirmation", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            manager.getLog().saveToFile("Logs.txt");
            System.out.println("Logs saved to file.");
            System.exit(0);
        }
    }

    public void showCurrentParcel(Parcel parcel) {
        currentParcelPanel.setCurrentParcel(parcel);
    }
}

package view;

import controller.Manager;
import model.Customer;
import util.Log;
import util.Observer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class CustomerUI implements Observer {
    private JFrame frame;
    private Manager manager;
    private Log log = Log.getInstance();

    private DefaultTableModel customerTableModel;
    private JTable customerTable;

    // Constructor for CustomerUI
    public CustomerUI(Manager manager) {
        this.manager = manager;
        manager.getQueueOfCustomers().addObserver(this);

        log.logInfo("Initializing CustomerUI.");
        initUI();
        log.logInfo("CustomerUI initialized successfully.");
    }

    // Initializes the user interface for the Customer Interface.
    private void initUI() {
        frame = new JFrame("Customer Interface");
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                log.logInfo("Window close event triggered.");
                confirmAndCloseApplication();
            }
        });

        customerTableModel = new DefaultTableModel(new String[]{"Queue Number", "Name", "Parcel ID"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        customerTable = new JTable(customerTableModel);
        JScrollPane scrollPane = new JScrollPane(customerTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Customer Queue"));

        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add to Queue");
        JButton backButton = new JButton("Back to Main Menu");

        addButton.addActionListener(e -> {
            log.logInfo("Add to Queue button clicked.");
            addCustomerToQueue();
        });
        backButton.addActionListener(e -> {
            log.logInfo("Back to Main Menu button clicked.");
            frame.dispose();
            new MainUI(manager);
        });

        controlPanel.add(addButton);
        controlPanel.add(backButton);
        frame.add(controlPanel, BorderLayout.SOUTH);

        refreshCustomerTable();

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // Displays a confirmation dialog when the user attempts to exit the application.
    private void confirmAndCloseApplication() {
        log.logInfo("Exit confirmation dialog triggered.");
        int choice = JOptionPane.showConfirmDialog(frame, "Are you sure you want to exit?", "Exit Confirmation", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            log.logInfo("User confirmed application exit. Saving logs.");
            manager.getLog().saveToFile("DepotSystemLogs.txt");
            System.out.println("Logs saved to file.");
            System.exit(0);
        } else {
            log.logInfo("User canceled application exit.");
        }
    }

    // Handles updates from observed objects.
    @Override
    public void update(String observableType) {
        if ("QueueOfCustomers".equals(observableType)) {
            log.logInfo("QueueOfCustomers update received. Refreshing customer table.");
            refreshCustomerTable();
        }
    }

    // Refreshes the customer table with the latest data from the customer queue.
    private void refreshCustomerTable() {
        log.logInfo("Refreshing customer table...");
        customerTableModel.setRowCount(0);
        for (Customer c : manager.getQueueOfCustomers().getCustomerQueue()) {
            customerTableModel.addRow(new Object[]{c.getQueueNumber(), c.getName(), c.getParcelID()});
            log.logInfo("Added customer to table: QueueNumber=" + c.getQueueNumber() + ", Name=" + c.getName() + ", ParcelID=" + c.getParcelID());
        }
        log.logInfo("Customer table refreshed successfully.");
    }

    // Handles the process of adding a new customer to the queue.
    private void addCustomerToQueue() {
        log.logInfo("Add Customer to Queue process initiated.");
        JTextField nameField = new JTextField();
        JTextField parcelIdField = new JTextField();

        Object[] message = {"Name:", nameField, "Parcel ID:", parcelIdField};

        int option = JOptionPane.showConfirmDialog(frame, message, "Add Customer to Queue", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String parcelID = parcelIdField.getText().trim();

            try {
                log.logInfo("Attempting to add customer: Name=" + name + ", ParcelID=" + parcelID);
                manager.addCustomer(name, parcelID);
                JOptionPane.showMessageDialog(frame, "Customer added successfully to the queue!", "Success", JOptionPane.INFORMATION_MESSAGE);
                log.logInfo("Customer added successfully: Name=" + name + ", ParcelID=" + parcelID);
                refreshCustomerTable();
            } catch (IllegalArgumentException ex) {
                log.logError("Failed to add customer: " + ex.getMessage());
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            log.logInfo("Add Customer to Queue process canceled by the user.");
        }
    }


}

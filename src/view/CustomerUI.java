package view;

import controller.Manager;
import model.Customer;
import util.Observer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class CustomerUI implements Observer {
    private JFrame frame;
    private Manager manager;

    private DefaultTableModel customerTableModel;
    private JTable customerTable;

    public CustomerUI(Manager manager) {
        this.manager = manager;
        manager.getQueueOfCustomers().addObserver(this);

        initUI();
    }

    private void initUI() {
        frame = new JFrame("Customer Interface");
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
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

        addButton.addActionListener(e -> addCustomerToQueue());
        backButton.addActionListener(e -> {
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

    private void confirmAndCloseApplication() {
        int choice = JOptionPane.showConfirmDialog(frame, "Are you sure you want to exit?", "Exit Confirmation", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            manager.getLog().saveToFile("DepotSystemLogs.txt");
            System.out.println("Logs saved to file.");
            System.exit(0);
        }
    }

    @Override
    public void update(String observableType) {
        if ("QueueOfCustomers".equals(observableType)) {
            refreshCustomerTable();
        }
    }

    private void refreshCustomerTable() {
        customerTableModel.setRowCount(0);
        for (Customer c : manager.getQueueOfCustomers().getCustomerQueue()) {
            customerTableModel.addRow(new Object[]{c.getQueueNumber(), c.getName(), c.getParcelID()});
        }
    }

    private void addCustomerToQueue() {
        JTextField nameField = new JTextField();
        JTextField parcelIdField = new JTextField();

        Object[] message = {"Name:", nameField, "Parcel ID:", parcelIdField};

        int option = JOptionPane.showConfirmDialog(frame, message, "Add Customer to Queue", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String parcelID = parcelIdField.getText().trim();

            try {
                manager.addCustomer(name, parcelID); // Delegate logic to the Manager
                JOptionPane.showMessageDialog(frame, "Customer added successfully to the queue!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

}

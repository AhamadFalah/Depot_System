package view;

import controller.Manager;
import model.Customer;
import model.Parcel;
import util.Log;
import util.Observer;
import util.Observable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CustomersPanel extends JPanel implements Observer {
    private Manager manager;
    private WorkerUI workerUI;
    private Log log = Log.getInstance();

    private DefaultTableModel customerTableModel;
    private JTable customerTable;

    // Constructor for CustomerPanel
    public CustomersPanel(Manager manager, WorkerUI workerUI) {
        this.manager = manager;
        this.workerUI = workerUI;

        log.logInfo("Initializing CustomersPanel.");

        manager.getQueueOfCustomers().addObserver(this);

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Customer Queue"));

        customerTableModel = new DefaultTableModel(new String[]{"Queue Number", "Name", "Parcel ID"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        customerTable = new JTable(customerTableModel);

        JScrollPane scrollPane = new JScrollPane(customerTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton processNextButton = new JButton("Process Next Customer");
        JButton generateReportButton = new JButton("Generate Report");
        JButton viewFeesButton = new JButton("View Fees");
        JButton addCustomerButton = new JButton("Add Customer to Queue"); // New Button

        buttonPanel.add(processNextButton);
        buttonPanel.add(generateReportButton);
        buttonPanel.add(viewFeesButton);
        buttonPanel.add(addCustomerButton); // Add to panel

        add(buttonPanel, BorderLayout.SOUTH);

        processNextButton.addActionListener(e -> {
            log.logInfo("Process Next Customer button clicked.");
            processNextCustomer();
        });
        generateReportButton.addActionListener(e -> {
            log.logInfo("Generate Report button clicked.");
            WorkerUIHelper.generateReport(this, manager);
        });
        viewFeesButton.addActionListener(e -> {
            log.logInfo("View Fees button clicked.");
            WorkerUIHelper.showFeesAndDiscounts(this);
        });
        addCustomerButton.addActionListener(e -> {
            log.logInfo("Add Customer to Queue button clicked.");
            addCustomerToQueue();
        });

        refreshCustomerTable();
        log.logInfo("CustomersPanel initialized successfully.");
    }

    // Updates the UI based on notifications from observable objects.
    @Override
    public void update(String observableType) {
        if ("QueueOfCustomers".equals(observableType)) {
            refreshCustomerTable();
        }
    }

    // Refreshes the customer table to reflect the current state of the customer queue.
    private void refreshCustomerTable() {
        log.logInfo("Refreshing customer table...");
        customerTableModel.setRowCount(0);

        int customerCount = 0;
        for (Customer c : manager.getQueueOfCustomers().getCustomerQueue()) {
            customerTableModel.addRow(new Object[]{c.getQueueNumber(), c.getName(), c.getParcelID()});
            log.logInfo("Added customer to table: QueueNumber=" + c.getQueueNumber() + ", Name=" + c.getName() + ", ParcelID=" + c.getParcelID());
            customerCount++;
        }

        log.logInfo("Customer table refreshed. Total customers displayed: " + customerCount);
    }

    // Processes the next customer in the queue.
    private void processNextCustomer() {
        log.logInfo("Process Next Customer initiated.");

        if (manager.getQueueOfCustomers().isEmpty()) {
            log.logInfo("No customers in the queue.");
            JOptionPane.showMessageDialog(this, "No customers in the queue.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Customer currentCustomer = manager.getQueueOfCustomers().getCustomer();
        Parcel currentParcel = manager.getParcelMap().findParcel(currentCustomer.getParcelID());

        if (currentParcel == null) {
            log.logError("Parcel not found for customer: " + currentCustomer.getName() + " [Parcel ID: " + currentCustomer.getParcelID() + "]");
            JOptionPane.showMessageDialog(this, "Parcel not found for the customer.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        log.logInfo("Processing customer: " + currentCustomer.getName() + " [Parcel ID: " + currentCustomer.getParcelID() + "]");
        double totalFee = manager.getWorker().calculateFee(currentParcel);
        double discount = manager.getWorker().calculateDiscount(currentParcel.getParcelID(), totalFee);
        double finalFee = totalFee - discount;

        String feeDetails = String.format(
                "Customer: %s\nParcel ID: %s\n\nFee Breakdown:\nBase Fee: £5.00\nWeight Fee: £%.2f\nDepot Fee: £%.2f\nDiscount: £%.2f\nTotal Fee: £%.2f",
                currentCustomer.getName(),
                currentParcel.getParcelID(),
                currentParcel.getWeight() * 0.5,
                currentParcel.getDaysInDepot() * 0.2,
                discount,
                finalFee
        );

        int initialChoice = JOptionPane.showConfirmDialog(
                this,
                feeDetails + "\n\nDo you want to proceed with this customer?",
                "Fee Structure",
                JOptionPane.YES_NO_OPTION
        );

        if (initialChoice == JOptionPane.YES_OPTION) {
            int processChoice = JOptionPane.showConfirmDialog(
                    this,
                    "Do you want to process this customer?",
                    "Confirm Processing",
                    JOptionPane.YES_NO_OPTION
            );

            if (processChoice == JOptionPane.YES_OPTION) {
                log.logInfo("Processing parcel: " + currentParcel.getParcelID());
                double fee = finalFee;
                currentParcel.setStatus("Collected");
                manager.getCollectedParcels().add(currentParcel);
                manager.addToTotalFees(fee);
                workerUI.showCurrentParcel(currentParcel);

                boolean removed = manager.getQueueOfCustomers().removeCustomerByParcelID(currentCustomer.getParcelID());
                if (removed) {
                    log.logInfo("Customer removed from queue: " + currentCustomer.getName() + " [Parcel ID: " + currentCustomer.getParcelID() + "]");
                    JOptionPane.showMessageDialog(
                            this,
                            String.format("Parcel processed successfully.\nFee: £%.2f\nCustomer '%s' removed from the queue.", fee, currentCustomer.getName()),
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }

                int receiptChoice = JOptionPane.showConfirmDialog(
                        this,
                        "Do you want to generate a receipt?",
                        "Generate Receipt",
                        JOptionPane.YES_NO_OPTION
                );

                if (receiptChoice == JOptionPane.YES_OPTION) {
                    log.logInfo("Generating receipt for Parcel ID: " + currentParcel.getParcelID());
                    String receipt = manager.generateReceipt(currentParcel, fee);
                    JOptionPane.showMessageDialog(this, receipt, "Receipt", JOptionPane.INFORMATION_MESSAGE);
                }

                log.logInfo("Parcel processed: " + currentParcel.getParcelID() + ". Final Fee: £" + String.format("%.2f", fee));
                refreshCustomerTable();
                manager.getParcelMap().notifyParcelMapObservers("ParcelMap");
            } else {
                log.logInfo("Processing cancelled for customer: " + currentCustomer.getName());
                manager.getQueueOfCustomers().add(currentCustomer);
                refreshCustomerTable();
            }
        } else if (initialChoice == JOptionPane.NO_OPTION) {
            log.logInfo("User chose not to proceed with customer: " + currentCustomer.getName());
            Object[] options = {"Remove from Queue", "Move to Back"};
            int queueChoice = JOptionPane.showOptionDialog(
                    this,
                    "What would you like to do with this customer?",
                    "Queue Options",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[1]
            );

            if (queueChoice == 0) {
                boolean removed = manager.getQueueOfCustomers().removeCustomerByParcelID(currentCustomer.getParcelID());
                if (removed) {
                    log.logInfo("Customer removed from queue: " + currentCustomer.getName() + " [Parcel ID: " + currentCustomer.getParcelID() + "]");
                    JOptionPane.showMessageDialog(
                            this,
                            String.format("Customer %s has been removed from the queue.", currentCustomer.getName()),
                            "Customer Removed",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } else {
                    log.logError("Failed to remove customer: " + currentCustomer.getName() + " [Parcel ID: " + currentCustomer.getParcelID() + "]");
                    JOptionPane.showMessageDialog(
                            this,
                            "Failed to remove the customer from the queue.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            } else if (queueChoice == 1) {
                manager.getQueueOfCustomers().add(currentCustomer);
                log.logInfo("Customer moved to back of queue: " + currentCustomer.getName() + " [Parcel ID: " + currentCustomer.getParcelID() + "]");
                JOptionPane.showMessageDialog(
                        this,
                        String.format("Customer %s has been moved to the back of the queue.", currentCustomer.getName()),
                        "Customer Moved",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

            refreshCustomerTable();
        }
    }

    // Initiates the process for adding a new customer to the queue.
    private void addCustomerToQueue() {
        log.logInfo("Initiating Add Customer to Queue process.");

        JTextField nameField = new JTextField();
        JTextField parcelIdField = new JTextField();

        Object[] message = {"Name:", nameField, "Parcel ID:", parcelIdField};

        int option = JOptionPane.showConfirmDialog(this, message, "Add Customer to Queue", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String parcelID = parcelIdField.getText().trim();

            try {
                log.logInfo("Attempting to add customer: Name=" + name + ", Parcel ID=" + parcelID);

                // Delegate validation and addition logic to Manager
                manager.addCustomer(name, parcelID);

                // Display success message
                JOptionPane.showMessageDialog(this,
                        "Customer added successfully to the queue!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                log.logInfo("Customer added successfully to the queue: Name=" + name + ", Parcel ID=" + parcelID);

                // Refresh the table after addition
                refreshCustomerTable();
            } catch (IllegalArgumentException ex) {
                log.logError("Failed to add customer: " + ex.getMessage());
                // Display error message from the Manager's validation
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            log.logInfo("Add Customer to Queue process canceled by the user.");
        }
    }
}

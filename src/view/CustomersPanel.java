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

    private DefaultTableModel customerTableModel;
    private JTable customerTable;

    public CustomersPanel(Manager manager, WorkerUI workerUI) {
        this.manager = manager;
        this.workerUI = workerUI;

        manager.getQueueOfCustomers().addObserver(this);

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Customer Queue"));

        customerTableModel = new DefaultTableModel(new String[]{"Queue Number", "Name", "Parcel ID"}, 0);
        customerTable = new JTable(customerTableModel);

        JScrollPane scrollPane = new JScrollPane(customerTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton processNextButton = new JButton("Process Next Customer");
        JButton generateReportButton = new JButton("Generate Report");
        JButton viewFeesButton = new JButton("View Fees");

        buttonPanel.add(processNextButton);
        buttonPanel.add(generateReportButton);
        buttonPanel.add(viewFeesButton);

        add(buttonPanel, BorderLayout.SOUTH);

        processNextButton.addActionListener(e -> processNextCustomer());
        generateReportButton.addActionListener(e -> WorkerUIHelper.generateReport(this, manager));
        viewFeesButton.addActionListener(e -> WorkerUIHelper.showFeesAndDiscounts(this));

        refreshCustomerTable();
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

    private void processNextCustomer() {
        if (manager.getQueueOfCustomers().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No customers in the queue.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Get the customer at the front of the queue
        Customer currentCustomer = manager.getQueueOfCustomers().getCustomer();
        Parcel currentParcel = manager.getParcelMap().findParcel(currentCustomer.getParcelID());

        if (currentParcel == null) {
            JOptionPane.showMessageDialog(this, "Parcel not found for the customer.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Display the fee structure
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
            // Confirm processing the customer
            int processChoice = JOptionPane.showConfirmDialog(
                    this,
                    "Do you want to process this customer?",
                    "Confirm Processing",
                    JOptionPane.YES_NO_OPTION
            );

            if (processChoice == JOptionPane.YES_OPTION) {
                // Process the parcel and mark it as collected
                double fee = manager.getWorker().calculateFee(currentParcel) - manager.getWorker().calculateDiscount(currentParcel.getParcelID(), manager.getWorker().calculateFee(currentParcel));
                currentParcel.setStatus("Collected");
                manager.getCollectedParcels().add(currentParcel);
                manager.addToTotalFees(fee);

                // Update the CurrentParcelPanel
                workerUI.showCurrentParcel(currentParcel);

                // Remove the customer from the queue using the new method
                boolean removed = manager.getQueueOfCustomers().removeCustomerByParcelID(currentCustomer.getParcelID());
                if (removed) {
                    Log.getInstance().logEvent("Customer removed from queue: " + currentCustomer.getName() + " [Parcel ID: " + currentCustomer.getParcelID() + "]");
                    JOptionPane.showMessageDialog(
                            this,
                            String.format("Parcel processed successfully.\nFee: £%.2f\nCustomer '%s' removed from the queue.", fee, currentCustomer.getName()),
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            String.format("Parcel processed successfully.\nFee: £%.2f", fee),
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }

                // Prompt to generate receipt
                int receiptChoice = JOptionPane.showConfirmDialog(
                        this,
                        "Do you want to generate a receipt?",
                        "Generate Receipt",
                        JOptionPane.YES_NO_OPTION
                );

                if (receiptChoice == JOptionPane.YES_OPTION) {
                    String receipt = manager.generateReceipt(currentParcel, fee);
                    JOptionPane.showMessageDialog(this, receipt, "Receipt", JOptionPane.INFORMATION_MESSAGE);
                }

                Log.getInstance().logEvent("Parcel processed: " + currentParcel.getParcelID() + ". Final Fee: £" + String.format("%.2f", fee));

                // Refresh the customer table and parcel map observers
                refreshCustomerTable();
                manager.getParcelMap().notifyParcelMapObservers("ParcelMap");
            } else {
                manager.getQueueOfCustomers().add(currentCustomer);
                refreshCustomerTable();
            }
        } else if (initialChoice == JOptionPane.NO_OPTION) {
            // If "No", give the option to remove or move to the back
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
                // Remove customer from the queue
                boolean removed = manager.getQueueOfCustomers().removeCustomerByParcelID(currentCustomer.getParcelID());
                if (removed) {
                    Log.getInstance().logEvent("Customer removed from queue: " + currentCustomer.getName() + " [Parcel ID: " + currentCustomer.getParcelID() + "]");
                    JOptionPane.showMessageDialog(
                            this,
                            String.format("Customer %s has been removed from the queue.", currentCustomer.getName()),
                            "Customer Removed",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "Failed to remove the customer from the queue.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            } else if (queueChoice == 1) {
                // Move customer to the back of the queue
                manager.getQueueOfCustomers().add(currentCustomer);
                Log.getInstance().logEvent("Customer moved to back of queue: " + currentCustomer.getName() + " [Parcel ID: " + currentCustomer.getParcelID() + "]");
                JOptionPane.showMessageDialog(
                        this,
                        String.format("Customer %s has been moved to the back of the queue.", currentCustomer.getName()),
                        "Customer Moved",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

            // Refresh the customer queue
            refreshCustomerTable();
        }
    }
}

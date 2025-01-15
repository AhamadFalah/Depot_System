package view;

import controller.Manager;
import model.Customer;
import model.Parcel;
import util.Log;
import util.Observer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;

public class ParcelsPanel extends JPanel implements Observer {
    private Manager manager;
    private WorkerUI workerUI;
    private DefaultTableModel parcelTableModel;
    private JTable parcelTable;
    private Log log = Log.getInstance();

    public ParcelsPanel(Manager manager, WorkerUI workerUI) {
        this.manager = manager;
        this.workerUI = workerUI;

        log.logInfo("Initializing ParcelsPanel.");

        // Observe ParcelMap changes
        manager.getParcelMap().addObserver(this);

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Parcels"));

        parcelTableModel = new DefaultTableModel(new String[]{"ID", "Weight (KGs)", "Dimensions (WxHxL)", "Status", "Days in Depot"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // All cells non editable
            }
        };
        parcelTable = new JTable(parcelTableModel);
        JScrollPane scrollPane = new JScrollPane(parcelTable);
        add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton processParcelButton = new JButton("Process Selected Parcel");
        JButton editParcelButton = new JButton("Edit Selected Parcel");
        JButton searchParcelButton = new JButton("Search Parcel");
        JButton refreshButton = new JButton("Refresh Parcels");
        JButton addParcelButton = new JButton("Add Parcel");
        JButton viewCollectedButton = new JButton("View Collected Parcels");

        buttonPanel.add(processParcelButton);
        buttonPanel.add(editParcelButton);
        buttonPanel.add(searchParcelButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(addParcelButton);
        buttonPanel.add(viewCollectedButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Listeners
        processParcelButton.addActionListener(e -> {
            log.logInfo("Process Selected Parcel button clicked.");
            processSelectedParcel();
        });
        editParcelButton.addActionListener(e -> {
            log.logInfo("Edit Selected Parcel button clicked.");
            editSelectedParcel();
        });
        searchParcelButton.addActionListener(e -> {
            log.logInfo("Search Parcel button clicked.");
            searchParcel();
        });
        refreshButton.addActionListener(e -> {
            log.logInfo("Refresh Parcels button clicked.");
            refreshParcelTable();
        });
        addParcelButton.addActionListener(e -> {
            log.logInfo("Add Parcel button clicked.");
            addParcel();
        });
        viewCollectedButton.addActionListener(e -> {
            log.logInfo("View Collected Parcels button clicked.");
            showCollectedParcels();
        });

        refreshParcelTable();
        log.logInfo("ParcelsPanel initialized successfully.");
    }

    // Updates the view based on the type of observable change
    @Override
    public void update(String observableType) {
        if ("ParcelMap".equals(observableType)) {
            refreshParcelTable();
        }
    }

    // Refreshes the parcel table by clearing and repopulating it with the latest data from the ParcelMap
    private void refreshParcelTable() {
        log.logInfo("Refreshing parcel table...");
        try {
            parcelTableModel.setRowCount(0);
            List<Parcel> allParcels = manager.getParcelMap().getAllParcels();

            for (Parcel p : allParcels) {
                parcelTableModel.addRow(new Object[]{
                        p.getParcelID(),
                        p.getWeight(),
                        p.getDimensions(),
                        p.getStatus(),
                        p.getDaysInDepot()
                });
                log.logInfo("Added parcel to table: ID=" + p.getParcelID() +
                        ", Weight=" + p.getWeight() +
                        ", Dimensions=" + p.getDimensions() +
                        ", Status=" + p.getStatus() +
                        ", DaysInDepot=" + p.getDaysInDepot());
            }

            log.logInfo("Parcel table refreshed successfully with " + allParcels.size() + " parcels.");
        } catch (Exception e) {
            log.logError("Error occurred while refreshing parcel table: " + e.getMessage());
        }
    }

    // Processes the selected parcel from the table
    private void processSelectedParcel() {
        log.logInfo("Initiating process for selected parcel.");
        int selectedRow = parcelTable.getSelectedRow(); // Get the selected row
        if (selectedRow == -1) {
            log.logError("No parcel selected for processing.");
            JOptionPane.showMessageDialog(this, "Select a parcel to process.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String parcelID = (String) parcelTableModel.getValueAt(selectedRow, 0); // Get Parcel ID from the table
        Parcel selectedParcel = manager.getParcelMap().findParcel(parcelID);

        if (selectedParcel == null) {
            log.logError("Parcel not found: ID=" + parcelID);
            JOptionPane.showMessageDialog(this, "Parcel not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if ("Collected".equalsIgnoreCase(selectedParcel.getStatus())) {
            log.logInfo("Parcel already collected: ID=" + parcelID);
            JOptionPane.showMessageDialog(this, "This parcel has already been collected.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        log.logInfo("Processing parcel: ID=" + parcelID);

        // Find the associated customer in the queue (if any)
        Customer associatedCustomer = manager.getQueueOfCustomers().getCustomerQueue().stream()
                .filter(customer -> customer.getParcelID().equals(parcelID))
                .findFirst()
                .orElse(null);

        double totalFee = manager.getWorker().calculateFee(selectedParcel);
        double discount = manager.getWorker().calculateDiscount(selectedParcel.getParcelID(), totalFee);
        double finalFee = totalFee - discount;

        log.logInfo(String.format("Fee calculated for parcel: ID=%s, Total Fee=£%.2f, Discount=£%.2f, Final Fee=£%.2f",
                parcelID, totalFee, discount, finalFee));

        String feeDetails = String.format(
                "Parcel ID: %s\nWeight: %.2f kg\nBase Fee: £5.00\nWeight Fee: £%.2f\nDepot Fee: £%.2f\nDiscount: £%.2f\nTotal Fee: £%.2f\n\nConfirm collection?",
                selectedParcel.getParcelID(),
                selectedParcel.getWeight(),
                selectedParcel.getWeight() * 0.5,
                selectedParcel.getDaysInDepot() * 0.2,
                discount,
                finalFee
        );

        int initialChoice = JOptionPane.showConfirmDialog(
                this,
                feeDetails,
                "Confirm Parcel Collection",
                JOptionPane.YES_NO_OPTION
        );

        if (initialChoice == JOptionPane.YES_OPTION) {
            log.logInfo("User confirmed parcel collection: ID=" + parcelID);

            int processChoice = JOptionPane.showConfirmDialog(
                    this,
                    "Do you want to process this parcel?",
                    "Confirm Processing",
                    JOptionPane.YES_NO_OPTION
            );

            if (processChoice == JOptionPane.YES_OPTION) {
                log.logInfo("Processing confirmed for parcel: ID=" + parcelID);
                selectedParcel.setStatus("Collected");
                manager.getCollectedParcels().add(selectedParcel);
                manager.addToTotalFees(finalFee);

                workerUI.showCurrentParcel(selectedParcel);

                if (associatedCustomer != null) {
                    boolean removed = manager.getQueueOfCustomers().removeCustomerByParcelID(parcelID);
                    if (removed) {
                        log.logInfo("Customer removed from queue: Name=" + associatedCustomer.getName() + ", ParcelID=" + parcelID);
                        JOptionPane.showMessageDialog(
                                this,
                                String.format("Parcel processed successfully.\nFee: £%.2f\nCustomer '%s' removed from the queue.", finalFee, associatedCustomer.getName()),
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                }

                int receiptChoice = JOptionPane.showConfirmDialog(
                        this,
                        "Do you want to generate a receipt?",
                        "Generate Receipt",
                        JOptionPane.YES_NO_OPTION
                );

                if (receiptChoice == JOptionPane.YES_OPTION) {
                    log.logInfo("Receipt generation initiated for parcel: ID=" + parcelID);
                    String receipt = manager.generateReceipt(selectedParcel, finalFee);
                    JOptionPane.showMessageDialog(this, receipt, "Receipt", JOptionPane.INFORMATION_MESSAGE);
                    log.logInfo("Receipt generated for parcel: ID=" + parcelID);
                }

                log.logInfo("Parcel processing completed: ID=" + parcelID + ", Final Fee=£" + String.format("%.2f", finalFee));
                refreshParcelTable();
                manager.getParcelMap().notifyParcelMapObservers("ParcelMap");
            }
        } else {
            log.logInfo("User canceled parcel collection for: ID=" + parcelID);

            Object[] options = {"Remove from Queue", "Move to Back"};
            int queueChoice = JOptionPane.showOptionDialog(
                    this,
                    "What would you like to do with the associated customer (if any)?",
                    "Queue Options",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[1]
            );

            if (queueChoice == 0 && associatedCustomer != null) {
                boolean removed = manager.getQueueOfCustomers().removeCustomerByParcelID(parcelID);
                if (removed) {
                    log.logInfo("Customer removed from queue: Name=" + associatedCustomer.getName() + ", ParcelID=" + parcelID);
                    JOptionPane.showMessageDialog(
                            this,
                            String.format("Customer '%s' has been removed from the queue.", associatedCustomer.getName()),
                            "Customer Removed",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
            } else if (queueChoice == 1 && associatedCustomer != null) {
                boolean removed = manager.getQueueOfCustomers().removeCustomerByParcelID(parcelID);
                if (removed) {
                    manager.getQueueOfCustomers().add(associatedCustomer);
                    log.logInfo("Customer moved to back of queue: Name=" + associatedCustomer.getName() + ", ParcelID=" + parcelID);
                    JOptionPane.showMessageDialog(
                            this,
                            String.format("Customer '%s' has been moved to the back of the queue.", associatedCustomer.getName()),
                            "Customer Moved",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } else {
                    log.logError("Failed to move customer to the back of the queue: Name=" + associatedCustomer.getName() + ", ParcelID=" + parcelID);
                    JOptionPane.showMessageDialog(
                            this,
                            "Failed to move the customer to the back of the queue.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }

            refreshParcelTable();
            manager.getParcelMap().notifyParcelMapObservers("ParcelMap");
        }
    }

    // Handles the process of adding and validating a new parcel to the system
    private void addParcel() {
        log.logInfo("Initiating add parcel process.");

        JTextField parcelIDField = new JTextField();
        JTextField weightField = new JTextField();
        JTextField widthField = new JTextField();
        JTextField heightField = new JTextField();
        JTextField lengthField = new JTextField();
        JTextField daysField = new JTextField();

        Object[] msg = {
                "Parcel ID:", parcelIDField,
                "Weight (kg):", weightField,
                "Width:", widthField,
                "Height:", heightField,
                "Length:", lengthField,
                "Days in Depot:", daysField
        };

        int option = JOptionPane.showConfirmDialog(this, msg, "Add New Parcel", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String pid = parcelIDField.getText().trim();
            String weightText = weightField.getText().trim();
            String widthText = widthField.getText().trim();
            String heightText = heightField.getText().trim();
            String lengthText = lengthField.getText().trim();
            String daysText = daysField.getText().trim();

            log.logInfo("Collecting parcel data from user input.");

            // Check for empty fields
            if (pid.isEmpty() || weightText.isEmpty() || widthText.isEmpty() ||
                    heightText.isEmpty() || lengthText.isEmpty() || daysText.isEmpty()) {
                log.logError("Validation failed: Missing required fields.");
                JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double weight, width, height, length;
            int days;

            // Validate numeric inputs
            try {
                weight = Double.parseDouble(weightText);
                width = Double.parseDouble(widthText);
                height = Double.parseDouble(heightText);
                length = Double.parseDouble(lengthText);
                days = Integer.parseInt(daysText);

                if (weight <= 0 || width <= 0 || height <= 0 || length <= 0 || days < 0) {
                    log.logError("Validation failed: Invalid numeric inputs.");
                    JOptionPane.showMessageDialog(this, "Weight, Width, Height, and Length must be positive numbers. Days in Depot cannot be negative.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException e) {
                log.logError("Validation failed: Non-numeric inputs detected.");
                JOptionPane.showMessageDialog(this, "Please enter valid numeric values for Weight, Width, Height, Length, and Days in Depot.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return;
            }

            log.logInfo("Validation passed. Preparing parcel for addition.");

            // Concatenate dimensions
            String dimensions = String.format("%.2fx%.2fx%.2f", width, height, length);

            Parcel newParcel = new Parcel(pid, weight, dimensions, "Pending", days);
            log.logInfo("New parcel created: " + newParcel.toString());

            manager.addNewParcel(newParcel);
            log.logInfo("Parcel added to the system: ID=" + pid);

            refreshParcelTable();
            log.logInfo("Parcel table refreshed after addition.");
        } else {
            log.logInfo("Add parcel process canceled by user.");
        }
    }

    // Handles the process of editing and validating an existing parcel in the system
    private void editSelectedParcel() {
        log.logInfo("Initiating edit parcel process.");
        int selectedRow = parcelTable.getSelectedRow();

        if (selectedRow == -1) {
            log.logError("No parcel selected for editing.");
            JOptionPane.showMessageDialog(this, "Select a parcel to edit.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String parcelID = (String) parcelTableModel.getValueAt(selectedRow, 0);
        log.logInfo("Selected parcel for editing: ID=" + parcelID);

        Parcel parcel = manager.searchParcel(parcelID);
        if (parcel == null) {
            log.logError("Parcel not found: ID=" + parcelID);
            JOptionPane.showMessageDialog(this, "Parcel not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        log.logInfo("Parcel found: " + parcel);

        // Extract existing dimensions
        String[] dims = parcel.getDimensions().split("x");
        String existingWidth = dims.length > 0 ? dims[0] : "";
        String existingHeight = dims.length > 1 ? dims[1] : "";
        String existingLength = dims.length > 2 ? dims[2] : "";

        JTextField weightField = new JTextField(String.valueOf(parcel.getWeight()));
        JTextField widthField = new JTextField(existingWidth);
        JTextField heightField = new JTextField(existingHeight);
        JTextField lengthField = new JTextField(existingLength);
        JTextField daysField = new JTextField(String.valueOf(parcel.getDaysInDepot()));

        Object[] msg = {
                "Edit Parcel ID: " + parcelID,
                "Weight (kg):", weightField,
                "Width:", widthField,
                "Height:", heightField,
                "Length:", lengthField,
                "Days in Depot:", daysField
        };

        int option = JOptionPane.showConfirmDialog(this, msg, "Edit Parcel", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            log.logInfo("User confirmed parcel edits for ID=" + parcelID);

            String weightText = weightField.getText().trim();
            String widthText = widthField.getText().trim();
            String heightText = heightField.getText().trim();
            String lengthText = lengthField.getText().trim();
            String daysText = daysField.getText().trim();

            if (weightText.isEmpty() || widthText.isEmpty() || heightText.isEmpty() || lengthText.isEmpty() || daysText.isEmpty()) {
                log.logError("Validation failed: Missing required fields for ID=" + parcelID);
                JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double weight, width, height, length;
            int days;

            try {
                weight = Double.parseDouble(weightText);
                width = Double.parseDouble(widthText);
                height = Double.parseDouble(heightText);
                length = Double.parseDouble(lengthText);
                days = Integer.parseInt(daysText);

                if (weight <= 0 || width <= 0 || height <= 0 || length <= 0 || days < 0) {
                    log.logError("Validation failed: Invalid numeric inputs for ID=" + parcelID);
                    JOptionPane.showMessageDialog(this, "Weight, Width, Height, and Length must be positive numbers. Days in Depot cannot be negative.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException e) {
                log.logError("Validation failed: Non-numeric inputs detected for ID=" + parcelID);
                JOptionPane.showMessageDialog(this, "Please enter valid numeric values for Weight, Width, Height, Length, and Days in Depot.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Format dimensions without forcing two decimal places
            DecimalFormat df = new DecimalFormat("#.##");
            String newDimensions = df.format(width) + "x" + df.format(height) + "x" + df.format(length);

            log.logInfo(String.format("Updating parcel: ID=%s, Weight=%.2f, Dimensions=%s, Days=%d", parcelID, weight, newDimensions, days));

            manager.updateParcel(parcelID, weight, newDimensions, days);
            log.logInfo("Parcel updated successfully: ID=" + parcelID);

            refreshParcelTable();
            log.logInfo("Parcel table refreshed after editing.");
        } else {
            log.logInfo("User canceled parcel edits for ID=" + parcelID);
        }
    }

    // Handles the process of searching an existing parcel in the system
    private void searchParcel() {
        log.logInfo("Initiating parcel search.");
        String searchTerm = JOptionPane.showInputDialog(this, "Enter Parcel ID to search:", "Search Parcel", JOptionPane.PLAIN_MESSAGE);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            log.logInfo("Parcel search canceled by user or input was empty.");
            return;
        }

        log.logInfo("Searching for parcel with ID: " + searchTerm.trim());

        Parcel parcel = manager.getParcelMap().findParcel(searchTerm.trim());
        if (parcel == null) {
            log.logError("Parcel not found: ID=" + searchTerm.trim());
            JOptionPane.showMessageDialog(this, "Parcel not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        log.logInfo("Parcel found: " + parcel);

        // Display the found parcel in the table
        parcelTableModel.setRowCount(0);
        parcelTableModel.addRow(new Object[]{
                parcel.getParcelID(),
                parcel.getWeight(),
                parcel.getDimensions(),
                parcel.getStatus(),
                parcel.getDaysInDepot()
        });
        log.logInfo("Parcel details displayed in the table: ID=" + parcel.getParcelID());

        // Update the CurrentParcelPanel with the found parcel
        workerUI.showCurrentParcel(parcel);
        log.logInfo("Parcel displayed in CurrentParcelPanel: ID=" + parcel.getParcelID());
    }

    // Displays a table of collected parcels to the user.
    private void showCollectedParcels() {
        log.logInfo("Displaying collected parcels.");

        List<Parcel> collected = manager.getCollectedParcels();

        if (collected.isEmpty()) {
            log.logInfo("No collected parcels to display.");
            JOptionPane.showMessageDialog(this, "No parcels have been collected yet.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        log.logInfo("Preparing table for displaying collected parcels.");

        DefaultTableModel collectedModel = new DefaultTableModel(
                new String[]{"ID", "Weight (KGs)", "Dimensions (WxHxL)", "Status", "Days in Depot"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };

        for (Parcel p : collected) {
            collectedModel.addRow(new Object[]{
                    p.getParcelID(),
                    p.getWeight(),
                    p.getDimensions(),
                    p.getStatus(),
                    p.getDaysInDepot()
            });
        }

        log.logInfo("Table populated with collected parcels. Total parcels: " + collected.size());

        JTable table = new JTable(collectedModel);
        JScrollPane scroll = new JScrollPane(table);
        JOptionPane.showMessageDialog(this, scroll, "Collected Parcels", JOptionPane.INFORMATION_MESSAGE);

        log.logInfo("Collected parcels displayed to the user.");
    }
}

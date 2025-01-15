package controller;

import model.Parcel;
import model.ParcelMap;
import model.Customer;
import model.QueueOfCustomers;
import util.Log;

import javax.swing.*;
import java.io.*;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Manager {
    private static final double BASE_FEE = 5.00;      // Base fee in GBP
    private static final double WEIGHT_FEE_PER_KG = 0.50; // Weight fee per kg
    private static final double DEPOT_FEE_PER_DAY = 0.20; // Depot fee per day

    private QueueOfCustomers queueOfCustomers = new QueueOfCustomers();
    private ParcelMap parcelMap = new ParcelMap();
    private DepotWorker worker = new DepotWorker("DepotWorker");
    private List<Parcel> collectedParcels = new ArrayList<>();
    private double totalFeesCollected = 0.0;
    private Log log = Log.getInstance();

    // Retrieves the queue of customers in the depot system
    public QueueOfCustomers getQueueOfCustomers() {
        return queueOfCustomers;
    }

    // Retrieves the parcel map in the depot system
    public ParcelMap getParcelMap() {
        return parcelMap;
    }

    // Retrieves the worker in the depot system
    public DepotWorker getWorker() {
        return worker;
    }

    // Retrieves log in the depot system
    public Log getLog() {
        return log;
    }

    // Retrieves the collected parcel in the depot system
    public List<Parcel> getCollectedParcels() {
        return collectedParcels;
    }

    // Adds fee to the total fees collected
    public void addToTotalFees(double fee) {
        totalFeesCollected += fee;
    }

    // Loads customer and parcel data from the given files
    public void loadFiles(String customerFilename, String parcelFilename) {
        try {
            log.logInfo("Attempting to load customer file: " + customerFilename);
            loadCustomers(customerFilename);
            log.logInfo("Customer file loaded successfully: " + customerFilename);
        } catch (Exception e) {
            log.logError("Failed to load customer file: " + customerFilename + ". Error: " + e.getMessage());
        }

        try {
            log.logInfo("Attempting to load parcel file: " + parcelFilename);
            loadParcels(parcelFilename);
            log.logInfo("Parcel file loaded successfully: " + parcelFilename);
        } catch (Exception e) {
            log.logError("Failed to load parcel file: " + parcelFilename + ". Error: " + e.getMessage());
        }
    }

    // Loads customer data
    private void loadCustomers(String filename) {
        log.logInfo("Attempting to load customers from file: " + filename);

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            int queueNumber = 1;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 2) {
                    String name = data[0].trim();
                    String parcelID = data[1].trim();
                    Customer c = new Customer(name, queueNumber++, parcelID);
                    queueOfCustomers.add(c);
                    log.logInfo("Customer added: Name=" + name + ", ParcelID=" + parcelID);
                } else {
                    log.logError("Invalid customer data format: " + line);
                }
            }
            log.logInfo("Customers loaded successfully from file: " + filename);
        } catch (IOException e) {
            log.logError("Error reading customer file: " + filename + ". Error: " + e.getMessage());
        }
    }

    // Loads parcel data
    private void loadParcels(String filename) {
        log.logInfo("Attempting to load parcels from file: " + filename);

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 5) {
                    try {
                        String parcelID = data[0].trim();
                        double weight = Double.parseDouble(data[1].trim());
                        String dims = data[2].trim();
                        String status = data[3].trim();
                        int days = Integer.parseInt(data[4].trim());

                        Parcel p = new Parcel(parcelID, weight, dims, status, days);
                        parcelMap.addParcel(p);

                        log.logInfo("Parcel added: ID=" + parcelID + ", Weight=" + weight + "kg, Dimensions=" + dims +
                                ", Status=" + status + ", DaysInDepot=" + days);
                    } catch (NumberFormatException e) {
                        log.logError("Invalid number format in parcel data: " + line + ". Error: " + e.getMessage());
                    } catch (Exception e) {
                        log.logError("Error adding parcel: " + line + ". Error: " + e.getMessage());
                    }
                } else {
                    log.logError("Invalid parcel data format: " + line);
                }
            }
            log.logInfo("Parcels loaded successfully from file: " + filename);
        } catch (IOException e) {
            log.logError("Error reading parcel file: " + filename + ". Error: " + e.getMessage());
        }
    }

    // Adds a new parcel to the system after validating its details
    public void addNewParcel(Parcel parcel) {
        // Validate Parcel ID
        if (parcel.getParcelID() == null || parcel.getParcelID().isEmpty()) {
            log.logError("Failed to add parcel: Parcel ID is missing or empty.");
            JOptionPane.showMessageDialog(null, "Parcel ID is required!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate Parcel ID format
        if (!parcel.getParcelID().matches("^[XC][0-9]{3}$")) {
            log.logError("Failed to add parcel: Invalid Parcel ID format. Parcel ID: " + parcel.getParcelID());
            JOptionPane.showMessageDialog(null, "Invalid Parcel ID format! Parcel ID must start with 'X' or 'C' followed by 3 digits (e.g., X123 or C123).", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate Weight
        if (parcel.getWeight() <= 0) {
            log.logError("Failed to add parcel: Invalid weight for Parcel ID " + parcel.getParcelID());
            JOptionPane.showMessageDialog(null, "Weight must be greater than 0!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate Dimensions format
        if (parcel.getDimensions() == null || parcel.getDimensions().isEmpty()) {
            log.logError("Failed to add parcel: Dimensions are missing for Parcel ID " + parcel.getParcelID());
            JOptionPane.showMessageDialog(null, "Dimensions are required!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] dims = parcel.getDimensions().split("x");
        if (dims.length != 3) {
            log.logError("Failed to add parcel: Dimensions format incorrect for Parcel ID " + parcel.getParcelID());
            JOptionPane.showMessageDialog(null, "Dimensions must be in the format WidthxHeightxLength (e.g., 10x10x10).", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double width = Double.parseDouble(dims[0]);
            double height = Double.parseDouble(dims[1]);
            double length = Double.parseDouble(dims[2]);

            if (width <= 0 || height <= 0 || length <= 0) {
                log.logError("Failed to add parcel: Dimensions must be positive numbers for Parcel ID " + parcel.getParcelID());
                JOptionPane.showMessageDialog(null, "Width, Height, and Length must be positive numbers.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            log.logError("Failed to add parcel: Non-numeric dimensions for Parcel ID " + parcel.getParcelID());
            JOptionPane.showMessageDialog(null, "Dimensions must contain valid numeric values.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate Days in Depot
        if (parcel.getDaysInDepot() < 0) {
            log.logError("Failed to add parcel: Invalid days in depot for Parcel ID " + parcel.getParcelID());
            JOptionPane.showMessageDialog(null, "Days in depot cannot be negative!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check for duplicate Parcel ID
        if (parcelMap.findParcel(parcel.getParcelID()) != null) {
            log.logError("Failed to add parcel: Duplicate Parcel ID " + parcel.getParcelID());
            JOptionPane.showMessageDialog(null, "Parcel with ID " + parcel.getParcelID() + " already exists!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Add Parcel to ParcelMap
        parcelMap.addParcel(parcel);
        log.logInfo("Parcel successfully added: " + parcel.toString());
        JOptionPane.showMessageDialog(null, "Parcel added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    // Updates the details of an existing parcel in the system
    public void updateParcel(String parcelID, double newWeight, String newDimensions, int newDays) {

        log.logInfo("Attempting to update parcel: " + parcelID);

        Parcel parcel = parcelMap.findParcel(parcelID);
        if (parcel == null) {
            log.logError("Failed to update parcel: Parcel not found - " + parcelID);
            JOptionPane.showMessageDialog(null, "Parcel not found: " + parcelID, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate Weight
        if (newWeight <= 0) {
            log.logError("Failed to update parcel: Invalid weight for Parcel ID " + parcelID);
            JOptionPane.showMessageDialog(null, "Weight must be greater than 0!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate Dimensions format
        if (newDimensions == null || newDimensions.isEmpty()) {
            log.logError("Failed to update parcel: Dimensions are missing for Parcel ID " + parcelID);
            JOptionPane.showMessageDialog(null, "Dimensions are required!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] dims = newDimensions.split("x");
        if (dims.length != 3) {
            log.logError("Failed to update parcel: Dimensions format incorrect for Parcel ID " + parcelID);
            JOptionPane.showMessageDialog(null, "Dimensions must be in the format WidthxHeightxLength (e.g., 10x10x10).", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double width = Double.parseDouble(dims[0]);
            double height = Double.parseDouble(dims[1]);
            double length = Double.parseDouble(dims[2]);

            if (width <= 0 || height <= 0 || length <= 0) {
                log.logError("Failed to update parcel: Dimensions must be positive numbers for Parcel ID " + parcelID);
                JOptionPane.showMessageDialog(null, "Width, Height, and Length must be positive numbers.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            log.logError("Failed to update parcel: Non-numeric dimensions for Parcel ID " + parcelID);
            JOptionPane.showMessageDialog(null, "Dimensions must contain valid numeric values.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate Days in Depot
        if (newDays < 0) {
            log.logError("Failed to update parcel: Invalid days in depot for Parcel ID " + parcelID);
            JOptionPane.showMessageDialog(null, "Days in depot cannot be negative!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Update parcel details
        parcel.setWeight(newWeight);
        parcel.setDimensions(newDimensions);
        parcel.setDaysInDepot(newDays);

        log.logInfo("Parcel updated successfully: " + parcel.toString());
        JOptionPane.showMessageDialog(null, "Parcel updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

        // Notify observers
        parcelMap.notifyParcelMapObservers("ParcelMap");
        log.logInfo("ParcelMap observers notified after updating parcel: " + parcelID);
    }

    // Searches for a parcel in the system by its Parcel ID
    public Parcel searchParcel(String parcelID) {
        log.logInfo("Search initiated for Parcel ID: " + parcelID);

        if (parcelID == null || parcelID.trim().isEmpty()) {
            log.logError("Search failed: Parcel ID is null or empty.");
            JOptionPane.showMessageDialog(null, "Please enter a valid Parcel ID.", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        Parcel parcel = parcelMap.findParcel(parcelID.trim());

        if (parcel == null) {
            log.logError("Search failed: Parcel not found for Parcel ID: " + parcelID);
            JOptionPane.showMessageDialog(null, "Parcel with ID " + parcelID + " not found.", "Not Found", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }

        log.logInfo("Search successful: Parcel found - " + parcel.toString());
        return parcel;
    }

    // Generates a detailed report of the depot system
    public void generateReport(String filename) {
        log.logInfo("Generating report: " + filename);

        // Ensure the "reports" directory exists
        File reportsDir = new File("reports");
        if (!reportsDir.exists()) {
            boolean created = reportsDir.mkdirs();
            if (created) {
                log.logInfo("Reports directory created successfully.");
            } else {
                log.logError("Failed to create reports directory.");
                JOptionPane.showMessageDialog(null, "Failed to create reports directory.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // File path for the report in the "reports" folder
        File reportFile = new File(reportsDir, filename);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportFile))) {
            writer.write("Depot System Report:\n");

            // Collected parcels
            writer.write("Collected Parcels:\n");
            writer.write("--------------------\n");
            if (collectedParcels.isEmpty()) {
                writer.write("No parcels have been collected yet.\n");
                log.logInfo("No collected parcels found for the report.");
            } else {
                for (Parcel p : collectedParcels) {
                    writer.write(p.toString() + "\n");
                    log.logInfo("Collected parcel added to report: " + p.toString());
                }
            }

            writer.write("\n");

            // Uncollected parcels
            writer.write("Uncollected Parcels:\n");
            writer.write("--------------------\n");
            List<Parcel> uncollectedParcels = new ArrayList<>();
            for (Parcel p : parcelMap.getAllParcels()) {
                if ("Pending".equalsIgnoreCase(p.getStatus())) {
                    uncollectedParcels.add(p);
                }
            }
            if (uncollectedParcels.isEmpty()) {
                writer.write("No pending parcels.\n");
                log.logInfo("No uncollected parcels found for the report.");
            } else {
                for (Parcel p : uncollectedParcels) {
                    writer.write(p.toString() + "\n");
                    log.logInfo("Uncollected parcel added to report: " + p.toString());
                }
            }

            writer.write("\n");

            // Summary
            writer.write("Summary:\n");
            writer.write("--------\n");
            writer.write(String.format("Total fees collected: £%.2f\n", totalFeesCollected));
            log.logInfo("Summary section added to report: Total fees collected = £" + totalFeesCollected);

            log.logInfo("Report generated successfully: " + reportFile.getAbsolutePath());
            JOptionPane.showMessageDialog(null, "Report generated successfully at:\n" + reportFile.getAbsolutePath(), "Report Generated", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            log.logError("Error writing report to file: " + reportFile.getAbsolutePath() + ". Error: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error writing report: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Generates a receipt of the depot system
    public String generateReceipt(Parcel parcel, double fee) {
        log.logInfo("Generating receipt for Parcel ID: " + parcel.getParcelID());

        DecimalFormat df = new DecimalFormat("#.##");
        StringBuilder receipt = new StringBuilder();
        receipt.append("----- Parcel Receipt -----\n");
        receipt.append("Parcel ID: ").append(parcel.getParcelID()).append("\n");
        receipt.append("Weight: ").append(df.format(parcel.getWeight())).append(" kg\n");
        receipt.append("Dimensions: ").append(parcel.getDimensions()).append("\n");
        receipt.append("Status: ").append(parcel.getStatus()).append("\n");
        receipt.append("Days in Depot: ").append(parcel.getDaysInDepot()).append("\n");
        receipt.append("--------------------------\n\n");
        receipt.append("----- Price Breakdown -----\n");
        receipt.append(String.format("Base Fee: £%.2f\n", BASE_FEE));
        receipt.append(String.format("Weight Fee (%.2f kg x £%.2f): £%.2f\n",
                parcel.getWeight(), WEIGHT_FEE_PER_KG, parcel.getWeight() * WEIGHT_FEE_PER_KG));
        receipt.append(String.format("Depot Fee (%d days x £%.2f): £%.2f\n",
                parcel.getDaysInDepot(), DEPOT_FEE_PER_DAY, parcel.getDaysInDepot() * DEPOT_FEE_PER_DAY));
        double totalBeforeDiscount = BASE_FEE + (parcel.getWeight() * WEIGHT_FEE_PER_KG) +
                (parcel.getDaysInDepot() * DEPOT_FEE_PER_DAY);
        double discount = totalBeforeDiscount - fee;
        receipt.append(String.format("Total Before Discount: £%.2f\n", totalBeforeDiscount));
        receipt.append(String.format("Discount: £%.2f\n", discount));
        receipt.append("--------------------------\n");
        receipt.append(String.format("Total Fee: £%.2f\n", fee));
        receipt.append("--------------------------\n");
        receipt.append("Thank you for using our service!\n");

        // Define the directory to save receipts
        String receiptsDirPath = "receipts";
        File receiptsDir = new File(receiptsDirPath);
        if (!receiptsDir.exists()) {
            boolean dirCreated = receiptsDir.mkdirs();
            if (dirCreated) {
                log.logInfo("Receipts directory created successfully: " + receiptsDirPath);
            } else {
                log.logError("Failed to create receipts directory: " + receiptsDirPath);
            }
        }

        // Generate a timestamp for the filename
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = LocalDateTime.now().format(dtf);

        // Define the receipt file name
        String receiptFileName = String.format("Receipt_%s_%s.txt", parcel.getParcelID(), timestamp);
        File receiptFile = new File(receiptsDir, receiptFileName);

        // Write the receipt to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(receiptFile))) {
            writer.write(receipt.toString());
            log.logInfo("Receipt saved successfully: " + receiptFile.getAbsolutePath());
        } catch (IOException e) {
            log.logError("Error writing receipt to file: " + e.getMessage());
        }

        log.logInfo("Receipt generated for Parcel ID: " + parcel.getParcelID());
        return receipt.toString();
    }

    // Adds a customer to the queue if all validations pass
    public boolean addCustomer(String name, String parcelID) {
        log.logInfo("Attempting to add customer: Name=" + name + ", ParcelID=" + parcelID);

        // Validate name
        if (name == null || name.trim().isEmpty()) {
            log.logError("Failed to add customer: Name is missing.");
            throw new IllegalArgumentException("Customer name cannot be empty!");
        }

        // Validate Parcel ID format
        if (parcelID == null || !parcelID.matches("^[XC]\\d{3}$")) {
            log.logError("Failed to add customer: Invalid Parcel ID format - " + parcelID);
            throw new IllegalArgumentException("Invalid Parcel ID format! It must start with 'X' or 'C' followed by 3 digits (e.g., X123 or C456).");
        }

        // Check if Parcel ID is already in the queue
        boolean existsInQueue = queueOfCustomers.getCustomerQueue().stream()
                .anyMatch(customer -> customer.getParcelID().equals(parcelID));
        if (existsInQueue) {
            log.logError("Failed to add customer: Parcel ID " + parcelID + " is already in the queue.");
            throw new IllegalArgumentException("Parcel ID " + parcelID + " is already in the queue!");
        }

        // Verify if the parcel exists
        Parcel parcel = parcelMap.findParcel(parcelID);
        if (parcel == null) {
            log.logError("Failed to add customer: Parcel ID " + parcelID + " not found.");
            throw new IllegalArgumentException("Parcel with ID " + parcelID + " not found!");
        }

        // Check if the parcel is already collected
        if ("Collected".equalsIgnoreCase(parcel.getStatus())) {
            log.logError("Failed to add customer: Parcel ID " + parcelID + " has already been collected.");
            throw new IllegalArgumentException("Parcel ID " + parcelID + " has already been collected!");
        }

        // Add the customer to the queue
        int queueNumber = queueOfCustomers.size() + 1;
        Customer newCustomer = new Customer(name.trim(), queueNumber, parcelID);
        queueOfCustomers.add(newCustomer);

        log.logInfo("Customer successfully added: Name=" + name.trim() + ", ParcelID=" + parcelID);
        return true;
    }

}

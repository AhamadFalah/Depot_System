package controller;

import model.Parcel;
import model.ParcelMap;
import model.Customer;
import model.QueueOfCustomers;
import util.Log;
import util.ParcelValidationException;

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


    public QueueOfCustomers getQueueOfCustomers() {
        return queueOfCustomers;
    }


    public ParcelMap getParcelMap() {
        return parcelMap;
    }

    public DepotWorker getWorker() {
        return worker;
    }

    public Log getLog() {
        return log;
    }

    public List<Parcel> getCollectedParcels() {
        return collectedParcels;
    }


    public void addToTotalFees(double fee) {
        totalFeesCollected += fee;
    }

    public void loadFiles(String customerFilename, String parcelFilename) {
        log.logEvent("Attempting to load customer file: " + customerFilename);
        loadCustomers(customerFilename);
        log.logEvent("Customer file loaded: " + customerFilename);

        log.logEvent("Attempting to load parcel file: " + parcelFilename);
        loadParcels(parcelFilename);
        log.logEvent("Parcel file loaded: " + parcelFilename);
    }

    private void loadCustomers(String filename) {
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
                }
            }
            System.out.println("Customers loaded successfully.");
        } catch (IOException e) {
            log.logEvent("Error reading customer file: " + e.getMessage());
            System.err.println("Error reading customer file: " + e.getMessage());
        }
    }

    private void loadParcels(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 5) {
                    String parcelID = data[0].trim();
                    double weight = Double.parseDouble(data[1].trim());
                    String dims = data[2].trim();
                    String status = data[3].trim();
                    int days = Integer.parseInt(data[4].trim());

                    Parcel p = new Parcel(parcelID, weight, dims, status, days);
                    parcelMap.addParcel(p);
                }
            }
            System.out.println("Parcels loaded successfully.");
        } catch (IOException e) {
            log.logEvent("Error reading parcel file: " + e.getMessage());
            System.err.println("Error reading parcel file: " + e.getMessage());
        }
    }

    public void addNewParcel(Parcel parcel) {
        // Validate Parcel ID
        if (parcel.getParcelID() == null || parcel.getParcelID().isEmpty()) {
            log.logEvent("Failed to add parcel: Parcel ID is missing or empty.");
            JOptionPane.showMessageDialog(null, "Parcel ID is required!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate Parcel ID format
        if (!parcel.getParcelID().matches("^[XC][0-9]{3}$")) {
            log.logEvent("Failed to add parcel: Invalid Parcel ID format. Parcel ID: " + parcel.getParcelID());
            JOptionPane.showMessageDialog(null, "Invalid Parcel ID format! Parcel ID must start with 'X' or 'C' followed by 3 digits (e.g., X123 or C123).", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate Weight
        if (parcel.getWeight() <= 0) {
            log.logEvent("Failed to add parcel: Invalid weight for Parcel ID " + parcel.getParcelID());
            JOptionPane.showMessageDialog(null, "Weight must be greater than 0!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate Dimensions format
        if (parcel.getDimensions() == null || parcel.getDimensions().isEmpty()) {
            log.logEvent("Failed to add parcel: Dimensions are missing for Parcel ID " + parcel.getParcelID());
            JOptionPane.showMessageDialog(null, "Dimensions are required!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] dims = parcel.getDimensions().split("x");
        if (dims.length != 3) {
            log.logEvent("Failed to add parcel: Dimensions format incorrect for Parcel ID " + parcel.getParcelID());
            JOptionPane.showMessageDialog(null, "Dimensions must be in the format WidthxHeightxLength (e.g., 10x10x10).", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double width = Double.parseDouble(dims[0]);
            double height = Double.parseDouble(dims[1]);
            double length = Double.parseDouble(dims[2]);

            if (width <= 0 || height <= 0 || length <= 0) {
                log.logEvent("Failed to add parcel: Dimensions must be positive numbers for Parcel ID " + parcel.getParcelID());
                JOptionPane.showMessageDialog(null, "Width, Height, and Length must be positive numbers.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            log.logEvent("Failed to add parcel: Non-numeric dimensions for Parcel ID " + parcel.getParcelID());
            JOptionPane.showMessageDialog(null, "Dimensions must contain valid numeric values.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate Days in Depot
        if (parcel.getDaysInDepot() < 0) {
            log.logEvent("Failed to add parcel: Invalid days in depot for Parcel ID " + parcel.getParcelID());
            JOptionPane.showMessageDialog(null, "Days in depot cannot be negative!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check for duplicate Parcel ID
        if (parcelMap.findParcel(parcel.getParcelID()) != null) {
            log.logEvent("Failed to add parcel: Duplicate Parcel ID " + parcel.getParcelID());
            JOptionPane.showMessageDialog(null, "Parcel with ID " + parcel.getParcelID() + " already exists!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Add Parcel to ParcelMap
        parcelMap.addParcel(parcel);
        log.logEvent("Parcel successfully added: " + parcel.toString());
        JOptionPane.showMessageDialog(null, "Parcel added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public void updateParcel(String parcelID, double newWeight, String newDimensions, int newDays) {
        Parcel parcel = parcelMap.findParcel(parcelID);
        if (parcel == null) {
            log.logEvent("Failed to update parcel: Parcel not found - " + parcelID);
            JOptionPane.showMessageDialog(null, "Parcel not found: " + parcelID, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate Weight
        if (newWeight <= 0) {
            log.logEvent("Failed to update parcel: Invalid weight for Parcel ID " + parcelID);
            JOptionPane.showMessageDialog(null, "Weight must be greater than 0!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate Dimensions format
        if (newDimensions == null || newDimensions.isEmpty()) {
            log.logEvent("Failed to update parcel: Dimensions are missing for Parcel ID " + parcelID);
            JOptionPane.showMessageDialog(null, "Dimensions are required!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] dims = newDimensions.split("x");
        if (dims.length != 3) {
            log.logEvent("Failed to update parcel: Dimensions format incorrect for Parcel ID " + parcelID);
            JOptionPane.showMessageDialog(null, "Dimensions must be in the format WidthxHeightxLength (e.g., 10x10x10).", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double width = Double.parseDouble(dims[0]);
            double height = Double.parseDouble(dims[1]);
            double length = Double.parseDouble(dims[2]);

            if (width <= 0 || height <= 0 || length <= 0) {
                log.logEvent("Failed to update parcel: Dimensions must be positive numbers for Parcel ID " + parcelID);
                JOptionPane.showMessageDialog(null, "Width, Height, and Length must be positive numbers.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            log.logEvent("Failed to update parcel: Non-numeric dimensions for Parcel ID " + parcelID);
            JOptionPane.showMessageDialog(null, "Dimensions must contain valid numeric values.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate Days in Depot
        if (newDays < 0) {
            log.logEvent("Failed to update parcel: Invalid days in depot for Parcel ID " + parcelID);
            JOptionPane.showMessageDialog(null, "Days in depot cannot be negative!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Update parcel details
        parcel.setWeight(newWeight);
        parcel.setDimensions(newDimensions);
        parcel.setDaysInDepot(newDays);
        log.logEvent("Parcel updated: " + parcel.toString());
        JOptionPane.showMessageDialog(null, "Parcel updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

        // Notify observers
        parcelMap.notifyParcelMapObservers("ParcelMap");
    }

    public Parcel searchParcel(String parcelID) {
        if (parcelID == null || parcelID.trim().isEmpty()) {
            log.logEvent("Search attempted with null or empty Parcel ID.");
            JOptionPane.showMessageDialog(null, "Please enter a valid Parcel ID.", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        Parcel parcel = parcelMap.findParcel(parcelID.trim());

        if (parcel == null) {
            log.logEvent("Parcel not found: " + parcelID);
            JOptionPane.showMessageDialog(null, "Parcel with ID " + parcelID + " not found.", "Not Found", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }

        log.logEvent("Parcel found: " + parcel.toString());
        return parcel;
    }

    public void generateReport(String filename) {
        log.logEvent("Generating report: " + filename);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("Depot System Report:\n");

            // Collected parcels
            writer.write("Collected Parcels:\n");
            writer.write("--------------------\n");
            if (collectedParcels.isEmpty()) {
                writer.write("No parcels have been collected yet.\n");
            } else {
                for (Parcel p : collectedParcels) {
                    writer.write(p.toString() + "\n");
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
            } else {
                for (Parcel p : uncollectedParcels) {
                    writer.write(p.toString() + "\n");
                }
            }

            writer.write("\n");

            // Summary
            writer.write("Summary:\n");
            writer.write("--------\n");
            writer.write(String.format("Total fees collected: £%.2f\n", totalFeesCollected));

            log.logEvent("Report generated successfully: " + filename);
            JOptionPane.showMessageDialog(null, "Report generated successfully at:\n" + new File(filename).getAbsolutePath(), "Report Generated", JOptionPane.INFORMATION_MESSAGE);
            System.out.println("Report generated successfully: " + filename);
        } catch (IOException e) {
            log.logEvent("Error writing report: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error writing report: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("Error writing report: " + e.getMessage());
        }
    }

    public String generateReceipt(Parcel parcel, double fee) {
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
            if (!dirCreated) {
                log.logEvent("Failed to create receipts directory.");
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
            log.logEvent("Receipt saved successfully: " + receiptFile.getAbsolutePath());
        } catch (IOException e) {
            log.logEvent("Error writing receipt to file: " + e.getMessage());
        }

        return receipt.toString();
    }

    public void addCustomerFromKeyboard(String name, String parcelID) {
        log.logEvent("Attempting to add customer: " + name + " with Parcel ID: " + parcelID);

        Parcel parcel = parcelMap.findParcel(parcelID);
        if (parcel == null) {
            log.logEvent("Failed to add customer: Parcel ID " + parcelID + " not found.");
            JOptionPane.showMessageDialog(null, "Parcel not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if ("Collected".equalsIgnoreCase(parcel.getStatus())) {
            log.logEvent("Failed to add customer: Parcel ID " + parcelID + " has already been collected.");
            JOptionPane.showMessageDialog(null, "Parcel is already collected!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int queueNumber = queueOfCustomers.size() + 1;
        Customer newCustomer = new Customer(name, queueNumber, parcelID);
        queueOfCustomers.add(newCustomer);

        log.logEvent("Customer successfully added: " + name + " with Parcel ID: " + parcelID);
        JOptionPane.showMessageDialog(null, "Customer added successfully to the queue!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

}

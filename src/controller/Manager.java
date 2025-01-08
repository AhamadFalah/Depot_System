package controller;

import model.Customer;
import model.Parcel;
import model.ParcelMap;
import model.QueueOfCustomers;
import util.Log;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Manager {
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

    public double getTotalFeesCollected() {
        return totalFeesCollected;
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
            Log.getInstance().logEvent("Failed to add parcel: Parcel ID is missing or empty.");
            JOptionPane.showMessageDialog(null, "Parcel ID is required!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate Parcel ID format
        if (!parcel.getParcelID().matches("^[XC][0-9]{3}$")) {
            Log.getInstance().logEvent("Failed to add parcel: Invalid Parcel ID format. Parcel ID: " + parcel.getParcelID());
            JOptionPane.showMessageDialog(null, "Invalid Parcel ID format! Parcel ID must start with 'X' or 'C' followed by 3 digits (e.g., X123 or C123).", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate Weight
        if (parcel.getWeight() <= 0) {
            Log.getInstance().logEvent("Failed to add parcel: Invalid weight for Parcel ID " + parcel.getParcelID());
            JOptionPane.showMessageDialog(null, "Weight must be greater than 0!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate Dimensions
        if (parcel.getDimensions() == null || parcel.getDimensions().isEmpty()) {
            Log.getInstance().logEvent("Failed to add parcel: Dimensions are missing for Parcel ID " + parcel.getParcelID());
            JOptionPane.showMessageDialog(null, "Dimensions are required!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate Days in Depot
        if (parcel.getDaysInDepot() < 0) {
            Log.getInstance().logEvent("Failed to add parcel: Invalid days in depot for Parcel ID " + parcel.getParcelID());
            JOptionPane.showMessageDialog(null, "Days in depot cannot be negative!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check for duplicate Parcel ID
        if (parcelMap.findParcel(parcel.getParcelID()) != null) {
            Log.getInstance().logEvent("Failed to add parcel: Duplicate Parcel ID " + parcel.getParcelID());
            JOptionPane.showMessageDialog(null, "Parcel with ID " + parcel.getParcelID() + " already exists!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Add Parcel to ParcelMap
        parcelMap.addParcel(parcel);
        Log.getInstance().logEvent("Parcel successfully added: " + parcel.toString());
        JOptionPane.showMessageDialog(null, "Parcel added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }


    public void generateReport(String filename) {
        log.logEvent("Generating report: " + filename);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("Depot System Report:\n");

            // Collected parcels
            writer.write("\nCollected Parcels:\n");
            for (Parcel p : collectedParcels) {
                writer.write(p.toString() + "\n");
            }

            // Uncollected
            writer.write("\nUncollected Parcels:\n");
            for (Parcel p : parcelMap.getAllParcels()) {
                if ("Pending".equalsIgnoreCase(p.getStatus())) {
                    writer.write(p.toString() + "\n");
                }
            }

            // Summary
            writer.write("\nSummary:\n");
            writer.write("Total fees collected: £" + String.format("%.2f", totalFeesCollected) + "\n");

            log.logEvent("Report generated successfully: " + filename);
            System.out.println("Report generated successfully: " + filename);
        } catch (IOException e) {
            log.logEvent("Error writing report: " + e.getMessage());
            System.err.println("Error writing report: " + e.getMessage());
        }
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


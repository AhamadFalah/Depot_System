package com.example.depot_system.controller;

import com.example.depot_system.model.Customer;
import com.example.depot_system.model.Parcel;
import com.example.depot_system.model.ParcelMap;
import com.example.depot_system.model.QueueOfCustomers;
import com.example.depot_system.util.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Manager {
    private QueueOfCustomers queueOfCustomers = new QueueOfCustomers();
    private ParcelMap parcelMap = new ParcelMap();
    private DepotWorker worker = new DepotWorker("DepotWorker1");
    private List<Parcel> collectedParcels = new ArrayList<>();
    private List<Customer> collectedCustomers = new ArrayList<>();
    private double totalFeesCollected = 0.0;
    Log log = Log.getInstance();

    public void loadFiles(String customerFilename, String parcelFilename) {
        log.logEvent("Loading customers from file: " + customerFilename);
        loadCustomers(customerFilename);
        log.logEvent("Finished loading customers from file: " + customerFilename);

        log.logEvent("Loading parcels from file: " + parcelFilename);
        loadParcels(parcelFilename);
        log.logEvent("finished loading parcels from file: " + parcelFilename);
    }

    private void loadCustomers(String customerFilename) {
        try (BufferedReader br = new BufferedReader(new FileReader(customerFilename))) {
            String line;
            int queueNumber = 1;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 2) {
                    String name = data[0].trim();
                    String parcelID = data[1].trim();
                    Customer customer = new Customer(name, queueNumber++, parcelID);
                    queueOfCustomers.add(customer);
                } else {
                    System.err.println("Invalid data format in line: " + line + ". Skipping this customer.");
                }
            }
            System.out.println("Customers loaded successfully.");
        } catch (IOException e) {
            System.err.println("Error reading customer file: " + e.getMessage());
        }
    }

    private void loadParcels(String parcelFilename) {
        try (BufferedReader br = new BufferedReader(new FileReader(parcelFilename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                String parcelID = data[0].trim();
                double weight = Double.parseDouble(data[1].trim());
                String dimensions = data[2].trim();
                String status = data[3].trim();
                int daysInDepot = Integer.parseInt(data[4].trim());
                Parcel parcel = new Parcel(parcelID, weight, dimensions, status, daysInDepot);
                parcelMap.addParcel(parcel);
            }
            System.out.println("Parcels loaded successfully.");
        } catch (IOException e) {
            System.err.println("Error reading parcel file: " + e.getMessage());
        }
    }

    public void processNextCustomer() {
        if (queueOfCustomers.isEmpty()) {
            log.logEvent("No customers in the queue.");
            System.out.println("No customers in the queue.");
            return;
        }
        Customer customer = queueOfCustomers.getCustomer();
        Parcel parcel = parcelMap.findParcel(customer.getParcelID());

        if (parcel != null && "Pending".equals(parcel.getStatus())) {
            double fee = worker.processCustomer(customer, parcelMap);
            collectedParcels.add(parcel);
            collectedCustomers.add(customer);
            totalFeesCollected += fee;
            log.logEvent("Processed customer: " + customer.getName() + " with Parcel ID: " + customer.getParcelID());
            System.out.println("Customer processed successfully: " + customer.getName());
        } else {
            log.logEvent("Skipped customer: " + customer.getName() + ". Parcel not found or already collected.");
            System.out.println("Parcel not found or already collected for customer: " + customer.getName());
        }
    }

    public void addNewParcel(Parcel parcel) {
        if (parcelMap.findParcel(parcel.getParcelID()) != null) {
            log.logEvent("Attempted to add a duplicate parcel: " + parcel.getParcelID());
            System.out.println("Parcel already exists for customer: " + parcel.getParcelID());
        } else {
            parcelMap.addParcel(parcel);
            log.logEvent("Added new parcel: " + parcel.getParcelID());
            System.out.println("New parcel added: " + parcel);
        }
    }

    public void addNewCustomer(Customer customer) {
        int queueNumber = queueOfCustomers.size() + 1;
        customer.setQueueNumber(queueNumber);
        queueOfCustomers.add(customer);
        log.logEvent("Added new customer: " + customer.getName() + " with Parcel ID: " + customer.getParcelID());
        System.out.println("New customer added: " + customer);
    }

    public void generateReport(String outputFilename) {

        log.logEvent("Starting report generation: " + outputFilename);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilename))) {
            writer.write("Depot System Report:\n");

            writer.write("\nCollected Parcels:\n");
            for (Parcel parcel : collectedParcels) {
                double fee = worker.calculateFee(parcel);
                writer.write(String.format("Parcel ID: %s, Fee: £%.2f, Status: %s\n", parcel.getParcelID(), fee, parcel.getStatus()));
            }

            writer.write("\nUncollected Parcels:\n");
            for (Parcel parcel : parcelMap.getAllParcels()) {
                if ("Pending".equals(parcel.getStatus())) {
                    writer.write(parcel.toString() + "\n");
                }
            }

            writer.write("\nSummary:\n");
            long parcelsOverThreshold = parcelMap.getAllParcels().stream().filter(parcel -> parcel.getDaysInDepot() > 5 && "Pending".equals(parcel.getStatus())).count();
            writer.write("Parcels in depot for more than 5 days: " + parcelsOverThreshold + "\n");

            writer.write("Total fees collected: £" + String.format("%.2f", totalFeesCollected) + "\n");

            log.logEvent("Report generated successfully: " + outputFilename);
            System.out.println("Report generated: " + outputFilename);
        } catch (IOException e) {
            log.logEvent("Error writing report: " + e.getMessage());
            System.err.println("Error writing report: " + e.getMessage());
        }
    }

    public void addCustomerFromKeyboard() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter customer name:");
        String name = scanner.nextLine().trim();

        System.out.println("Enter parcel ID:");
        String parcelID = scanner.nextLine().trim();

        // Check if the parcel exists
        Parcel parcel = parcelMap.findParcel(parcelID);

        if (parcel == null) {
            log.logEvent("Failed to add customer: " + name + ". Parcel ID: " + parcelID + " not found.");
            System.out.println("No parcel found with the given Parcel ID: " + parcelID);
            return;
        }

        if ("Collected".equalsIgnoreCase(parcel.getStatus())) {
            log.logEvent("Failed to add customer: " + name + ". Parcel ID: " + parcelID + " already collected.");
            System.out.println("The parcel with ID " + parcelID + " has already been collected.");
            return;
        }

        if (!parcel.getStatus().equalsIgnoreCase("Pending")) {
            log.logEvent("Failed to add customer: " + name + ". Parcel ID: " + parcelID + " is not marked as 'Pending'.");
            System.out.println("The parcel with ID " + parcelID + " is not marked as 'Pending'.");
            return;
        }

        Customer customer = new Customer(name, parcelID);
        queueOfCustomers.add(customer);
        log.logEvent("Customer added by keyboard: " + customer.getName() + " with Parcel ID: " + customer.getParcelID());
        System.out.println("Customer added to the queue: " + customer);
    }

    public void processCustomerByParcelId(String parcelId) {
        log.logEvent("Attempting to process customer with Parcel ID: " + parcelId);

        Customer customerToProcess = null;
        for (Customer customer : queueOfCustomers.getCustomerQueue()) {
            if (customer.getParcelID().equals(parcelId)) {
                customerToProcess = customer;
                break;
            }
        }

        if (customerToProcess == null) {
            log.logEvent("No customer found with Parcel ID: " + parcelId);
            System.out.println("No customer found with parcel ID: " + parcelId);
        } else {
            log.logEvent("Found customer: " + customerToProcess.getName() + " with Parcel ID: " + parcelId);

            double fee = worker.processCustomer(customerToProcess, parcelMap);
            queueOfCustomers.getCustomerQueue().remove(customerToProcess);

            collectedCustomers.add(customerToProcess);
            totalFeesCollected += fee;

            log.logEvent("Customer processed successfully: " + customerToProcess.getName());
            log.logEvent("Customer removed from the queue: " + customerToProcess.getName());
            log.logEvent("Fee collected: £" + String.format("%.2f", fee));

            System.out.println("Customer processed and removed from the queue: " + customerToProcess);
        }
    }


    public static void main(String[] args) {
        Manager manager = new Manager();
        Log log = Log.getInstance();

        log.logEvent("Application started.");
        manager.loadFiles("Custs (1).csv", "Parcels.csv");


        Parcel newParcel = new Parcel("X123", 10.0, "20x15x10", "Pending", 2);
        manager.addNewParcel(newParcel);

        Customer newCustomer = new Customer("Falah Ahamad", "X123");
        manager.addNewCustomer(newCustomer);

        manager.processNextCustomer();

        manager.processCustomerByParcelId("X123");

        String reportFileName = "DepotReportss.txt";
        manager.generateReport(reportFileName);

        String logFileName = "DepotSystemLogs.txt";
        log.saveToFile(logFileName);
        System.out.println("Logs saved to file: " + logFileName);
        log.logEvent("Application ended.");
    }

}


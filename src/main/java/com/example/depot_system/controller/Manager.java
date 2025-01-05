package com.example.depot_system.controller;

import com.example.depot_system.model.Customer;
import com.example.depot_system.model.Parcel;
import com.example.depot_system.model.ParcelMap;
import com.example.depot_system.model.QueueOfCustomers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Manager {
    private QueueOfCustomers queueOfCustomers = new QueueOfCustomers();
    private ParcelMap parcelMap = new ParcelMap();
    private DepotWorker worker = new DepotWorker("DepotWorker1");

    public void loadFiles(String customerFilename, String parcelFilename) {
        loadCustomers(customerFilename);
        loadParcels(parcelFilename);
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
            System.out.println("No customers in the queue.");
        } else {
            Customer customer = queueOfCustomers.getCustomer();
            worker.processCustomer(customer, parcelMap);
        }
    }

    public void addNewParcel(Parcel parcel) {
        if(parcelMap.findParcel(parcel.getParcelID()) != null){
            System.out.println("Parcel already exists for customer: " + parcel.getParcelID());
        }else {
            parcelMap.addParcel(parcel);
            System.out.println("New parcel added: " + parcel);
        }
    }

    public void addNewCustomer(Customer customer) {
        int queueNumber = queueOfCustomers.size() + 1;
        customer.setQueueNumber(queueNumber);
        queueOfCustomers.add(customer);
        System.out.println("New customer added: " + customer);
    }




    public static void main(String[] args) {
        Manager manager = new Manager();
        manager.loadFiles("Custs (1).csv", "Parcels.csv");
        manager.processNextCustomer();
        manager.addNewCustomer(new Customer("Falah Ahamad", "X126"));
        manager.addNewParcel(new Parcel("X126", 5.5, "10x10x10", "Pending", 3));
    }
}

package com.example.depot_system.controller;

import com.example.depot_system.model.Customer;
import com.example.depot_system.model.Parcel;
import com.example.depot_system.model.ParcelMap;
import com.example.depot_system.model.QueueOfCustomers;

public class Manager {
    private QueueOfCustomers queueOfCustomers = new QueueOfCustomers();
    private ParcelMap parcelMap = new ParcelMap();
    private DepotWorker worker = new DepotWorker("DepotWorker1");

//    public void loadFiles(String customerFilename, String parcelFilename) {
//    }

//    public void processNextCustomer() {
//    }

//    public void addNewCustomer(Customer customer) {
//    }

//    public void addNewParcel(Parcel parcel) {
//    }

//    public static void main(String[] args) {
//    }
}

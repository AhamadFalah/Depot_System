package com.example.depot_system.controller;

import com.example.depot_system.model.Customer;
import com.example.depot_system.model.Parcel;

public class DepotWorker {
    private String name;
    private Customer currentCustomer;
    private Parcel currentParcel;

    public DepotWorker(String name) {
        this.name = name;
    }

//    public void processCustomer(Customer customer, Parcel parcel) {
//    }

//    public double calculateFee(Parcel parcel) {
//    }

//    public void releaseParcel() {
//    }
}

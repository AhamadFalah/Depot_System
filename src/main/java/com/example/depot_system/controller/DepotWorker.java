package com.example.depot_system.controller;

import com.example.depot_system.model.Customer;
import com.example.depot_system.model.Parcel;
import com.example.depot_system.model.ParcelMap;

public class DepotWorker {
    private String name;
    private Customer currentCustomer;
    private Parcel currentParcel;

    public DepotWorker(String name) {
        this.name = name;
    }

    public void processCustomer(Customer customer, ParcelMap parcelMap) {

        Parcel parcel = parcelMap.findParcel(customer.getParcelID());

        if (parcel == null) {
            System.out.println("No parcel found for customer: " + customer.getName());
        } else {
            System.out.println("Processing customer: " + customer.getName());
            System.out.println("Parcel details: " + parcel);

            double fee = calculateFee(parcel);
//            System.out.println("Total fee: £" + fee);

            System.out.print("Confirm collection (yes/no): ");
            String response = new java.util.Scanner(System.in).nextLine().trim();

            if ("yes".equalsIgnoreCase(response)) {
                releaseParcel(parcel, parcelMap, customer);
            } else {
                System.out.println("Parcel not collected.");
            }
        }
    }

    public void releaseParcel(Parcel parcel, ParcelMap parcelMap, Customer customer) {
        // Update parcel status
        parcel.setStatus("Collected");
        parcelMap.updateParcelStatus(parcel.getParcelID(), "Collected");

        System.out.println("Parcel released successfully for customer: " + customer.getName());
        System.out.println("Updated parcel status: " + parcel);

    }

    public double calculateFee(Parcel parcel) {

        double baseFee = 5.0;
        System.out.println("Base fee: £" + baseFee);

        double weightFee = parcel.getWeight() * 0.5;
        System.out.println("Weight fee(£0.5 per kg): £" + weightFee);

        double sizeFee = 0.0;

        try {
            String[] dimensions = parcel.getDimenions().split("x");
            if (dimensions.length == 3) {
                double volume = Double.parseDouble(dimensions[0]) * Double.parseDouble(dimensions[1]) * Double.parseDouble(dimensions[2]);
                sizeFee = volume * 0.001;
                System.out.println("Size fee: £" + sizeFee);
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid dimnsions format for parcel: " + parcel.getParcelID());
        }

        double depotFee = parcel.getDaysInDepot() * 0.2;
        System.out.println("Depot stay fee (£0.2 per day): £" + depotFee);

        double totalFee = baseFee + weightFee + sizeFee + depotFee;
        System.out.println("Total fee before discount: £" + totalFee);

        double discount = calculateDiscount(parcel.getParcelID(), totalFee);
        System.out.println("Discuont applied: £" + discount);

        double finalFee = totalFee - discount;
        System.out.println("Final fee after discount: £" + finalFee);

        return finalFee;
    }

    public double calculateDiscount(String parcelID, double totalFee) {
        double percentage = 0.0;

        if (parcelID.startsWith("X") && parcelID.endsWith("0")) {
            percentage = 20.0; // 20% discount for parcel ID ending with 0
        } else if (parcelID.startsWith("X") && parcelID.endsWith("5")) {
            percentage = 10.0; // 10% discount for parcel ID ending with 5
        } else if (parcelID.startsWith("C")) {
            percentage = 5.0; // 5% discount for parcel ID starting with C
        }

        double discount = (percentage / 100.0) * totalFee;

        System.out.println("Discount (" + percentage + "%) for parcel ID " + parcelID + ": £" + discount);

        return discount;
    }




}

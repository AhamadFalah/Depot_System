package com.example.depot_system.controller;

import com.example.depot_system.model.Customer;
import com.example.depot_system.model.Parcel;
import com.example.depot_system.model.ParcelMap;
import com.example.depot_system.util.Log;

public class DepotWorker {
    private String name;
    private Customer currentCustomer;
    private Parcel currentParcel;
    private Log log = Log.getInstance();

    public DepotWorker(String name) {
        this.name = name;
    }

    public double processCustomer(Customer customer, ParcelMap parcelMap) {
        log.logEvent("Started processing customer: " + customer.getName() + " with Parcel ID: " + customer.getParcelID());

        Parcel parcel = parcelMap.findParcel(customer.getParcelID());

        if (parcel == null) {
            log.logEvent("No parcel found for customer: " + customer.getName());
            System.out.println("No parcel found for customer: " + customer.getName());
        } else {
            log.logEvent("Parcel found for customer: " + customer.getName() + " - " + parcel.toString());
            System.out.println("Processing customer: " + customer.getName());
            System.out.println("Parcel details: " + parcel);

            double fee = calculateFee(parcel);
            log.logEvent("Calculated fee for parcel ID " + parcel.getParcelID() + ": £" + fee);

//            System.out.println("Total fee: £" + fee);

            System.out.print("Confirm collection (yes/no): ");
            String response = new java.util.Scanner(System.in).nextLine().trim();

            if ("yes".equalsIgnoreCase(response)) {
                releaseParcel(parcel, parcelMap, customer);
                log.logEvent("Parcel collected by customer: " + customer.getName());
            } else {
                System.out.println("Parcel not collected.");
                log.logEvent("Parcel not collected by customer: " + customer.getName());
            }
            return fee;
        }
        return 0;
    }

    public void releaseParcel(Parcel parcel, ParcelMap parcelMap, Customer customer) {
        // Update parcel status
        log.logEvent("Releasing parcel: " + parcel.getParcelID() + " for customer: " + customer.getName());
        parcel.setStatus("Collected");
        parcelMap.updateParcelStatus(parcel.getParcelID(), "Collected");

        System.out.println("Parcel released successfully for customer: " + customer.getName());
        System.out.println("Updated parcel status: " + parcel);
        log.logEvent("Parcel status updated to 'Collected': " + parcel.getParcelID());

    }

    public double calculateFee(Parcel parcel) {
        log.logEvent("Calculating fee for parcel: " + parcel.getParcelID());

        double baseFee = 5.0;
        log.logEvent("Base fee: £" + baseFee);
        System.out.println("Base fee: £" + baseFee);

        double weightFee = parcel.getWeight() * 0.5;
        log.logEvent("Weight fee (£0.5 per kg): £" + weightFee);
        System.out.println("Weight fee(£0.5 per kg): £" + weightFee);

        double sizeFee = 0.0;
        try {
            String[] dimensions = parcel.getDimenions().split("x");
            if (dimensions.length == 3) {
                double volume = Double.parseDouble(dimensions[0]) * Double.parseDouble(dimensions[1]) * Double.parseDouble(dimensions[2]);
                sizeFee = volume * 0.001;
                log.logEvent("Size fee based on volume: £" + sizeFee);
                System.out.println("Size fee: £" + sizeFee);
            }
        } catch (NumberFormatException e) {
            log.logEvent("Invalid dimensions format for parcel: " + parcel.getParcelID());
            System.err.println("Invalid dimensions format for parcel: " + parcel.getParcelID());
        }

        double depotFee = parcel.getDaysInDepot() * 0.2;
        log.logEvent("Depot stay fee (£0.2 per day): £" + depotFee);
        System.out.println("Depot stay fee (£0.2 per day): £" + depotFee);

        double totalFee = baseFee + weightFee + sizeFee + depotFee;
        log.logEvent("Total fee before discount: £" + totalFee);
        System.out.println("Total fee before discount: £" + totalFee);

        double discount = calculateDiscount(parcel.getParcelID(), totalFee);
        log.logEvent("Discount applied: £" + discount);
        System.out.println("Discount applied: £" + discount);

        double finalFee = totalFee - discount;
        log.logEvent("Final fee after discount: £" + finalFee);
        System.out.println("Final fee after discount: £" + finalFee);

        return finalFee;
    }

    public double calculateDiscount(String parcelID, double totalFee) {
        log.logEvent("Calculating discount for parcel ID: " + parcelID);
        double percentage = 0.0;

        if (parcelID.startsWith("X") && parcelID.endsWith("0")) {
            percentage = 20.0; // 20% discount for parcel ID ending with 0
        } else if (parcelID.startsWith("X") && parcelID.endsWith("5")) {
            percentage = 10.0; // 10% discount for parcel ID ending with 5
        } else if (parcelID.startsWith("C")) {
            percentage = 5.0; // 5% discount for parcel ID starting with C
        }

        double discount = (percentage / 100.0) * totalFee;
        log.logEvent("Discount for parcel ID " + parcelID + " (" + percentage + "%): £" + discount);

        return discount;
    }

}

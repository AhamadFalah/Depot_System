package controller;

import model.Customer;
import model.Parcel;
import model.ParcelMap;
import util.Log;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DepotWorker {
    private String name;
    private Log log = Log.getInstance();

    // Constructor for DepotWorker
    public DepotWorker(String name) {
        this.name = name;
        log.logInfo("DepotWorker initialized with name: " + name);
    }

    // Calculates the total fee for a given parcel
    public double calculateFee(Parcel parcel) {
        log.logInfo("Calculating fee for parcel: " + parcel.getParcelID());
        double baseFee = 5.00;
        double weightFee = parcel.getWeight() * 0.5;
        double depotFee = parcel.getDaysInDepot() * 0.2;
        double total = baseFee + weightFee + depotFee;
        log.logInfo("Fee calculated for parcel: " + parcel.getParcelID() +
                ". Base: £5.00, Weight Fee: £" + String.format("%.2f", weightFee) +
                ", Depot Fee: £" + String.format("%.2f", depotFee) +
                ", Total: £" + String.format("%.2f", total));
        return total;
    }

    // Calculates the discount for a given parcel based on its ID and total fee
    public double calculateDiscount(String parcelID, double totalFee) {
        log.logInfo("Calculating discount for parcel: " + parcelID);
        double discount = 0.0;
        if (parcelID.startsWith("X") && parcelID.endsWith("0")) {
            discount = totalFee * 0.20;
        } else if (parcelID.startsWith("X") && parcelID.endsWith("5")) {
            discount = totalFee * 0.10;
        } else if (parcelID.startsWith("C")) {
            discount = totalFee * 0.05;
        }
        log.logInfo("Discount applied for parcel: " + parcelID + ". Discount: £" + String.format("%.2f", discount));
        return discount;
    }

}

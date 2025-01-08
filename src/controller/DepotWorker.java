package controller;

import model.Customer;
import model.Parcel;
import model.ParcelMap;
import util.Log;

public class DepotWorker {
    private String name;

    public DepotWorker(String name) {
        this.name = name;
    }


    public double processCustomer(Customer customer, ParcelMap parcelMap) {
        Parcel parcel = parcelMap.findParcel(customer.getParcelID());
        if (parcel == null) {
            Log.getInstance().logEvent("Parcel not found for customer: " + customer.getName() + " [Parcel ID: " + customer.getParcelID() + "]");
            return 0.0;
        }
        double fee = calculateFee(parcel);
        parcel.setStatus("Collected");
        Log.getInstance().logEvent("Parcel collected: " + parcel.getParcelID() + " by customer: " + customer.getName() + ". Fee: £" + String.format("%.2f", fee));

        parcelMap.notifyParcelMapObservers("ParcelMap");


        return fee;
    }


    public double calculateFee(Parcel parcel) {
        double baseFee = 5.00;
        double weightFee = parcel.getWeight() * 0.5;
        double depotFee = parcel.getDaysInDepot() * 0.2;
        double total = baseFee + weightFee + depotFee;
        Log.getInstance().logEvent("Fee calculated for parcel: " + parcel.getParcelID() + ". Base: £5.00, Weight: £" + String.format("%.2f", weightFee) + ", Depot: £" + String.format("%.2f", depotFee) + ". Total: £" + String.format("%.2f", total));
        return total;
    }

    public double calculateDiscount(String parcelID, double totalFee) {
        double discount = 0.0;
        if (parcelID.startsWith("X") && parcelID.endsWith("0")) {
            discount = totalFee * 0.20;
        } else if (parcelID.startsWith("X") && parcelID.endsWith("5")) {
            discount = totalFee * 0.10;
        } else if (parcelID.startsWith("C")) {
            discount = totalFee * 0.05;
        }
        Log.getInstance().logEvent("Discount applied for parcel: " + parcelID + ". Discount: £" + String.format("%.2f", discount));
        return discount;
    }
}

package view;

import controller.Manager;
import model.Parcel;
import util.Log;

import javax.swing.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WorkerUIHelper {

    public static void showCollectionPopup(java.awt.Component parent, Manager manager, Parcel parcel, Runnable onConfirm) {
        double totalFee = manager.getWorker().calculateFee(parcel);
        double discount = manager.getWorker().calculateDiscount(parcel.getParcelID(), totalFee);
        double finalFee = totalFee - discount;

        String feeDetails = String.format(
                "Parcel ID: %s\nWeight: %.2f kg\nBase Fee: £5.00\nWeight Fee: £%.2f\nDepot Fee: £%.2f\nDiscount: £%.2f\nTotal Fee: £%.2f\n\nConfirm collection?",
                parcel.getParcelID(), parcel.getWeight(), parcel.getWeight() * 0.5, parcel.getDaysInDepot() * 0.2, discount, finalFee
        );

        int choice = JOptionPane.showConfirmDialog(parent, feeDetails, "Confirm Collection", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            Log.getInstance().logEvent("Parcel collection confirmed: " + parcel.getParcelID() + ". Final Fee: £" + String.format("%.2f", finalFee));
            onConfirm.run();
        } else {
            Log.getInstance().logEvent("Parcel collection canceled: " + parcel.getParcelID());
        }
    }

    public static void generateReport(java.awt.Component parent, Manager manager) {
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        manager.generateReport("DepotSystemReport_" + dateTime + ".txt");
        JOptionPane.showMessageDialog(parent, "Report generated successfully!", "Report", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showFeesAndDiscounts(java.awt.Component parent) {
        String feeStructure = """
                    Fee Structure:
                    -----------------------------
                    Base Fee: £5.00
                    Weight Fee: £0.50 per kg
                    Size Fee: (if any) e.g., £0.001 per cm^3
                    Depot Fee: £0.20 per day
                
                    Discounts:
                    -----------------------------
                    - Parcel IDs starting with 'X' and ending with '0': 20% discount
                    - Parcel IDs starting with 'X' and ending with '5': 10% discount
                    - Parcel IDs starting with 'C': 5% discount
                """;

        JOptionPane.showMessageDialog(parent, feeStructure, "Fees and Discounts", JOptionPane.INFORMATION_MESSAGE);
    }
}

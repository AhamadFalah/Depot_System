package view;

import controller.Manager;
import model.Parcel;
import util.Log;

import javax.swing.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WorkerUIHelper {
    private static final Log log = Log.getInstance();

    // Displays a confirmation popup for parcel collection
    public static void showCollectionPopup(java.awt.Component parent, Manager manager, Parcel parcel, Runnable onConfirm) {
        log.logInfo("Displaying collection popup for parcel: " + parcel.getParcelID());
        double totalFee = manager.getWorker().calculateFee(parcel);
        double discount = manager.getWorker().calculateDiscount(parcel.getParcelID(), totalFee);
        double finalFee = totalFee - discount;

        String feeDetails = String.format(
                "Parcel ID: %s\nWeight: %.2f kg\nBase Fee: £5.00\nWeight Fee: £%.2f\nDepot Fee: £%.2f\nDiscount: £%.2f\nTotal Fee: £%.2f\n\nConfirm collection?",
                parcel.getParcelID(), parcel.getWeight(), parcel.getWeight() * 0.5, parcel.getDaysInDepot() * 0.2, discount, finalFee
        );

        int choice = JOptionPane.showConfirmDialog(parent, feeDetails, "Confirm Collection", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            log.logInfo("Parcel collection confirmed: " + parcel.getParcelID() + ". Final Fee: £" + String.format("%.2f", finalFee));
            onConfirm.run();
        } else {
            log.logInfo("Parcel collection canceled: " + parcel.getParcelID());
        }
    }


    // Generates a report for the depot system and saves it to a file
    public static void generateReport(java.awt.Component parent, Manager manager) {
        log.logInfo("Generating depot system report.");
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String reportFilename = "DepotSystemReport_" + dateTime + ".txt";
        manager.generateReport(reportFilename);
        log.logInfo("Report generated successfully: " + reportFilename);
        JOptionPane.showMessageDialog(parent, "Report generated successfully!", "Report", JOptionPane.INFORMATION_MESSAGE);
    }


    // Displays the fee structure and discount rules for the depot system
    public static void showFeesAndDiscounts(java.awt.Component parent) {
        log.logInfo("Displaying fee structure and discounts.");

        //Display the fee structure and discount
        String feeStructure = """
                Fee Structure:
                -----------------------------
                Base Fee: £5.00
                Weight Fee: £0.50 per kg
                Depot Fee: £0.20 per day

                Discounts:
                -----------------------------
                - Parcel IDs starting with 'X' and ending with '0': 20% discount
                - Parcel IDs starting with 'X' and ending with '5': 10% discount
                - Parcel IDs starting with 'C': 5% discount
            """;

        //Show the fee structure to the user in a popup
        JOptionPane.showMessageDialog(parent, feeStructure, "Fees and Discounts", JOptionPane.INFORMATION_MESSAGE);
    }
}

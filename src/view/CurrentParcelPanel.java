package view;

import model.Parcel;
import util.Log;

import javax.swing.*;
import java.awt.*;

public class CurrentParcelPanel extends JPanel {
    private JTextArea parcelDetailsArea;
    private Parcel currentParcel;
    private Log log = Log.getInstance();

    // Constructor for CurrentParcelPanel
    public CurrentParcelPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Currently Processed Parcel"));

        parcelDetailsArea = new JTextArea(5, 40);
        parcelDetailsArea.setEditable(false);

        add(new JScrollPane(parcelDetailsArea), BorderLayout.CENTER);
        showNoParcelMessage();

        log.logInfo("CurrentParcelPanel initialized.");
    }

    // Updates the CurrentParcelPanel with the details of the provided parcel.
    public void setCurrentParcel(Parcel parcel) {
        this.currentParcel = parcel;
        if (parcel != null) {
            log.logInfo("Updating CurrentParcelPanel with Parcel ID: " + parcel.getParcelID());
        } else {
            log.logInfo("Clearing CurrentParcelPanel (no parcel being processed).");
        }
        refreshDisplay();
    }

    // Refreshes the display of the CurrentParcelPanel.
    private void refreshDisplay() {
        if (currentParcel == null) {
            showNoParcelMessage();
        } else {
            parcelDetailsArea.setText(String.format(
                    "Parcel ID: %s\nWeight: %.2f kg\nDimensions: %s\nStatus: %s\nDays in Depot: %d",
                    currentParcel.getParcelID(),
                    currentParcel.getWeight(),
                    currentParcel.getDimensions(),
                    currentParcel.getStatus(),
                    currentParcel.getDaysInDepot()
            ));
            log.logInfo("Parcel details displayed: " + currentParcel.toString());
        }
    }

    // Displays a default message in the CurrentParcelPanel when no parcel is being processed.
    private void showNoParcelMessage() {
        parcelDetailsArea.setText("No parcel currently being processed.");
        log.logInfo("Displayed message: No parcel currently being processed.");
    }

}

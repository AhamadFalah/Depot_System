package view;

import model.Parcel;

import javax.swing.*;
import java.awt.*;

public class CurrentParcelPanel extends JPanel {
    private JTextArea parcelDetailsArea;
    private Parcel currentParcel;

    public CurrentParcelPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Currently Processed Parcel"));

        parcelDetailsArea = new JTextArea(5, 40);
        parcelDetailsArea.setEditable(false);

        add(new JScrollPane(parcelDetailsArea), BorderLayout.CENTER);
        showNoParcelMessage();
    }

    public void setCurrentParcel(Parcel parcel) {
        this.currentParcel = parcel;
        refreshDisplay();
    }


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
        }
    }

    private void showNoParcelMessage() {
        parcelDetailsArea.setText("No parcel currently being processed.");
    }
}

package view;

import controller.Manager;
import model.Customer;
import model.Parcel;
import util.Observer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ParcelsPanel extends JPanel implements Observer {
    private Manager manager;
    private WorkerUI workerUI;
    private DefaultTableModel parcelTableModel;
    private JTable parcelTable;

    public ParcelsPanel(Manager manager, WorkerUI workerUI) {
        this.manager = manager;
        this.workerUI = workerUI;

        // Observe ParcelMap changes
        manager.getParcelMap().addObserver(this);

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Parcels"));

        parcelTableModel = new DefaultTableModel(new String[]{"ID", "Weight", "Dimensions", "Status", "Days in Depot"}, 0);
        parcelTable = new JTable(parcelTableModel);
        JScrollPane scrollPane = new JScrollPane(parcelTable);
        add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton processParcelButton = new JButton("Process Selected Parcel");
        JButton editParcelButton = new JButton("Edit Selected Parcel");
        JButton searchParcelButton = new JButton("Search Parcel");
        JButton refreshButton = new JButton("Refresh Parcels");

        JButton addParcelButton = new JButton("Add Parcel");
        JButton viewCollectedButton = new JButton("View Collected Parcels");

        buttonPanel.add(processParcelButton);
        buttonPanel.add(editParcelButton);
        buttonPanel.add(searchParcelButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(addParcelButton);
        buttonPanel.add(viewCollectedButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Listeners
        processParcelButton.addActionListener(e -> processSelectedParcel());
        editParcelButton.addActionListener(e -> editSelectedParcel());
        searchParcelButton.addActionListener(e -> searchParcel());
        refreshButton.addActionListener(e -> refreshParcelTable());
        addParcelButton.addActionListener(e -> addParcel());
        viewCollectedButton.addActionListener(e -> showCollectedParcels());

        refreshParcelTable();
    }

    @Override
    public void update(String observableType) {
        if ("ParcelMap".equals(observableType)) {
            refreshParcelTable();
        }
    }


    private void refreshParcelTable() {
        parcelTableModel.setRowCount(0);
        List<Parcel> allParcels = manager.getParcelMap().getAllParcels();
        for (Parcel p : allParcels) {
            parcelTableModel.addRow(new Object[]{p.getParcelID(), p.getWeight(), p.getDimensions(), p.getStatus(), p.getDaysInDepot()});
        }
    }

    private void processSelectedParcel() {
        int selectedRow = parcelTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a parcel to process.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String parcelID = (String) parcelTableModel.getValueAt(selectedRow, 0);
        Parcel parcel = manager.getParcelMap().findParcel(parcelID);
        if (parcel == null) {
            JOptionPane.showMessageDialog(this, "Parcel not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Mark as current
        workerUI.showCurrentParcel(parcel);

        // Show fee popup
        WorkerUIHelper.showCollectionPopup(this, manager, parcel, () -> {
            double fee = manager.getWorker().processCustomer(new Customer("", parcel.getParcelID()), manager.getParcelMap());

            manager.getCollectedParcels().add(parcel);
            manager.addToTotalFees(fee);

            // Remove from queue if present
            manager.getQueueOfCustomers().getCustomerQueue().removeIf(c -> c.getParcelID().equals(parcelID));
            refreshParcelTable();
        });
    }

    private void editSelectedParcel() {
        int selectedRow = parcelTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a parcel to edit.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String parcelID = (String) parcelTableModel.getValueAt(selectedRow, 0);
        Parcel parcel = manager.getParcelMap().findParcel(parcelID);
        if (parcel == null) {
            JOptionPane.showMessageDialog(this, "Parcel not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JTextField weightField = new JTextField(String.valueOf(parcel.getWeight()));
        JTextField dimsField = new JTextField(parcel.getDimensions());
        JComboBox<String> statusField = new JComboBox<>(new String[]{"Pending", "Collected"});
        statusField.setSelectedItem(parcel.getStatus());
        JTextField daysField = new JTextField(String.valueOf(parcel.getDaysInDepot()));

        Object[] message = {"Weight:", weightField, "Dimensions:", dimsField, "Status:", statusField, "Days in Depot:", daysField};

        int option = JOptionPane.showConfirmDialog(this, message, "Edit Parcel", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                parcel.setWeight(Double.parseDouble(weightField.getText()));
                parcel.setDimensions(dimsField.getText());
                parcel.setStatus(statusField.getSelectedItem().toString());
                parcel.setDaysInDepot(Integer.parseInt(daysField.getText()));

                refreshParcelTable();

                workerUI.showCurrentParcel(parcel);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid numeric input.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void searchParcel() {
        String searchTerm = JOptionPane.showInputDialog(this, "Enter Parcel ID to search:", "Search Parcel", JOptionPane.PLAIN_MESSAGE);
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return;
        }

        Parcel parcel = manager.getParcelMap().findParcel(searchTerm.trim());
        if (parcel == null) {
            JOptionPane.showMessageDialog(this, "Parcel not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        parcelTableModel.setRowCount(0);
        parcelTableModel.addRow(new Object[]{parcel.getParcelID(), parcel.getWeight(), parcel.getDimensions(), parcel.getStatus(), parcel.getDaysInDepot()});

        workerUI.showCurrentParcel(parcel);
    }

    private void addParcel() {
        JTextField parcelIDField = new JTextField();
        JTextField weightField = new JTextField();
        JTextField dimsField = new JTextField();
        JTextField daysField = new JTextField();

        Object[] msg = {"Parcel ID:", parcelIDField, "Weight:", weightField, "Dimensions (e.g. 4x2x2):", dimsField, "Days in Depot:", daysField};

        int option = JOptionPane.showConfirmDialog(this, msg, "Add New Parcel", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String pid = parcelIDField.getText().trim();
                double w = Double.parseDouble(weightField.getText().trim());
                String d = dimsField.getText().trim();
                int days = Integer.parseInt(daysField.getText().trim());

                Parcel newParcel = new Parcel(pid, w, d, "Pending", days);
                manager.addNewParcel(newParcel);

                refreshParcelTable();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid numeric value for weight or days.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showCollectedParcels() {
        List<Parcel> collected = manager.getCollectedParcels();

        DefaultTableModel collectedModel = new DefaultTableModel(new String[]{"ID", "Weight", "Dimensions", "Status", "Days in Depot"}, 0);
        for (Parcel p : collected) {
            collectedModel.addRow(new Object[]{p.getParcelID(), p.getWeight(), p.getDimensions(), p.getStatus(), p.getDaysInDepot()});
        }

        JTable table = new JTable(collectedModel);
        JScrollPane scroll = new JScrollPane(table);
        JOptionPane.showMessageDialog(this, scroll, "Collected Parcels", JOptionPane.INFORMATION_MESSAGE);
    }
}

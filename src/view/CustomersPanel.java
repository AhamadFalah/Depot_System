package view;

import controller.Manager;
import model.Customer;
import model.Parcel;
import util.Observer;
import util.Observable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CustomersPanel extends JPanel implements Observer {
    private Manager manager;
    private WorkerUI workerUI;

    private DefaultTableModel customerTableModel;
    private JTable customerTable;

    public CustomersPanel(Manager manager, WorkerUI workerUI) {
        this.manager = manager;
        this.workerUI = workerUI;

        manager.getQueueOfCustomers().addObserver(this);

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Customer Queue"));

        customerTableModel = new DefaultTableModel(new String[]{"Queue Number", "Name", "Parcel ID"}, 0);
        customerTable = new JTable(customerTableModel);

        JScrollPane scrollPane = new JScrollPane(customerTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton processNextButton = new JButton("Process Next Customer");
        JButton generateReportButton = new JButton("Generate Report");
        JButton viewFeesButton = new JButton("View Fees");

        buttonPanel.add(processNextButton);
        buttonPanel.add(generateReportButton);
        buttonPanel.add(viewFeesButton);

        add(buttonPanel, BorderLayout.SOUTH);

        processNextButton.addActionListener(e -> processNextCustomer());
        generateReportButton.addActionListener(e -> WorkerUIHelper.generateReport(this, manager));
        viewFeesButton.addActionListener(e -> WorkerUIHelper.showFeesAndDiscounts(this));

        refreshCustomerTable();
    }

    @Override
    public void update(String observableType) {
        if ("QueueOfCustomers".equals(observableType)) {
            refreshCustomerTable();
        }
    }

    private void refreshCustomerTable() {
        customerTableModel.setRowCount(0);
        for (Customer c : manager.getQueueOfCustomers().getCustomerQueue()) {
            customerTableModel.addRow(new Object[]{c.getQueueNumber(), c.getName(), c.getParcelID()});
        }
    }


    private void processNextCustomer() {
        if (manager.getQueueOfCustomers().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No customers in the queue.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Customer c = manager.getQueueOfCustomers().getCustomer();
        Parcel p = manager.getParcelMap().findParcel(c.getParcelID());
        if (p == null) {
            JOptionPane.showMessageDialog(this, "Parcel not found for the customer.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        workerUI.showCurrentParcel(p);

        WorkerUIHelper.showCollectionPopup(this, manager, p, () -> {
            double fee = manager.getWorker().processCustomer(c, manager.getParcelMap());
            p.setStatus("Collected");
            manager.getCollectedParcels().add(p);
            manager.addToTotalFees(fee);

            // Notify observers about parcel changes
            manager.getParcelMap().notifyParcelMapObservers("ParcelMap");

            refreshCustomerTable();
        });
    }

}


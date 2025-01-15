package model;

public class Customer {
    private String name;
    private int queueNumber;
    private String parcelID;

    // Constructor for Customer
    public Customer(String name, int queueNumber, String parcelID) {
        this.name = name;
        this.queueNumber = queueNumber;
        this.parcelID = parcelID;
    }

    // Constructor for Customer without queueNumber
    public Customer(String name, String parcelID) {
        this.name = name;
        this.queueNumber = -1;
        this.parcelID = parcelID;
    }

    // Getters
    public String getName() {
        return name;
    }

    public int getQueueNumber() {
        return queueNumber;
    }

    public String getParcelID() {
        return parcelID;
    }

    // Setters
    public void setQueueNumber(int queueNumber) {
        this.queueNumber = queueNumber;
    }

    // Returns a string of the customer
    @Override
    public String toString() {
        return "Customer{" +
                "name='" + name + '\'' +
                ", queueNumber=" + queueNumber +
                ", parcelID='" + parcelID + '\'' +
                '}';
    }
}

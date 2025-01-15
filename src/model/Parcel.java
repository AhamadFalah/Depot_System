package model;

public class Parcel {
    private String parcelID;
    private double weight;
    private String dimensions;
    private String status;         // "Pending" or "Collected"
    private int daysInDepot;

    // Constructor for Parcel
    public Parcel(String parcelID, double weight, String dimensions, String status, int daysInDepot) {
        this.parcelID = parcelID;
        this.weight = weight;
        this.dimensions = dimensions;
        this.status = status;
        this.daysInDepot = daysInDepot;
    }

    // Getters
    public String getParcelID() {
        return parcelID;
    }

    public double getWeight() {
        return weight;
    }

    public String getDimensions() {
        return dimensions;
    }

    public String getStatus() {
        return status;
    }

    public int getDaysInDepot() {
        return daysInDepot;
    }

    // Setters
    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDaysInDepot(int daysInDepot) {
        this.daysInDepot = daysInDepot;
    }

    // Returns a string of the parcel
    @Override
    public String toString() {
        return "Parcel{" + "parcelID='" + parcelID + '\'' + ", weight=" + weight + ", dimensions='" + dimensions + '\'' + ", status='" + status + '\'' + ", daysInDepot=" + daysInDepot + '}';
    }
}


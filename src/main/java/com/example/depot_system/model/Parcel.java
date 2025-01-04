package com.example.depot_system.model;

public class Parcel {
    //Parcel attributes
    private String parcelID;
    private double weight;
    private String dimenions;
    private String status;
    private int daysInDepot;

    //Parcel Constructor

    public Parcel(String parcelID, double weight, String dimenions, String status, int daysInDepot) {
        this.parcelID = parcelID;
        this.weight = weight;
        this.dimenions = dimenions;
        this.status = status;
        this.daysInDepot = daysInDepot;
    }

    public String getParcelID() {
        return parcelID;
    }

    public double getWeight() {
        return weight;
    }

    public String getStatus() {
        return status;
    }

    public String getDimenions() {
        return dimenions;
    }

    public int getDaysInDepot() {
        return daysInDepot;
    }


    @Override
    public String toString() {
        return "Parcel{" +
                "parcelID='" + parcelID + '\'' +
                ", weight=" + weight +
                ", dimenions='" + dimenions + '\'' +
                ", status='" + status + '\'' +
                ", daysInDepot=" + daysInDepot +
                '}';
    }
}

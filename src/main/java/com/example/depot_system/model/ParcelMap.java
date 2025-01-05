package com.example.depot_system.model;

import java.util.HashMap;
import java.util.List;

public class ParcelMap {
    private HashMap<String, Parcel> parcels = new HashMap<>();

    public void addParcel(Parcel parcel) {
        parcels.put(parcel.getParcelID(), parcel);
    }

    public void removeParcel(String parcelID) {
        parcels.remove(parcelID);
    }

    public Parcel findParcel(String parcelID) {
        return parcels.get(parcelID);
    }

    public int size(){
        return parcels.size();
    }



    public void updateParcelStatus(String parcelId, String status) {
        Parcel parcel = parcels.get(parcelId);
        if (parcel != null) {
            parcel.setStatus(status);
        }
    }

    public Parcel getParcel(String parcelId) {
        return parcels.get(parcelId);
    }

}

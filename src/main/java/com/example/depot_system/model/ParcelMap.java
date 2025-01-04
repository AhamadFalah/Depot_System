package com.example.depot_system.model;

import java.util.ArrayList;
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

    public Parcel findParcelById(String parcelId) {
        return parcels.get(parcelId);
    }

    public void updateParcelStatus(String parcelId, String status) {
        Parcel parcel = parcels.get(parcelId);
        if (parcel != null) {
            parcel.setStatus(status);
        }
    }

    public List<Parcel> getAllParcels() {
        return new ArrayList<>(parcels.values());
    }

    public Parcel getParcel(String parcelId) {
        return parcels.get(parcelId);
    }



}

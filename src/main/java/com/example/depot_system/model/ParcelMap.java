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

//    public List<Parcel> findParcelById(){
//    }

}

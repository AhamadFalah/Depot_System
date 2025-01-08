package model;

import util.Observable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ParcelMap extends Observable {
    private final HashMap<String, Parcel> parcels = new HashMap<>();

    public void addParcel(Parcel parcel) {
        parcels.put(parcel.getParcelID(), parcel);
        notifyObservers("ParcelMap");
    }

    public void notifyParcelMapObservers(String observableType) {
        notifyObservers(observableType);
    }

    public Parcel findParcel(String parcelID) {
        return parcels.get(parcelID);
    }

    public List<Parcel> getAllParcels() {
        return new ArrayList<>(parcels.values());
    }
}

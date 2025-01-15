package model;

import util.Observable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ParcelMap extends Observable {
    private final HashMap<String, Parcel> parcels = new HashMap<>();

    // Adds a new parcel to the collection and notifies observers about the update
    public void addParcel(Parcel parcel) {
        parcels.put(parcel.getParcelID(), parcel);
        notifyObservers("ParcelMap");
    }

    // Triggers a notification to observers for the parcel map
    public void notifyParcelMapObservers(String observableType) {
        notifyObservers(observableType);
    }

    // Finds a parcel in the collection by its parcel ID
    public Parcel findParcel(String parcelID) {
        return parcels.get(parcelID);
    }

    // Retrieves all parcels in the collection as a list
    public List<Parcel> getAllParcels() {
        return new ArrayList<>(parcels.values());
    }
}

package util;

import java.util.ArrayList;
import java.util.List;

public abstract class Observable {
    private List<Observer> observers = new ArrayList<>();

    // Adds an observer to the list of observers for this object
    public void addObserver(Observer o) {
        observers.add(o);
    }

    // Removes an observer from the list of observers for this object
    public void removeObserver(Observer o) {
        observers.remove(o);
    }

    // Notifies all registered observers of a change in this object
    protected void notifyObservers(String observableType) {
        for (Observer ob : observers) {
            ob.update(observableType);
        }
    }
}


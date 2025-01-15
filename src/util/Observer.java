package util;

public interface Observer {
    // Updates the observer when notified of changes in the observable object
    void update(String observableType);
}

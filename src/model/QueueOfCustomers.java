package model;

import util.Observable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class QueueOfCustomers extends Observable {
    private LinkedList<Customer> customers = new LinkedList<>();

    // Adds a customer to the queue
    public void add(Customer customer) {
        customers.add(customer);
        notifyObservers("QueueOfCustomers");
    }

    // Retrieves and removes the customer at the front of the queue
    public Customer getCustomer() {
        if (!customers.isEmpty()) {
            Customer c = customers.removeFirst();
            notifyObservers("QueueOfCustomers");
            return c;
        }
        return null;
    }

    // Checks whether the queue is empty
    public boolean isEmpty() {
        return customers.isEmpty();
    }

    // Returns the number of customers in the queue
    public int size() {
        return customers.size();
    }

    // Provides an unmodifiable view of the current customer queue
    public List<Customer> getCustomerQueue() {
        return Collections.unmodifiableList(customers);
    }

    // Removes a customer from the queue based on the parcel ID and notifies observers if successful
    public boolean removeCustomerByParcelID(String parcelID) {
        Customer toRemove = null;
        for (Customer customer : customers) {
            if (customer.getParcelID().equals(parcelID)) {
                toRemove = customer;
                break;
            }
        }
        if (toRemove != null) {
            customers.remove(toRemove);
            notifyObservers("QueueOfCustomers");
            return true;
        }
        return false;
    }
}

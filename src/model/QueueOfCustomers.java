package model;

import util.Observable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class QueueOfCustomers extends Observable {
    private LinkedList<Customer> customers = new LinkedList<>();

    public void add(Customer customer) {
        customers.add(customer);
        notifyObservers("QueueOfCustomers");
    }

    public Customer getCustomer() {
        if (!customers.isEmpty()) {
            Customer c = customers.removeFirst();
            notifyObservers("QueueOfCustomers");
            return c;
        }
        return null;
    }

    public boolean isEmpty() {
        return customers.isEmpty();
    }

    public int size() {
        return customers.size();
    }

    public List<Customer> getCustomerQueue() {
        return Collections.unmodifiableList(customers);
    }

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

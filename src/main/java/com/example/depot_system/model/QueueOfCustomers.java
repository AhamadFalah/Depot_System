package com.example.depot_system.model;

import java.util.LinkedList;
import java.util.List;

public class QueueOfCustomers {
    private LinkedList<Customer> customers = new LinkedList<>();

    public void add(Customer customer) {
        customers.add(customer);
    }

    public Customer removeCustomer() {
        return customers.poll();
    }

    public boolean isEmpty() {
        return customers.isEmpty();
    }

    public int size(){
    return customers.size();
    }

    public List<Customer> getCustomerQueue(){
        return new LinkedList<>(customers);
    }

    public Customer getCustomer(){
        if(!customers.isEmpty()){
            Customer nextCustomer = customers.removeFirst();
            return nextCustomer;
        }else{
            return null;
        }
    }
}

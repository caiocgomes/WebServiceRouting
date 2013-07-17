package com.springapp.mvc;

/**
 * Created with IntelliJ IDEA.
 * User: caio
 * Date: 28/05/13
 * Time: 15:58
 * To change this template use File | Settings | File Templates.
 */
public class Route{

    public Integer quantityBus;
    public Integer quantityClients;
    public Integer busCapacity;

    public Integer getBusCapacity() {
        return busCapacity;
    }

    public void setBusCapacity(Integer busCapacity) {
        this.busCapacity = busCapacity;
    }    

    public Integer getQuantityBus() {
        return quantityBus;
    }

    public void setQuantityBus(Integer quantityBus) {
        this.quantityBus = quantityBus;
    }
    
    public Integer getQuantityClients() {
        return quantityClients;
    }

    public void setQuantityClients(Integer quantityClients) {
        this.quantityClients = quantityClients;
    }
}

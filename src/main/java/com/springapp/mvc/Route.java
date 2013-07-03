package com.springapp.mvc;

/**
 * Created with IntelliJ IDEA.
 * User: caio
 * Date: 28/05/13
 * Time: 15:58
 * To change this template use File | Settings | File Templates.
 */
public class Route{

    public Integer numberBus;
    public Integer numberStops;

    public Integer getMaximumClientPerBus() {
        return maximumClientPerBus;
    }

    public void setMaximumClientPerBus(Integer maximumClientPerBus) {
        this.maximumClientPerBus = maximumClientPerBus;
    }

    public Integer maximumClientPerBus;

    public Integer getNumberBus() {
        return numberBus;
    }

    public void setNumberBus(Integer numberBus) {
        this.numberBus = numberBus;
    }

    public Integer getNumberStops() {
        return numberStops;
    }

    public void setNumberStops(Integer numberStops) {
        this.numberStops = numberStops;
    }
}

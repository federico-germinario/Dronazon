package Smart_city;

import Dronazon.Coordinate;
import Server_amministratore.Drone;

/**
 * Classe che rappresenta un generico drone all'interno della rete
 *
 * @author Federico Germinario
 */

public class OtherDrone extends Drone implements Comparable<OtherDrone> {
    private Coordinate coordinate;
    private int battery;
    private boolean deliveryInProgress;

    public OtherDrone(){
        super();
        this.coordinate = null;
        this.battery = 100;
        this.deliveryInProgress = false;
    }

    public OtherDrone(int id, String ip_address, int port){
        super(id, ip_address, port);
        this.coordinate = null;
        this.battery = 100;
        this.deliveryInProgress = false;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public void setDeliveryInProgress(boolean deliveryInProgress) {
        this.deliveryInProgress = deliveryInProgress;
    }

    public boolean isDeliveryInProgress() {
        return deliveryInProgress;
    }

    @Override
    public int compareTo(OtherDrone d) {
        int compareId = d.getId();
        return this.getId() - compareId;
    }
}
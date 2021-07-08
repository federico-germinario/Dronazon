package Smart_city;

import Dronazon.Order;

/**
 * Classe che permette di gestire gli ordini pendenti
 *
 * @author Federico Germinario
 */
public class PendingOrder {
    private Order order;
    private OtherDrone droneDelivery;

    public PendingOrder (){
        order = null;
        droneDelivery = null;
    }

    /**
     * Inserisco l'ordine nella classe e rimango in attesa che un drone finisca una consegna
     *
     * @param order
     * @return drone libero per la consegna
     */
    public synchronized OtherDrone take(Order order){
        this.order = order;
        this.droneDelivery = null;
        while (droneDelivery == null){
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return droneDelivery;
    }

    /**
     * Inserisco nella classe il drone che ha terminato la consegna e invio
     * una notifica al thread rimasto in attesa che un drone si liberasse
     *
     * @param droneDelivery drone che ha terminato la consegna
     */
    public synchronized void put(OtherDrone droneDelivery){
        this.droneDelivery = droneDelivery;
        notify();
    }

}

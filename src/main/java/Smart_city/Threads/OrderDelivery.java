package Smart_city.Threads;

import Dronazon.Order;
import Smart_city.MyDrone;
import Smart_city.OtherDrone;
import Smart_city.PendingOrder;

/**
 * Thread che consente la consegna di un ordine tramite
 * una chiamata gRPC
 *
 * @author Federico Germinario
 */
public class OrderDelivery extends Thread{
    private final MyDrone myDrone;
    private OtherDrone drone;  // Drone che dovrà effettuare la consegna
    private Order order;
    private PendingOrder pendingOrder;

    public OrderDelivery(MyDrone myDrone, OtherDrone drone, Order order, PendingOrder pendingOrder){
        this.myDrone = myDrone;
        this.drone = drone;
        this.order = order;
        this.pendingOrder = pendingOrder;
    }

    @Override
    public void run() {
        try {
            // Gestisto l'ordine
            myDrone.delivery(drone, order);

            /* Notifico alla coda degli ordini pendenti che si è liberato un drone nel caso
               in cui ci fosse un ordine pendente */
            pendingOrder.put(drone);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }
    }
}

package Smart_city.Threads;

import Dronazon.Order;
import Smart_city.DeliveryQueue;
import Smart_city.MyDrone;
import Smart_city.OtherDrone;
import Smart_city.PendingOrder;

import java.util.ArrayList;

/**
 * Classe che permette la gestione degli ordini che vengono estratti dalla coda
 * DeliveryQueue
 *
 * @author Federico Germinario
 */
public class OrdersManagement extends Thread{
    private final MyDrone myDrone;
    private final DeliveryQueue queue;
    private final PendingOrder pendingOrder;
    private boolean stop;

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";

    public OrdersManagement(MyDrone myDrone, DeliveryQueue queue){
        this.myDrone = myDrone;
        this.queue = queue;
        pendingOrder = new PendingOrder();
        stop = false;
    }

    @Override
    public void run() {
        ArrayList<OrderDelivery> threads = new ArrayList<>();
        while (!stop || !queue.isEmpty()) {

            // Estraggo l'ordine dalla coda
            Order order = queue.take();

            if(order != null){

                // Cerco il drone per la consegna dell'ordine
                OtherDrone droneDelivery = myDrone.deliveryAssignment(order.getRitiro());

                if(droneDelivery == null){
                    System.out.println("Nessun drone è disponibile per la cosengna dell'ordine " + order.getId() + "! Resto in attesa!\n");

                    // Aspetto che si liberi un drone
                    droneDelivery = pendingOrder.take(order);
                }

                System.out.println(ANSI_RED + "======= INFO ORDINE =======");
                System.out.println("Id: " + order.getId());
                System.out.println("Ritiro: " + order.getRitiro());
                System.out.println("Consegna: " + order.getConsegna());
                System.out.println("Ordine affidato al drone: " + droneDelivery.getId());
                System.out.println("===========================\n" + ANSI_RESET);

                // Start thread per la gestione della consegna dell'ordine
                OrderDelivery orderDelivery = new OrderDelivery(myDrone, droneDelivery, order, pendingOrder);
                orderDelivery.start();
                threads.add(orderDelivery);
            }
        }

        // Prima di chiudere il thread mi assicuro che non ci siano consegne in corso
        for (OrderDelivery t : threads){
            try {
                t.join();
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    public void stopThread(){
        stop=true;
    }
}



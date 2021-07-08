package Smart_city;

import Dronazon.Order;

import java.util.ArrayList;

/**
 * Classe che rappresenta la coda di ordini. L'inserimento degli elementi della coda viene effettuata dal thread ReceiverOrders
 * mentre gli elementi vengono estratti dal thread OrdersManagement.
 *
 * @author Federico Germinario
 */

public class DeliveryQueue {

    public ArrayList<Order> queue = new ArrayList<>();

    /**
     * Inserimento ordine nella coda
     * @param order
     */
    public synchronized void put(Order order) {
        queue.add(order);
        notify();
    }

    /**
     * Estrazione di un elemento dalla coda. Se la coda è vuota
     * aspetta che qualcuno faccia una put per massimo 8 secondi.
     *
     * @return Ordine
     *         null tempo scaduto
     */
    public synchronized Order take() {
        Order order = null;
        long timeout = 8000;
        long now = System.currentTimeMillis();
        long deadline = now + timeout;

        while(queue.size() == 0 && now < deadline) {
            try {
                wait(deadline - now);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            now = System.currentTimeMillis();
        }

        if(queue.size()>0){
            order = queue.get(0);
            queue.remove(0);
        }

        return order;
    }

    /**
     * Metodo per verificare se la coda sia vuota
     *
     * @return true coda vuota
     *         false coda non vuota
     */
    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

}

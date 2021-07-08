package Smart_city.Threads;

import Dronazon.Order;
import Smart_city.DeliveryQueue;
import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.*;

/**
 * Thread che si occupa di connettersi al broker MQTT della smart-city e
 * di registrarsi come subscriber al topic "dronazon/smartcity/orders/",
 * per ricevere informazioni sulle nuove consegne
 *
 * @author Federico Germinario
 */

public class ReceiverOrders extends Thread{
    private final DeliveryQueue queue;
    private boolean stop;

    public ReceiverOrders(DeliveryQueue queue){
        this.queue = queue;
        stop = false;
    }

    @Override
    public void run() {
        MqttClient client;
        String broker = "tcp://localhost:1883";
        String clientId = MqttClient.generateClientId();
        String topic = "dronazon/smartcity/orders/";
        int qos = 2;

        try {
            client = new MqttClient(broker, clientId);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            // Connessione client
            client.connect(connOpts);
            System.out.println("Connesso al Broker " + broker);

            // Callback
            client.setCallback(new MqttCallback() {

                public void messageArrived(String topic, MqttMessage message) {
                    String receivedMessage = new String(message.getPayload());
                    Gson gson = new Gson();
                    Order order = gson.fromJson(receivedMessage, Order.class);

                    // Inserisco l'ordine nella coda
                    queue.put(order);
                }

                public void connectionLost(Throwable cause) {
                    System.out.println("Connessione persa! causa:" + cause.getMessage());
                }

                public void deliveryComplete(IMqttDeliveryToken token) {
                }

            });

            client.subscribe(topic, qos);
            System.out.println("Sottoscritto al topic: " + topic);
            System.out.println();

            waitStop();

            client.disconnect();
            System.out.println("Client MQTT disconnesso!");

        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }
    }

    private synchronized void waitStop(){
        while(!stop){
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void stopThread(){
        stop = true;
        notify();
    }

}

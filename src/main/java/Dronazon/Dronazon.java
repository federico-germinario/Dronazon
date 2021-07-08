package Dronazon;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.*;

import java.util.Scanner;

/**
 * Processo che simula un sito di e-commerce, generando
 * periodicamente nuove consegne da dover effettuare e comunicando le
 * informazioni relative ad esse allo stormo di droni
 *
 * @author Federico Germinario
 */
public class Dronazon {
    private static boolean stop = false;

    public static void main(String[] args) {
        MqttClient client;
        String broker = "tcp://localhost:1883";
        String clientId = MqttClient.generateClientId();
        String topic = "dronazon/smartcity/orders/";
        int qos = 2;

        // Thread per gestire la chiusura del processo
        Thread stopThread = new Thread(() -> {
            Scanner s = new Scanner(System.in);
            while (!stop) {
                if (s.nextLine().equalsIgnoreCase("quit")) {
                    stop = true;
                } else {
                    System.out.println("Command not found");
                }
            }
            s.close();
        });
        stopThread.start();

        try {
            client = new MqttClient(broker, clientId);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            // Connessione del client al broker
            client.connect(connOpts);
            System.out.println("Connesso al Broker " + broker);

            // Callback
            client.setCallback(new MqttCallback() {
                public void messageArrived(String topic, MqttMessage message) {
                }

                public void connectionLost(Throwable cause) {
                    System.out.println("Connessione persa! causa:" + cause.getMessage());
                }

                // Questo metodo viene chiamato ogni qual volta il client riceve tutti gli acknowledgments dal broker
                public void deliveryComplete(IMqttDeliveryToken token) {
                    if (token.isComplete()) {
                        System.out.println("Messaggio inviato correttamente!\n");
                    }
                }
            });

            Gson gson = new Gson();
            while (!stop){
                Order order = new Order();

                // Converto l'oggetto Ordine in una stringa formattata in json
                String jsonOrdine = gson.toJson(order);
                MqttMessage message = new MqttMessage(jsonOrdine.getBytes());

                message.setQos(qos);
                client.publish(topic, message);
                System.out.println("==== INFO ORDINE ====");
                System.out.println("Id: " + order.getId());
                System.out.println("Ritiro: " + order.getRitiro());
                System.out.println("Consegna: " + order.getConsegna());
                System.out.println("=====================");

                Thread.sleep(5000);
            }

            stopThread.join();

            if(client.isConnected())
                client.disconnect();
            System.out.println("Publisher disconnesso!");

        } catch (MqttException me ) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

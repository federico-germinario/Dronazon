package Smart_city;

import Server_amministratore.Drone;
import Server_amministratore.GlobalStatistics;
import Server_amministratore.MessageToDrone;
import Smart_city.Simulators.PM10Simulator;
import Smart_city.Threads.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Main drone
 *
 * @author Federico Germinario
 */
public class MainDrone {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLUE = "\u001B[34m";

    public static void main(String[] args) throws InterruptedException {

        MyDrone myDrone = new MyDrone();
        RestInterface restInterface = new RestInterface();

        System.out.println();
        System.out.println(ANSI_BLUE + "====== Drone info ======");
        System.out.println("Id: " + myDrone.getId());
        System.out.println("Indirizzo ip: " + myDrone.getIp_address());
        System.out.println("Porta: " + myDrone.getPort());
        System.out.println("Batteria: " + myDrone.getBattery());
        System.out.println("========================\n\n" + ANSI_RESET);

        // Registrazione al server amministratore
        MessageToDrone messageFromServer = restInterface.registration(myDrone.getId(), myDrone.getIp_address(), myDrone.getPort());
        if (messageFromServer == null) { return; }
        System.out.println("Registrazione al server avvenuta correttamente!\n");

        // Aggiorno le mie coordinate
        myDrone.setCoordinate(messageFromServer.getCoordinate());

        ArrayList<OtherDrone> droneList = new ArrayList<>();
        for (Drone d : messageFromServer.getDronesList()) {
            OtherDrone otherDrone = new OtherDrone(d.getId(), d.getIp_address(), d.getPort());
            if(otherDrone.getId() == myDrone.getId()) otherDrone.setCoordinate(myDrone.getCoordinate());
            droneList.add(otherDrone);
        }

        // Assegno al drone la lista dei droni ricevuti dal server amministratore
        myDrone.setDroneList(droneList);

        Server server = ServerBuilder.forPort(myDrone.getPort()).addService(new ServiceImpl(myDrone)).build();

        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        System.out.println("Server RPC started!\n");

        ////////////////////////////// INIZIALIZZAZIONE ///////////////////////////////
        // Controllo se sono l'unico drone nella rete
        if (myDrone.getDroneList().size() == 1) {
            OtherDrone me = new OtherDrone(myDrone.getId(), myDrone.getIp_address(), myDrone.getPort());
            myDrone.setDroneMaster(me);
            myDrone.setSuccessorDrone(me);
            myDrone.becomeMaster();
        }else {
            // Cerco il mio drone successore
            myDrone.findNextDrone();

            // Cerco il drone precedente al mio
            OtherDrone dronePrev = myDrone.findDronePrev();
            System.out.println("Annuncio al Drone " + dronePrev.getId() + " che il suo successivo sono io [" + myDrone.getId() + "]");

            ArrayList<InitializationThread> threads = new ArrayList<>();
            ArrayList<OtherDrone> droneListCopy = myDrone.getDroneList();

            // Mi inserisco nella rete e mi presento a tutti i droni della rete
            for (OtherDrone d : droneListCopy) {
                if (d.getId() != myDrone.getId()) {
                    InitializationThread initializationThread = new InitializationThread(myDrone, d, dronePrev);
                    initializationThread.start();
                    threads.add(initializationThread);
                }
            }

            // Mi assicuro che l'inserimento nella rete e la presentazione siano andati a buon fine
            for (InitializationThread thread : threads) {
                thread.join();
            }

            // Se non ricevo il drone master induco l'elezione
            if(myDrone.getDroneMaster() == null){
                System.out.println("Drone master sconosciuto!");
                myDrone.requestElection();
            }
        }

        // Avvio sensore per il rilevamento dell'inquinamento dell'aria
        PM10Simulator pm10Simulator = new PM10Simulator(myDrone.getBufferPM10());
        pm10Simulator.start();

        // Avvio thread di ping sul drone successivo
        PingThread pingThread = new PingThread(myDrone);
        pingThread.start();

        // Avvio thread di chiusura esplicita drone
        QuitThread quitThread = new QuitThread(myDrone);
        quitThread.start();

        // Avvio thread controllo batteria residua
        CheckBatteryThread checkBatteryThread = new CheckBatteryThread(myDrone, quitThread);
        checkBatteryThread.start();

        // Avvio thread gestione statistiche
        StatsThread statsThread = new StatsThread(myDrone);
        statsThread.start();

        while(true){

            // Resto in attesa che la batteria scenda sotto il 15% o di una rischiesta espicita di uscita
            synchronized (quitThread){
                while (!myDrone.isQuit()){
                    quitThread.wait();
                }
            }

            if(myDrone.isMaster()){

                // Resto in attesa della terminazione della consegna di cui si sta occupando
                myDrone.waitDelivery();

                // Disconnessione dal broker MQTT e assegnamento richieste pendenti
                myDrone.stopThreadMaster();

                // Stop threads
                pm10Simulator.stopMeGently();
                pm10Simulator.join();

                pingThread.interrupt();
                pingThread.join();

                checkBatteryThread.interrupt();
                checkBatteryThread.join();

                statsThread.interrupt();
                statsThread.join();

                /* Avvio l'arresto ordinato del server RPC. Le chiamate preesistenti
                   continueranno la loro esecuzione (per massimo 5 secondi) mentre le
                   nuove chimate verranno rifiutate.
                 */
                server.shutdown().awaitTermination(5, TimeUnit.SECONDS);

                // Calcolo e invio le statistiche globali al server amministratore
                GlobalStatistics globalStatistics = myDrone.getDroneStatistics().calculationGlobalStat();
                restInterface.postStatistics(globalStatistics);

                restInterface.remove(myDrone.getId());
                System.out.println("Richiesta di uscita del drone inviata al server amministratore");

            }else {
                // Resto in attesa della terminazione della consegna di cui si sta occupando
                myDrone.waitDelivery();

                // Stop threads
                pm10Simulator.stopMeGently();
                pm10Simulator.join();

                pingThread.interrupt();
                pingThread.join();

                checkBatteryThread.interrupt();
                checkBatteryThread.join();

                statsThread.interrupt();
                statsThread.join();

                /* Avvio l'arresto ordinato del server RPC. Le chiamate preesistenti
                   continueranno la loro esecuzione (per massimo 5 secondi) mentre le
                   nuove chimate verranno rifiutate.
                 */
                server.shutdown().awaitTermination(5, TimeUnit.SECONDS);

                restInterface.remove(myDrone.getId());
                System.out.println("Richiesta di uscita del drone inviata al server amministratore");
            }
            break;
        }
        System.exit(0);
    }

}



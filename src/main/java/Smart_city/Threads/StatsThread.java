package Smart_city.Threads;

import Server_amministratore.GlobalStatistics;
import Smart_city.MyDrone;
import Smart_city.RestInterface;

/**
 * Thread che si occupa di stampare ogni 10 s le informazioni del drone e
 * nel caso sia master inviare le statistiche globali al server amministratore
 *
 * @author Federico Germinario
 */

public class StatsThread extends Thread{
    private final MyDrone myDrone;
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RESET = "\u001B[0m";

    public StatsThread(MyDrone myDrone){
        this.myDrone = myDrone;
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                return;
            }

            // Stampo le informazione del drone
            System.out.println();
            System.out.println(ANSI_GREEN + "==== INFO DRONE ======");
            System.out.println("N consegne: " + myDrone.getNDelivery());
            System.out.println("Km percorsi: " + myDrone.getTotKM());
            System.out.println("Batteria: " + myDrone.getBattery());
            System.out.println("======================\n" + ANSI_RESET);

            if(myDrone.isMaster()) {
                // Calcolo le statistiche globali
                GlobalStatistics globalStatistics = myDrone.getDroneStatistics().calculationGlobalStat();
                RestInterface restInterface = new RestInterface();

                // Invio le statistiche globali al server amministratore
                restInterface.postStatistics(globalStatistics);
            }
        }
    }
}
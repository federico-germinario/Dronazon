package Smart_city;

import Server_amministratore.GlobalStatistics;

import java.util.ArrayList;

/**
 * Classe che contiene tutte informazioni che il master riceve
 * dai droni dopo aver completato una consegna
 *
 * @author Federico Germinario
 */
public class DroneStatistics {

    public class Drone{
        private int id;
        private int nDeliveries;

        public Drone(int id, int nDeliveries){
            this.id = id;
            this.nDeliveries = nDeliveries;
        }

        public int getnDeliveries() {
            return nDeliveries;
        }

        public void incNDeliveries() {
            nDeliveries++;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

    private final ArrayList<Drone> nDeliveriesList;
    private final ArrayList<Float> kmList;
    private final ArrayList<Double> pollutionMeasurementsList;
    private final ArrayList<Integer> batteryList;

    private final Object deliveryLock;
    private final Object kmLock;
    private final Object pollutionLock;
    private final Object batteryLock;

    public DroneStatistics(){
        nDeliveriesList = new ArrayList<>();
        kmList = new ArrayList<>();
        pollutionMeasurementsList = new ArrayList<>();
        batteryList = new ArrayList<>();

        deliveryLock = new Object();
        kmLock = new Object();
        pollutionLock = new Object();
        batteryLock = new Object();
    }

    /**
     * Restituisce una copia dell'array contenente il numero di consegne effettuate
     * da ogni singolo drone e svuota l'array originale
     *
     * @return copia dell'array contenente il numero di consegne effettuate
     */
    public ArrayList<Drone> getAndCleanNDeliveriesList() {
        synchronized (deliveryLock) {
            ArrayList<Drone> copy = new ArrayList<>(nDeliveriesList);
            nDeliveriesList.clear();

            return copy;
        }
    }

    /**
     * Incrementa il numero di consegne effettuate da un drone
     *
     * @param id
     */
    public void addNDeliveries(int id){
        synchronized (deliveryLock) {
            for (Drone d : nDeliveriesList) {
                if (d.getId() == id) {
                    d.incNDeliveries();
                    return;
                }
            }
            nDeliveriesList.add(new Drone(id, 1));
        }
    }

    /**
     * Restituisce la copia dell'array contenente i km effettuati dai droni e
     * svuota l'array originale
     *
     * @return copia dell'array contenente i km effettuati dai droni
     */
    public ArrayList<Float> getAndCleanKmList() {
        synchronized (kmLock) {
            ArrayList<Float> copy = new ArrayList<>(kmList);
            kmList.clear();

            return copy;
        }
    }

    /**
     * Aggiunge all'array contenente i km effettuati dai droni un
     * nuovo chilometraggio
     *
     * @param km
     * @return true aggiunta eseguita con successo
     *         false aggiunta fallita
     */
    public boolean addKm(float km){
        synchronized (kmLock) {
            return kmList.add(km);
        }
    }

    /**
     * Restituisce la copia dell'array contenente le misurazioni dell'inquinamento e
     * svuota l'array originale
     *
     * @return copia dell'array contenente le misurazioni dell'inquinamento
     */
    public ArrayList<Double> getAndCleanPollutionMeasurementsList() {
        synchronized (pollutionLock){
            ArrayList<Double> copy = new ArrayList<>(pollutionMeasurementsList);
            pollutionMeasurementsList.clear();

            return copy;
        }
    }

    /**
     * Aggiunge una nuova misurazione dell'inquinamento nell'array
     *
     * @param m misurazione
     * @return true aggiunta eseguita con successo
     *         false aggiunta fallita
     */
    public boolean addPollution(Double m){
        synchronized (pollutionLock) {
            return pollutionMeasurementsList.add(m);
        }
    }


    /**
     * Restituisce la copia dell'array contenente le batterie residue e
     * svuota l'array originale
     *
     * @return copia dell'array contenente le batterie residue
     */
    public ArrayList<Integer> getAndCleanBatteryList() {
        synchronized (batteryLock){
            ArrayList<Integer> copy = new ArrayList<>(batteryList);
            batteryList.clear();

            return copy;
        }
    }

    /**
     * Aggiunge una percentuale di batteria residua nell'array
     *
     * @param b percentuale batteria
     * @return true aggiunta eseguita con successo
     *         false aggiunta fallita
     */
    public boolean addBattery(int b){
        synchronized (batteryLock) {
            return batteryList.add(b);
        }
    }


    /**
     * Calcolo statistiche globali
     *
     * @return statistiche globali
     */
    public GlobalStatistics calculationGlobalStat(){
        ArrayList<Drone> nDeliveriesList = getAndCleanNDeliveriesList();
        ArrayList<Float> kmList = getAndCleanKmList();
        ArrayList<Double> pollutionMeasurementsList = getAndCleanPollutionMeasurementsList();
        ArrayList<Integer> batteryList = getAndCleanBatteryList();

        GlobalStatistics globalStatistics = new GlobalStatistics();

        if(nDeliveriesList.size() == 0){
            globalStatistics.setMedia_consegne(0);
        }else {
            float somma = 0;

            for (Drone d : nDeliveriesList) {
                somma += d.nDeliveries;
            }

            globalStatistics.setMedia_consegne(somma / nDeliveriesList.size());
        }


        if(kmList.size() == 0){
            globalStatistics.setMedia_km(0);
        }else {
            float somma = 0;

            for (float km : kmList) {
                somma += km;
            }

            globalStatistics.setMedia_km((somma / kmList.size()));
        }


        if(pollutionMeasurementsList.size() == 0){
            globalStatistics.setMedia_inquinamento(0);
        }else {
            float somma = 0;

            for (double m : pollutionMeasurementsList) {
                somma += m;
            }

            globalStatistics.setMedia_inquinamento(somma / pollutionMeasurementsList.size());
        }

        if(batteryList.size() == 0){
            globalStatistics.setMedia_batteriaResidua(0);
        }else {
            float somma = 0;

            for (int b : batteryList) {
                somma += b;
            }

            globalStatistics.setMedia_batteriaResidua(somma / batteryList.size());
        }

        globalStatistics.setTimestamp(System.currentTimeMillis());

        return globalStatistics;
    }

}

package Server_amministratore;

import Server_amministratore.Exceptions.NoContentException;
import com.sun.jersey.api.NotFoundException;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

/**
 * Classe che gestisce e contiene le statistiche della smart city
 *
 * @author Federico Germinario
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Statistics {
    private ArrayList<GlobalStatistics> statistics;
    private static Statistics istance;

    private Statistics(){
        statistics = new ArrayList<>();
    }

    /**
     * Singleton
     * @return istance
     */
    public synchronized static Statistics getInstance(){
        if(istance==null)
            istance = new Statistics();
        return istance;
    }

    /**
     * Restituisce una copia dell'array delle statistiche globali
     */
    public synchronized ArrayList<GlobalStatistics> getStatistics() {
        return new ArrayList<>(statistics);
    }

    /**
     * Metodo setter array delle statistiche globali
     * @param statistics
     */
    public void setStatistics(ArrayList<GlobalStatistics> statistics) {
        this.statistics = statistics;
    }

    /**
     * Permette di aggiungere una statistica globale all'array
     *
     * @param globalStatistics
     * @return true aggiunta avvenuta correttamente
     *         false aggiunta fallita
     */
    public synchronized boolean add(GlobalStatistics globalStatistics){
        return statistics.add(globalStatistics);
    }


    /**
     * Restituisce le ultime n statistiche globali relative alla smart-city
     *
     * @param n
     * @return copia dell'array aggiornata
     * @throws IllegalArgumentException
     * @throws NoContentException
     */
    public ArrayList<GlobalStatistics> getLastStats(int n) throws IllegalArgumentException, NoContentException {
        if(n==0) throw new IllegalArgumentException();

        ArrayList<GlobalStatistics> statisticsCopy = getStatistics();
        if(statisticsCopy.size() == 0) throw new NoContentException();

        ArrayList<GlobalStatistics> lastStatistics= new ArrayList<>();

        if(n>statisticsCopy.size()){
            n=statisticsCopy.size();
        }

        for (int i=statisticsCopy.size()-n; i < statisticsCopy.size(); i++){
            lastStatistics.add(statisticsCopy.get(i));
        }
        return lastStatistics;
    }


    /**
     * Restituisce la media del numero di consegne effettuate dai droni della smart-city tra due timestamp
     *
     * @param ts1 timestamp 1
     * @param ts2 timestamp 2
     * @return media calcolata
     * @throws IllegalArgumentException
     * @throws NoContentException
     * @throws NotFoundException
     */
    public float averageDeliveries(long ts1, long ts2) throws IllegalArgumentException, NoContentException, NotFoundException {
        if(ts1<0 || ts2<0 || ts1>ts2) throw new IllegalArgumentException();

        ArrayList<GlobalStatistics> statisticsCopy = getStatistics();
        if(statisticsCopy.size()==0) throw new NoContentException();

        float somma = 0;
        int n = 0;
        long ts;

        for(GlobalStatistics globalStatistics: statisticsCopy){
            ts=globalStatistics.getTimestamp();
            if(ts >= ts1 && ts <= ts2) {
                somma += globalStatistics.getMedia_consegne();
                n++;
            }
        }

        if(n==0) throw new NotFoundException();
        return somma/n;
    }


    /**
     * Restituisce la media del numero di km effettuati dai droni della smart-city tra due timestamp
     *
     * @param ts1 timestamp 1
     * @param ts2 timestamp 2
     * @return media km effettuati
     * @throws IllegalArgumentException
     * @throws NoContentException
     * @throws NotFoundException
     */
    public float averageKm(long ts1, long ts2) throws IllegalArgumentException, NoContentException, NotFoundException {
        if(ts1<0 || ts2<0 || ts1>ts2) throw new IllegalArgumentException();

        ArrayList<GlobalStatistics> statisticsCopy = getStatistics();
        if(statisticsCopy.size()==0) throw new NoContentException();
        float somma = 0;
        int n = 0;
        long ts;

        for(GlobalStatistics globalStatistics: statisticsCopy){
            ts=globalStatistics.getTimestamp();
            if(ts >= ts1 && ts <= ts2) {
                somma += globalStatistics.getMedia_km();
                n++;
            }
        }

        if(n==0) throw new NotFoundException();

        return somma/n;
    }
}
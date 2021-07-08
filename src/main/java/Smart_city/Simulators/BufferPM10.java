package Smart_city.Simulators;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

/**
 * Classe che rappresenta una coda usando la tecnica della sliding window
 * con un overlap del 50% e dimensione del buffer di 8 misurazioni
 *
 * @author Federico Germinario
 */

public class BufferPM10 implements Buffer {

    private final Queue<Measurement> buffer;
    private final List<Measurement> avgList;
    private final int maxSize;
    private final int overlap;

    public BufferPM10(){
        buffer = new LinkedList<>();
        avgList = new LinkedList<>();
        maxSize = 8;
        overlap = 4;
    }

    /**
     * Metodo per aggiungere una misurazione alla coda
     *
     * @param m misura da aggiungere
     */
    @Override
    public synchronized void addMeasurement(Measurement m) {
        buffer.add(m);
        if(buffer.size() >= maxSize){
            int counter = 0;

            // Calcolo la somma dei valori presenti prima dell'overlap e li elimino dalla coda
            for(int i = 0; i < overlap; i++){
                counter += Objects.requireNonNull(buffer.poll()).getValue();
            }

            // Calcolo la somma dei valori presenti dopo dell'overlap
            for(Measurement measurement: buffer){
                counter += measurement.getValue();
            }

            // Calcolo la media di tutti i valori
            int average = counter/maxSize;

            // Inserisco la media nella lista delle medie
            Measurement measAvg = new Measurement(m.getId(), m.getType(), average, System.currentTimeMillis());
            avgList.add(measAvg);
        }

    }

    /**
     * Metedo utilizzato per ottenere tutte le misurazioni contenute nella struttura dati e
     * il suo svuotamento
     *
     * @return lista della misurazioni
     */
    @Override
    public synchronized List<Measurement> readAllAndClean() {
        List<Measurement> avgListCopy = new LinkedList<>(avgList);
        avgList.clear();
        return avgListCopy;
    }

}

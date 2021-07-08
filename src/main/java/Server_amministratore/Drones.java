package Server_amministratore;

import Server_amministratore.Exceptions.ConflictIdException;
import Server_amministratore.Exceptions.NoContentException;
import com.sun.jersey.api.NotFoundException;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe per la gestione e la memorizzazione della lista dei droni da parte del
 * Server Amministratore
 *
 * @author Federico Germinario
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Drones {

    private List<Drone> dronesList;
    private static Drones instance;

    private Drones(){
        dronesList = new ArrayList<>();
    }

    /**
     * Restituisce una copia della lista dei droni
     *
     * @return
     * @throws NoContentException lista vuota
     */
    public synchronized ArrayList<Drone> getDronesList() throws NoContentException {
        if(dronesList.size() == 0) throw new NoContentException();
        return new ArrayList<>(dronesList);
    }

    /**
     * Metedo setter lista dei droni
     * @param dronesList
     */
    public synchronized void setDronesList(ArrayList<Drone> dronesList) {
        this.dronesList = dronesList;
    }

    /**
     * Singleton
     * @return instance
     */
    public synchronized static Drones getInstance(){
        if(instance==null)
            instance = new Drones();
        return instance;
    }

    /**
     * Aggiunta di un drone alla lista dei droni
     *
     * @param d drone da aggiungere
     * @return copia della lista dei droni aggiornata
     * @throws ConflictIdException
     */
    public synchronized ArrayList<Drone> add(Drone d) throws ConflictIdException {
        for(Drone drone: dronesList){
            if (drone.getId() == d.getId()){
                throw new ConflictIdException();
            }
        }
        dronesList.add(d);
        return new ArrayList<>(dronesList);
    }

    /**
     * Rimozione drone dalla lista dei droni
     *
     * @param d drone da rimuovere
     * @return 0 se il drone è stato rimosso correttamente
     * @throws NotFoundException
     */
    public synchronized int removeDrone(Drone d) throws NotFoundException {
        for(Drone drone: dronesList){
            if (drone.getId() == d.getId()){
                dronesList.remove(drone);
                return 0;
            }
        }
        throw new NotFoundException();
    }

    /**
     * Rimozione drone dalla lista
     *
     * @param id id drone da rimuovere
     * @return 0 se il drone è stato rimosso correttamente
     * @throws NotFoundException
     */
    public synchronized int removeDrone(int id) throws NotFoundException {
        for(Drone drone: dronesList){
            if (drone.getId() == id){
                dronesList.remove(drone);
                return 0;
            }
        }
        throw new NotFoundException();
    }
}

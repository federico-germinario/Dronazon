package Server_amministratore;

import Dronazon.Coordinate;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

/**
 * Classe che rappresenta il messaggio che verrà invito al drone dopo il
 * corretto inserimento nella rete da parte del Server Amministratore
 *
 * @author Federico Germinario
 */
@XmlRootElement
public class MessageToDrone {
    private Coordinate coordinate;
    private ArrayList<Drone> dronesList;

    public MessageToDrone(){}

    public MessageToDrone(ArrayList<Drone> dronesList){
        int x = (int) (Math.random() * (9 + 1));   //0-9
        int y = (int) (Math.random() * (9 + 1));   //0-9

        this.coordinate = new Coordinate(x,y);
        this.dronesList = dronesList;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public ArrayList<Drone> getDronesList() {
        return dronesList;
    }

    public void setDronesList(ArrayList<Drone> dronesList) {
        this.dronesList = dronesList;
    }
}

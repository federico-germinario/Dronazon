package Server_amministratore;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Classe che rappresenta un drone
 *
 * @author Federico Germinario
 */
@XmlRootElement
public class Drone {

    private int id;
    private String ip_address;
    private int port;

    public Drone(){
        id = 1 + (int) (Math.random() * ((50-1) + 1)); // Id casuale [1-50]
        port= 8080 + id;
        ip_address = "localhost";
    }

    public Drone(int id, String ip_address, int port){
        this.id = id;
        this.ip_address = ip_address;
        this.port = port;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIp_address() {
        return ip_address;
    }

    public void setIp_address(String ip_address) {
        this.ip_address = ip_address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}

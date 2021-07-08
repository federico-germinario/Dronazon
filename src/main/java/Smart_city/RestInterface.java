package Smart_city;

import Server_amministratore.Drone;
import Server_amministratore.GlobalStatistics;
import Server_amministratore.MessageToDrone;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Classe che mette a disposizione un interfaccia Rest con il server amministratore
 *
 * @author Federico Germinario
 */
public class RestInterface {
    Client client;
    String serverAddress;
    ClientResponse response;

    public RestInterface(){
        this.client = Client.create();
        this.serverAddress = "http://localhost:1337";
        this.response = null;
    }

    /**
     * Metodo che permette la registrazione al server amministratore
     *
     * @param id
     * @param ip
     * @param port
     * @return messaggio dal server amministratore
     */
    public MessageToDrone registration(int id, String ip, int port){
        String postPath = "/drones/add";
        String url = serverAddress + postPath;
        WebResource webResource = client.resource(url);

        String input = new Gson().toJson(new Drone(id, ip, port));
        try {
            response = webResource.type("application/json").post(ClientResponse.class, input);
        } catch (ClientHandlerException e) {
            System.out.println("Server non disponibile");
            return null;
        }

        int status = response.getStatus();
        if (status != 200) {
            if (status == 409) System.out.println("Failed: id già presente");
            else System.out.println("Failed : HTTP error code : " + status);
            return null;
        }

        return response.getEntity(MessageToDrone.class);
    }

    /**
     * Metodo che permette di richiedere la rimozione di un drone al server
     * amministratore
     *
     * @param id
     * @return 0 ok
     *         -1 errore
     */
    public int remove(int id){
        String deletePath = "/drones/delete/" + id;
        String url = serverAddress + deletePath;
        WebResource webResource = client.resource(url);

        try {
            response = webResource.delete(ClientResponse.class);
        } catch (ClientHandlerException e) {
            System.out.println("Server non disponibile");
            return -1;
        }

        int status = response.getStatus();
        if (status != 200) {
            if (status == 404) System.out.println("Failed : ID non trovato");
            System.out.println("Failed : HTTP error code : " + status);
            return -1;
        }

        return 0;
    }

    /**
     * Metodo che permette di inviare le statistiche globali al server amministratore
     *
     * @param globalStatistics
     * @return 0 ok
     *         -1 errore
     */
    public int postStatistics(GlobalStatistics globalStatistics){
        String postPath = "/drones/statistics";
        String url = serverAddress + postPath;
        WebResource webResource = client.resource(url);

        String input = new Gson().toJson(globalStatistics);
        try {
            response = webResource.type("application/json").post(ClientResponse.class, input);
        } catch (ClientHandlerException e) {
            System.out.println("Server non disponibile");
            return -1;
        }

        int status = response.getStatus();
        if (status != 200) {
            System.out.println("Failed : HTTP error code : " + status);
            return -1;
        }

        return 0;
    }

}

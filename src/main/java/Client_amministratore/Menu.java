package Client_amministratore;

import Server_amministratore.Drone;
import Server_amministratore.GlobalStatistics;
import com.sun.jersey.api.client.*;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;

import java.util.ArrayList;
import java.util.Scanner;


/**
 * Classe utilizzata come interfaccia con il Server Amministratore REST
 *
 * @author Federico Germinario
 */
public class Menu {
    private final Scanner scanner;
    private final Client client;
    private final String serverAddress;
    private ClientResponse response;

    public Menu(Scanner scanner){
        this.scanner = scanner;

        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        clientConfig.getClasses().add(JacksonJsonProvider.class);
        this.client = Client.create(clientConfig);

        this.serverAddress="http://localhost:1337";
        this.response = null;
    }

    public void printMenu() {
        System.out.println("====================================================================================================================");
        System.out.println("|                                                 MENU SELECTION                                                   |");
        System.out.println("====================================================================================================================");
        System.out.println("| Options:                                                                                                         |");
        System.out.println("|    1. Visualizza elenco dei droni presenti nella rete                                                            |");
        System.out.println("|    2. Visualizza le ultime n statistiche globali relative alla smart-city                                        |");
        System.out.println("|    3. Visualizza la media del numero di consegne effettuate dai droni della smart-city tra due timestamp t1 e t2 |");
        System.out.println("|    4. Visualizza la media dei km percorsi dai droni della smart-city tra due timestamp t1 e t2                   |");
        System.out.println("|    5. Stop Client Amministratore                                                                                 |");
        System.out.println("====================================================================================================================");
        System.out.print("Select option: ");
    }

    public void selectOption(int s){
        switch (s){
            case 1:
                option1();
                break;
            case 2:
                option2();
                break;
            case 3:
                option3();
                break;
            case 4:
                option4();
                break;
            case 5:
                break;
            default:
                System.out.println("Command not found, try again");
        }
    }

    /**
     * Richiesta REST al server amministratore per la visualizzazione
     * dell'elenco dei droni presenti nella rete
     */
    private void option1() {

        String getPath = "/administrators/getdronelist";
        String url = serverAddress + getPath;
        WebResource webResource = client.resource(url);

        try {
            response = webResource.type("application/json").get(ClientResponse.class);
        } catch (ClientHandlerException e) {
            System.out.println("Server non disponibile");
            return;
        }

        int status = response.getStatus();
        if (status != 200) {
            if (status == 204) System.out.println("Failed: la lista dei droni è vuota!");
            else System.out.println("Failed : HTTP error code : " + status);
            return;
        }

        ArrayList<Drone> drones = response.getEntity(new GenericType<ArrayList<Drone>>() {});
        System.out.println("Lista droni:");
        for (Drone d : drones) {
            System.out.println("======================");
            System.out.println("Id: " + d.getId());
            System.out.println("Ip address: " + d.getIp_address());
            System.out.println("Port: " + d.getPort());
            System.out.println("======================");
            System.out.println();
        }

    }

    /**
     * Richiesta REST al server amministratore per la visualizzazione delle
     * ultime n statistiche globali relative alla smart-city
     */
    private void option2(){

        System.out.print("Inserire n: ");

        while(!scanner.hasNextInt()) {
            scanner.nextLine();
            System.out.println("Enter an integer");
            scanner.next();
        }
        int n = scanner.nextInt();


        String getPath = "/administrators/getLastStats/" + n;
        String url = serverAddress + getPath;
        WebResource webResource = client.resource(url);

        try {
            response = webResource.type("application/json").get(ClientResponse.class);
        } catch (ClientHandlerException e) {
            System.out.println("Server non disponibile");
            return;
        }

        int status = response.getStatus();
        if (status != 200) {
            switch (status){
                case 400:
                    System.out.println("Failed: parametro non corretto!");
                    break;
                case 204:
                    System.out.println("Failed: non sono presenti statistiche globali!");
                    break;
                default:
                    System.out.println("Failed. HTTP error code: " + status);
            }
            return;
        }

        ArrayList<GlobalStatistics> lastStats  = response.getEntity(new GenericType<ArrayList<GlobalStatistics>>(){});
        System.out.println("Ultime " + n + " statistiche globali:");
        for (GlobalStatistics globalStatistics : lastStats){
            System.out.println("==================================================");
            System.out.println("Media numero di consegne: " + globalStatistics.getMedia_consegne());
            System.out.println("Media km percorsi: " + globalStatistics.getMedia_km());
            System.out.println("Media livello di inquinamento:  " + globalStatistics.getMedia_inquinamento());
            System.out.println("Media livello di batteria residuo:  " + globalStatistics.getMedia_batteriaResidua());
            System.out.println("Timestamp: :  " + globalStatistics.getTimestamp());
            System.out.println("==================================================");
            System.out.println();
        }
    }

    /**
     * Richiesta REST al server amministratore per la visualizzazione della media del numero
     * di consegne effettuate dai droni della smart-city tra due timestamp t1 e t2
     */
    private void option3(){

        System.out.print("Inserire timestamp1: ");
        while(!scanner.hasNextLong()) {
            scanner.nextLine();
            System.out.println("Enter an long");
            scanner.next();
        }
        long ts1 = scanner.nextLong();

        System.out.print("Inserire timestamp2: ");
        while(!scanner.hasNextLong()) {
            scanner.nextLine();
            System.out.println("Enter an long");
            scanner.next();
        }
        long ts2 = scanner.nextLong();

        String getPath = "/administrators/getAverageDeliveries/" + ts1 + "/" + ts2;
        String url = serverAddress + getPath;
        WebResource webResource = client.resource(url);

        try {
            response = webResource.type("text/plain").get(ClientResponse.class);
        } catch (ClientHandlerException e) {
            System.out.println("Server non disponibile");
            return;
        }

        int status = response.getStatus();
        if (status != 200) {
            switch (status){
                case 404:
                    System.out.println("Failed: non è stata trovata nessuna media del numero di consegne tra i due timestamp");
                case 400:
                    System.out.println("Failed: parametri non corretti!");
                    break;
                case 204:
                    System.out.println("Failed: non sono presenti statistiche globali!");
                    break;
                default:
                    System.out.println("Failed. HTTP error code: " + status);
                }
                return;
        }

        String output = response.getEntity(String.class);
        System.out.println("La media del numero di consegne effettuate dai droni tra " + ts1 + " e " + ts2 + " è: " + output);
    }

    /**
     * Richiesta REST al server amministratore per la visualizzazione della media dei
     * km percorsi dai droni della smart-city tra due timestamp t1 e t2
     */
    private void option4(){
        System.out.print("Inserire timestamp1: ");
        while(!scanner.hasNextLong()) {
            scanner.nextLine();
            System.out.println("Enter an long");
            scanner.next();
        }
        long ts1 = scanner.nextLong();

        System.out.print("Inserire timestamp2: ");
        while(!scanner.hasNextLong()) {
            scanner.nextLine();
            System.out.println("Enter an long");
            scanner.next();
        }
        long ts2 = scanner.nextLong();


        String getPath = "/administrators/getAverageKm/" + ts1 + "/" + ts2;
        String url = serverAddress + getPath;
        WebResource webResource = client.resource(url);

        try {
            response = webResource.type("text/plain").get(ClientResponse.class);
        } catch (ClientHandlerException e) {
            System.out.println("Server non disponibile");
            return;
        }

        int status = response.getStatus();
        if (status != 200) {
            switch (status){
                case 404:
                    System.out.println("Failed: non è stata trovata nessuna media del numero di consegne tra i due timestamp");
                case 400:
                    System.out.println("Failed: parametri non corretti!");
                    break;
                case 204:
                    System.out.println("Failed: non sono presenti statistiche globali!");
                    break;
                default:
                    System.out.println("Failed. HTTP error code: " + status);
            }
            return;
        }

        String output = response.getEntity(String.class);
        System.out.println("La media dei km percorsi dai droni tra " + ts1 + " e " + ts2 + " è: " + output);
    }

}

package Server_amministratore;

import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.util.Scanner;

/**
 * Server REST che riceve statistiche dai droni e che permette
 * la gestione dinamica della rete di droni
 *
 * @author Federico Germinario
 */

public class ServerAdministrator {

    private static final String HOST = "localhost";
    private static final int PORT = 1337;

    public static void main(String[] args) throws IOException {
        boolean stop = false;

        HttpServer server = HttpServerFactory.create("http://"+HOST+":"+PORT+"/");
        server.start();

        System.out.println("Server running!");
        System.out.println("Server started on: http://"+HOST+":"+PORT);

        System.out.println("Hit quit to stop");

        Scanner s = new Scanner(System.in);
        while (!stop){
            if (s.nextLine().equalsIgnoreCase("quit"))
                stop = true;
            else
                System.out.println("Command not found");
        }
        s.close();

        server.stop(0);
        System.out.println("Server stopped");

        System.exit(0);
    }
}


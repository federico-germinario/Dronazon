package Smart_city.Threads;

import Smart_city.MyDrone;
import java.util.Scanner;

/**
 * Thread in ascolto sullo STDIO per la chiusura esplicita del drone
 *
 * @author Federico Germinario
 */
public class QuitThread extends Thread{
    private final MyDrone myDrone;
    private boolean stop;
    private final Scanner s;

    public QuitThread(MyDrone myDrone){
        this.myDrone = myDrone;
        stop = false;
        s = new Scanner(System.in);
    }

    @Override
    public void run() {
        while (!stop) {
            if (s.nextLine().equalsIgnoreCase("quit")) {
                myDrone.setQuit();

                synchronized (this){
                    notify();
                }

                stop = true;
            } else
                System.out.println("Command not found");
        }
    }

    public void stopThread(){
        stop = true;
    }
}

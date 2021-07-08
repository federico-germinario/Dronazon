package Smart_city.Threads;

import Smart_city.MyDrone;

/**
 * Thread per il controllo della batteria residua
 *
 * @author Federico Germinario
 */
public class CheckBatteryThread extends Thread{
    private final MyDrone myDrone;
    private final QuitThread quitThread;
    private final Object checkBatteryLock;

    public CheckBatteryThread(MyDrone myDrone, QuitThread quitThread){
        this.myDrone = myDrone;
        this.quitThread = quitThread;
        checkBatteryLock = myDrone.getCheckBatteryLock();
    }

    @Override
    public void run() {

        while (!this.isInterrupted()) {

            // Aspetto che la batteria scenda sotto il 15 %
            synchronized (checkBatteryLock){
                while(myDrone.getBattery() >= 15){
                    try {
                        checkBatteryLock.wait();
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }

            if (myDrone.getBattery() < 15) {
                System.out.println("\nLivello di batteria inferiore al 15%");
                myDrone.setQuit();

                // Notifico al thread che la variabile quit è diventata true
                synchronized (quitThread){
                    quitThread.notify();
                }

                return;
            }
        }
    }
}

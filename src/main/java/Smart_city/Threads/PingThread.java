package Smart_city.Threads;

import Smart_city.MyDrone;
import Smart_city.OtherDrone;
import com.example.grpc.ServiceGrpc;
import com.example.grpc.ServiceOuterClass.*;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

/**
 * Thread che permette di capire se il drone successivo è uscito dalla rete
 *
 * @author Federico Germinario
 */
public class PingThread extends Thread{
    private final MyDrone myDrone;

    public PingThread(MyDrone myDrone){
        this.myDrone = myDrone;
    }

    @Override
    public void run() {

        while (!this.isInterrupted()){

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                return;
            }

            OtherDrone successorDrone = myDrone.getSuccessorDrone();
            String ip = successorDrone.getIp_address();
            int port = successorDrone.getPort();
            int id = successorDrone.getId();

            final ManagedChannel channel = ManagedChannelBuilder.forTarget(ip + ":" + port).usePlaintext().build();
            ServiceGrpc.ServiceStub stub = ServiceGrpc.newStub(channel);
            Empty request =  Empty.newBuilder().build();

            stub.ping(request, new StreamObserver<Empty>() {
                @Override
                public void onNext(Empty value) {
                    //System.out.println("Drone " + id + " is alive!");
                }

                @Override
                public void onError(Throwable t) {
                    if(channel.getState(true).equals(ConnectivityState.TRANSIENT_FAILURE)) {
                        if(id == myDrone.getSuccessorDrone().getId()) {
                            System.out.println("[PingThread] Il drone successivo [" + id + "] è uscito!");
                            boolean nextIsMaster = false;
                            OtherDrone master = myDrone.getDroneMaster();

                            // Controllo se il drone uscito dalla rete è il master
                            if (master != null && id == master.getId()) {
                                nextIsMaster = true;
                            }

                            myDrone.findNextNextDrone();

                            // Se il drone successivo era il master induco l'elezione
                            if (nextIsMaster) {
                                myDrone.requestElection();
                            }
                        }
                    }
                    channel.shutdownNow();

                }

                @Override
                public void onCompleted() {
                    channel.shutdownNow();
                }

            });
        }
    }

}

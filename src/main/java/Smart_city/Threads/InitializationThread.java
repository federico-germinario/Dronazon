package Smart_city.Threads;

import Smart_city.MyDrone;
import Smart_city.OtherDrone;
import com.example.grpc.ServiceGrpc;
import com.example.grpc.ServiceOuterClass;
import com.example.grpc.ServiceOuterClass.*;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.TimeUnit;

/**
 * Thread che permette tramite GRPC di presentarsi agli altri
 * droni ed entrare nella rete ad anello
 *
 * @author Federico Germinario
 */
public class InitializationThread extends Thread{
    private final MyDrone myDrone;
    private OtherDrone drone;
    private OtherDrone dronePrev;

    public InitializationThread(MyDrone myDrone, OtherDrone drone, OtherDrone dronePrev){
        this.myDrone = myDrone;
        this.drone = drone;
        this.dronePrev = dronePrev;
    }

    public void run() {
        final ManagedChannel channel = ManagedChannelBuilder.forTarget(drone.getIp_address() + ":" + drone.getPort()).usePlaintext().build();
        ServiceGrpc.ServiceStub stub = ServiceGrpc.newStub(channel);
        InitializationRequest request;

        request = ServiceOuterClass.InitializationRequest.newBuilder().setChangeSuccDrone(dronePrev.getId() == drone.getId()).
                setId(myDrone.getId()).setIp(myDrone.getIp_address()).setPort(myDrone.getPort()).
                setX(myDrone.getCoordinate().getX()).setY(myDrone.getCoordinate().getY()).build();

        stub.initialization(request, new StreamObserver<InitializationResponse>() {

            public void onNext(InitializationResponse response) {

                    if (response.getIsMaster()) {
                        myDrone.setDroneMaster(new OtherDrone(response.getIdMaster(), response.getIpMaster(), response.getPortMaster()));
                        System.out.println("[InitializationThread] Il master e': " + response.getIdMaster());
                    }
                    if (response.getChangeSuccDrone()) {
                        System.out.println("[InitializationThread] Inserimento nell'anello completato con successo!");
                    }
            }

            public void onError(Throwable throwable) {
                if(channel.getState(true).equals(ConnectivityState.TRANSIENT_FAILURE)){
                    System.out.println("[InitializationThread] Il drone " + drone.getId() + " è uscito!");
                    myDrone.removeDrone(drone.getId());
                }else {
                    System.out.println("[InitializationThread] Inserimento nell'anello fallito! Esco!");
                    System.exit(0);
                }
                channel.shutdownNow();
            }

            public void onCompleted() {
                channel.shutdownNow();
            }
        });

        try {
            channel.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
package Smart_city.Threads;

import Smart_city.MyDrone;
import Smart_city.OtherDrone;
import com.example.grpc.ServiceGrpc;
import com.example.grpc.ServiceOuterClass.*;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.TimeUnit;

/**
 * Thread che permette di far ricevere al nuovo drone master le informazioni
 * dai droni della rete
 *
 * @author Federico Germinario
 */
public class ReceiveDroneInfoThread extends Thread{

    private final MyDrone myDrone;
    private OtherDrone drone;

    public ReceiveDroneInfoThread(MyDrone myDrone, OtherDrone drone){
        this.myDrone = myDrone;
        this.drone = drone;
    }

    @Override
    public void run() {
        final ManagedChannel channel = ManagedChannelBuilder.forTarget(drone.getIp_address() + ":" + drone.getPort()).usePlaintext().build();
        ServiceGrpc.ServiceStub stub = ServiceGrpc.newStub(channel);
        Empty request = Empty.newBuilder().build();

        stub.infoDrone(request, new StreamObserver<InfoDrone>() {
            @Override
            public void onNext(InfoDrone response) {
                // Aggiorno le informazioni del drone nella lista dei droni
                myDrone.updateDroneList(response.getId(), response.getX(), response.getY(), response.getBattery());
            }

            @Override
            public void onError(Throwable t) {
                if (channel.getState(true).equals(ConnectivityState.TRANSIENT_FAILURE)) {
                    System.out.println("[ReceiveDroneInfoThread] Il drone " + drone.getId() + " è uscito!");
                    myDrone.removeDrone(drone.getId());
                }
                channel.shutdownNow();
            }

            @Override
            public void onCompleted() {
                channel.shutdownNow();
            }
        });
        try {
            channel.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

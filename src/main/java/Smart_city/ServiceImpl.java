package Smart_city;

import Dronazon.Coordinate;
import Smart_city.Simulators.Measurement;
import com.example.grpc.ServiceGrpc;
import com.example.grpc.ServiceOuterClass.*;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;

import java.util.List;

/**
 * Classe che implementa i metodi RPC
 *
 * @author Federico Germinario
 */
public class ServiceImpl extends ServiceGrpc.ServiceImplBase {
    private final MyDrone myDrone;
    private final Object initializationLock;

    public ServiceImpl(MyDrone myDrone){
        this.myDrone = myDrone;
        initializationLock = new Object();
    }

    public void initialization(InitializationRequest request, StreamObserver<InitializationResponse> responseObserver) {
        InitializationResponse response = null;
        OtherDrone newDrone = new OtherDrone(request.getId(), request.getIp(), request.getPort());

            // Richiesta cambio drone successivo + presentazione
            if (request.getChangeSuccDrone()) {
                synchronized(initializationLock) {

                    // Controllo se il cambio del drone successivo è corretto
                    if (myDrone.getId() == myDrone.getSuccessorDrone().getId() || request.getId() < myDrone.getSuccessorDrone().getId()
                            || myDrone.getMinDroneList() == myDrone.getSuccessorDrone().getId()) {

                        if (myDrone.isMaster()) {
                            response = InitializationResponse.newBuilder().setIsMaster(true).setChangeSuccDrone(true).
                                    setIdMaster(myDrone.getId()).setIpMaster(myDrone.getIp_address()).setPortMaster(myDrone.getPort()).build();

                            // Dato che sono il master memorizzo anche le coordinate del drone che ha effettuato la richiesta
                            newDrone.setCoordinate(new Coordinate(request.getX(), request.getY()));
                        } else {
                            response = InitializationResponse.newBuilder().setIsMaster(false).setChangeSuccDrone(true).build();
                        }

                        // Cambio il drone successivo
                        myDrone.setSuccessorDrone(newDrone);
                        System.out.println("ID drone successivo: " + newDrone.getId());

                    }else{
                        response = InitializationResponse.newBuilder().setChangeSuccDrone(true).build();
                    }
                }

            } else if (myDrone.isMaster()) {  // Solo richiesta di presentazione
                response = InitializationResponse.newBuilder().setIsMaster(true).setChangeSuccDrone(false).
                        setIdMaster(myDrone.getId()).setIpMaster(myDrone.getIp_address()).setPortMaster(myDrone.getPort()).build();

                newDrone.setCoordinate(new Coordinate(request.getX(), request.getY()));
            }
            myDrone.addDrone(newDrone);
            System.out.println("Drone " + newDrone.getId() + " aggiunto alla lista dei droni");

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

    public void ping(Empty request, StreamObserver<Empty> responseObserver) {
        Empty response = Empty.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public void election(ElectionRequest request, StreamObserver<ElectionRequest> responseObserver){
        Context ctx = Context.current().fork();
        ctx.run(() -> myDrone.election(request.getId(), request.getBattery()));
        ElectionRequest response = ElectionRequest.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public void elected(ElectedRequest request, StreamObserver<ElectedRequest> responseObserver){
        Context ctx = Context.current().fork();
        ctx.run(() -> myDrone.elected(request));
        ElectedRequest response = ElectedRequest.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public void infoDrone(Empty request, StreamObserver<InfoDrone> responseObserver){
        InfoDrone response = InfoDrone.newBuilder().setId(myDrone.getId()).
                setX(myDrone.getCoordinate().getX()).setY(myDrone.getCoordinate().getY()).
                setBattery(myDrone.getBattery()).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public void delivery(DeliveryRequest request, StreamObserver<DeliveryResponse> responseObserver) {
        myDrone.setDeliveryInProgress(true);
        System.out.println("Ho ricevuto l'ordine " + request.getId());
        System.out.println("Consegna in corso...");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }

        System.out.println("Ordine " + request.getId() + " consegnato!");

        // Decremento la batteria del drone del 10%
        myDrone.setBattery(myDrone.getBattery() - 10);

        // Aggiorno le coordinate del drone
        Coordinate consegna = new Coordinate(request.getXDelivery(), request.getYDelivery());
        myDrone.setCoordinate(consegna);

        // Calcolo la distanza percorsa dal drone
        Coordinate ritiro = new Coordinate(request.getXCollection(), request.getYCollection());
        double distance = myDrone.distance(myDrone.getCoordinate(), ritiro) + myDrone.distance(ritiro, consegna);

        // Ottengo le medie delle misurazioni relative al livello di inquinamento dell'aria
        // rilevate a partire dall'ultima consegna effettuata
        List<Measurement> pollutionMeasurements = myDrone.getBufferPM10().readAllAndClean();

        DeliveryResponse.Builder b = DeliveryResponse.newBuilder();

        b.setTimestampDelivery(System.currentTimeMillis()).
                setX(request.getXDelivery()).setY(request.getYDelivery()).
                setKm((int) distance).setBattery(myDrone.getBattery());

        for (Measurement m : pollutionMeasurements) {
            b.addPollutionMeasurements(m.getValue());
        }

        DeliveryResponse response = b.build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        myDrone.incrNDelivery();
        myDrone.incrTotKM((float) distance);
        myDrone.setDeliveryInProgress(false);

        Object deliveryCompletedLock = myDrone.getDeliveryCompletedLock();
        synchronized (deliveryCompletedLock){
            deliveryCompletedLock.notify();
        }

    }

    public void invalidationElection(InvalidationElectionRequest request, StreamObserver<Empty> responseObserver) {
        myDrone.setParticipantElection(false);

        Context ctx = Context.current().fork();
        if(request.getId() == myDrone.getId()){
            ctx.run(() -> myDrone.requestElection());

        }else {
            ctx.run(() -> myDrone.invalidationElection(request.getId()));
        }

        Empty response = Empty.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}

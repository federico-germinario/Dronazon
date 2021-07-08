package Smart_city;

import Dronazon.Coordinate;
import Dronazon.Order;
import Server_amministratore.Drone;
import Smart_city.Simulators.BufferPM10;
import Smart_city.Threads.OrdersManagement;
import Smart_city.Threads.ReceiveDroneInfoThread;
import Smart_city.Threads.ReceiverOrders;
import com.example.grpc.ServiceGrpc;
import com.example.grpc.ServiceOuterClass.*;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;


/**
 * Classe che rappresenta un drone
 *
 * @author Federico Germinario
 */
public class MyDrone extends Drone {

    private Coordinate coordinate;
    private ArrayList<OtherDrone> droneList;
    private final DroneStatistics droneStatistics;
    private int battery;
    private boolean master;
    OtherDrone droneMaster;
    OtherDrone successorDrone;
    private int nDelivery;
    private float totKM;

    private boolean participantElection;
    private boolean deliveryInProgress;
    private boolean quit;

    private final BufferPM10 bufferPM10;
    private final DeliveryQueue deliveryQueue;

    private ReceiverOrders receiverOrders;
    private OrdersManagement ordersManagement;

    private final Object deliveryCompletedLock;
    private final Object checkBatteryLock;
    private final Object droneListLock;
    private final Object deliveryInProgressLock;
    private final Object masterLock;
    private final Object successorDroneLock;
    private final Object droneMasterLock;
    private final Object participantElectionLock;

    public MyDrone(){
        super();
        this.coordinate = null;
        this.droneList = new ArrayList<>();
        this.battery = 100;
        this.master = false;
        this.successorDrone = null;
        this.droneMaster = null;
        this.participantElection = false;
        this.quit = false;
        this.bufferPM10 = new BufferPM10();
        this.deliveryInProgress = false;
        deliveryCompletedLock = new Object();
        droneStatistics = new DroneStatistics();
        deliveryQueue = new DeliveryQueue();
        nDelivery = 0;
        totKM = 0;
        checkBatteryLock = new Object();
        droneListLock = new Object();
        deliveryInProgressLock = new Object();
        masterLock = new Object();
        successorDroneLock = new Object();
        droneMasterLock = new Object();
        participantElectionLock = new Object();
    }

    public MyDrone(int id, String ip_address, int port){
        super(id, ip_address, port);
        coordinate = null;
        droneList = new ArrayList<>();
        battery = 100;
        master = false;
        successorDrone = null;
        droneMaster = null;
        participantElection = false;
        quit = false;
        bufferPM10 = new BufferPM10();
        deliveryInProgress = false;
        deliveryCompletedLock = new Object();
        droneStatistics = new DroneStatistics();
        deliveryQueue = new DeliveryQueue();
        nDelivery = 0;
        totKM = 0;
        checkBatteryLock = new Object();
        droneListLock = new Object();
        deliveryInProgressLock = new Object();
        masterLock = new Object();
        successorDroneLock = new Object();
        droneMasterLock = new Object();
        participantElectionLock = new Object();
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setDroneList(ArrayList<OtherDrone> otherDrones) {
        synchronized (droneListLock) {
            this.droneList = otherDrones;
        }
    }

    /**
     * Restituisce una copia della lista dei droni
     */
    public ArrayList<OtherDrone> getDroneList() {
        synchronized (droneListLock) {
            return new ArrayList<>(droneList);
        }
    }

    /**
     * Metodo che permette aggiornare le informazioni di un drone
     * presente all'interno della lista dei droni
     *
     * @param id
     * @param x
     * @param y
     * @param battery
     */
    public void updateDroneList(int id, int x, int y, int battery) {
        synchronized (droneListLock) {
            for (OtherDrone d : droneList) {
                if (d.getId() == id) {
                    Coordinate c = new Coordinate(x, y);
                    d.setCoordinate(c);
                    d.setBattery(battery);
                }
            }
        }
    }

    /**
     * Aggiunge un drone nella lista dei droni
     *
     * @param d drone da aggiungere
     */
    public void addDrone(OtherDrone d){
        synchronized (droneListLock) {
            droneList.add(d);
        }
    }

    /**
     * Rimuove un drone dalla lista dei droni
     *
     * @param id id drone da eliminare
     */
    public void removeDrone(int id) {
        synchronized (droneListLock) {
            Iterator<OtherDrone> iter = droneList.iterator();
            while (iter.hasNext()) {
                if (iter.next().getId() == id)
                    iter.remove();
            }
        }
    }


    /**
     * Restituisce il drone con id più piccolo presente nella rete
     *
     * @return id drone più piccolo
     *         -1 nessun drone presente
     */
    public int getMinDroneList(){
        ArrayList<OtherDrone> copy = getDroneList();
        Collections.sort(copy);
        if(copy.get(0) != null)
            return copy.get(0).getId();
        return -1;
    }

    /**
     * Metodo che verifica se un drone è presente nella lista dei droni
     *
     * @param id
     * @return true drone presente
     *         false drone non presente
     */
    public boolean droneIsPresent(int id){
        ArrayList<OtherDrone> copy = getDroneList();
        for (OtherDrone d : copy){
            if(d.getId() == id)
                return true;
        }
        return false;
    }


    /**
     * Restituisce il drone che mi precede nell'anello
     *
     */
    public OtherDrone findDronePrev(){
        OtherDrone dronePrev = null;
        ArrayList<OtherDrone> drones = getDroneList();
        Collections.sort(drones);

        for (int i = drones.size()-1; i >= 0; i--){
            OtherDrone d = drones.get(i);
            if(getId() > d.getId()){
                dronePrev = d;
                break;
            }
        }
        if(dronePrev == null && drones.get(drones.size()-1) != null) dronePrev = drones.get(drones.size()-1);
        return dronePrev;
    }


    /**
     * Cerca il drone che mi segue nell'anello e lo imposta come mio successivo
     */
    public void findNextDrone(){
        OtherDrone nextDrone = null;
        ArrayList<OtherDrone> otherDrones = getDroneList();
        Collections.sort(otherDrones);
        for (OtherDrone d : otherDrones) {
            if (d.getId() > getId()) {
                nextDrone = d;
                break;
            }
        }
        if(nextDrone == null && otherDrones.get(0) != null) nextDrone = otherDrones.get(0);
        setSuccessorDrone(nextDrone);
        System.out.println("ID drone successivo: " + Objects.requireNonNull(nextDrone).getId());
    }


    /**
     * Sostituisce il drone successivo con il successivo del successivo
     */
    public void findNextNextDrone(){
        OtherDrone nextDrone = null;
        ArrayList<OtherDrone> otherDrones = getDroneList();
        Collections.sort(otherDrones);
        for (OtherDrone d : otherDrones) {
            if (d.getId() > successorDrone.getId()) {
                nextDrone = d;
                break;
            }
        }
        if(nextDrone == null && otherDrones.get(0) != null) nextDrone = otherDrones.get(0);
        removeDrone(successorDrone.getId());
        setSuccessorDrone(nextDrone);
        System.out.println("Il nuovo successore è il drone "+ Objects.requireNonNull(nextDrone).getId());
    }

    public DroneStatistics getDroneStatistics() {
        return droneStatistics;
    }


    public Object getCheckBatteryLock() {
        return checkBatteryLock;
    }


    public float getTotKM() {
        return totKM;
    }

    public void setTotKM(float totKM) {
        this.totKM = totKM;
    }

    public void incrTotKM(float totKM){
        this.totKM += totKM;
    }


    public int getNDelivery() {
        return nDelivery;
    }

    public void setNDelivery(int nDelivery) {
        this.nDelivery = nDelivery;
    }

    public void incrNDelivery(){
        this.nDelivery ++;
    }


    public Object getDeliveryCompletedLock() {
        return deliveryCompletedLock;
    }


    public void setDeliveryInProgress(boolean deliveryInProgress) {
        synchronized (deliveryInProgressLock) {
            this.deliveryInProgress = deliveryInProgress;
        }
    }

    /**
     * Modifica il flag di consegna in corso di un determinato drone nella lista dei droni
     *
     * @param deliveryInProgress
     * @param id
     */
    public void setDeliveryInProgress(Boolean deliveryInProgress, int id){
        synchronized (droneListLock) {
            for (OtherDrone d : droneList) {
                if (d.getId() == id) {
                    d.setDeliveryInProgress(deliveryInProgress);
                }
            }
        }
    }

    public boolean isDeliveryInProgress() {
        synchronized (deliveryInProgressLock) {
            return deliveryInProgress;
        }
    }


    /**
     * Metodo che rimane in attesa della terminazione della consegna di cui si sta occupando
     */
    public void waitDelivery(){
        synchronized (deliveryCompletedLock){
            while (isDeliveryInProgress()){
                try {
                    deliveryCompletedLock.wait();
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }


    public void setBattery(int battery) {
        synchronized (checkBatteryLock){
            this.battery = battery;
            checkBatteryLock.notify();
        }
    }

    public int getBattery() {
        synchronized (checkBatteryLock) {
            return battery;
        }
    }

    public void setMaster(boolean master) {
        synchronized (masterLock) {
            this.master = master;
        }
    }

    public boolean isMaster() {
        synchronized (masterLock) {
            return master;
        }
    }


    public void setSuccessorDrone(OtherDrone succ) {
        synchronized (successorDroneLock) {
            this.successorDrone = succ;
        }
    }

    public synchronized OtherDrone getSuccessorDrone() {
        synchronized (successorDroneLock) {
            return successorDrone;
        }
    }


    public void setDroneMaster(OtherDrone droneMaster) {
        synchronized (droneMasterLock) {
            this.droneMaster = droneMaster;
        }
    }

    public OtherDrone getDroneMaster() {
        synchronized (droneMasterLock) {
            return droneMaster;
        }
    }

    public void setParticipantElection(boolean participantElection) {
        synchronized (participantElectionLock) {
            this.participantElection = participantElection;
        }
    }

    public boolean isParticipantElection() {
        synchronized (participantElectionLock) {
            return participantElection;
        }
    }


    public BufferPM10 getBufferPM10() {
        return bufferPM10;
    }


    public void setQuit() {
        this.quit = true;
    }

    public boolean isQuit() {
        return quit;
    }

    /**
     * Metodo di richiesta elezione
     */
    public void requestElection() {
        System.out.println("Il master è uscito dalla rete, induco l'elezione!");

        // Controllo se non sono l'unico nella rete
        if (getDroneList().size() != 1) {

            System.out.println("Election [Id: " + getId() + ", Batteria: " + battery + "] --> " + successorDrone.getId());
            // Invio il messaggio di elezione al drone successivo e mi marco come partecipante
            final ManagedChannel channel = ManagedChannelBuilder.forTarget(successorDrone.getIp_address() + ":" + successorDrone.getPort()).usePlaintext().build();
            ServiceGrpc.ServiceStub stub = ServiceGrpc.newStub(channel);

            // Se il drone sta effettuando una consegna considero la batteria che avrà al termine di essa
            int battery;
            if(isDeliveryInProgress()){
                battery = getBattery() - 10;
            }else {
                battery = getBattery();
            }

            ElectionRequest request = ElectionRequest.newBuilder().setId(getId()).setBattery(battery).build();
            stub.election(request, new StreamObserver<ElectionRequest>() {
                @Override
                public void onNext(ElectionRequest value) {
                   // System.out.println("Mi marco come partecipante");
                    setParticipantElection(true);
                }

                @Override
                public void onError(Throwable t) {
                    if (channel.getState(true).equals(ConnectivityState.TRANSIENT_FAILURE)) {
                        System.out.println("[requestElection] Il drone successivo [" + getId() +"] è uscito!");
                        findNextNextDrone();
                    }
                    channel.shutdownNow();

                    requestElection();
                }

                @Override
                public void onCompleted() {
                    channel.shutdownNow();
                }
            });
        }else {
            // Mi auto proclamo master
            setSuccessorDrone(new OtherDrone(getId(), getIp_address(), getPort()));
            setDroneMaster(new OtherDrone(getId(), getIp_address(), getPort()));
            becomeMaster();
        }
    }

    /**
     * Metodo che gestisce i messaggi di election di un'elezione
     *
     * @param id
     * @param battery
     */
    public void election(int id, int battery){
        System.out.println("Ricevuto messaggio Election [Id: " + id + ", Batteria: " + battery + "]");

        // Se sono il master e ricevo un messaggio di election non mio lo ignoro, elezione multipla in corso!
        if(isMaster() && getId() != id){
            System.out.println("Sono il master ma ho ricevuto il messaggio di election. Lo ignoro e invio un messaggio di elected nell'anello");
            System.out.println("Elected [Id: " + getId() + "]  --> " + successorDrone.getId());
            ElectedRequest request = ElectedRequest.newBuilder().setId(getId()).setIp(getIp_address()).
                    setPort(getPort()).build();

            final ManagedChannel channelElected = ManagedChannelBuilder.forTarget(successorDrone.getIp_address() + ":" + successorDrone.getPort()).usePlaintext().build();
            ServiceGrpc.ServiceStub stubElected = ServiceGrpc.newStub(channelElected);

            stubElected.elected(request, new StreamObserver<ElectedRequest>() {
                @Override
                public void onNext(ElectedRequest value) {
                    //System.out.println("Mi marco come non partecipante");
                    setParticipantElection(false);
                }

                @Override
                public void onError(Throwable t) {
                    if (channelElected.getState(true).equals(ConnectivityState.TRANSIENT_FAILURE)) {
                        System.out.println("[election] Il drone successivo [" +successorDrone.getId() + "] è uscito!");
                        findNextNextDrone();
                    } else {
                        System.out.println("[election] Errore! " + t.getMessage());
                    }
                    channelElected.shutdownNow();
                }

                @Override
                public void onCompleted() {
                    channelElected.shutdownNow();
                }
            });
        }else {
            if ((battery > this.getBattery()) || (battery == this.getBattery() && id > this.getId())) {
                // Inoltra il messaggio al successivo e si marca come partecipante
                final ManagedChannel channel = ManagedChannelBuilder.forTarget(successorDrone.getIp_address() + ":" + successorDrone.getPort()).usePlaintext().build();
                ServiceGrpc.ServiceStub stub = ServiceGrpc.newStub(channel);
                ElectionRequest request = ElectionRequest.newBuilder().setId(id).setBattery(battery).build();

                System.out.println("Inoltro il messaggio di election al successivo. Election [Id: " + id + ", Batteria: " + battery + "] --> " + successorDrone.getId());
                stub.election(request, new StreamObserver<ElectionRequest>() {
                    @Override
                    public void onNext(ElectionRequest value) {
                        //System.out.println("Mi marco come partecipante");
                        setParticipantElection(true);
                    }

                    @Override
                    public void onError(Throwable t) {
                        if (channel.getState(true).equals(ConnectivityState.TRANSIENT_FAILURE)) {
                            System.out.println("[election] Il drone successivo [" + successorDrone.getId() + "] è uscito!");

                            // Controllo se il drone successivo fosse il probabile nuovo master o se il drone di cui ho
                            // ricevuto l'election fosse uscito
                            if (successorDrone.getId() == id || !droneIsPresent(id)) {
                                findNextNextDrone();
                                invalidationElection(getId());
                            } else {
                                findNextNextDrone();
                                election(id, battery);
                            }
                        } else {
                            System.out.println("[election] Errore! " + t.getMessage());
                        }
                        channel.shutdownNow();
                    }

                    @Override
                    public void onCompleted() {
                        channel.shutdownNow();
                    }
                });
                try {
                    channel.awaitTermination(1, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } else if ((battery < this.getBattery() && !isParticipantElection()) || (id < getId() && !isParticipantElection())) {
                final ManagedChannel channel = ManagedChannelBuilder.forTarget(successorDrone.getIp_address() + ":" + successorDrone.getPort()).usePlaintext().build();
                ServiceGrpc.ServiceStub stub = ServiceGrpc.newStub(channel);

                int batteryToSend;
                if (isDeliveryInProgress()) {
                    batteryToSend = getBattery() - 10;
                } else {
                    batteryToSend = getBattery();
                }

                ElectionRequest request = ElectionRequest.newBuilder().setId(getId()).setBattery(batteryToSend).build();

                System.out.println("Inoltro il messaggio di election al successivo con il mio ID. Election [Id: " + getId() + ", Batteria: " + batteryToSend + "] --> " + successorDrone.getId());
                stub.election(request, new StreamObserver<ElectionRequest>() {
                    @Override
                    public void onNext(ElectionRequest value) {
                        //System.out.println("Mi marco come partecipante");
                        setParticipantElection(true);
                    }

                    @Override
                    public void onError(Throwable t) {
                        if (channel.getState(true).equals(ConnectivityState.TRANSIENT_FAILURE)) {
                            System.out.println("[election] Il drone successivo [" + successorDrone.getId() + "] è uscito!");
                            findNextNextDrone();
                            election(id, battery);
                        } else {
                            System.out.println("[election] Errore! " + t.getMessage());
                        }
                        channel.shutdownNow();
                    }

                    @Override
                    public void onCompleted() {
                        channel.shutdownNow();
                    }
                });

                try {
                    channel.awaitTermination(1, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                final ManagedChannel channel = ManagedChannelBuilder.forTarget(successorDrone.getIp_address() + ":" + successorDrone.getPort()).usePlaintext().build();
                ServiceGrpc.ServiceStub stub = ServiceGrpc.newStub(channel);
                ElectedRequest request = ElectedRequest.newBuilder().setId(getId()).setIp(getIp_address()).setPort(getPort()).build();

                System.out.println("Invio il messaggio di elezione avvenuta al successivo. Elected [Id: " + getId() + "] --> " +successorDrone.getId());
                stub.elected(request, new StreamObserver<ElectedRequest>() {
                    @Override
                    public void onNext(ElectedRequest value) {
                        //System.out.println("Mi marco come non partecipante");
                        setParticipantElection(false);
                    }

                    @Override
                    public void onError(Throwable t) {
                        if (channel.getState(true).equals(ConnectivityState.TRANSIENT_FAILURE)) {
                            System.out.println("[elected] Il drone successivo " + successorDrone.getId() + " è uscito!");
                            findNextNextDrone();
                            elected(request);
                        } else {
                            System.out.println("[elected] Errore! " + t.getMessage());
                        }
                        channel.shutdownNow();
                    }

                    @Override
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
    }

    /**
     * Metodo che gestisce i messaggi di elected di un'elezione
     * @param request
     */
    public void elected(ElectedRequest request){
        System.out.println("Ricevuto messaggio di Elected [Id: " + request.getId() + "]");

        // Se sono il master e ricevo un messaggio di elected lo ignoro, elezione multipla!
        if(isMaster() && getId() != request.getId()){
            System.out.println("Sono il master ma ho ricevuto il messaggio di elected. Lo ignoro e invio un nuovo messaggio di elected nell'anello");
            System.out.println("Elected [Id: " + getId() + "]  --> " + successorDrone.getId());
            ElectedRequest r = ElectedRequest.newBuilder().setId(getId()).setIp(getIp_address()).
                    setPort(getPort()).build();

            final ManagedChannel channelElected = ManagedChannelBuilder.forTarget(successorDrone.getIp_address() + ":" + successorDrone.getPort()).usePlaintext().build();
            ServiceGrpc.ServiceStub stubElected = ServiceGrpc.newStub(channelElected);

            stubElected.elected(r, new StreamObserver<ElectedRequest>() {
                @Override
                public void onNext(ElectedRequest value) {
                    //System.out.println("Mi marco come non partecipante");
                    setParticipantElection(false);
                }

                @Override
                public void onError(Throwable t) {
                    if (channelElected.getState(true).equals(ConnectivityState.TRANSIENT_FAILURE)) {
                        System.out.println("[elected] Il drone successivo [" + successorDrone.getId() + "] è uscito!");
                        findNextNextDrone();
                    } else {
                        System.out.println("[elected] Errore! " + t.getMessage());
                    }
                    channelElected.shutdownNow();
                }

                @Override
                public void onCompleted() {
                    channelElected.shutdownNow();
                }
            });
        }else {
            int id = request.getId();
            String ip = request.getIp();
            int port = request.getPort();

            setDroneMaster(new OtherDrone(id, ip, port));
            System.out.println("Il nuovo drone master e': " + request.getId());

            if (id == this.getId()) {
                if (!isMaster()) {
                    becomeMaster();
                    System.out.println("Sono il nuovo master!!");
                }
            } else {
                final ManagedChannel channelElected = ManagedChannelBuilder.forTarget(successorDrone.getIp_address() + ":" + successorDrone.getPort()).usePlaintext().build();
                ServiceGrpc.ServiceStub stubElected = ServiceGrpc.newStub(channelElected);

                System.out.println("Inoltro il messaggio di elezione avvenuta al successivo. Elected [Id: " + id + "] --> " + successorDrone.getId());
                stubElected.elected(request, new StreamObserver<ElectedRequest>() {
                    @Override
                    public void onNext(ElectedRequest value) {
                        //System.out.println("Mi marco come non partecipante");
                        setParticipantElection(false);
                    }

                    @Override
                    public void onError(Throwable t) {
                        if (channelElected.getState(true).equals(ConnectivityState.TRANSIENT_FAILURE)) {
                            System.out.println("[elected] Il drone successivo " + successorDrone.getId() + " è uscito!");

                            //Il drone successivo morto era il nuovo master
                            if (successorDrone.getId() == id) {
                                System.out.println("[elected] Il drone successivo morto era il nuovo master");
                                findNextNextDrone();
                                setParticipantElection(false);
                                requestElection();
                            } else {
                                findNextNextDrone();
                                elected(request);
                            }

                        } else {
                            System.out.println("[elected] Errore! " + t.getMessage());
                        }
                        channelElected.shutdownNow();
                    }

                    @Override
                    public void onCompleted() {
                        channelElected.shutdownNow();
                    }
                });

            }
        }
    }


    /**
     * Metodo che gestisce i messaggi di invalidazione di un'elezione
     *
     * @param id id del drone che richiede un'invalidazione dell'elezione
     */
    public void invalidationElection(int id){
        final ManagedChannel channel = ManagedChannelBuilder.forTarget(successorDrone.getIp_address() + ":" + successorDrone.getPort()).usePlaintext().build();
        ServiceGrpc.ServiceStub stub = ServiceGrpc.newStub(channel);

        InvalidationElectionRequest request = InvalidationElectionRequest.newBuilder().setId(id).build();

        System.out.println("Invio messaggio di invalidazione elezione! Invalidation --> " + successorDrone.getId());
        stub.invalidationElection(request, new StreamObserver<Empty>() {
            @Override
            public void onNext(Empty value) {
            }

            @Override
            public void onError(Throwable t) {
                if(channel.getState(true).equals(ConnectivityState.TRANSIENT_FAILURE)) {
                    System.out.println("[invalidationElection] Il drone successivo " + successorDrone.getId() + " è uscito!");
                    findNextNextDrone();
                }
                invalidationElection(getId());
                channel.shutdownNow();
            }

            @Override
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

    /**
     * Metodo che viene chiamato quando un drone viene eletto master
     * e permette l'attivazione di tutti i suoi servizi
     *
     */
    public void becomeMaster(){
        setMaster(true);
        System.out.println("Sono il master!");

        if(getDroneList().size()>1) {
            System.out.println("Attendo di ricevere le informazioni da tutti gli altri droni...");
            waitReceiveDroneInfo();
            System.out.println("Informazioni ricevute!");
        }

        // Start thread per la ricezione degli ordini tramite MQTT
        receiverOrders = new ReceiverOrders(deliveryQueue);
        receiverOrders.start();

        // Start thread per la gestione degli ordini verso i droni
        ordersManagement = new OrdersManagement(this, deliveryQueue);
        ordersManagement.start();
    }

    /**
     * Metodo che permette di fermare i threads di un drone master
     *
     * @throws InterruptedException
     */
    public void stopThreadMaster() throws InterruptedException {
        System.out.println("Disconnessione del broker MQTT della smart-city...");
        receiverOrders.stopThread();
        receiverOrders.join();

        System.out.println("Attendo la consegna degli ordini pendenti...");
        ordersManagement.stopThread();
        ordersManagement.join();
    }

    /**
     * Metodo che permette di attendere che il drone master abbia ricevuto le informazioni da tutti
     * i droni presenti nella rete
     */
    private void waitReceiveDroneInfo() {
        ArrayList<ReceiveDroneInfoThread> threads = new ArrayList<>();
        ArrayList<OtherDrone> drones = getDroneList();

        for (OtherDrone d : drones) {
            if (d.getId() != getId()) {
                ReceiveDroneInfoThread receiveDroneInfoThread = new ReceiveDroneInfoThread(this, d);
                receiveDroneInfoThread.start();
                threads.add(receiveDroneInfoThread);
            }
        }

        // Mi assicuro che tutti i thread abbiamo terminato
        for (ReceiveDroneInfoThread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }



    public double distance(Coordinate c1, Coordinate c2){
        return sqrt( pow(c2.getX() - c1.getX(), 2) + pow(c2.getY() - c1.getY(), 2) );
    }


    /**
     * Metodo che permette di trovare il drone a cui assegnare un ordine
     * in base alle coordinate del ritiro
     *
     * @param withdrawalCoordinates coordinate di ritiro
     * @return drone a cui assegnare l'ordine
     */
    public OtherDrone deliveryAssignment(Coordinate withdrawalCoordinates) {
        double minDistance = Double.POSITIVE_INFINITY;
        ArrayList<OtherDrone> droneList = getDroneList();
        OtherDrone droneReturn = null;

        for (OtherDrone d : droneList) {
            if (d.isDeliveryInProgress())
                continue;

            if(d.getCoordinate() == null){
                removeDrone(d.getId());
                continue;
            }

            double distance = distance(withdrawalCoordinates, d.getCoordinate());
            if (minDistance > distance) {
                minDistance = distance;
                droneReturn = d;
            } else if (minDistance == distance) {
                if (d.getBattery() > Objects.requireNonNull(droneReturn).getBattery()) {
                    droneReturn = d;
                } else if (d.getBattery() == droneReturn.getBattery()) {
                    if (d.getId() > droneReturn.getId()) {
                        droneReturn = d;
                    }
                }
            }
        }
        return droneReturn;
    }


    /**
     * Metodo che gestisce i messaggi di consegna
     *
     * @param drone drone a cui inviare l'ordine
     * @param order ordine da consegnare
     * @throws InterruptedException
     */
    public void delivery(OtherDrone drone, Order order) throws InterruptedException {
        final ManagedChannel channel = ManagedChannelBuilder.forTarget(drone.getIp_address() + ":" + drone.getPort()).usePlaintext().build();
        ServiceGrpc.ServiceStub stub = ServiceGrpc.newStub(channel);

        DeliveryRequest deliveryRequest = DeliveryRequest.newBuilder().setId(order.getId()).setXCollection(order.getRitiro().getX()).setYCollection(order.getRitiro().getY()).
                setXDelivery(order.getConsegna().getX()).setYDelivery(order.getConsegna().getY()).build();

        setDeliveryInProgress(true, drone.getId());
        stub.delivery(deliveryRequest, new StreamObserver<DeliveryResponse>() {

                    @Override
                    public void onNext(DeliveryResponse response) {
                        System.out.println("Ordine " + order.getId() + " completato!\n");

                        // Aggiorno le informazionidel drone che ha effettuato la consegna
                        updateDroneList(drone.getId(), response.getX(), response.getY(), response.getBattery());

                        // Aggiorno le statistiche globali del drone master
                        droneStatistics.addNDeliveries(drone.getId());
                        droneStatistics.addKm(response.getKm());

                        double somma = 0;
                        List<Double> pollutionMeasurements = response.getPollutionMeasurementsList();
                        for (Double m: pollutionMeasurements){
                            somma += m;
                        }

                        double averagePollution;
                        if(response.getPollutionMeasurementsCount() == 0)
                            averagePollution = 0.0;
                        else
                            averagePollution = somma/response.getPollutionMeasurementsCount();

                        droneStatistics.addPollution(averagePollution);
                        droneStatistics.addBattery(response.getBattery());
                    }

                    @Override
                    public void onError(Throwable t) {
                        if (channel.getState(true).equals(ConnectivityState.TRANSIENT_FAILURE)) {
                            System.out.println("[Delivery] Il drone " + drone.getId() + " è uscito!");
                            removeDrone(drone.getId());
                        } else {
                            System.out.println("Consegna ordine " + order.getId() + " fallita");
                            setDeliveryInProgress(false, drone.getId());
                        }
                        channel.shutdownNow();

                        System.out.println("Reinserisco l'ordine " + order.getId() + " nella coda");
                        deliveryQueue.put(order);
                    }

                    @Override
                    public void onCompleted() {
                        channel.shutdownNow();
                    }
        });
        channel.awaitTermination(8, TimeUnit.SECONDS);
        setDeliveryInProgress(false, drone.getId());
    }

}
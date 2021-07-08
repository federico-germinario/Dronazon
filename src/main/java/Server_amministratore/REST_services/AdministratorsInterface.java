package Server_amministratore.REST_services;

import Server_amministratore.*;
import Server_amministratore.Exceptions.NoContentException;
import com.sun.jersey.api.NotFoundException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.ArrayList;

/**
 * Interfaccia REST per permettere agli amministratori di effettuare interrogazioni
 *
 * @author Federico Germinario
 */

@Path("administrators")
public class AdministratorsInterface {

    @Path("getdronelist/")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getDroneList(){
        try {
            ArrayList<Drone> drones = Drones.getInstance().getDronesList();
            return Response.ok(drones).build();
        }catch (NoContentException e){
            return Response.status(Response.Status.NO_CONTENT).build();
        }
    }

    @Path("getLastStats/{n}")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getLastStats(@PathParam("n") int n){
        try {
            ArrayList<GlobalStatistics> lastStats = Statistics.getInstance().getLastStats(n);
            return Response.ok(lastStats).build();
        }catch (IllegalArgumentException e){
            return Response.status(Response.Status.BAD_REQUEST).build();
        }catch (NoContentException e){
            return Response.status(Response.Status.NO_CONTENT).build();
        }
    }

    @Path("getAverageDeliveries/{ts1}/{ts2}")
    @GET
    @Produces({"text/plain"})
    public Response getAverageDeliveries(@PathParam("ts1") long ts1, @PathParam("ts2") long ts2){
        try {
            float avarange = Statistics.getInstance().averageDeliveries(ts1, ts2);
            return Response.ok(String.valueOf(avarange)).build();
        }catch (NotFoundException e){
            return Response.status(Response.Status.NOT_FOUND).build();
        }catch (IllegalArgumentException e){
            return Response.status(Response.Status.BAD_REQUEST).build();
        }catch (NoContentException e){
            return Response.status(Response.Status.NO_CONTENT).build();
        }
    }

    @Path("getAverageKm/{ts1}/{ts2}")
    @GET
    @Produces({"text/plain"})
    public Response getAverageKm(@PathParam("ts1") long ts1, @PathParam("ts2") long ts2){
        try {
            float avarange = Statistics.getInstance().averageKm(ts1, ts2);
            return Response.ok(String.valueOf(avarange)).build();
        }catch (NotFoundException e){
            return Response.status(Response.Status.NOT_FOUND).build();
        }catch (IllegalArgumentException e){
            return Response.status(Response.Status.BAD_REQUEST).build();
        }catch (NoContentException e){
            return Response.status(Response.Status.NO_CONTENT).build();
        }
    }

}

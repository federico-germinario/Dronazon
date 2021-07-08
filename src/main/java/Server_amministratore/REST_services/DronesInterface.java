package Server_amministratore.REST_services;

import Server_amministratore.*;
import Server_amministratore.Exceptions.ConflictIdException;
import com.sun.jersey.api.NotFoundException;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;


/**
 * Interfaccia REST per gestire la rete di droni e ricevere le statistiche  
 *
 * @author Federico Germinario
 */
@Path("drones")
public class DronesInterface {

    @Path("add")
    @POST
    @Consumes({"application/json", "application/xml"})
    @Produces({"application/json", "application/xml"})
    public Response addDrone(Drone d){
        try {
            ArrayList<Drone> dronesList = Drones.getInstance().add(d);
            MessageToDrone message = new MessageToDrone(dronesList);
            return Response.ok(message).build();
        }catch (ConflictIdException e){
            return Response.status(Response.Status.CONFLICT).build(); // ID già presente
        }
    }

    @Path("delete/{id}")
    @DELETE
    public Response removeDrone(@PathParam("id") int id){
        try {
            Drones.getInstance().removeDrone(id);
            return Response.ok().build();
        }catch (NotFoundException e){
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("statistics")
    @POST
    @Consumes({"application/json", "application/xml"})
    public Response addGlobalStatistics(GlobalStatistics globalStatistics){
        if(Statistics.getInstance().add(globalStatistics))
            return Response.ok().build();
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

}


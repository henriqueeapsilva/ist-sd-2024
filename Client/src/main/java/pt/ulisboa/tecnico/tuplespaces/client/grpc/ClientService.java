package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.tuplespaces.client.PutObserver;
import pt.ulisboa.tecnico.tuplespaces.client.ResponseCollector;
import pt.ulisboa.tecnico.tuplespaces.client.util.OrderedDelayer;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov;

import static pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Executors;


public class ClientService {

    /* TODO: This class should implement the front-end of the replicated TupleSpaces service
        (according to the Xu-Liskov algorithm)*/

    private TupleSpacesReplicaStub[] stubs;
    OrderedDelayer delayer;

    private final int numServers;

    public ClientService(int numServers) {

        /* The delayer can be used to inject delays to the sending of requests to the
            different servers, according to the per-server delays that have been set  */
        delayer = new OrderedDelayer(numServers);
        this.stubs = new TupleSpacesReplicaStub[numServers];
        this.numServers = numServers;
    }

    public void createStubs(ArrayList<String> servers) {
        int i = 0;
        for (String address: servers){
            // creating channel for the stub
            ManagedChannel channel =  ManagedChannelBuilder.forTarget(address).usePlaintext().build();
            // creating the stub for each server (replica)
            stubs[i] = newStub(channel);
            i++;
        }
    }

    /* This method allows the command processor to set the request delay assigned to a given server */
    public void setDelay(int id, int delay) {
        delayer.setDelay(id, delay);

        /* TODO: Remove this debug snippet */
        System.out.println("[Debug only]: After setting the delay, I'll test it");
        for (Integer i : delayer) {
            System.out.println("[Debug only]: Now I can send request to stub[" + i + "]");
        }
        System.out.println("[Debug only]: Done.");
    }

    /* TODO: individual methods for each remote operation of the TupleSpaces service */

    // multicast communication from the client (worker) to all the servers (replicas)
    public String putOperation(String tuple) {
        try {
            // request
            TupleSpacesReplicaXuLiskov.PutRequest request = TupleSpacesReplicaXuLiskov.PutRequest.newBuilder().setNewTuple(tuple).build();
            // response collector
            ResponseCollector putRc = new ResponseCollector();
            // servers that have acknowledged the request

            for (Integer id : delayer) {
                TupleSpacesReplicaStub stub = stubs[id];
                PutObserver<TupleSpacesReplicaXuLiskov.PutResponse> observer = new PutObserver<>(putRc);
                stub.put(request, observer);
            }

            putRc.waitUntilAllReceived(numServers);

        } catch (StatusRuntimeException e){
            Status status = e.getStatus();
            return status.getDescription();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return "Operation Successful \n";
    }


    public void readMulticast(){

    }

    public void takeMulticast(){

    }

    /* Example: How to use the delayer before sending requests to each server
     *          Before entering each iteration of this loop, the delayer has already
     *          slept for the delay associated with server indexed by 'id'.
     *          id is in the range 0..(numServers-1).

        for (Integer id : delayer) {
            //stub[id].some_remote_method(some_arguments);
        }

    */

}

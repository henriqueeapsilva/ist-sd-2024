package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.tuplespaces.client.util.OrderedDelayer;

import static pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc.*;
import java.util.ArrayList;

public class ClientService {

    /* TODO: This class should implement the front-end of the replicated TupleSpaces service
        (according to the Xu-Liskov algorithm)*/

    private TupleSpacesReplicaStub[] stubs;
    OrderedDelayer delayer;

    public ClientService(int numServers) {

        /* The delayer can be used to inject delays to the sending of requests to the
            different servers, according to the per-server delays that have been set  */
        delayer = new OrderedDelayer(numServers);
        this.stubs = new TupleSpacesReplicaStub[numServers];
    }
    public void createStubs(ArrayList<String> servers) {
        int i = 0;
        for (String address: servers){
            System.out.println("entrou");
            ManagedChannel channel =  ManagedChannelBuilder.forTarget(address).usePlaintext().build();
            System.out.println(channel);
            stubs[i] = newStub(channel);
            System.out.println(stubs[i]);
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

    /* Example: How to use the delayer before sending requests to each server
     *          Before entering each iteration of this loop, the delayer has already
     *          slept for the delay associated with server indexed by 'id'.
     *          id is in the range 0..(numServers-1).

        for (Integer id : delayer) {
            //stub[id].some_remote_method(some_arguments);
        }

    */

}

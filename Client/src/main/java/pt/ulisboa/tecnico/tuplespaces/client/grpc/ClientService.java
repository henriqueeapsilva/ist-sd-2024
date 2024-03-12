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
            ArrayList<Integer> respondedServers = new ArrayList<>();

            // send request if all the servers have not responded - each iteration represents sending a request
            while (respondedServers.size() < numServers) {
                sendPutRequests(request, putRc);
                waitForPutResponses(putRc, respondedServers);
            }
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

    public void sendPutRequests(TupleSpacesReplicaXuLiskov.PutRequest request, ResponseCollector putRc) {
        // Use OrderedDelayer to manage delays for each server
        Iterator<Integer> delayerIterator = delayer.iterator();

        // sending the request for each server (replica)
        for (int i = 0; i < numServers; i++) {
            int serverId = delayerIterator.next();

            // Apply delay for this server
            int delay = delayer.setDelay(serverId, 1000);

            // sends the request
            TupleSpacesReplicaStub stub = stubs[serverId];
            PutObserver<TupleSpacesReplicaXuLiskov.PutResponse> observer = new PutObserver<>(putRc);
            stub.put(request, observer);

            // handles the timeout corresponding to the delay
            handleTimeout(delay, stub, request, observer);

        }
    }

    private void handleTimeout(int delay, TupleSpacesReplicaStub stub, TupleSpacesReplicaXuLiskov.PutRequest request, PutObserver<TupleSpacesReplicaXuLiskov.PutResponse> observer) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Thread.sleep(delay * 1000);
                if (!observer.isCompleted()) {
                    stub.put(request, observer);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public void waitForPutResponses(ResponseCollector putRc, ArrayList<Integer> respondedServers) throws InterruptedException {
        putRc.waitUntilAllReceived(numServers);
        // Check which servers have responded
        for (int i = 0; i < numServers; i++) {
            if (!respondedServers.contains(i) && putRc.hasReceivedResponseFrom(i)) {
                respondedServers.add(i);
            }
        }
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

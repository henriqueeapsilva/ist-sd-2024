package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.tuplespaces.client.observers.*;
import pt.ulisboa.tecnico.tuplespaces.client.util.OrderedDelayer;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov;

import static pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc.*;
import java.util.ArrayList;

public class ClientService {

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

    // multicast communication from the client (worker) to all the servers (replicas)
    public String putOperation(String tuple) {
        try {
            // request
            TupleSpacesReplicaXuLiskov.PutRequest request = TupleSpacesReplicaXuLiskov.PutRequest.newBuilder()
                    .setNewTuple(tuple).build();
            // response collector
            ResponseCollector putRc = new ResponseCollector();

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
        return "OK";
    }

    public String readOperation(String tuple) {
        String output;
        try {
            TupleSpacesReplicaXuLiskov.ReadRequest request = TupleSpacesReplicaXuLiskov.ReadRequest.newBuilder()
                    .setSearchPattern(tuple).build();

            ResponseCollector readRc = new ResponseCollector();

            for (Integer id: delayer) {
                TupleSpacesReplicaStub stub = stubs[id];
                ReadObserver<TupleSpacesReplicaXuLiskov.ReadResponse> observer = new ReadObserver<>(readRc);
                stub.read(request, observer);
            }

            readRc.waitForFirstResponse();

            output = readRc.getFirstResponse();

        } catch (StatusRuntimeException e){
            Status status = e.getStatus();
            return status.getDescription();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return "OK\n" + output;
    }

    public void takeOperationPhase1(String tuple, int clientId) {
        String output;
        try {
            TupleSpacesReplicaXuLiskov.TakePhase1Request request = TupleSpacesReplicaXuLiskov.TakePhase1Request.newBuilder()
                    .setSearchPattern(tuple).setClientId(clientId).build();

            ResponseCollector takeRc = new ResponseCollector();

            do {
                for (Integer id : delayer) {
                    TupleSpacesReplicaStub stub = stubs[id];
                    TakeObserver<TupleSpacesReplicaXuLiskov.TakePhase1Response> observer = new TakeObserver<>(takeRc, id);
                    stub.takePhase1(request, observer);
                }
            } while (handleTakeCases(tuple, clientId, takeRc));

            output = takeRc.getFirstResponse();

        } catch (StatusRuntimeException e){
            Status status = e.getStatus();
        }
    }

    public boolean handleTakeCases(String tuple, int clientId, ResponseCollector rc) {
        if (rc.getAcceptedRequests().size() == numServers && rc.getCollectedResponses().isEmpty()) {
            return true;
        }
        else if (rc.getAcceptedRequests().size() == numServers && !rc.getCollectedResponses().isEmpty()) {

        }
        return false;
    }
}

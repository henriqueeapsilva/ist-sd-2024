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
import java.util.List;
import java.util.Objects;

public class ClientService {

    private TupleSpacesReplicaStub[] stubs;
    OrderedDelayer delayer;

    private List<ManagedChannel> channels;

    private final int numServers;

    public ClientService(int numServers) {
        /* The delayer can be used to inject delays to the sending of requests to the
            different servers, according to the per-server delays that have been set  */
        delayer = new OrderedDelayer(numServers);
        this.stubs = new TupleSpacesReplicaStub[numServers];
        this.channels = new ArrayList<>();
        this.numServers = numServers;
    }

    public void createStubs(ArrayList<String> servers) {
        int i = 0;
        for (String address: servers) {
            System.out.println("Address: " + address);
            // creating channel for the stub
            ManagedChannel channel =  ManagedChannelBuilder.forTarget(address).usePlaintext().build();
            // creating the stub for each server (replica)
            stubs[i] = newStub(channel);
            this.channels.add(channel);
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

    public String putOperation(String tuple) {
        try {
            TupleSpacesReplicaXuLiskov.PutRequest request = TupleSpacesReplicaXuLiskov.PutRequest.newBuilder()
                    .setNewTuple(tuple).build();
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

    public String takeOperationPhase1(String tuple, int clientId) {
        String output = "";
        try {
            TupleSpacesReplicaXuLiskov.TakePhase1Request request = TupleSpacesReplicaXuLiskov.TakePhase1Request.newBuilder()
                    .setSearchPattern(tuple).setClientId(clientId).build();
            ResponseCollector takeRc;

            do {
                takeRc = new ResponseCollector();
                for (Integer id : delayer) {
                    TupleSpacesReplicaStub stub = stubs[id];
                    TakePhase1Observer<TupleSpacesReplicaXuLiskov.TakePhase1Response> observer = new TakePhase1Observer<>(takeRc, id);
                    stub.takePhase1(request, observer);
                }
                takeRc.waitForResponses(numServers);
            } while (handleTakeCases(clientId, takeRc));

            output = takeRc.getFirstResponse();

        } catch (StatusRuntimeException e){
            Status status = e.getStatus();
            return status.getDescription();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        takeOperationPhase2(tuple, clientId);
        return "OK\n" + output;
    }

    public void takeOperationPhase2(String tuple, int clientId) {
        try {
            TupleSpacesReplicaXuLiskov.TakePhase2Request request = TupleSpacesReplicaXuLiskov.TakePhase2Request.newBuilder()
                    .setTuple(tuple).setClientId(clientId).build();

            ResponseCollector takeRc = new ResponseCollector();
            for (Integer id : delayer) {
                TupleSpacesReplicaStub stub = stubs[id];
                TakePhase2Observer<TupleSpacesReplicaXuLiskov.TakePhase2Response> observer = new TakePhase2Observer<>(takeRc);
                stub.takePhase2(request, observer);
            }
            takeRc.waitUntilAllReceived(numServers);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean handleTakeCases(int clientId, ResponseCollector rc) {
        // case: all requests accepted and null interception
        if (rc.getAcceptedRequests().size() == numServers && rc.getCollectedResponses().isEmpty()) {
            return true;
        // case: the majority accepted the request
        } else if (rc.getAcceptedRequests().size() > numServers/2 && rc.getAcceptedRequests().size() != numServers) {
            return true;
        // case: the minority accepted the request
        } else if (rc.getAcceptedRequests().size() <= numServers/2) {
            TupleSpacesReplicaXuLiskov.TakePhase1ReleaseRequest request = TupleSpacesReplicaXuLiskov.TakePhase1ReleaseRequest
                    .newBuilder()
                    .setClientId(clientId).build();
            for (Integer id : rc.getAcceptedRequests()) {
                TupleSpacesReplicaStub stub = stubs[id];
                TakePhase1Observer<TupleSpacesReplicaXuLiskov.TakePhase1ReleaseResponse> observer = new TakePhase1Observer<>(rc, id);
                stub.takePhase1Release(request, observer);
            }
            return true;
        } else {
            return false;
        }
    }

    public String getTupleSpacesState(String qualifier) {
        String output = "";
        try {
            TupleSpacesReplicaXuLiskov.getTupleSpacesStateRequest request = TupleSpacesReplicaXuLiskov.
                    getTupleSpacesStateRequest.newBuilder().build();

            ResponseCollector rc = new ResponseCollector();
            TupleSpacesReplicaStub stub;

            switch(qualifier) {
                case "A":
                    stub = stubs[0];
                    break;
                case "B":
                    stub = stubs[1];
                    break;
                case "C":
                    stub = stubs[2];
                    break;
                default:
                    return "Invalid qualifier";
            }
            GetTupleSpacesObserver<TupleSpacesReplicaXuLiskov.getTupleSpacesStateResponse> observer =
                    new GetTupleSpacesObserver<>(rc);

            stub.getTupleSpacesState(request, observer);
            rc.waitForFirstResponse();
            output = rc.getCollectedResponses().toString();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return "OK\n" + output;
    }

    public void shutdown(){
        for (ManagedChannel channel : channels)
            channel.shutdown();
    }
}

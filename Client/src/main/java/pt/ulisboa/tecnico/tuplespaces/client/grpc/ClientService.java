package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.tuplespaces.client.observers.*;
import pt.ulisboa.tecnico.tuplespaces.client.util.OrderedDelayer;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaTotalOrder;
import static pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaGrpc.*;
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
    }

    public String putOperation(int seqNum, String tuple) {
        try {
            TupleSpacesReplicaTotalOrder.PutRequest request = TupleSpacesReplicaTotalOrder.PutRequest.newBuilder()
                    .setNewTuple(tuple).setSeqNumber(seqNum).build();

            ResponseCollector putRc = new ResponseCollector();

            for (Integer id : delayer) {
                TupleSpacesReplicaStub stub = stubs[id];
                PutObserver<TupleSpacesReplicaTotalOrder.PutResponse> observer = new PutObserver<>(putRc);
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
            TupleSpacesReplicaTotalOrder.ReadRequest request = TupleSpacesReplicaTotalOrder.ReadRequest.newBuilder().
                    setSearchPattern(tuple).build();
            ResponseCollector readRc = new ResponseCollector();
            for (Integer id: delayer) {
                TupleSpacesReplicaStub stub = stubs[id];
                ReadObserver<TupleSpacesReplicaTotalOrder.ReadResponse> observer = new ReadObserver<>(readRc);
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

    public String takeOperation(int seqNum, String tuple) {
        String output;
        try {

            TupleSpacesReplicaTotalOrder.TakeRequest request = TupleSpacesReplicaTotalOrder.TakeRequest.newBuilder()
                    .setSearchPattern(tuple).setSeqNumber(seqNum).build();

            ResponseCollector takeRc = new ResponseCollector();

            for (Integer id : delayer) {
                TupleSpacesReplicaStub stub = stubs[id];
                TakeObserver<TupleSpacesReplicaTotalOrder.TakeResponse> observer = new TakeObserver<>(takeRc);
                stub.take(request, observer);
            }
            takeRc.waitUntilAllReceived(numServers);
            output = takeRc.getFirstResponse();

        } catch (StatusRuntimeException e){
            Status status = e.getStatus();
            return status.getDescription();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return "OK\n" + output;
    }

    public String getTupleSpacesState(String qualifier) {
        String output = "";
        try {
            TupleSpacesReplicaTotalOrder.getTupleSpacesStateRequest request = TupleSpacesReplicaTotalOrder.
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
                    return "Invalid qualifier - Choose from: [A,B,C]";
            }
            GetTupleSpacesObserver<TupleSpacesReplicaTotalOrder.getTupleSpacesStateResponse> observer =
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

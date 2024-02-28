package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.*;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.*;

import java.util.List;

import static pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc.*;

public class ClientService {

    private TupleSpacesBlockingStub stub;
    private ManagedChannel channel;

    /*TODO: The gRPC client-side logic should be here.
          This should include a method that builds a channel and stub,
          as well as individual methods for each remote operation of this service. */
    public void createMainStub(String host, String port) {
        final String target = host + ":" + port;

         this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
         this.stub = newBlockingStub(this.channel);
    }

    public String put(String tuple) {
        try {
            PutResponse put = stub.put(PutRequest.newBuilder().setNewTuple(tuple).build());
            return "OK";
        } catch (StatusRuntimeException e) {
            Status status = e.getStatus();
            return status.getDescription();
        }
    }

    public String read(String tuple) {
        try {
            ReadResponse result = stub.read(ReadRequest.newBuilder().setSearchPattern(tuple).build());
            return result.getResult();
        } catch (StatusRuntimeException e) {
            Status status = e.getStatus();
            return status.getDescription();
        }
    }

    public String take(String tuple) {
        try {
            TakeResponse result = stub.take(TakeRequest.newBuilder().setSearchPattern(tuple).build());
            return result.getResult();
        } catch (StatusRuntimeException e) {
            Status status = e.getStatus();
            return status.getDescription();
        }
    }

    public List<String> getTupleSpacesState(String qualifier) {
        getTupleSpacesStateResponse tuple = stub.getTupleSpacesState(getTupleSpacesStateRequest.getDefaultInstance());
        return tuple.getTupleList();
    }
    public void closeChannel() {
        this.channel.shutdownNow();
    }
}


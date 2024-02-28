package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
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
        PutResponse put = stub.put(PutRequest.newBuilder().setNewTuple(tuple).build());
        return "OK";
    }

    public String read(String tuple) {
        ReadResponse result = stub.read(ReadRequest.newBuilder().setSearchPattern(tuple).build());
        return result.getResult();
    }

    public String take(String tuple) {
        TakeResponse result = stub.take(TakeRequest.newBuilder().setSearchPattern(tuple).build());
        return result.getResult();
    }

    public List<String> getTupleSpacesState(String qualifier) {
        getTupleSpacesStateResponse tuple = stub.getTupleSpacesState(getTupleSpacesStateRequest.getDefaultInstance());
        return tuple.getTupleList();
    }
    public void closeChannel() {
        this.channel.shutdownNow();
    }
}


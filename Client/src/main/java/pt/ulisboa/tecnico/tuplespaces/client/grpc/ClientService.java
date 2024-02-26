package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.*;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.*;
import static pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc.*;

public class ClientService {

    private TupleSpacesBlockingStub _stub;
    private ManagedChannel _channel;

    /*TODO: The gRPC client-side logic should be here.
          This should include a method that builds a channel and stub,
          as well as individual methods for each remote operation of this service. */
    public void createMainStub(String host, String port) {
        final String target = host + ":" + port;

         this._channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
         this._stub = newBlockingStub(this._channel);
    }

    public void put(String tuple) {
        PutResponse put = _stub.put(PutRequest.newBuilder().setNewTuple(tuple).build());
    }

    public void read(String tuple) {
        ReadResponse result = _stub.read(ReadRequest.newBuilder().setSearchPattern(tuple).build());
    }

    public void take(String tuple) {
        TakeResponse result = _stub.take(TakeRequest.newBuilder().setSearchPattern(tuple).build());
    }

    public void getTupleSpacesState(String qualifier) {
        getTupleSpacesStateResponse tuple = _stub.getTupleSpacesState(getTupleSpacesStateRequest.getDefaultInstance());
    }
    public void closeChannel() {
        this._channel.shutdownNow();
    }
}


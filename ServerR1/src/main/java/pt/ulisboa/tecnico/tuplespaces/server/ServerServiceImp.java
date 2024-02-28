package pt.ulisboa.tecnico.tuplespaces.server;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc;
import pt.ulisboa.tecnico.tuplespaces.server.domain.ServerState;

public class ServerServiceImp extends TupleSpacesGrpc.TupleSpacesImplBase {
    private ServerState tuplespaces = new ServerState();

    @Override
    public void put(TupleSpacesCentralized.PutRequest request, StreamObserver<TupleSpacesCentralized.PutResponse> responseObserver) {

        String newTuple = request.getNewTuple();

        // Adds the tuple to the ServerState
        tuplespaces.put(newTuple);

        // If the Response as no args, it serves as a placeholder to maintain the consistency of our service API.
        TupleSpacesCentralized.PutResponse response = TupleSpacesCentralized.PutResponse.newBuilder().build();

        // Send a single response through the stream.
        responseObserver.onNext(response);
        // Notify the client that the operation has been completed.
        responseObserver.onCompleted();
    }

    @Override
    public void read(TupleSpacesCentralized.ReadRequest request, StreamObserver<TupleSpacesCentralized.ReadResponse> responseObserver) {

        String searchPattern = request.getSearchPattern();

        TupleSpacesCentralized.ReadResponse response = TupleSpacesCentralized.ReadResponse.newBuilder().setResult(tuplespaces.read(searchPattern)).build();

        // Send a single response through the stream.
        responseObserver.onNext(response);
        // Notify the client that the operation has been completed.
        responseObserver.onCompleted();
    }

    @Override
    public void take(TupleSpacesCentralized.TakeRequest request, StreamObserver<TupleSpacesCentralized.TakeResponse> responseObserver) {

        String searchPattern = request.getSearchPattern();

        TupleSpacesCentralized.TakeResponse response = TupleSpacesCentralized.TakeResponse.newBuilder().setResult(tuplespaces.take(searchPattern)).build();

        // Send a single response through the stream.
        responseObserver.onNext(response);
        // Notify the client that the operation has been completed.
        responseObserver.onCompleted();
    }

    @Override
    public void getTupleSpacesState(TupleSpacesCentralized.getTupleSpacesStateRequest request, StreamObserver<TupleSpacesCentralized.getTupleSpacesStateResponse> responseObserver) {

        TupleSpacesCentralized.getTupleSpacesStateResponse response = TupleSpacesCentralized.getTupleSpacesStateResponse.newBuilder().addAllTuple(tuplespaces.getTupleSpacesState()).build();

        // Send a single response through the stream.
        responseObserver.onNext(response);
        // Notify the client that the operation has been completed.
        responseObserver.onCompleted();
    }
}

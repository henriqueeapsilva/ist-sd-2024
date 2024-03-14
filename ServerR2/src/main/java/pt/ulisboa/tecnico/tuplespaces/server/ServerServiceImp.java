package pt.ulisboa.tecnico.tuplespaces.server;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc.TupleSpacesReplicaImplBase;
import pt.ulisboa.tecnico.tuplespaces.server.domain.*;

import java.util.ArrayList;

import static pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.*;

public class
ServerServiceImp extends TupleSpacesReplicaImplBase {
    private final ServerState tuplespaces = new ServerState();

    @Override
    public void put(PutRequest request, StreamObserver<PutResponse> responseObserver) {
        try {
            String newTuple = request.getNewTuple();

            tuplespaces.put(newTuple);

            // If the Response as no args, it serves as a placeholder to maintain the consistency of our service API.
            PutResponse response = PutResponse.newBuilder().build();

            // Send a single response through the stream.
            responseObserver.onNext(response);
            // Notify the client that the operation has been completed.
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Invalid tuple format!").asRuntimeException());
        }
    }

    @Override
    public void read(ReadRequest request, StreamObserver<ReadResponse> responseObserver) {
        try {
            String searchPattern = request.getSearchPattern();

            ReadResponse response = ReadResponse.newBuilder().setResult(tuplespaces.read(searchPattern)).build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Invalid tuple format!").asRuntimeException());
        }
    }

    @Override
    public void takePhase1(TakePhase1Request request, StreamObserver<TakePhase1Response> responseObserver) {
        try {
            int clientId = request.getClientId();
            String searchPattern = request.getSearchPattern();
            TakePhase1Response response;

            tuplespaces.waitForMatchingTuple(searchPattern, false);
            // send empty list
            if (tuplespaces.isLocked(searchPattern)) {
                response = TakePhase1Response.newBuilder()
                        .addAllReservedTuples(new ArrayList<>()).build();
            }
            else {
                tuplespaces.aquireLock(searchPattern);
                tuplespaces.setClientId(searchPattern, clientId);
                response = TakePhase1Response.newBuilder()
                        .addAllReservedTuples(tuplespaces.getAllMatchingTuples(searchPattern)).build();
            }
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Invalid tuple").asRuntimeException());
        }
    }

    @Override
    public void getTupleSpacesState(getTupleSpacesStateRequest request, StreamObserver<getTupleSpacesStateResponse> responseObserver) {

        getTupleSpacesStateResponse response = getTupleSpacesStateResponse.newBuilder().addAllTuple(tuplespaces.getTupleSpacesState()).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}

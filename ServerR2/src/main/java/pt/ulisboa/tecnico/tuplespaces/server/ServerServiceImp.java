package pt.ulisboa.tecnico.tuplespaces.server;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc.TupleSpacesReplicaImplBase;
import pt.ulisboa.tecnico.tuplespaces.server.domain.*;

import java.util.ArrayList;
import java.util.List;

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
            Integer clientId = request.getClientId();
            String searchPattern = request.getSearchPattern();
            TakePhase1Response response;

            tuplespaces.waitForMatchingTuple(searchPattern,false); // verifies if there is any match -> if not waits
            List<String> tuples = tuplespaces.getAllMatchingTuples(searchPattern, clientId);
            System.out.println(tuples);

            System.out.println("Vou criar a tua resposta.");
            response = TakePhase1Response.newBuilder()
                        .addAllReservedTuples(tuples).build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Invalid tuple").asRuntimeException());
        }
    }

    @Override
    public void takePhase1Release(TakePhase1ReleaseRequest request, StreamObserver<TakePhase1ReleaseResponse> responseObserver) {
        int clientId = request.getClientId();
        tuplespaces.releaseLocks(clientId);

        TakePhase1ReleaseResponse releaseResponse = TakePhase1ReleaseResponse.newBuilder().build();
        responseObserver.onNext(releaseResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void takePhase2(TakePhase2Request request, StreamObserver<TakePhase2Response> responseObserver) {
        int clientId = request.getClientId();
        String pattern = request.getTuple();

        tuplespaces.take(pattern);
        tuplespaces.releaseLocks(clientId);

        TakePhase2Response response = TakePhase2Response.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getTupleSpacesState(getTupleSpacesStateRequest request, StreamObserver<getTupleSpacesStateResponse> responseObserver) {

        getTupleSpacesStateResponse response = getTupleSpacesStateResponse.newBuilder().addAllTuple(tuplespaces.getTupleSpacesState()).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}

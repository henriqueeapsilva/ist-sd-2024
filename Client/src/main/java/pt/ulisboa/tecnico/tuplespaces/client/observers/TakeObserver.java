package pt.ulisboa.tecnico.tuplespaces.client.observers;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov;

import io.grpc.stub.StreamObserver;

public class TakeObserver<R> implements StreamObserver<R> {

    ResponseCollector collector;
    private Integer serverId;

    public TakeObserver(ResponseCollector rc, int serverId){
        this.serverId = serverId;
        collector = rc;
    }

    @Override
    public void onNext(R response) {
        TupleSpacesReplicaXuLiskov.TakePhase1Response takeResponse = (TupleSpacesReplicaXuLiskov.TakePhase1Response) response;
        collector.interceptResponses(takeResponse.getReservedTuplesList());
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("Received error: " + throwable);
    }

    @Override
    public void onCompleted() {
        // System.out.println("Request completed");
    }
}
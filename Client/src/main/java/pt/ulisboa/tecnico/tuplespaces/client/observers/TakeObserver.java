package pt.ulisboa.tecnico.tuplespaces.client.observers;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaTotalOrder;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov;

import io.grpc.stub.StreamObserver;

public class TakeObserver<R> implements StreamObserver<R> {

    ResponseCollector collector;

    public TakeObserver(ResponseCollector rc){
        collector = rc;
    }

    @Override
    public void onNext(R response) {
        TupleSpacesReplicaTotalOrder.TakeResponse takeResponse = (TupleSpacesReplicaTotalOrder.TakeResponse) response;
        collector.addString(takeResponse.getResult());
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
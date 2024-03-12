package pt.ulisboa.tecnico.tuplespaces.client.observers;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov;

import static pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc.*;

import io.grpc.stub.StreamObserver;

public class ReadObserver<R> implements StreamObserver<R> {

    ResponseCollector collector;

    public ReadObserver(ResponseCollector rc){
        collector = rc;
    }

    @Override
    public void onNext(R response) {
        if (response instanceof TupleSpacesReplicaXuLiskov.ReadResponse) {
            TupleSpacesReplicaXuLiskov.ReadResponse readResponse = (TupleSpacesReplicaXuLiskov.ReadResponse) response;
            String result = readResponse.getResult();
            collector.addString(result);
        } else {
            // Do the same for take
        }
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
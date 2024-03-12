package pt.ulisboa.tecnico.tuplespaces.client;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov;

public class PutObserver<R> implements StreamObserver<R> {

    ResponseCollector collector;

    private boolean completed = false;

    public PutObserver(ResponseCollector rc){
        collector = rc;
    }

    @Override
    public void onNext(R r) {
        collector.addString("ack");
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("Received error: " + throwable);
    }

    @Override
    public void onCompleted() {
        completed = true;
        System.out.println("Request completed");
    }

    public boolean isCompleted(){
        return completed;
    }
}

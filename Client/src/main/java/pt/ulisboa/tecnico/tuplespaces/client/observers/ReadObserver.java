package pt.ulisboa.tecnico.tuplespaces.client.observers;

import io.grpc.stub.StreamObserver;

public class ReadObserver<R> implements StreamObserver<R> {

    ResponseCollector collector;

    public ReadObserver(ResponseCollector rc){
        collector = rc;
    }

    @Override
    public void onNext(R r) {
        collector.addString(r.toString());
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

package pt.ulisboa.tecnico.tuplespaces.client.observers;

import io.grpc.stub.StreamObserver;

public class TakePhase2Observer<R> implements StreamObserver<R> {

    ResponseCollector collector;
    private Integer serverId;

    public TakePhase2Observer(ResponseCollector rc, int serverId){
        this.serverId = serverId;
        collector = rc;
    }

    @Override
    public void onNext(R response) {
        collector.addString("ack");
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
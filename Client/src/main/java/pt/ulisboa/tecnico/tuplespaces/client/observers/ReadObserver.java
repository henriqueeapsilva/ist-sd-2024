package pt.ulisboa.tecnico.tuplespaces.client.observers;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaTotalOrder;

import io.grpc.stub.StreamObserver;

public class ReadObserver<R> implements StreamObserver<R> {

    ResponseCollector collector;

    public ReadObserver(ResponseCollector rc){
        collector = rc;
    }

    @Override
    public void onNext(R response) {
        TupleSpacesReplicaTotalOrder.ReadResponse readResponse = (TupleSpacesReplicaTotalOrder.ReadResponse) response;
        String result = readResponse.getResult();
        collector.addString(result);
        collector.incrementNumResponses();
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
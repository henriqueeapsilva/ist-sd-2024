package pt.ulisboa.tecnico.tuplespaces.client.observers;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaTotalOrder;

import java.util.List;

public class GetTupleSpacesObserver<R>implements StreamObserver<R> {
    ResponseCollector collector;

    public GetTupleSpacesObserver(ResponseCollector rc) {
        collector = rc;
    }

    @Override
    public void onNext(R response) {
        TupleSpacesReplicaTotalOrder.getTupleSpacesStateResponse
                getTupleSpacesStateResponse = (TupleSpacesReplicaTotalOrder.getTupleSpacesStateResponse) response;
        List<String> result = getTupleSpacesStateResponse.getTupleList();

        for (String tuple : result) {
            collector.addString(tuple);
        }
        collector.incrementNumResponses();
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("Received error: " + throwable);
    }

    @Override
    public void onCompleted() {
    }
}

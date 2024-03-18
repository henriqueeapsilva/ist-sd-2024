package pt.ulisboa.tecnico.tuplespaces.client.observers;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.sequencer.contract.SequencerOuterClass;

public class SequencerObserver<R> implements StreamObserver<R> {

    ResponseCollector collector;

    public SequencerObserver(ResponseCollector rc){
        collector = rc;
    }

    @Override
    public void onNext(R r) {
        SequencerOuterClass.GetSeqNumberResponse response = (SequencerOuterClass.GetSeqNumberResponse) r;
        collector.setSeqNumber(response.getSeqNumber());
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


package pt.ulisboa.tecnico.tuplespaces.client.observers;

import java.util.ArrayList;
import java.util.List;

public class ResponseCollector {
    ArrayList<String> collectedResponses;
    private List<Integer> acceptedRequests;
    private Integer seqNumber;
    private boolean isFirst;
    private Integer numResponses = 0;


    public ResponseCollector() {
        isFirst = true;
        acceptedRequests = new ArrayList<>();
        collectedResponses = new ArrayList<>();
        seqNumber = -1;
    }

    public ArrayList<String> getCollectedResponses() {
        return collectedResponses;
    }

    synchronized public  void interceptResponses(List<String> tuples) {
        if (isFirst) {
            for (String tuple : tuples) {
                addString(tuple);
            }
            this.isFirst = false;
        }
        else {
            for (String tuple : collectedResponses) {
                if (!tuples.contains(tuple)) {
                    collectedResponses.remove(tuple);
                }
            }
            notifyAll();
        }
    }

    synchronized public void incrementNumResponses() {
        numResponses++;
        notifyAll();
    }

    public void addAcceptedRequest(Integer serverId) {
        acceptedRequests.add(serverId);
    }

    synchronized public void addString(String s) {
        collectedResponses.add(s);
        notifyAll();
    }

    public String getFirstResponse() {
        return collectedResponses.get(0);
    }

    synchronized public void waitUntilAllReceived(int n) throws InterruptedException {
        while (collectedResponses.size() < n) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

  synchronized public void waitForResponses(int n) throws InterruptedException {
        while (numResponses < n) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    synchronized public void waitForFirstResponse() throws InterruptedException {
        while (numResponses == 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    synchronized public void setSeqNumber(Integer i){
        seqNumber = i;
        notifyAll();
    }

    synchronized public Integer getNextSeqNumber(){
        while(seqNumber == -1) {
           try {
               wait();
           } catch (InterruptedException e){
               throw new RuntimeException();
           }
        }

        return seqNumber;
    }

    public List<Integer> getAcceptedRequests() {
        return acceptedRequests;
    }
}

package pt.ulisboa.tecnico.tuplespaces.client.observers;

import java.util.ArrayList;
import java.util.List;

public class ResponseCollector {
    ArrayList<String> collectedResponses;
    private List<Integer> acceptedRequests;
    private boolean isFirst;
    private Integer numResponses = 0;


    public ResponseCollector() {
        isFirst = true;
        acceptedRequests = new ArrayList<>();
        collectedResponses = new ArrayList<>();
    }

    public ArrayList<String> getCollectedResponses() {
        return collectedResponses;
    }

    public synchronized void interceptResponses(List<String> tuples) {
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

    public synchronized void incrementNumResponses() {
        numResponses++;
    }

    public void addAcceptedRequest(Integer serverId) {
        acceptedRequests.add(serverId);
    }

    synchronized public void addString(String s) {
        synchronized (this) {
            collectedResponses.add(s);
            notifyAll();
        }
    }

    public String getFirstResponse() {
        return collectedResponses.get(0);
    }

    synchronized public void waitUntilAllReceived(int n) throws InterruptedException {
        synchronized (this) {
            while (collectedResponses.size() < n) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

  synchronized public void waitForResponses(int n) throws InterruptedException {
        while (numResponses < n) {
            System.out.println(numResponses);
            System.out.println(n);
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    synchronized public void waitForFirstResponse() throws InterruptedException {
        while (collectedResponses.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public List<Integer> getAcceptedRequests() {
        return acceptedRequests;
    }
}

package pt.ulisboa.tecnico.tuplespaces.client.observers;

import java.util.ArrayList;
import java.util.List;

public class ResponseCollector {
    ArrayList<String> collectedResponses;
    private int numRejectedRequests;
    private List<Integer> acceptedRequests;
    private boolean isFirst;


    public ResponseCollector() {
        numRejectedRequests = 0;
        isFirst = true;
        collectedResponses = new ArrayList<String>();
    }

    public ArrayList<String> getCollectedResponses() {
        return collectedResponses;
    }

    public void interceptResponses(List<String> tuples) {
        if (isFirst) {
            for (String tuple : tuples) {
                collectedResponses.add(tuple);
            }
            this.isFirst = false;
        }
        else {
            for (String tuple : collectedResponses) {
                if (!tuples.contains(tuple)) {
                    collectedResponses.remove(tuple);
                }
            }
        }
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

    synchronized public String getStrings() {
        synchronized (this) {
            String res = new String();
            for (String s : collectedResponses) {
                res = res.concat(s);
            }
            return res;
        }
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

    synchronized public void waitForFirstResponse() throws InterruptedException {
        synchronized (this) {
            while (collectedResponses.isEmpty()) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public int getRejectedRequests() {
        return numRejectedRequests;
    }

    public List<Integer> getAcceptedRequests() {
        return acceptedRequests;
    }
}

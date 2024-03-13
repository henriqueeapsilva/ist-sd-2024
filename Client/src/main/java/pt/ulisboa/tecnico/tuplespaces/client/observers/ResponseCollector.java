package pt.ulisboa.tecnico.tuplespaces.client.observers;

import java.util.ArrayList;

public class ResponseCollector {
    ArrayList<String> collectedResponses;

    public ResponseCollector() {
        collectedResponses = new ArrayList<String>();
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
                    if (collectedResponses.size() < n) {
                        wait();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    synchronized public void waitForFirstResponse() throws InterruptedException {
        synchronized (this) {
            while (collectedResponses.size() < 1) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}

package pt.ulisboa.tecnico.tuplespaces.client;

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
        synchronized (this){
            try {
                if(collectedResponses.size() < n){
                    wait();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
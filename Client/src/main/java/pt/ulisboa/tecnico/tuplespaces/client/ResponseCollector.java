package pt.ulisboa.tecnico.tuplespaces.client;

import java.util.ArrayList;
import java.util.HashSet;

public class ResponseCollector {
    ArrayList<String> collectedResponses;
    HashSet<Integer> respondedServers;

    public ResponseCollector() {
        collectedResponses = new ArrayList<String>();
        respondedServers = new HashSet<Integer>();
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
        synchronized (this) {
            while (collectedResponses.size() < n)
                wait();
        }
    }

    synchronized public boolean hasReceivedResponseFrom(int serverId) {
        synchronized (this) {
            return respondedServers.contains(serverId);
        }
    }

    synchronized public void markResponseReceivedFrom(int serverId) {
        synchronized (this) {
            respondedServers.add(serverId);
        }
    }
}
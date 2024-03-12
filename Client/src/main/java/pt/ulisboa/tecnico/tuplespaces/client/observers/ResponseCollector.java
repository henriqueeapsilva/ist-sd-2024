package pt.ulisboa.tecnico.tuplespaces.client.observers;

import java.util.ArrayList;
import java.util.regex.*;

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
        String firstResponse = collectedResponses.get(0);
        // match the prefix + zero or more whitespaces (\\s*), than takes what is inside the quotes ignoring the quotes inside the correct format.
        Pattern pattern = Pattern.compile("result:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
        Matcher matcher = pattern.matcher(firstResponse);
        if (matcher.find()) {
            return matcher.group(1).replace("\\\"", "\""); // Replace escaped quotes with regular quotes
        } else {
            return firstResponse;
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
                if (collectedResponses.size() < n){
                    wait();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    synchronized public void waitForFirstResponse() throws InterruptedException {
        synchronized (this) {
            try {
                if (collectedResponses.size() < 1) {
                    wait();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
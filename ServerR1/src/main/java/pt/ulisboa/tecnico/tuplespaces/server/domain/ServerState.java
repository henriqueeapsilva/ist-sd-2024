package pt.ulisboa.tecnico.tuplespaces.server.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BinaryOperator;

public class ServerState {

  private List<String> tuples;

  public ServerState() {
    this.tuples = new ArrayList<String>();

  }

  public boolean isValidTuple(String tuple) {
    return tuple.charAt(0) != '<' || tuple.charAt(tuple.length() - 1) != '>' || tuple.contains(" ");
  }

  public synchronized void put(String tuple) {
    if (isValidTuple(tuple)) {
      throw new IllegalArgumentException();
    }
    tuples.add(tuple);
    notifyAll();
  }

  private String getMatchingTuple(String pattern) {
    for (String tuple : this.tuples) {
      if (tuple.matches(pattern)) {
        return tuple;
      }
    }
    return null;
  }

  private synchronized String waitForMatchingTuple(String pattern, boolean removeAfter) {
    String matchingTuple = getMatchingTuple(pattern);
    while (matchingTuple == null) {
      try {
        wait(); // wait until a tuple is added
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      matchingTuple = getMatchingTuple(pattern);
    }
    if (removeAfter) {
      tuples.remove(matchingTuple);
    }
    return matchingTuple;
  }

  public String read(String pattern) {
    if (isValidTuple(pattern)) {
      throw new IllegalArgumentException();
    }
    return waitForMatchingTuple(pattern, false);
  }

  public String take(String pattern) {
    if (isValidTuple(pattern)) {
      throw new IllegalArgumentException();
    }
    return waitForMatchingTuple(pattern, true);
  }

  public List<String> getTupleSpacesState() {
    return new ArrayList<>(tuples);
  }
}

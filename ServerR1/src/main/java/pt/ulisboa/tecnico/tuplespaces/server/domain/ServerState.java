package pt.ulisboa.tecnico.tuplespaces.server.domain;

import java.util.ArrayList;
import java.util.List;


public class ServerState {

  private List<Tuple> tuples;

  public ServerState() {
    this.tuples = new ArrayList<>();

  }

  public boolean isValidTuple(String tuple) {
    return tuple.charAt(0) != '<' || tuple.charAt(tuple.length() - 1) != '>' || tuple.contains(" ");
  }

  public void put(String tuple) {
    Tuple newTuple = new Tuple(tuple);

    if (isValidTuple(tuple)) {
      throw new IllegalArgumentException();
    }
    synchronized (this) {
      tuples.add(newTuple);
      notifyAll();
    }
  }

  private Tuple getMatchingTuple(String pattern) {
    for (Tuple tuple : this.tuples) {
      if (tuple.getField().matches(pattern)) {
          return tuple;
      }
    }
    return null;
  }

  private String waitForMatchingTuple(String pattern, boolean removeAfter) {
    Tuple matchingTuple = getMatchingTuple(pattern);
    synchronized (this) {
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
    }
    return matchingTuple.getField();
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

  public synchronized List<String> getTupleSpacesState() {
    List<String> tupleSpaces = new ArrayList<>();
    for (Tuple tuple : this.tuples) {
      tupleSpaces.add(tuple.getField());
    }
    return tupleSpaces;
  }
}

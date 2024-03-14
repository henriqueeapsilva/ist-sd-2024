package pt.ulisboa.tecnico.tuplespaces.server.domain;

import java.util.ArrayList;
import java.util.List;


public class ServerState {

  private List<Tuple> tuples;

  public ServerState() {
    this.tuples = new ArrayList<>();
  }

  public void setClientId(String pattern, int id) {
    Tuple tuple = getMatchingTuple(pattern);
    tuple.setClientId(id);
  }

  public void releaseLocks(int clientId) {
    for (Tuple tuple: tuples) {
      if (tuple.getClientId() == clientId) {
        tuple.unlock();
      }
    }
  }

  public boolean isInvalidTuple(String tuple) {
    return tuple.charAt(0) != '<' || tuple.charAt(tuple.length() - 1) != '>' || tuple.contains(" ");
  }

  public void aquireLock(String pattern) {
    if (!isLocked(pattern)) {
      Tuple tuple = getMatchingTuple(pattern);
      tuple.lock();
    }
  }

  public void releaseLock(String pattern) {
    if (isInvalidTuple(pattern)) {
      throw new IllegalArgumentException();
    }
    Tuple tuple = getMatchingTuple(pattern);
    tuple.setFlag(false);
  }

  public void put(String tuple) {
    Tuple newTuple = new Tuple(tuple);

    if (isInvalidTuple(tuple)) {
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

  public List<String> getAllMatchingTuples(String pattern, Integer clientID) {
    List<String> matchingTuples = new ArrayList<>();

    for (Tuple tuple : this.tuples) {
      if (tuple.getField().matches(pattern) && !isLocked(pattern)) {
        matchingTuples.add(tuple.getField());
        aquireLock(pattern);
        setClientId(pattern, clientID);
      }
    }
    for (String tuple : matchingTuples)
      System.out.println(tuple);
    return matchingTuples;
  }

  public void releaseLocks(String pattern) {
    for (Tuple tuple: tuples) {
      if (tuple.getField().matches(pattern) && tuple.isTaken()) {
        tuple.unlock();
      }
    }
  }

  public String waitForMatchingTuple(String pattern, boolean removeAfter) {
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

  public boolean isLocked(String pattern) {
      Tuple tuple = getMatchingTuple(pattern);
      if (isInvalidTuple(pattern)) {
        throw new IllegalArgumentException();
      }
      return tuple.isTaken();
  }

  public String read(String pattern) {
    if (isInvalidTuple(pattern)) {
      throw new IllegalArgumentException();
    }
    return waitForMatchingTuple(pattern, false);
  }


  public String take(String pattern) {
    if (isInvalidTuple(pattern)) {
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

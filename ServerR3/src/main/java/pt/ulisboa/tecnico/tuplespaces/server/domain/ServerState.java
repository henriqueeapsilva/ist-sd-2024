package pt.ulisboa.tecnico.tuplespaces.server.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class ServerState {

  private List<Tuple> tuples;

  private List<WaitingTake> waitingTakes;

  private Integer nextTask;

  public ServerState() {
    this.tuples = new ArrayList<>();
    this.waitingTakes = new ArrayList<>();
    this.nextTask = 1;
  }

  public void setClientId(Tuple tuple, int id) {
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

  public void aquireLock(Tuple tuple) {
    if (!isLocked(tuple)) {
      tuple.lock();
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
      if (tuple.getField().matches(pattern)  && !isLocked(tuple)) {
        matchingTuples.add(tuple.getField());
        aquireLock(tuple);
        setClientId(tuple, clientID);
      }
    }
    return matchingTuples;
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

  public boolean isLocked(Tuple tuple) {
      if (isInvalidTuple(tuple.getField())) {
        throw new IllegalArgumentException();
      }
      return tuple.isTaken();
  }

  public void advanceNextTask() {
    this.nextTask++;
    notifyAll();
  }


  // ----------- Operations -----------

  public void put(String tuple, Integer seqNum) {
    Tuple newTuple = new Tuple(tuple);

    if (isInvalidTuple(tuple)) {
      throw new IllegalArgumentException();
    }
    synchronized (this) {
      tuples.add(newTuple);
      notifyAll();
    }
  }

  public String read(String pattern) {
    if (isInvalidTuple(pattern)) {
      throw new IllegalArgumentException();
    }
    return waitForMatchingTuple(pattern, false);
  }

  public String take(String pattern, Integer seqNumber) {

    if (isInvalidTuple(pattern)) { // checks if the tuple is valid
      throw new IllegalArgumentException();
    }

    // Will wait for it's turn to execute
    while (!Objects.equals(seqNumber, nextTask)) {
      try {
        wait();
      } catch (InterruptedException e) {
        throw new RuntimeException();
      }
    }

    /* TODO: take searches for a matching tuple & fail

        if it doesn't find one - create a WaitingTake
        and adds it to the list of waiting takes   (DONE)

        then invokes advanceNextTask();   (DONE)

         - this one will wait for a put to unlock it. (DONE)

         - [Unlock Process] this process will unlock, remove the WaitingTake, invoke a put response and invoke advanceNextTask().

    * */

    Tuple matchingTuple = getMatchingTuple(pattern);

    if (matchingTuple == null) { // case where it doesn't find a matching Tuple
      WaitingTake waitingTake = new WaitingTake(pattern);
      waitingTakes.add(waitingTake);
      advanceNextTask();
      waitingTake.blockTake();

      return pattern;
    }

    else { // case where it finds a matching tuple

      tuples.remove(matchingTuple);
      advanceNextTask();
      return matchingTuple.getField();
    }
  }

  public synchronized List<String> getTupleSpacesState() {
    List<String> tupleSpaces = new ArrayList<>();
    for (Tuple tuple : this.tuples) {
      tupleSpaces.add(tuple.getField());
    }
    return tupleSpaces;
  }
}

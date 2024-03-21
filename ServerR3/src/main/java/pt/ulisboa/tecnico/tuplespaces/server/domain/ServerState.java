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

  public boolean isInvalidTuple(String tuple) {
    return tuple.charAt(0) != '<' || tuple.charAt(tuple.length() - 1) != '>' || tuple.contains(" ");
  }

  private Tuple getMatchingTuple(String pattern) {
    for (Tuple tuple : this.tuples) {
      if (tuple.getField().matches(pattern)) {
          return tuple;
      }
    }
    return null;
  }

  public String waitForMatchingTuple(String pattern) {
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
    }
    return matchingTuple.getField();
  }

  public synchronized void advanceTask() {
    this.nextTask++;
    notifyAll();
  }

  public synchronized void addTuple(Tuple tuple) {
    tuples.add(tuple);
    notifyAll();
  }

  // ----------- Operations -----------

  public void put(String tuple, Integer seqNum) {
    Tuple newTuple = new Tuple(tuple);

    if (isInvalidTuple(tuple)) {
      throw new IllegalArgumentException();
    }
    // Wait for it's turn to execute
    while (!Objects.equals(seqNum, nextTask)) {
      try {
        wait();
      } catch (InterruptedException e) {
        throw new RuntimeException();
      }
    }
    for (WaitingTake take: this.waitingTakes) {
      if (take.getPattern().matches(tuple)) {
        take.unblockTake();
        advanceTask();
        return;
      }
    }
    addTuple(newTuple);
    advanceTask();
  }

  public String read(String pattern) {
    if (isInvalidTuple(pattern)) {
      throw new IllegalArgumentException();
    }
    return waitForMatchingTuple(pattern);
  }

  public String take(String pattern, Integer seqNum) {
    if (isInvalidTuple(pattern)) {
      throw new IllegalArgumentException();
    }
    while (!Objects.equals(seqNum, nextTask)) {
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
      waitingTake.blockTake();
      advanceTask();

      return pattern;
    }
    else { // case where it finds a matching tuple
      tuples.remove(matchingTuple);
      advanceTask();
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

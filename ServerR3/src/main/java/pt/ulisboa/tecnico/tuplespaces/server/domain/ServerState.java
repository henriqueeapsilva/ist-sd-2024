package pt.ulisboa.tecnico.tuplespaces.server.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class ServerState {
  private List<String> tuples;
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

  private String getMatchingTuple(String pattern) {
    for (String tuple : this.tuples) {
      if (tuple.matches(pattern)) {
          return tuple;
      }
    }
    return null;
  }

  public String waitForMatchingTuple(String pattern) {
    String matchingTuple = getMatchingTuple(pattern);
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
    return matchingTuple;
  }

  public void waitForTurnToExec(Integer seqNum) {
    while (!Objects.equals(seqNum, nextTask)) {
      try {
        wait();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public synchronized void advanceTask() {
    this.nextTask++;
    notifyAll();
  }

  public synchronized void addTuple(String tuple) {
    tuples.add(tuple);
    notifyAll();
  }

  public synchronized void unblockTake(WaitingTake take){
    take.unblockTake();
    waitingTakes.remove(take);
  }

  // ----------- Operations -----------

  public void put(String tuple, Integer seqNum) {
    if (isInvalidTuple(tuple)) {
      throw new IllegalArgumentException();
    }
    waitForTurnToExec(seqNum);

    for (WaitingTake take: this.waitingTakes) {
      if (take.getPattern().matches(tuple)) {
        unblockTake(take);
        advanceTask();
        return;
      }
    }
    addTuple(tuple);
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
    waitForTurnToExec(seqNum);

    String matchingTuple = getMatchingTuple(pattern);

    if (matchingTuple == null) { // case where it doesn't find a matching Tuple
      WaitingTake waitingTake = new WaitingTake(pattern);
      waitingTakes.add(waitingTake);
      advanceTask();
      waitingTake.waitForUnblock();
      return pattern;
    }
    else { // case where it finds a matching tuple
      tuples.remove(matchingTuple);
      advanceTask();
      return matchingTuple;
    }
  }

  public List<String> getTupleSpacesState() {
    return tuples;
  }
}

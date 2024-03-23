package pt.ulisboa.tecnico.tuplespaces.server.domain;

public class WaitingTake {
    private final String pattern;
    private boolean isBlocked;

    public WaitingTake(String pattern) {
        this.pattern = pattern;
        this.isBlocked = true;
    }

    public synchronized void waitForUnblock(){
        while (isBlocked){
            try {
                wait();
            } catch (InterruptedException e){
                throw new RuntimeException();
            }
        }
    }

    public synchronized void unblockTake(){
        isBlocked = false;
        notifyAll();
    }

    public String getPattern() {
        return pattern;
    }
}

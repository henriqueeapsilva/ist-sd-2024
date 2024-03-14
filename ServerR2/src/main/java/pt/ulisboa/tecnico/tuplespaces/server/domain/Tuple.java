package pt.ulisboa.tecnico.tuplespaces.server.domain;

public class Tuple {
    private String field;

    private boolean flag = false;

    private int clientId;

    public Tuple(String field){
        this.field = field;
    }

    public String getField() {
        return field;
    }

    public boolean isTaken() { return flag;}

    public void setFlag(boolean flag) { this.flag = flag;}

    public void lock() { this.flag = true;}

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }
}

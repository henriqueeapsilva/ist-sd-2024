package pt.ulisboa.tecnico.tuplespaces.server;

import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc;
import pt.ulisboa.tecnico.tuplespaces.server.domain.ServerState;

public class ServerServiceImpl extends TupleSpacesGrpc.TupleSpacesImplBase {
    private ServerState tuplespaces = new ServerState();

}

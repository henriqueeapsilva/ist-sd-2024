package pt.ulisboa.tecnico.tuplespaces.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.NameServerGrpc;
import static pt.ulisboa.tecnico.tuplespaces.centralized.contract.NameServerOuterClass.*;
import pt.ulisboa.tecnico.tuplespaces.client.grpc.ClientService;

import java.util.ArrayList;

public class ClientMain {
    static final int numServers = 3;
    public static void main(String[] args) {

        System.err.println(ClientMain.class.getSimpleName());

        // receive and print arguments
        System.err.printf("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.err.printf("arg[%d] = %s%n", i, args[i]);
        }

        // check arguments
        if (args.length != 3) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: mvn exec:java -Dexec.args=<host> <port>");
            return;
        }
        // get the host and the port
        final String host = args[0];
        final String port = args[1];
        final String service = args[2];

        String target = host + ":" + port;

        //Get the serve addresses from naming server
        ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        NameServerGrpc.NameServerBlockingStub stub = NameServerGrpc.newBlockingStub(channel);

        ArrayList<String> servers = new ArrayList<>(); //ArrayList to register the servers hostname and ports
        try {
            lookupResponse response = stub.lookup(lookupRequest.newBuilder()
                    .setService(service).build());
            if (!response.getServersList().isEmpty()) {
                String address;
                String[] parts;
                for (int i = 0; i < numServers; i++) {
                    address = response.getServers(i);
                    servers.add(address);
                }
            }
        } catch (Exception e) {
            System.err.println("Error occurred during lookup: " + e.getMessage());
        }
        CommandProcessor parser = new CommandProcessor(new ClientService(numServers));
        parser.parseInput(servers);
    }
}
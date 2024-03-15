package pt.ulisboa.tecnico.tuplespaces.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.NameServerGrpc;
import static pt.ulisboa.tecnico.tuplespaces.centralized.contract.NameServerOuterClass.*;
import pt.ulisboa.tecnico.tuplespaces.client.grpc.ClientService;

import java.util.ArrayList;

public class ClientMain {
    static int numServers = 0;
    public static void main(String[] args) {
        System.err.println(ClientMain.class.getSimpleName());

        // receive and print arguments
        System.err.printf("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.err.printf("arg[%d] = %s%n", i, args[i]);
        }
        // check arguments
        if (args.length != 1) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: mvn exec:java -Dexec.args=<clientID>");
            return;
        }
        // get the host and the port
        final String host = "localhost";
        final String port = "5001";
        final String service = "TupleSpaces";
        final String clientId = args[0];

        String target = host + ":" + port;

        // Get the serve addresses from naming server
        ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        NameServerGrpc.NameServerBlockingStub stub = NameServerGrpc.newBlockingStub(channel);

        // ArrayList to register the servers hostname and ports
        ArrayList<String> servers = new ArrayList<>();

        try { // getting all the servers addresses
            lookupResponse response = stub.lookup(lookupRequest.newBuilder()
                    .setService(service).build());

            numServers = response.getServersList().size();
            if (numServers != 0) {
                String address;

                for (int i = 0; i < numServers; i++ ){
                    address = response.getServers(i);
                    System.out.println("Connected to:" + address);
                    servers.add(address);
                }
            }
        } catch (Exception e) {
            System.err.println("Error occurred during lookup: " + e.getMessage());
        }
        // Shutdown connection with naming server
        channel.shutdownNow();
        CommandProcessor parser = new CommandProcessor(new ClientService(numServers));
        parser.parseInput(servers, Integer.parseInt(clientId));
    }
}
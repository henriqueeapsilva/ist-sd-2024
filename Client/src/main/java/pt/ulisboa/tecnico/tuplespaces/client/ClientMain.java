package pt.ulisboa.tecnico.tuplespaces.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.NameServerGrpc;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.NameServerOuterClass;
import pt.ulisboa.tecnico.tuplespaces.client.grpc.ClientService;

public class ClientMain {
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

        //Get serverAddress from naming server
        ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        NameServerGrpc.NameServerBlockingStub stub = NameServerGrpc.newBlockingStub(channel);

        try {
            NameServerOuterClass.lookupResponse response = stub.lookup(NameServerOuterClass.lookupRequest.newBuilder()
                    .setService(service).setQualifier("A").build());

            // Perform further processing based on retrieved servers if needed
            if (!response.getServersList().isEmpty()) {
                // Assuming the first server in the list
                String firstServer = response.getServers(0);
                String[] parts = firstServer.split(":");
                if (parts.length == 2) {
                    String serverHost = parts[0];
                    String serverPort = parts[1];

                    // Perform any additional processing with the server host and port
                    System.out.println("Processing server: " + serverHost + ":" + serverPort);
                    CommandProcessor parser = new CommandProcessor(new ClientService());
                    parser.parseInput(serverHost, serverPort);
                }
            }
        } catch (Exception e) {
            System.err.println("Error occurred during lookup: " + e.getMessage());
        }

    }
}
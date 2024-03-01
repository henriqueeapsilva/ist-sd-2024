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

        //Get serverAddress from NamingServer
        ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        NameServerGrpc.NameServerBlockingStub stub = NameServerGrpc.newBlockingStub(channel);

        NameServerOuterClass.lookupResponse response = stub.lookup(NameServerOuterClass.lookupRequest.newBuilder()
                .setService(service).setQualifier("A").build());

        System.out.println("chegou aqui");
        //get the host and port from the address
        String[] servers = response.getServersList().toArray(new String[0]);
        for (String server: servers){
            System.out.println(server);
        }
        System.out.println("chegou aqui 2");
        if (servers.length > 0) {
            String server = servers[0];
            String[] parts = server.split(":");

            String serverHost = parts[0];
            String serverPort = parts[1];

            CommandProcessor parser = new CommandProcessor(new ClientService());
            parser.parseInput(serverHost, serverPort);
        }
        System.out.println("chegou aqui 3");
        //CommandProcessor parser = new CommandProcessor(new ClientService());
        //parser.parseInput(host, port);

    }
}
package pt.ulisboa.tecnico.tuplespaces.server;


import io.grpc.*;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.NameServerGrpc;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.NameServerOuterClass;

import java.io.IOException;

public class ServerMain {

    public static void main(String[] args) throws IOException, InterruptedException{

        System.err.println(ServerMain.class.getSimpleName());

        // receive and print arguments
        System.err.printf("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.err.printf("arg[%d] = %s%n", i, args[i]);
        }
        // check arguments
        if (args.length < 1) {
            System.err.println("Argument(s) missing!");
            System.err.printf("Usage: java %s port%n", ServerMain.class.getName());
        }

        final int port = Integer.parseInt(args[0]);
        final BindableService impl = new ServerServiceImp();
        final String qualifier = args[1];
        final String service = args[2];

        // Create a new server to listen on port
        Server server = ServerBuilder.forPort(port).addService(impl).build();

        // Register on NamingServer
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 5001).usePlaintext().build();

        String target = "localhost:" + port;

        NameServerGrpc.NameServerBlockingStub stub = NameServerGrpc.newBlockingStub(channel);
        NameServerOuterClass.registerResponse response = stub.register(NameServerOuterClass.registerRequest.newBuilder()
                .setName(service).setQualifier(qualifier).setAddress(target).build());

        server.start();

        System.err.println("Server started");

        // Handle Ctrl + C
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Delete server from NameServer
            stub.delete(NameServerOuterClass.deleteRequest.newBuilder()
                    .setServicename(service).setAddress(target).build());
            server.shutdownNow();
        }));
        server.awaitTermination();
    }
}


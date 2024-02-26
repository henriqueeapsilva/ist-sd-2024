package pt.ulisboa.tecnico.tuplespaces.server;

import io.grpc.BindableService;

public class ServerMain {

    public static void main(String[] args) {
        System.out.printf("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.printf("arg[%d] = %s%n", i, args[i]);
        }
        final int port = Integer.parseInt(args[0]);
    }
}


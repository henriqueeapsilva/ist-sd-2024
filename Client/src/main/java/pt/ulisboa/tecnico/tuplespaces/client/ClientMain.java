package pt.ulisboa.tecnico.tuplespaces.client;

import com.google.rpc.DebugInfo;
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
        if (args.length != 2) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: mvn exec:java -Dexec.args=<host> <port>");
            return;
        }

        // get the host and the port
        final String host = args[0];
        final String port = args[1];

        CommandProcessor parser = new CommandProcessor(new ClientService());
        parser.parseInput(host, port);

    }
}

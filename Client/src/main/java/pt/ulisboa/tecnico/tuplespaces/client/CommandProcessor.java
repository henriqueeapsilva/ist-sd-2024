package pt.ulisboa.tecnico.tuplespaces.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.sequencer.contract.SequencerOuterClass;
import pt.ulisboa.tecnico.sequencer.contract.SequencerGrpc;
import pt.ulisboa.tecnico.tuplespaces.client.grpc.ClientService;
import pt.ulisboa.tecnico.tuplespaces.client.observers.ResponseCollector;
import pt.ulisboa.tecnico.tuplespaces.client.observers.SequencerObserver;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaGrpc;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaTotalOrder;


import java.util.ArrayList;
import java.util.Scanner;


public class CommandProcessor {

    private static final String SPACE = " ";
    private static final String BGN_TUPLE = "<";
    private static final String END_TUPLE = ">";
    private static final String PUT = "put";
    private static final String READ = "read";
    private static final String TAKE = "take";
    private static final String SLEEP = "sleep";
    private static final String SET_DELAY = "setdelay";
    private static final String EXIT = "exit";
    private static final String GET_TUPLE_SPACES_STATE = "getTupleSpacesState";

    private final ClientService clientService;

    private final String sequencerTarget;

    public CommandProcessor(ClientService clientService, String seqTarget) {
        this.clientService = clientService;
        this.sequencerTarget = seqTarget;
    }

    void parseInput(ArrayList<String> servers, int clientId) {

        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        clientService.createStubs(servers);
        
        Integer seqNumber = getSeqNumber();

        while (!exit) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            String[] split = line.split(SPACE);
            switch (split[0]) {
                case PUT:
                    this.put(split);
                    break;

                case READ:
                    this.read(split);
                    break;

                case TAKE:
                    this.take(split);
                    break;

                case GET_TUPLE_SPACES_STATE:
                    this.getTupleSpacesState(split);
                    break;

                case SLEEP:
                    this.sleep(split);
                    break;

                case SET_DELAY:
                    this.setdelay(split);
                    break;

                case EXIT:
                    clientService.shutdown();
                    exit = true;
                    break;

                default:
                    this.printUsage();
                    break;
            }
        }
        scanner.close();
    }

    private void put(String[] split){
        int seqNum = getSeqNumber();
        if (!this.inputIsValid(split)) {
            return;
        }
        String tuple = split[1];

        System.out.println(clientService.putOperation(seqNum, tuple));
    }

    private void read(String[] split){
        if (!this.inputIsValid(split)) {
            this.printUsage();
            return;
        }
        String tuple = split[1];

        System.out.println(clientService.readOperation(tuple));
    }


    private void take(String[] split){
        int seqNum = getSeqNumber();
        if (!this.inputIsValid(split)) {
            this.printUsage();
            return;
        }
        String tuple = split[1];

        System.out.println(clientService.takeOperation(seqNum, tuple));
    }

    private void getTupleSpacesState(String[] split) {
        if (split.length != 2){
            this.printUsage();
            return;
        }
        String qualifier = split[1];

        System.out.println(clientService.getTupleSpacesState(qualifier));
    }

    private void sleep(String[] split) {
        if (split.length != 2){
            this.printUsage();
            return;
        }
        Integer time;

        // checks if input String can be parsed as an Integer
        try {
            time = Integer.parseInt(split[1]);
        } catch (NumberFormatException e) {
            this.printUsage();
            return;
        }

        try {
            Thread.sleep(time*1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void setdelay(String[] split) {
        if (split.length != 3){
            this.printUsage();
            return;
        }
        int qualifier = indexOfServerQualifier(split[1]);
        if (qualifier == -1)
            System.out.println("Invalid server qualifier");

        Integer time;

        // checks if input String can be parsed as an Integer
        try {
            time = Integer.parseInt(split[2]);
        } catch (NumberFormatException e) {
            this.printUsage();
            return;
        }
        // register delay <time> for when calling server <qualifier>
        this.clientService.setDelay(qualifier, time);
    }

    private int getSeqNumber(){

        ManagedChannel channel = ManagedChannelBuilder.forTarget(sequencerTarget).usePlaintext().build();
        SequencerGrpc.SequencerStub stub = SequencerGrpc.newStub(channel);

        SequencerOuterClass.GetSeqNumberRequest seqNumberRequest = SequencerOuterClass.GetSeqNumberRequest.newBuilder().build();

        ResponseCollector rc = new ResponseCollector();
        SequencerObserver<SequencerOuterClass.GetSeqNumberResponse> seqObserver = new SequencerObserver<>(rc);

        stub.getSeqNumber(seqNumberRequest,seqObserver);

        channel.shutdown();

        return rc.getNextSeqNumber();
    }



    private void printUsage() {
        System.out.println("Usage:\n" +
                "- put <element[,more_elements]>\n" +
                "- read <element[,more_elements]>\n" +
                "- take <element[,more_elements]>\n" +
                "- getTupleSpacesState <server>\n" +
                "- sleep <integer>\n" +
                "- setdelay <server> <integer>\n" +
                "- exit\n");
    }

    private int indexOfServerQualifier(String qualifier) {
        switch (qualifier) {
            case "A":
                return 0;
            case "B":
                return 1;
            case "C":
                return 2;
            default:
                return -1;
        }
    }

    private boolean inputIsValid(String[] input){
        if (input.length < 2
                ||
                !input[1].substring(0,1).equals(BGN_TUPLE)
                ||
                !input[1].endsWith(END_TUPLE)
                ||
                input.length > 2
        ) {
            this.printUsage();
            return false;
        }
        else {
            return true;
        }
    }
}

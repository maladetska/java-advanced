package info.kgeorgiy.ja.okorochkova.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import static info.kgeorgiy.ja.okorochkova.hello.Common.checkArgs;

import java.util.function.Supplier;

/**
 * Class implements {@link HelloClient} interface.
 *
 * @author Maria Okorochkova (@maladetska)
 */
abstract public class AbstractUDPClient implements HelloClient {
    /**
     * Get the command line, create new HelloNonblockingUDPClient or HelloUDPClient and run it.
     *
     * @param args           args-type: "host [port [prefix [threads [requests]]]]"
     * @param instanceServer HelloNonblockingUDPClient or HelloUDPClient
     */
    public static void main(final String[] args, final Supplier<HelloClient> instanceServer) {
        try {
            checkArgs(args, 5);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            return;
        }

        HelloClient client = instanceServer.get();
        client.run(
                args[0],
                Integer.parseInt(args[0]),
                args[2],
                args.length > 3 ? Integer.parseInt(args[3]) : 5,
                args.length > 4 ? Integer.parseInt(args[4]) : 10
        );
    }

    protected static final int TIME_OUT = 20;
    protected int port;
    protected String prefix;
    protected int requests;

    protected boolean checkResponseContainsRequest(final String response, final String request) {
        System.out.println("RECEIVED RESPONSE: " + response);
        return response.contains(request);
    }
}

package info.kgeorgiy.ja.okorochkova.hello;

import java.util.function.Supplier;
import java.util.concurrent.*;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import static info.kgeorgiy.ja.okorochkova.hello.Common.checkArgs;

/**
 * Class implements {@link HelloServer} interface.
 *
 * @author Maria Okorochkova (@maladetska)
 */
abstract public class AbstractUDPServer implements HelloServer {
    /**
     * Get the command line, create HelloNonblockingUDPServer or HelloUDPServer and start it.
     *
     * @param args           args-type: "port [threads]"
     * @param instanceServer HelloNonblockingUDPServer or HelloUDPServer
     */
    public static void main(final String[] args, final Supplier<HelloServer> instanceServer) {
        try {
            checkArgs(args, 2);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            return;
        }

        try (final HelloServer server = instanceServer.get()) {
            server.start(
                    Integer.parseInt(args[0]),
                    args.length == 2 ? Integer.parseInt(args[1]) : 5);
            TimeUnit.SECONDS.sleep(TIME_OUT);
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }
    }

    protected static final int TIME_OUT = 20;

    protected ExecutorService workers;
}

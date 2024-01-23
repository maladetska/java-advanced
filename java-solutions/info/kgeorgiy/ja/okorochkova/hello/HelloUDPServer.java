package info.kgeorgiy.ja.okorochkova.hello;;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import static info.kgeorgiy.ja.okorochkova.hello.Common.*;

import java.util.concurrent.*;
import java.net.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Class extends {@link AbstractUDPServer} that implements {@link HelloServer} interface.
 *
 * @author Maria Okorochkova (@maladetska)
 */
public class HelloUDPServer extends AbstractUDPServer {
    private static final int TIME_OUT = 20;
    private DatagramSocket socket;

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(int port, int threads) {
        try {
            workers = Executors.newFixedThreadPool(threads);
            socket = new DatagramSocket(port);

            IntStream.range(0, threads).forEach(
                    (i) -> workers.execute(this::receiveAndSend)
            );
        } catch (final IllegalArgumentException e) {
            System.err.println("Count of threads must be more than 0: " + e.getMessage());
        } catch (final SocketException e) {
            System.err.println("Problem with socket: " + e.getMessage());
        }
    }

    private void receiveAndSend() {
        try {
            final DatagramPacket packet = new DatagramPacket(
                    new byte[socket.getReceiveBufferSize()],
                    socket.getReceiveBufferSize()
            );
            while (!Thread.interrupted() && !socket.isClosed()) {
                socket.receive(packet);
                byte[] request = ("Hello, " + new String(
                        packet.getData(),
                        packet.getOffset(),
                        packet.getLength(),
                        StandardCharsets.UTF_8
                )).getBytes(StandardCharsets.UTF_8);
                socket.send(
                        new DatagramPacket(
                                request,
                                packet.getOffset(),
                                request.length,
                                packet.getAddress(),
                                packet.getPort()
                        )
                );
            }
        } catch (final SocketException e) {
            if (!socket.isClosed()) {
                System.err.println("Problem with socket " + e.getMessage());
            }
        } catch (final SocketTimeoutException e) {
            System.err.println("Timeout has expired: " + e.getMessage());
        } catch (final IOException e) {
            System.err.println("Problem for socket receive or send: " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        socket.close();
        closeWorkers(workers, TIME_OUT);
    }

    /**
     * Get the command line, create new HelloUDPServer and start by {@link AbstractUDPServer#main(String[], Supplier)}.
     *
     * @param args args-type: "port [threads]"
     */
    public static void main(final String[] args) {
        main(args, HelloUDPServer::new);
    }
}

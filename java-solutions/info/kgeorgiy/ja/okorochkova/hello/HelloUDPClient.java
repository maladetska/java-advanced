package info.kgeorgiy.ja.okorochkova.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import static info.kgeorgiy.ja.okorochkova.hello.Common.*;

import java.util.concurrent.*;
import java.net.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Class extends {@link AbstractUDPClient} that implements {@link HelloClient} interface.
 *
 * @author Maria Okorochkova (@maladetska)
 */
public class HelloUDPClient extends AbstractUDPClient {
    private InetAddress address;

    /**
     * {@inheritDoc}
     */
    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        try {
            this.port = port;
            this.prefix = prefix;
            this.requests = requests;

            address = InetAddress.getByName(host);

            ExecutorService workers = Executors.newFixedThreadPool(threads);
            IntStream.range(0, threads).forEach(
                    (threadNumber) -> workers.execute(
                            () -> {
                                try {
                                    distributionByThreads(threadNumber);
                                } catch (final SocketException e) {
                                    System.err.println("Problem with socket: " + e.getMessage());
                                }
                            }
                    )
            );

            closeWorkers(workers, TIME_OUT);
        } catch (final IllegalArgumentException e) {
            System.err.println("Count of threads must be more than 0: " + e.getMessage());
        } catch (final UnknownHostException e) {
            System.err.println("Problem with IP address: " + e.getMessage());
        }
    }

    private void distributionByThreads(final int threadNumber) throws SocketException {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIME_OUT);
            IntStream.range(0, requests).forEach(
                    (requestNumber) -> {
                        try {
                            sendAndReceive(
                                    socket,
                                    new DatagramPacket(
                                            new byte[socket.getReceiveBufferSize()],
                                            socket.getReceiveBufferSize(),
                                            address,
                                            port
                                    ),
                                    prefix + (threadNumber + 1) + "_" + (requestNumber + 1)
                            );
                        } catch (final SocketException e) {
                            System.err.println("Problem with packet: " + e.getMessage());
                        }
                    }
            );

        } catch (final SocketException e) {
            System.err.println(e.getMessage());
        }
    }

    private void sendAndReceive(final DatagramSocket socket, final DatagramPacket packet, final String request) {
        while (!Thread.interrupted() && !socket.isClosed()) {
            try {
                byte[] bytesRequest = request.getBytes(StandardCharsets.UTF_8);
                socket.send(
                        new DatagramPacket(
                                bytesRequest,
                                packet.getOffset(),
                                bytesRequest.length,
                                address,
                                port
                        )
                );

                socket.receive(packet);

                if (checkResponseContainsRequest(
                        new String(
                                packet.getData(),
                                packet.getOffset(),
                                packet.getLength(),
                                StandardCharsets.UTF_8
                        ),
                        request
                )) break;

            } catch (final IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     * Get the command line, create new HelloUDPClient
     * and run it by {@link AbstractUDPClient#main(String[], Supplier)}.
     *
     * @param args args-type: "host [port [prefix [threads [requests]]]]"
     */
    public static void main(final String[] args) {
        main(args, HelloUDPClient::new);
    }
}

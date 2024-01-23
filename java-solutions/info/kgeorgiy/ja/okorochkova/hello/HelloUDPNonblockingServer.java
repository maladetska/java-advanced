package info.kgeorgiy.ja.okorochkova.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import static info.kgeorgiy.ja.okorochkova.hello.Common.*;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

import static java.nio.channels.SelectionKey.*;

/**
 * Class extends {@link AbstractUDPServer} that implements {@link HelloServer} interface.
 *
 * @author Maria Okorochkova (@maladetska)
 */
public class HelloUDPNonblockingServer extends AbstractUDPServer {
    private record Response(SocketAddress address, String text) {
    }

    private ExecutorService singleThreadExecutor;

    private Selector selector;
    private DatagramChannel channel;
    private Queue<Response> responses;

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(int port, int threads) {
        try {
            workers = Executors.newFixedThreadPool(threads);
            selector = Selector.open();

            channel = DatagramChannel.open();
            channel.configureBlocking(false);
            channel.register(selector, OP_READ);
            channel.bind(new InetSocketAddress(port));

            responses = new ConcurrentLinkedDeque<>();

            singleThreadExecutor = Executors.newSingleThreadExecutor();
            singleThreadExecutor.submit(this::keyWorker);

        } catch (final IllegalArgumentException e) {
            System.err.println("Count of threads must be more than 0: " + e.getMessage());
        } catch (final ClosedChannelException e) {
            System.err.println("Channel is close: " + e.getMessage());
        } catch (final IOException e) {
            System.err.println("Problem with selector opening: " + e.getMessage());
        }
    }

    private void keyWorker() {
        while (!Thread.interrupted() && !channel.socket().isClosed()) {
            try {
                selector.select();
                for (final Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext(); ) {
                    final Key key = new Key(i.next());
                    key.writeOrRead();
                    i.remove();
                }
            } catch (final ClosedSelectorException e) {
                System.out.println("Selector closed.");
            } catch (final IOException e) {
                System.err.println("Problem with selector: " + e.getMessage());
                close();
            }
        }
    }

    private class Key implements IKey {
        private final SelectionKey key;

        private Key(final SelectionKey key) {
            this.key = key;
        }

        @Override
        public void writeOrRead() {
            if (key.isReadable()) {
                read();
            } else if (key.isWritable()) {
                write();
            } else {
                System.err.println("Key is not readable and writable");
            }
        }

        @Override
        public void write() {
            final Response response = responses.poll();
            if (response != null) {
                try {
                    channel.send(
                            ByteBuffer.wrap(response.text.getBytes(StandardCharsets.UTF_8)),
                            response.address
                    );
                } catch (final IOException e) {
                    System.err.println("Problem with channel sending: " + e.getMessage());
                }
            }
            key.interestOpsOr(OP_READ);
        }

        @Override
        public void read() {
            try {
                final ByteBuffer buffer = ByteBuffer.allocate(channel.socket().getReceiveBufferSize());
                final SocketAddress address = channel.receive(buffer);
                workers.submit(() -> {
                    responses.add(
                            new Response(
                                    address,
                                    "Hello, ".concat(StandardCharsets.UTF_8.decode(buffer.flip()).toString())
                            )
                    );
                    key.interestOps(OP_WRITE);
                    selector.wakeup();
                });
            } catch (final SocketException e) {
                System.err.println("Problem with channel's socket: " + e.getMessage());
            } catch (final IOException e) {
                System.err.println("Problem with receiving channel: " + e.getMessage());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        try {
            channel.close();
            selector.close();
            closeExecutorServiceClone(singleThreadExecutor); // singleThreadExecutor.close();
            closeExecutorServiceClone(workers); // workers.close();
        } catch (final IOException e) {
            System.err.println("Cannot close selector or channel: " + e.getMessage());
        }
    }

    /**
     * Get the command line, create HelloNonblockingUDPServer
     * and start it by {@link AbstractUDPServer#main(String[], Supplier)}.
     *
     * @param args args-type: "port [threads]"
     */
    public static void main(final String[] args) {
        main(args, HelloUDPNonblockingServer::new);
    }
}

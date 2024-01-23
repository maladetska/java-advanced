package info.kgeorgiy.ja.okorochkova.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.nio.charset.StandardCharsets;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Supplier;
import java.nio.channels.*;
import java.io.IOException;

import static java.nio.channels.SelectionKey.*;

/**
 * Class extends {@link AbstractUDPClient} that implements {@link HelloClient} interface.
 *
 * @author Maria Okorochkova (@maladetska)
 */
public class HelloUDPNonblockingClient extends AbstractUDPClient {
    private record RequestThreadParams(int threadNumber, ByteBuffer socketBytesArray) {
    }

    private static class Request {
        int requestNumber;
        final RequestThreadParams threadParams;

        private Request(final RequestThreadParams thread) {
            this.threadParams = thread;
        }
    }

    private Selector selector;
    private InetSocketAddress socketAddress;
    private List<DatagramChannel> channels;

    private void addChannels(final int i) throws IOException {
        DatagramChannel channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.connect(socketAddress);
        channel.register(
                selector,
                OP_WRITE,
                new Request(
                        new RequestThreadParams(
                                i,
                                ByteBuffer.allocate(channel.socket().getReceiveBufferSize())
                        )
                )
        );
        channels.add(channel);
    }

    private void createChannels(final int threads) {
        try {
            selector = Selector.open();
            channels = new ArrayList<>(Collections.nCopies(threads, DatagramChannel.open()));
            for (int i = 0; i < threads; i++) {
                addChannels(i);
            }
        } catch (final IOException e) {
            System.err.println("Cannot create channels for: " + e);
        }
    }

    private void channelProcessing() {
        try {
            while (!Thread.interrupted() && !selector.keys().isEmpty()) {
                final Set<SelectionKey> selectedKeys = selector.selectedKeys();
                selector.select(TIME_OUT);
                if (selectedKeys.isEmpty()) {
                    for (SelectionKey key : selector.keys()) {
                        key.interestOps(OP_WRITE);
                    }
                }
                for (final Iterator<SelectionKey> i = selectedKeys.iterator(); i.hasNext(); ) {
                    final Key key = new Key(i.next());
                    key.writeOrRead();
                    i.remove();
                }
            }
        } catch (final ClosedSelectorException e) {
            System.out.println("Selector closed.");
        } catch (final IOException e) {
            try {
                selector.close();
            } catch (IOException ex) {
                System.err.println("Cannot close selector: " + ex.getMessage());
            }
            System.out.println(e.getMessage());
        } finally {
            for (DatagramChannel channel : channels) {
                try {
                    channel.close();
                } catch (IOException ex) {
                    System.err.println("Cannot close channel: " + ex.getMessage());
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        this.port = port;
        this.prefix = prefix;
        this.requests = requests;

        socketAddress = new InetSocketAddress(host, port);

        createChannels(threads);
        channelProcessing();
    }

    private class Key implements IKey {
        private final SelectionKey key;
        private DatagramChannel channel;
        private Request request;

        private Key(final SelectionKey key) {
            this.key = key;
        }

        @Override
        public void writeOrRead() {
            channel = (DatagramChannel) key.channel();
            request = (Request) key.attachment();
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
            final var message = prefix + (request.threadParams.threadNumber + 1) + "_" + (request.requestNumber + 1);
            try {
                channel.send(
                        ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)),
                        socketAddress
                );
            } catch (final IOException e) {
                System.err.println("Problem with channel sending: " + e.getMessage());
            }
            key.interestOps(OP_READ);
        }

        @Override
        public void read() {
            try {
                channel.receive(request.threadParams.socketBytesArray.clear());
            } catch (IOException e) {
                System.err.println("Problem with receiving channel: " + e.getMessage());
            }

            if (checkResponseContainsRequest(
                    StandardCharsets.UTF_8.decode(request.threadParams.socketBytesArray.flip()).toString(),
                    prefix + (request.threadParams.threadNumber + 1) + "_" + (request.requestNumber + 1)
            )) request.requestNumber++;

            key.interestOps(OP_WRITE);
            if (request.requestNumber >= requests) {
                try {
                    channel.close();
                } catch (final IOException e) {
                    System.err.println("Cannot close channel: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Get the command line, create new HelloUDPNonblockingClient
     * and run it by {@link AbstractUDPClient#main(String[], Supplier)}.
     *
     * @param args args-type: "host [port [prefix [threads [requests]]]]"
     */
    public static void main(final String[] args) {
        main(args, HelloUDPNonblockingClient::new);
    }
}
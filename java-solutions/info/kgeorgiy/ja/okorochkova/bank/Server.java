package info.kgeorgiy.ja.okorochkova.bank;

import info.kgeorgiy.ja.okorochkova.bank.bank.IBank;
import info.kgeorgiy.ja.okorochkova.bank.bank.RemoteBank;

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;

public final class Server {
    private final static int DEFAULT_PORT = 8888;

    public static void main(final String... args) {
        final int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        IBank bank;
        try {
            bank = new RemoteBank(port);
            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind("bank", UnicastRemoteObject.exportObject(bank, port));
            System.out.println("Bank starts");
        } catch (RemoteException e) {
            System.out.println("Cannot export object: " + e.getMessage());
        }
    }
}

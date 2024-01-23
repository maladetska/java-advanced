package info.kgeorgiy.ja.okorochkova.bank.account;

import java.io.Closeable;
import java.rmi.RemoteException;

public class RemoteAccount implements IAccount {
    private final String id;
    private int amount;
    private String passport; // :NOTE:

    public RemoteAccount(final String passport, final String subId) {
        this.id = passport + ':' + subId;
        amount = 0;
        this.passport = passport;
    }

    public RemoteAccount(final String id) {
        this.id = id;
        amount = 0;
    }

    public String getPassport() throws RemoteException {
        return passport;
    }

    private boolean checkAmount() {
        return amount >= 0;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public synchronized int getAmount() throws RemoteException {
        System.out.println("Getting amount of money for account " + id);
        return amount;
    }

    @Override
    public synchronized void setAmount(final int amount) throws RemoteException {
        System.out.println("Setting amount of money for account " + id);
        this.amount = amount;
    }

    @Override
    public void add(final int amount) throws RemoteException {
        this.amount += amount;
        if (!checkAmount()) {
            System.err.println("Amount is less than zero!");
        }
    }

    @Override
    public void subtract(final int amount) throws RemoteException {
        this.amount -= amount;
        if (!checkAmount()) {
            System.err.println("Amount is less than zero!");
        }
    }
}

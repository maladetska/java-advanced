package info.kgeorgiy.ja.okorochkova.bank.account;

import java.rmi.*;

public interface IAccount extends Remote {
    /**
     * Returns account identifier.
     */
    String getId() throws RemoteException;

    /**
     * Returns amount of money in the account.
     */
    int getAmount() throws RemoteException;

    String getPassport() throws  RemoteException;

    /**
     * Sets amount of money in the account.
     */
    void setAmount(int amount) throws RemoteException;

    /**
     * Add amount.
     */
    void add(final int money) throws RemoteException;

    /**
     * Subtract amount.
     */
    void subtract(final int money) throws RemoteException;
}

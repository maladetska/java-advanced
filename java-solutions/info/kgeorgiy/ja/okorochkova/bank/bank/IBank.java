package info.kgeorgiy.ja.okorochkova.bank.bank;

import info.kgeorgiy.ja.okorochkova.bank.account.IAccount;
import info.kgeorgiy.ja.okorochkova.bank.person.*;

import java.rmi.*;
import java.util.Map;

public interface IBank extends Remote {
    /**
     * Creates a new account with specified identifier if it does not already exist.
     *
     * @param id account id
     * @return created or existing account.
     */
    IAccount createAccount(final String id) throws RemoteException;

    /**
     * Returns account by identifier.
     *
     * @param id account id
     * @return account with specified identifier or {@code null} if such account does not exist.
     */
    IAccount getAccount(final String id) throws RemoteException;

    /**
     * Returns all person's accounts.
     *
     * @param passportId passport id
     * @return List of accounts with specified identifier or {@code null} if such account does not exist.
     */
    Map<String, IAccount> getAllAccounts(final String passportId) throws RemoteException;

    /**
     * Create new person by passportId.
     *
     * @param passportId passport id
     * @param firstName  first name
     * @param lastName   last name
     * @return new Person
     */
    IPerson createPerson(final String passportId,
                         final String firstName,
                         final String lastName) throws RemoteException;

    /**
     * Returns person by passportId.
     *
     * @param passportId passport id
     * @return Person
     */
    IPerson getPerson(String passportId, int flag) throws RemoteException;

    /**
     * Returns local person for create instance
     * by {@link info.kgeorgiy.ja.okorochkova.bank.Client}
     * and {@link info.kgeorgiy.ja.okorochkova.bank.BankTests}
     *
     * @param passportId passport id
     * @return new local Person
     */
    LocalPerson getLocalPerson(String passportId) throws RemoteException;
}

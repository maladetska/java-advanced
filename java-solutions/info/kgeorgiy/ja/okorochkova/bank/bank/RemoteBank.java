package info.kgeorgiy.ja.okorochkova.bank.bank;

import info.kgeorgiy.ja.okorochkova.bank.account.*;
import info.kgeorgiy.ja.okorochkova.bank.person.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;

public class RemoteBank implements IBank {
    private final int port;
    private final ConcurrentMap<String, IAccount> accounts = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, IPerson> persons = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Map<String, IAccount>> accountsByPerson = new ConcurrentHashMap<>(); // :NOTE: не консистентность данных

    public RemoteBank(final int port) {
        this.port = port;
    }

    @Override
    public IAccount createAccount(final String id) throws RemoteException {
        if (id == null) {
            System.err.println("Incorrect argument_s ");
            return null;
        }
        final IAccount account = new RemoteAccount(id);
//        accounts.putIfAbsent()
//        accounts.computeIfAbsent()
        if (!accounts.containsKey(id)) {
            int i = id.indexOf(':');
            final String passport = id.substring(0, i);
            final String subId = id.substring(i);

            Map<String, IAccount> accs = new HashMap<>();
            if (accountsByPerson.get(passport) != null) {
                accs = accountsByPerson.get(passport);
            }

            accs.put(subId, account);
            System.out.println("Creating account " + id);
            accounts.put(passport, account);
            accountsByPerson.put(passport, accs);
            UnicastRemoteObject.exportObject(account, port);
            return account;
        } else {
            return getAccount(id);
        }
    }

    @Override
    public IAccount getAccount(final String id) throws RemoteException {
        if (id == null) {
            System.err.println("Incorrect argument_s ");
            return null;
        }
        IAccount account = accounts.get(id);
        if (account == null) {
            System.out.println("Account do not exist" + id);
            return null;
        }
        System.out.println("Account exists" + id);
        return accounts.get(id);
    }

    @Override
    public Map<String, IAccount> getAllAccounts(final String passportId) throws RemoteException {
        if (passportId == null) {
            System.err.println("Incorrect argument_s ");
            return null;
        }
        if (persons.get(passportId) != null) {
            return accountsByPerson.get(passportId);
        }
        return null;
    }

    @Override
    public IPerson createPerson(final String passportId, final String firstName, final String lastName) throws RemoteException {
        if (passportId == null || firstName == null || lastName == null) {
            return null;
        }
        IPerson person = new RemotePerson(passportId, firstName, lastName);
        if (persons.putIfAbsent(passportId, person) != null) {
            System.out.println("Creating person: passport = " + passportId);
            UnicastRemoteObject.exportObject(person, port);
            return person;
        } else {
//            IPerson person2 = persons.get(passportId);
            if (firstName.equals(person.getFirstName()) && lastName.equals(person.getLastName())) {
                return person;
            } else {
                return null;
            }
        }

    }

    @Override
    public IPerson getPerson(final String passportId, final int flag) throws RemoteException {
        if (passportId == null) {
            return null;
        }
        IPerson person = persons.get(passportId);
        if (person != null) {
            if (flag == 1) {
                return getLocalPerson(passportId);
            }
            return person;
        }
        System.err.println("Person does not exist " + passportId);
        return null;
    }

    // :NOTE: Возвращает RemoteAccounts
    public Map<String, IAccount> getLocalAccounts(final String passportId) throws RemoteException {
        if (passportId == null) {
            System.err.println("Incorrect argument_s ");
            return null;
        }
        Map<String, IAccount> accsRemote = getAllAccounts(passportId);
        Map<String, IAccount> accsLocal = new ConcurrentHashMap<>();
        for (final IAccount acc : accsRemote.values()) {
            accsLocal.put(passportId, acc); // :NOTE: LocalAccount
        }
        return accsLocal;
    }

    @Override
    public LocalPerson getLocalPerson(final String passportId) throws RemoteException {
        if (passportId == null) {
            System.err.println("Incorrect argument_s ");
            return null;
        }
        IPerson person = persons.get(passportId);
        if (person != null) {
            Map<String, IAccount> accountsLocal = getLocalAccounts(passportId);
            return new LocalPerson(passportId, person.getFirstName(), person.getLastName(), accountsLocal); // :NOTE: accountsLocal is RemoteAccounts
        }
        return null;
    }
}

package info.kgeorgiy.ja.okorochkova.bank;

import info.kgeorgiy.ja.okorochkova.bank.account.IAccount;
import info.kgeorgiy.ja.okorochkova.bank.bank.IBank;
import info.kgeorgiy.ja.okorochkova.bank.person.IPerson;

import java.rmi.*;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Objects;

public final class Client {
    /**
     * Utility class.
     */
    private Client() {
    }

    public static void main(final String... args) throws RemoteException {
        if (args != null && args.length == 5 && Arrays.stream(args).allMatch(Objects::nonNull)) {
            System.err.println("Null arguments");
            return;
        }
        final IBank bank;
        try {
            bank = (IBank) Naming.lookup("//localhost/bank");
        } catch (final NotBoundException e) {
            System.out.println("Bank is not bound");
            return;
        } catch (final MalformedURLException e) {
            System.out.println("Bank URL is invalid");
            return;
        }

        String firstName = args[0];
        String lastName = args[1];
        String passportId = args[2];
        String accountId = args[3];
        int change = Integer.parseInt(args[4]);

        IPerson person = bank.getPerson(passportId,1);
        if(person == null){
            System.out.println("Person does not exist. Creating person");
            person = bank.createPerson(passportId, firstName, lastName);
        }
        String subId = passportId + ":" + accountId;

        IAccount account = bank.getAccount(subId);
        if (account == null) {
            System.out.println("Creating account");
            account = bank.createAccount(accountId);
        } else {
            System.out.println("Account already exists");
        }
        System.out.println("Account id: " + account.getId());
        System.out.println("Money: " + account.getAmount());
        System.out.println("Adding money");
        account.setAmount(account.getAmount() + 100);
        System.out.println("Money: " + account.getAmount());
    }
}

package info.kgeorgiy.ja.okorochkova.bank.person;

import info.kgeorgiy.ja.okorochkova.bank.account.IAccount;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Map;

public class LocalPerson extends Person implements Serializable {
    private final Map<String, IAccount> accounts;
//    public LocalPerson(final int passportIdNumber,
//                        final String firstName,
//                        final String lastName,
//                        final String patronym) {
//        super(passportIdNumber, firstName, lastName, patronym);
//    }

    public LocalPerson(final String passportIdString,
                       final String firstName,
                       final String lastName, Map<String, IAccount> accounts) {
        super(passportIdString, firstName, lastName);
        this.accounts = accounts;
    }

    @Override
    public String getPassportIdNumber() throws RemoteException {
        return passportIdString;
    }
}

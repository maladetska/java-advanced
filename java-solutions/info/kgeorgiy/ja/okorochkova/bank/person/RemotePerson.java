package info.kgeorgiy.ja.okorochkova.bank.person;

import java.rmi.RemoteException;

public class RemotePerson extends Person {
//    public RemotePerson(final int passportIdNumber,
//                        final String firstName,
//                        final String lastName,
//                        final String patronym) {
//        super(passportIdNumber, firstName, lastName, patronym);
//    }

    public RemotePerson(final String passportIdString,
                        final String firstName,
                        final String lastName) {
        super(passportIdString, firstName, lastName);
    }

    @Override
    public String getPassportIdNumber() throws RemoteException {
        return passportIdString;
    }
}

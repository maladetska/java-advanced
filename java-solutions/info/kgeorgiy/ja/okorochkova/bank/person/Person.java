package info.kgeorgiy.ja.okorochkova.bank.person;

import java.rmi.RemoteException;

abstract public class Person implements IPerson {
    final String passportIdString;
    final String firstName;
    final String lastName;
    final String patronym;

//    public Person(final int passportIdNumber,
//                  final String name,
//                  final String lastName,
//                  final String patronym
//    ) {
//        this.passportIdNumber = passportIdNumber;
//        this.passportIdString = Integer.toString(passportIdNumber);
//        this.firstName = name;
//        this.lastName = lastName;
//        this.patronym = patronym;
//    }

    public Person(final String passportIdString,
                  final String name,
                  final String lastName
    ) {
//        this.passportIdString = Integer.parseInt(passportIdString);
        this.passportIdString = passportIdString;
        this.firstName = name;
        this.lastName = lastName;
        this.patronym = null;
    }

    @Override
    public String getPassportIdString() throws RemoteException {
        return passportIdString;
    }

    @Override
    public String getFirstName() throws RemoteException {
        return firstName;
    }

    @Override
    public String getLastName() throws RemoteException {
        return lastName;
    }
    @Override
    public String getPatronym() throws RemoteException {
        return patronym;
    }
}

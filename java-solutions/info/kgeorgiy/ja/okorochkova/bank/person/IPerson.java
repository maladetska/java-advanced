package info.kgeorgiy.ja.okorochkova.bank.person;

import java.rmi.*;

public interface IPerson extends Remote {
    String getPassportIdNumber() throws RemoteException;
    String getPassportIdString() throws RemoteException;
    String getFirstName() throws RemoteException;
    String getLastName() throws RemoteException;
    String getPatronym() throws RemoteException;

}

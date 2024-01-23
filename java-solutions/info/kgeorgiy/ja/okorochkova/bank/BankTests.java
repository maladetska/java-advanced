package info.kgeorgiy.ja.okorochkova.bank;

import info.kgeorgiy.ja.okorochkova.bank.person.*;
import info.kgeorgiy.ja.okorochkova.bank.bank.*;
import org.junit.jupiter.api.*;

import java.rmi.RemoteException;
import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;

@DisplayName("IBank's tests")
public class BankTests {
    private static final int WORD_LIMIT = 10;
    private static final int ITER_LIMIT = 50;
    private static final int PORT = 8080;
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String NUMBERS = "0123456789";
    private static final String ALPHABET_AND_NUMBURS = ALPHABET + NUMBERS;
    private static IBank testBank;
    private static Registry testRegistry;

    @BeforeAll
    static void beforeAll() throws RemoteException {
        testRegistry = LocateRegistry.createRegistry(PORT);
    }

    @BeforeEach
    void beforeEach() throws RemoteException {
        testBank = new RemoteBank(PORT);
        testRegistry.rebind(
                "testBank",
                UnicastRemoteObject.exportObject(testBank, PORT));
    }

    @Test
    public void test00_nullPerson() throws RemoteException {
        Assertions.assertNull(testBank.createPerson(null, null, null));
    }

    @Test
    public void test01_personClone() throws RemoteException {
        String testFirstName = randomString();
        String testLastName = randomString();
        String testPassport = randomString();
        testBank.createPerson(testPassport, testFirstName, testLastName);
        Assertions.assertNotNull(testBank.createPerson(testPassport, testFirstName, testLastName));
    }

    @Test
    public void test02_passportClone() throws RemoteException {
        String testPassport = randomString();
        testBank.createPerson(randomString(), randomString(), testPassport);
        Assertions.assertNotNull(testBank.createPerson(randomString(), randomString(), testPassport));
    }

    @Test
    public void test04_checkNonExistentAccount() throws RemoteException {
        Assertions.assertNull(testBank.getAccount(createPassportSubId(randomString(), randomString())));
    }

    @Test
    public void test05_checkNonExistentPerson() throws RemoteException {
        Assertions.assertNull(testBank.getPerson(randomString(), 0));
    }

    @Test
    public void test06_createRemotePerson() throws RemoteException {
        for (int i = 0; i < ITER_LIMIT; i++) {
            String testFirstName = randomString();
            String testLastName = randomString();
            String testPassport = randomString();
            testBank.createPerson(testPassport, testFirstName, testLastName);
            IPerson person = testBank.getPerson(testPassport, 0);
            checkData(testFirstName, testLastName, testPassport, person);
        }
    }

    @Test
    public void test07_nullAccount() throws RemoteException {
        Assertions.assertNull(testBank.createAccount(null));
    }

    @Test
    public void test09_createLocalIPerson() throws RemoteException {
        for (int i = 0; i < ITER_LIMIT; i++) {
            String firstName = randomString();
            String lastName = randomString();
            String passport = randomString();

            Assertions.assertNotNull(testBank.createPerson(passport, firstName, lastName));

            testBank.createAccount(createPassportSubId(passport, randomString()));

            IPerson localIPerson = testBank.getPerson(passport, 1);
            checkData(firstName, lastName, passport, localIPerson);
        }
    }

    public void checkData(final String first, final String last, final String passport, final IPerson loc)
            throws RemoteException {
        Assertions.assertEquals(first, loc.getFirstName());
        Assertions.assertEquals(last, loc.getLastName());
        Assertions.assertEquals(passport, loc.getPassportIdNumber());
    }

    @Test
    public void test10_getAccounts() throws RemoteException {
        final int countPerson = 70;
        final int countAccounts = 20;
        for (int i = 0; i < countPerson; i++) {
            String passport = randomString();
            testBank.createPerson(passport, randomString(), randomString());
            testBank.getPerson(passport, 0);
            manyAccounts(passport, countAccounts);
            Assertions.assertEquals(testBank.getAllAccounts(passport).size(), countAccounts);
        }
    }

    private void manyAccounts(String passport, final int countAccounts) throws RemoteException {
        for (int j = 0; j < countAccounts; j++) {
            testBank.createAccount(createPassportSubId(passport, randomString()));
        }
    }

    private String createPassportSubId(final String passport, final String subId) {
        return passport + ':' + subId;
    }

    private String randomString() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < WORD_LIMIT; i++) {
            int index = random.nextInt(ALPHABET_AND_NUMBURS.length());
            char randomChar = ALPHABET_AND_NUMBURS.charAt(index);
            sb.append(randomChar);
        }
        return sb.toString();
    }

    // :NOTE: Хочется тест на одновременное использование RemotePerson и LocalPerson
}


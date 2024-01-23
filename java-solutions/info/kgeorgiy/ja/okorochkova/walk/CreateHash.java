package info.kgeorgiy.ja.okorochkova.walk;

import java.nio.file.*;
import java.security.*;
import java.io.*;

public class CreateHash {

    private final Path path;
    final MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
    private byte[] digest = null;
    private static final int BUFFER_SIZE = 4096;
    // :NOTE: хардкод количества байтов
    public static final String NO_SUCH_FILE_HASH = "0".repeat(64);

    public Path getPath() {
        return path;
    }

    public byte[] getHash() {
        return digest;
    }

    public CreateHash(final Path fPath) throws NoSuchAlgorithmException {
        path = fPath;

        try (InputStream is = Files.newInputStream(path)) {
            byte[] bufferDigest = new byte[BUFFER_SIZE];
            for (int cnt = is.read(bufferDigest); cnt > 0; cnt = is.read(bufferDigest)) {
                messageDigest.update(bufferDigest, 0, cnt);
            }
            digest = messageDigest.digest();
        } catch (final IOException ignored) {
            // :NOTE: не чистится messageDigest
        }
    }

    public String writeHash(final byte[] byteArray) {
        // :NOTE: хардкод байтов
        final StringBuilder str = new StringBuilder(40);
        if (byteArray == null) {
            str.append(NO_SUCH_FILE_HASH);
        } else {
            for (final byte b : byteArray) {
                str.append(String.format("%02x", b));
            }
        }

        return str.toString();
    }
}





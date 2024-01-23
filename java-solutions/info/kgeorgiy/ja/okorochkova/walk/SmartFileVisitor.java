package info.kgeorgiy.ja.okorochkova.walk;

import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.*;
import java.io.*;

import static info.kgeorgiy.ja.okorochkova.walk.Walker.writeHashAndFilename;

public class SmartFileVisitor extends SimpleFileVisitor<Path> {
    private final BufferedWriter output;

    SmartFileVisitor(BufferedWriter output) {
        this.output = output;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        try {
            CreateHash cr = new CreateHash(file);
            output.write(writeHashAndFilename(cr.writeHash(cr.getHash()), file.toString()));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 error" + e);
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        output.write(Walker.writeHashAndFilename(file.toString()));

        return FileVisitResult.CONTINUE;
    }
}
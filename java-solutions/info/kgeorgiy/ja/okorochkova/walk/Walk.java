package info.kgeorgiy.ja.okorochkova.walk;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.nio.file.*;
import java.io.*;

public class Walk extends Walker {
    public static void main(String[] args) throws IOException {
        try {
            checkArgs(args);
            pathInitializing(args[0], args[1]);
        } catch (final InvalidPathException e) {
            System.err.println("Wrong input or output paths to files. " + e.getMessage());
            return;
        } catch (final SecurityException e) {
            System.err.println("Security error " + e.getMessage());
            return;
        } catch (final IllegalArgumentException | IOException e) {
            System.err.println(e.getMessage());
            return;
        }

        try (BufferedReader in = Files.newBufferedReader(inputFile, StandardCharsets.UTF_8)) {
            try (BufferedWriter out = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
                while (in.ready()) {
                    String currFile = in.readLine();
                    try {
                        Path currPath = Paths.get(currFile);
                        try {
                            CreateHash cr = new CreateHash(currPath);
                            out.write(writeHashAndFilename(cr.writeHash(cr.getHash()), currFile));
                        } catch (final NoSuchAlgorithmException e) {
                            System.err.println("SHA-256 error" + e);
                        }
                    } catch (final SecurityException e) {
                        System.err.println("Problem with security " + e.getMessage());
                    } catch (final InvalidPathException e) {
                        out.write(writeHashAndFilename(currFile));
                        System.err.println("Wrong file path: " + e.getInput());
                    } catch (final FileNotFoundException e) {
                        System.err.println("File not found " + e.getMessage());
                    } catch (final FileSystemNotFoundException e) {
                        System.err.println("Problem with file system" + e.getMessage());
                    }
                }
            } catch (final IOException e) {
                // :NOTE: здесь ловятся ошибки  in.readLine()
                System.err.println("Problem with output file " + e.getMessage());
            } catch (final SecurityException e) {
                System.err.println("Security problem with input file " + e.getMessage());
            }
        } catch (final IOException e) {
            System.err.println("Problem with input file " + e.getMessage());
        } catch (final SecurityException e) {
            System.err.println("Security problem with input file " + e.getMessage());
        }
    }
}

package info.kgeorgiy.ja.okorochkova.walk;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.io.*;

public class RecursiveWalk extends Walker {

    public static void main(String[] args) throws IOException {
        try {
            checkArgs(args);
            pathInitializing(args[0], args[1]);
        } catch (InvalidPathException e) {
            System.err.println("Wrong input or output paths to files.");
            return;
        } catch (SecurityException e) {
            System.err.println("Security error " + e.getMessage());
            return;
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            return;
        }

        try (BufferedReader in = Files.newBufferedReader(inputFile, StandardCharsets.UTF_8)) {
            try (BufferedWriter out = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
                SmartFileVisitor fv = new SmartFileVisitor(out);
                while (in.ready()) {
                    String currFile = in.readLine();
                    try {
                        Path currPath = Path.of(currFile);
                        try {
                            Files.walkFileTree(currPath, fv);
                        } catch (RuntimeException e) {
                            System.err.println(e.getMessage());
                        } catch (NotDirectoryException e) {
                            System.err.println("Problem with path file: " + e);
                        }
                    } catch (SecurityException e) {
                        System.err.println("Problem with security " + e.getMessage());
                    } catch (InvalidPathException e) {
                        out.write(writeHashAndFilename(currFile));
                        System.err.println("Wrong file path: " + e.getInput());
                    } catch (FileNotFoundException e) {
                        System.err.println("File not found " + e.getMessage());
                    } catch (FileSystemNotFoundException e) {
                        System.err.println("Problem with file system" + e.getMessage());
                    }
                }
            } catch (IOException | SecurityException e) {
                System.err.println("Problem with output file " + e.getMessage());
            }
        } catch (IOException | SecurityException e) {
            System.err.println("Problem with input file " + e.getMessage());
        }
    }
}

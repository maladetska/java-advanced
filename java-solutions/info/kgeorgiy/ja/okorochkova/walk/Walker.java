package info.kgeorgiy.ja.okorochkova.walk;

import static info.kgeorgiy.ja.okorochkova.walk.CreateHash.NO_SUCH_FILE_HASH;

import java.io.IOException;
import java.nio.file.*;

abstract public class Walker {
    static Path inputFile = null;
    static Path outputFile = null;

    static void checkArgs(final String[] args) {
        if (args == null) {
            throw new IllegalArgumentException(
                "There are no arguments. Please enter the input file and output file."
            );
        }
        if (args.length > 2) {
            throw new IllegalArgumentException(
                "There are more then two arguments. Please enter the input file and output file.");
        } else if (args.length < 2) {
            throw new IllegalArgumentException(
                "There are less then arguments, please enter the input file and output files"
            );
        }
        if (args[0] == null || args[1] == null) {
            throw new IllegalArgumentException(
                "Please enter the input file and output file correct."
            );
        }
    }

    static void pathInitializing(final String arg0, final String arg1) throws IOException {
        inputFile = Paths.get(arg0);
        outputFile = Paths.get(arg1);
        if (!Files.isWritable(inputFile) || Files.notExists(inputFile) ) {
            throw new InvalidPathException("", "");
        }
        if (Files.notExists(outputFile)) {
            Files.createDirectories(outputFile.getParent());
        }
    }

    public static String writeHashAndFilename(final String hash, final String filePath) {
        // :NOTE: \n работает только на части ОС
        return hash + ' ' + filePath + '\n';
    }

    public static String writeHashAndFilename(final String filePath) {
        return NO_SUCH_FILE_HASH + ' ' + filePath + '\n';
    }
}

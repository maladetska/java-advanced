package info.kgeorgiy.ja.okorochkova.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.jar.*;
import java.util.zip.ZipEntry;
import javax.tools.ToolProvider;

/**
 * Class implements {@link JarImpler} interface.
 *
 * @author Maria Okorochkova (@maladetska)
 */
public class Implementor implements JarImpler {
    /**
     * "Impl" constant for end the classname.
     */
    public final static String IMPL = "Impl";
    /**
     * Java-format file constant.
     */
    private final static String JAVA_FORMAT = ".java";
    /**
     * Class-format file constant.
     */
    private final static String CLASS_FORMAT = ".class";
    /**
     * Char separator constant.
     */
    private final static char SLASH = '/';
    /**
     * Dot constant.
     */
    private final static char DOT = '.';
    /**
     * UTF-8 constant.
     */
    private final static String UNICODE = "UTF-8";
    /**
     * Current {@link java.util.jar.Manifest} version constant.
     */
    private final static String MANIFEST_VERSION = "1.0";
    /**
     * Main class for {@link java.util.jar.Manifest} constant.
     */
    private final static String MAIN_CLASS = "info.kgeorgiy.ja.okorochkova.implementor.Implementor";
    /**
     * Class path for {@link java.util.jar.Manifest} constant.
     */
    private final static String CLASS_PATH =
            "./../java-advanced-2023/artifacts/info.kgeorgiy.java.advanced.implementor.jar";

    /**
     * Default constructor.
     */
    public Implementor() {
    }

    /**
     * Construct {@code token} implementation and put it on special path.
     *
     * @param token type token to create implementation for.
     * @param path  root directory.
     * @throws ImplerException when implementation cannot be generated.
     */
    @Override
    public void implement(final Class<?> token, final Path path) throws ImplerException {
        try {
            Path file = Paths.get(path.toString() + SLASH
                    + token.getPackageName().replace(DOT, SLASH) + SLASH
                    + token.getSimpleName() + IMPL + JAVA_FORMAT);
            Files.createDirectories(file.getParent());
            final CodeConstructor code = new CodeConstructor(token, file);
            code.writeCodeInFile();
        } catch (final IOException e) {
            System.err.println("Have a problem with directory" + e.getMessage());
        }
    }

    /**
     * Constructs and returns {@link java.util.jar.Manifest} for create {@code .jar} file.
     *
     * @return {@link java.util.jar.Manifest} with current attributes.
     */
    public Manifest createManifest() {
        final Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, MANIFEST_VERSION);
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, MAIN_CLASS);
        manifest.getMainAttributes().put(Attributes.Name.CLASS_PATH, CLASS_PATH);

        return manifest;
    }

    /**
     * Implements {@code token} class, compile it and creates {@code .jar} file.
     *
     * @param token   type token to create implementation for.
     * @param jarFile target <var>.jar</var> file.
     * @throws ImplerException when implementation failed.
     */
    @Override
    public void implementJar(final Class<?> token, final Path jarFile) throws ImplerException {
        String pack = token.getPackageName().replace(DOT, SLASH);
        String javaFileFormat = token.getSimpleName() + IMPL + JAVA_FORMAT;
        String classFileFormat = token.getSimpleName() + IMPL + CLASS_FORMAT;
        String pathToJavaFile = pack + SLASH + javaFileFormat;
        String pathToClassFile = pack + SLASH + classFileFormat;

        try {
            Path currPath = Files.createTempDirectory(jarFile.toAbsolutePath().getParent(), "curr");
            implement(token, currPath);
            ToolProvider.getSystemJavaCompiler().run(
                    System.in,
                    System.out,
                    System.err,
                    "-encoding",
                    UNICODE,
                    "-cp",
                    Paths.get(token.getProtectionDomain().getCodeSource().getLocation().toURI()).toString(),
                    currPath.toString() + SLASH + pathToJavaFile
            );

            try (final JarOutputStream out = new JarOutputStream(Files.newOutputStream(jarFile), createManifest())) {
                out.putNextEntry(new ZipEntry(pathToClassFile));
                Files.copy(Paths.get(currPath.toString() + SLASH + pathToClassFile), out);
            } catch (final IOException e) {
                System.err.println("Problem with output file: " + e.getMessage());
            }
        } catch (final IOException e) {
            System.err.println("Cannot create a directory: " + e.getMessage());
        } catch (final URISyntaxException e) {
            System.err.println("Problem with URI generation: " + e.getMessage());
        }
    }

    /**
     * Main for Implementor to launch {@link #implement(Class, Path)} or {@link #implementJar(Class, Path)}.
     * If count of arguments is one then launch {@link #implement(Class, Path)}.
     * If count of arguments is three then launch {@link #implementJar(Class, Path)}.
     *
     * @param args: class name for {@link #implement(Class, Path)}
     *              or class name and path to write jar for {@link #implementJar(Class, Path)}.
     */
    public static void main(final String[] args) {
        try {
            final Implementor implementor = new Implementor();

            if (args.length == 1) {
                try {
                    final Class<?> token = Class.forName(args[0]);
                    final Path path = Path.of(token.getPackageName());
                    implementor.implement(token, path);
                } catch (final ImplerException e) {
                    System.err.println("Implementor failed");
                }
            } else if (args.length == 3) {
                final Class<?> token = Class.forName(args[1]);
                final Path path = Path.of(args[2]);
                try {
                    implementor.implementJar(token, path);
                } catch (final ImplerException e) {
                    System.err.println("JarImplementor failed");
                }
            } else {
                System.err.println("Wrong count of arguments");
            }
        } catch (final ClassNotFoundException e) {
            System.err.println("ClassNotFound");
        }
    }
}
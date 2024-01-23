package info.kgeorgiy.ja.okorochkova.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The class constructs the class and its methods, parse it in the {@link String} type.
 *
 * @author Maria Okorochkova (@maladetska)
 */
public class CodeConstructor {
    /**
     * No-symbol constant.
     */
    private final static String EMPTY = "";
    /**
     * One whitespace constant.
     */
    private final static char SPACE = ' ';
    /**
     * Tabulator constant.
     */
    private final static char TAB = '\t';
    /**
     * Line separator constant. Depends on the operating system.
     */
    private final static String LINE_SEPARATOR = System.lineSeparator();
    /**
     * Сomma constant. Implements with a whitespace after.
     */
    private final static String COMMA = ", ";
    /**
     * Semicolon constant. Implements with translation to the next line and indention after.
     */
    private final static String EXPRESSION_END = ";" + LINE_SEPARATOR;
    /**
     * Opening parenthesis constant.
     */
    private final static String LEFT_PARENTHESIS = "(";
    /**
     * Closing parenthesis constant. Implements with a whitespace after.
     */
    private final static String RIGHT_PARENTHESIS = ") ";
    /**
     * Opening brace constant. Implements with translation to the next line and indention after.
     */
    private final static String LEFT_BRACE = "{" + LINE_SEPARATOR + TAB;
    /**
     * Closing brace constant. Implements with translation to the next line after.
     */
    private final static String RIGHT_BRACE = "}" + LINE_SEPARATOR;
    /**
     * Word <var>return</var> constant. Implements with a whitespace after.
     */
    private final static String RETURN = "return ";
    /**
     * Word <var>public</var> constant. Implements with a whitespace after.
     */
    private final static String PUBLIC = "public ";
    /**
     * Word <var>implements</var> constant. Implements with a whitespace after.
     */
    private final static String IMPLEMENTS = "implements ";
    /**
     * Word <var>throws</var> constant. Implements with a whitespace after.
     */
    private final static String THROWS = "throws ";
    /**
     * Word <var>package</var> constant. Implements with a whitespace after.
     */
    private final static String PACKAGE = "package ";
    /**
     * Word <var>class</var> constant. Implements with a whitespace after.
     */
    private final static String INTERFACE = "class ";
    /**
     * Word <var>true</var> constant.
     */
    private final static String TRUE = "true";
    /**
     * Word <var>null</var> constant.
     */
    private final static String NULL = "null";
    /**
     * Symbol <var>0</var> constant.
     */
    private final static char ZERO = '0';

    /**
     * Path to the class.
     */
    private final Path pathToFile;
    /**
     * Class package.
     */
    private final String pack;
    /**
     * Class name.
     */
    private final String name;
    /**
     * Implementing interface of the class.
     */
    private final String implementingInterface;
    /**
     * Arrays of class methods.
     */
    private final Method[] methods;
    /**
     * Class written in {@link String} type.
     */
    private final String code;
    /**
     * Class written in unicode {@link String} type.
     */
    private final String codeInUnicode;

    /**
     * Construct code for the class and write it in {@link String} type to {@link #code},
     * also write the string result in unicode to {@link #codeInUnicode}.
     *
     * @param token type token to create implementation for.
     * @param root  root directory, where need to implement the class.
     * @throws ImplerException when {@code token} is not {@code interface} or {@code token} is {@code private}.
     */
    public CodeConstructor(final Class<?> token, final Path root) throws ImplerException {
        if (!token.isInterface()) {
            throw new ImplerException("Has to be interface.");
        } else if (Modifier.isPrivate(token.getModifiers())) {
            throw new ImplerException("Has to be public or protected interface.");
        }
        this.pathToFile = root;
        pack = token.getPackageName().length() != 0 ? token.getPackageName() : EMPTY;

        name = token.getSimpleName() + Implementor.IMPL;
        implementingInterface = token.getCanonicalName();
        methods = token.getMethods();

        code = constructInterface();

        byte[] charset = code.getBytes(StandardCharsets.UTF_8);
        codeInUnicode = new String(charset, StandardCharsets.UTF_8);
    }

    /**
     * Writing the unicode result from {@link #codeInUnicode} to {@link #pathToFile}.
     *
     * @throws ImplerException when cannot write the result in file.
     */
    public void writeCodeInFile() throws ImplerException {
        try (BufferedWriter out = Files.newBufferedWriter(pathToFile, StandardCharsets.UTF_8)) {
            out.write(this.getCodeInUnicode());
        } catch (final IOException e) {
            throw new ImplerException("Cannot write class: ", e);
        }
    }

    /**
     * Returns code of the class in unicode {@link String} type.
     *
     * @return string of {@link #codeInUnicode}.
     */
    public String getCodeInUnicode() {
        return codeInUnicode;
    }

    /**
     * Returns code of the class in {@link String} type.
     *
     * @return string of {@link #code}.
     */
    public String getCode() {
        return code;
    }

    /**
     * Constructs and returns the class to a {@link #code}.
     *
     * @return full class code: with the head and the body.
     */
    private String constructInterface() {
        StringBuilder methodsIntoInterface = new StringBuilder();
        for (final Method method : methods) {
            MethodConstructor currMethod = new MethodConstructor(method);
            methodsIntoInterface.append(currMethod.getMethod());
        }

        return createPackage() + createClassHead() + LEFT_BRACE + methodsIntoInterface + RIGHT_BRACE;
    }

    /**
     * Constructs and returns a package-string of the class.
     *
     * @return package-string and translation to the next line.
     */
    private String createPackage() {
        if (Objects.equals(pack, EMPTY)) {
            return pack;
        }
        return PACKAGE + pack + EXPRESSION_END + LINE_SEPARATOR;
    }

    /**
     * Constructs and returns the head of the class.
     *
     * @return a string of the form:
     * {@code public interface }
     * {@link #name}
     * {@code implements }
     * {@link #implementingInterface}
     */
    private String createClassHead() {
        return PUBLIC + INTERFACE + name + SPACE + IMPLEMENTS + implementingInterface + SPACE;
    }

    /**
     * The class constructs methods, parse it in the {@link String} type.
     */
    private static class MethodConstructor {
        /**
         * Method name.
         */
        private final String name;
        /**
         * Method modifier.
         */
        private final String modifier;
        /**
         * Method type.
         */
        private final Class<?> type;
        /**
         * Method arguments. Names of arguments are keys; Types of arguments are values.
         */
        private final Map<String, String> args;
        /**
         * Method arguments. Indexes of arguments are keys; Names of arguments are values.
         */
        private final Map<Integer, String> argsIdx;
        /**
         * Arguments number.
         */
        private final int countOfArgs;
        /**
         * Method exceptions.
         */
        private final Class<?>[] exceptions;
        /**
         * Exceptions number.
         */
        private final int exceptionsCount;
        /**
         * Method written in {@link String} type.
         */
        private final String methodCode;

        /**
         * Construct a code for the method and write it in {@link String} type to {@link #methodCode}.
         *
         * @param token type token to create implementation for.
         */
        private MethodConstructor(final Method token) {
            name = token.getName();
            modifier = Modifier.toString(token.getModifiers() & ~Modifier.ABSTRACT & ~Modifier.TRANSIENT);
            type = token.getReturnType();
            exceptions = token.getExceptionTypes();
            exceptionsCount = exceptions.length;

            argsIdx = new HashMap<>();
            args = new HashMap<>();
            int i = 0;
            for (Parameter parameter : token.getParameters()) {
                argsIdx.put(i, parameter.getName());
                args.put(parameter.getName(), parameter.getType().getCanonicalName());
                i++;
            }
            countOfArgs = i;

            methodCode = constructMethod();
        }

        /**
         * Returns code of the method in {@link String} type.
         *
         * @return string of {@link #methodCode}.
         */
        private String getMethod() {
            return methodCode;
        }

        /**
         * Constructs and returns method code to {@link #methodCode}.
         *
         * @return full method code: with the head and the body.
         */
        private String constructMethod() {
            return createMethodHead()
                    + LEFT_BRACE + TAB
                    + (type != void.class ? createReturn(type) + TAB : EMPTY)
                    + RIGHT_BRACE;
        }

        /**
         * Constructs and returns method return-string.
         * If type is {@code boolean} then returns {@link #TRUE};
         * if type is {@code byte}, {@code char}, {@code short},
         * {@code int}, {@code long}, {@code float} or {@code double}
         * then returns {@link #ZERO};
         * else returns {@link #NULL}.
         *
         * @param type type of method.
         * @return method return-string starts with <var>returns</var>.
         * @see java.lang.Boolean#TYPE
         * @see java.lang.Character#TYPE
         * @see java.lang.Byte#TYPE
         * @see java.lang.Short#TYPE
         * @see java.lang.Integer#TYPE
         * @see java.lang.Long#TYPE
         * @see java.lang.Float#TYPE
         * @see java.lang.Double#TYPE
         * @see java.lang.Void#TYPE
         */
        private static String createReturn(final Class<?> type) {
            return RETURN
                    + (type == boolean.class ? TRUE : (type.isPrimitive() ? ZERO : NULL))
                    + EXPRESSION_END;
        }

        /**
         * Constructs and returns the head of the method.
         *
         * @return a string of the form:
         * {@link #modifier}
         * {@link #type}
         * {@link #name}
         * {@code (}
         * {@link #args}
         * {@code ...)}
         * {@link #exceptions}
         */
        private String createMethodHead() {
            return modifier + SPACE + type.getCanonicalName() + SPACE
                    + name + writeArgs() + writeExceptions();
        }

        /**
         * Constructs and returns arguments list of the method.
         *
         * @return List of arguments in parentheses or empty parentheses.
         */
        private String writeArgs() {
            if (countOfArgs > 0) {
                StringBuilder p = new StringBuilder();
                for (int i = 0; i < countOfArgs - 1; i++) {
                    p.append(args.get(argsIdx.get(i)));
                    p.append(SPACE);
                    p.append(argsIdx.get(i));
                    p.append(COMMA);
                }
                p.append(args.get(argsIdx.get(countOfArgs - 1)));
                p.append(SPACE);
                p.append(argsIdx.get(countOfArgs - 1));

                return LEFT_PARENTHESIS + p + RIGHT_PARENTHESIS;
            }
            return LEFT_PARENTHESIS + RIGHT_PARENTHESIS;
        }

        /**
         * Constructs and returns exceptions list of the method.
         *
         * @return List of exceptions in parentheses or empty parentheses.
         */
        private String writeExceptions() {
            if (exceptionsCount > 0) {
                StringBuilder exceptions = new StringBuilder();
                for (int i = 0; i < exceptionsCount - 1; i++) {
                    exceptions.append(this.exceptions[i].getCanonicalName());
                    exceptions.append(COMMA);
                }
                exceptions.append(this.exceptions[exceptionsCount - 1].getCanonicalName());
                return SPACE + THROWS + exceptions + SPACE;
            }
            return EMPTY;
        }
    }
}

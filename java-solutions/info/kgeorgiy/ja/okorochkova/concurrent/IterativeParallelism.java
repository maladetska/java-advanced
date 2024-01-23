package info.kgeorgiy.ja.okorochkova.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ScalarIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Class implements {@link ScalarIP} interface.
 *
 * @author Maria Okorochkova (@maladetska)
 */
public class IterativeParallelism implements ScalarIP {
    /**
     * Mapper for working parallel mapping.
     */
    private final ParallelMapper parallelMapper;

    /**
     * Initializing {@link #parallelMapper} by the specified value.
     */
    public IterativeParallelism(ParallelMapper mapper) {
        this.parallelMapper = mapper;
    }

    /**
     * Initializing {@link #parallelMapper} .
     */
    public IterativeParallelism() {
        this.parallelMapper = null;
    }

    /**
     * Try join all threads.
     *
     * @param threadList all threads.
     * @throws InterruptedException if executing thread was interrupted.
     */
    public static void join(final List<Thread> threadList)
            throws InterruptedException {
        InterruptedException exc = null;
        for (int j = 0; j < threadList.size(); ) {
            Thread thread = threadList.get(j);
            try {
                thread.join();
                j++;
            } catch (InterruptedException e) {
                if (exc == null) {
                    exc = e;
                } else {
                    exc.addSuppressed(e);
                }

                for (int i = j; i < threadList.size(); i++) {
                    threadList.get(i).interrupt();
                }
            }
        }
        if (exc != null) {
            throw exc;
        }
    }

    /**
     * Split task for threads.
     *
     * @param valueSize  number of values.
     * @param threads    number of concurrent threads.
     * @param values     values to test.
     * @param resultList list of result values.
     * @param <T>        value type.
     */
    private static <T> void splitTasks(
            final int valueSize,
            final int threads,
            final List<? extends T> values,
            final List<Stream<? extends T>> resultList
    ) {
        final int div = valueSize / threads;
        final int mod = valueSize % threads;

        final int sizeChunk = (mod == 1 && threads != valueSize - 1)
                ? div + 1 :
                div;
        final int lastSizeChunk = (mod == 0)
                ? sizeChunk
                : valueSize - sizeChunk * (threads - 1);

        int l = 0;
        int r = sizeChunk;
        for (int i = 0; i < threads; i++) {
            final int finalL = l;
            final int finalR = r;

            resultList.set(i, values.subList(finalL, finalR).stream());

            l = (i + 1) * sizeChunk;
            r = (i + 2 != threads) ? (i + 2) * sizeChunk : l + lastSizeChunk;
        }
    }

    /**
     * Return result.
     *
     * @param streamUFunction stream with applying comparator for values.
     * @param values          values to test.
     * @param countOfThreads  number of concurrent threads.
     * @param <T>             value type.
     * @param <U>             result type.
     * @return result of given values.
     * @throws InterruptedException if executing thread was interrupted.
     */
    private <T, U> Stream<U> getAnswer(final Function<Stream<? extends T>, U> streamUFunction,
                                       final List<? extends T> values,
                                       final int countOfThreads) throws InterruptedException {
        final int valueSize = values.size();
        final int threads = (countOfThreads + valueSize > 2)
                ? Math.min(countOfThreads, valueSize)
                : 1;
        List<U> result = new ArrayList<>(Collections.nCopies(threads, null));

        List<Stream<? extends T>> splittedTasks = new ArrayList<>(Collections.nCopies(threads, null));
        splitTasks(valueSize, threads, values, splittedTasks);

        if (parallelMapper == null) {
            List<Thread> resultThreadList = new ArrayList<>(threads);
            for (int i = 0; i < threads; i++) {
                final int finalI = i;
                resultThreadList.add(new Thread(
                                () -> result.set(finalI,
                                        streamUFunction.apply(splittedTasks.get(finalI)))
                        )
                );
                resultThreadList.get(i).start();
            }
            join(resultThreadList);

            return result.stream();
        } else {
            return parallelMapper.map(streamUFunction, splittedTasks).stream();
        }
    }

    /**
     * Returns maximum value.
     *
     * @param threads    number of concurrent threads.
     * @param values     values to get maximum of.
     * @param comparator value comparator.
     * @param <T>        value type.
     * @return maximum of given values
     * @throws InterruptedException             if executing thread was interrupted.
     * @throws java.util.NoSuchElementException if no values are given.
     */
    public <T> T maximum(final int threads, final List<? extends T> values, final Comparator<? super T> comparator)
            throws InterruptedException {
        return getAnswer(stream -> stream.max(comparator).orElseThrow(), values, threads)
                .max(comparator).orElseThrow();
    }

    /**
     * Returns minimum value.
     *
     * @param threads    number of concurrent threads.
     * @param values     values to get minimum of.
     * @param comparator value comparator.
     * @param <T>        value type.
     * @return minimum of given values
     * @throws InterruptedException             if executing thread was interrupted.
     * @throws java.util.NoSuchElementException if no values are given.
     */
    @Override
    public <T> T minimum(final int threads, final List<? extends T> values, final Comparator<? super T> comparator)
            throws InterruptedException {
        return getAnswer(stream -> stream.min(comparator).orElseThrow(), values, threads)
                .min(comparator).orElseThrow();
    }

    /**
     * Returns whether all values satisfy predicate.
     *
     * @param threads   number of concurrent threads.
     * @param values    values to test.
     * @param predicate test predicate.
     * @param <T>       value type.
     * @return whether all values satisfy predicate or {@code true}, if no values are given.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> boolean all(final int threads, final List<? extends T> values, final Predicate<? super T> predicate)
            throws InterruptedException {
        return getAnswer(stream -> stream.allMatch(predicate), values, threads)
                .allMatch(Predicate.isEqual(true));
    }

    /**
     * Returns whether any of values satisfies predicate.
     *
     * @param threads   number of concurrent threads.
     * @param values    values to test.
     * @param predicate test predicate.
     * @param <T>       value type.
     * @return whether any value satisfies predicate or {@code false}, if no values are given.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> boolean any(final int threads, final List<? extends T> values, final Predicate<? super T> predicate)
            throws InterruptedException {
        return getAnswer(stream -> stream.anyMatch(predicate), values, threads)
                .anyMatch(Predicate.isEqual(true));
    }

    /**
     * Returns number of values satisfying predicate.
     *
     * @param threads   number of concurrent threads.
     * @param values    values to test.
     * @param predicate test predicate.
     * @param <T>       value type.
     * @return number of values satisfying predicate.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> int count(final int threads, final List<? extends T> values, final Predicate<? super T> predicate)
            throws InterruptedException {
        return getAnswer(stream -> stream.filter(predicate).toList(), values, threads)
                .mapToInt(Collection::size).sum();
    }

}
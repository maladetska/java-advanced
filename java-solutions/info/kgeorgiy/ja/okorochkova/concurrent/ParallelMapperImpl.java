package info.kgeorgiy.ja.okorochkova.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;

/**
 * Class implements {@link ParallelMapper} interface.
 *
 * @author Maria Okorochkova (@maladetska)
 */
public class ParallelMapperImpl implements ParallelMapper {
    /**
     * Threads with tasks into.
     */
    private final List<Thread> threads;
    /**
     * Tasks of {@link Runnable} type.
     */
    private final Queue<Runnable> queueOfTasks;

    /**
     * Get all {@link Runnable} vars and write it on {@link #queueOfTasks}.
     *
     * @throws InterruptedException if executing thread was interrupted.
     */
    private void runTaskInThread() throws InterruptedException {
        Runnable task;
        while (!Thread.currentThread().isInterrupted()) {
            synchronized (queueOfTasks) {
                while (queueOfTasks.isEmpty()) queueOfTasks.wait();
                task = queueOfTasks.poll();
            }
            task.run();
        }
    }

    /**
     * Constructor for {@link ParallelMapperImpl},
     *
     * @param countOfThreads number of threads that need to be created and worked on.
     */
    public ParallelMapperImpl(final int countOfThreads) {
        queueOfTasks = new ArrayDeque<>();
        threads = new ArrayList<>();

        for (int i = 0; i < countOfThreads; i++) {
            threads.add(new Thread(() -> {
                try {
                    runTaskInThread();
                } catch (InterruptedException ignored) {
                }
            }));

            threads.get(i).start();
        }
    }

    /**
     * Do {@code function} for every element in {@code args} in parallel.
     *
     * @param function function for testing values.
     * @param args     values to test.
     * @param <T>      value type.
     * @param <U>      result type.
     * @return values obtained by using the function.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T, U> List<U> map(final Function<? super T, ? extends U> function,
                              final List<? extends T> args) throws InterruptedException {

        final int count = args.size();
        List<U> resultList = new ArrayList<>(Collections.nCopies(count, null));
        WorkOutedTasks workOutedTasks = new WorkOutedTasks(count);

        for (int i = 0; i < count; i++) {
            final int finalI = i;
            synchronized (queueOfTasks) {
                queueOfTasks.add(
                        () -> {
                            try {
                                resultList.set(
                                        finalI,
                                        function.apply(args.get(finalI))
                                );
                                workOutedTasks.notifyTask();
                            } catch (RuntimeException e) {
                                workOutedTasks.setException(e);
                            }
                        });

                queueOfTasks.notify();
            }
        }

        workOutedTasks.waitEveryTask();
        if (workOutedTasks.checkException()) {
            throw new RuntimeException(workOutedTasks.getExceptionMessage());
        }
        return resultList;
    }

    /**
     * Close all threads that have not been closed.
     */
    @Override
    public void close() {
        for (Thread thread : threads) {
            thread.interrupt();
        }

        boolean checkUninterruptedThread = false;
        for (int j = 0; j < threads.size(); ) {
            Thread thread = threads.get(j);
            try {
                thread.join();
                j++;
            } catch (InterruptedException ignored) {
                checkUninterruptedThread = true;
            }
        }
        if (checkUninterruptedThread) Thread.currentThread().interrupt();
    }

    /**
     * Class for store count of tasks that have not yet worked.
     */
    public static class WorkOutedTasks {
        /**
         * Number of tasks that have not yet worked.
         */
        private int currNumberWorkerTasks;
        /**
         * Exception where the incident exception will be stored.
         */
        private RuntimeException exception;

        /**
         * Return message of {@link #exception}.
         *
         * @return {@link #exception}
         */
        String getExceptionMessage() {
            return exception.getMessage();
        }

        /**
         * Put exception in {@link #exception}.
         *
         * @param e new RuntimeException.
         */
        synchronized void setException(RuntimeException e) {
            if (exception == null) {
                exception = e;
            } else {
                exception.addSuppressed(e);
            }
        }

        /**
         * Check exception existence.
         *
         * @return true if {@link #exception} not null else false.
         */
        boolean checkException() {
            return exception.getCause() != null;
        }

        /**
         * Initializing the number of tasks that have not yet worked.
         * First, the number of all tasks will be written there.
         *
         * @param numberOfTasks initial number of task.
         */
        WorkOutedTasks(final int numberOfTasks) {
            exception = new RuntimeException();
            currNumberWorkerTasks = numberOfTasks;
        }

        /**
         * Wait for task.
         *
         * @throws InterruptedException if executing task was interrupted.
         */
        synchronized void waitEveryTask() throws InterruptedException {
            try {
                while (currNumberWorkerTasks > 0) {
                    wait();
                }
            } catch (InterruptedException e) {
                throw new InterruptedException(
                        "Error in wait for executing task was interrupted: "
                                + e.getMessage());
            }
        }

        /**
         * Notify next task.
         */
        synchronized void notifyTask() {
            currNumberWorkerTasks--;
            if (currNumberWorkerTasks == 0) {
                notify();
            }
        }
    }
}
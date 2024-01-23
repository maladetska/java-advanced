package info.kgeorgiy.ja.okorochkova.hello;

import java.util.concurrent.*;

class Common {
    protected static void checkArgs(final String[] args, final int maxNumberOfArgs) {
        if (args == null) {
            throw new IllegalArgumentException(
                    "There are no arguments. Please enter the post number or/and count of threads."
            );
        }
        if (args.length > maxNumberOfArgs) {
            throw new IllegalArgumentException(
                    "There are more then two arguments. Please enter the post number or/and count of threads."
            );
        }
        for (String arg : args) {
            if (arg == null) {
                throw new IllegalArgumentException("Please enter arguments correct.");
            }
        }
        try {
            for (String arg : args) {
                Integer.parseInt(arg);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Please enter args correct.");
        }
    }

    protected static void closeWorkers(final ExecutorService workers, long timeout) {
        workers.shutdown();
        try {
            if (!workers.awaitTermination(timeout, TimeUnit.SECONDS)) {
                workers.shutdownNow();
                if (!workers.awaitTermination(timeout, TimeUnit.SECONDS))
                    System.err.println("Workers did not terminate");
            }
        } catch (InterruptedException e) {
            workers.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    protected static void closeExecutorServiceClone(final ExecutorService es) {
        boolean terminated = es.isTerminated();
        if (!terminated) {
            es.shutdown();
            boolean interrupted = false;
            while (!terminated) {
                try {
                    terminated = es.awaitTermination(1L, TimeUnit.DAYS);
                } catch (InterruptedException e) {
                    if (!interrupted) {
                        es.shutdownNow();
                        interrupted = true;
                    }
                }
            }
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

package info.kgeorgiy.ja.okorochkova.crawler;

import java.util.concurrent.*;
import java.util.*;

import info.kgeorgiy.java.advanced.crawler.*;

import static java.util.concurrent.Executors.newFixedThreadPool;

import java.io.IOException;

/**
 * Class implements {@link Crawler} interface.
 *
 * @author Maria Okorochkova (@maladetska)
 */
public class WebCrawler implements Crawler {
    private static final int MAX_ITERS_FOR_CLOSE = 50;
    private static final double TIME_SCALE = 1;
    private final Downloader downloader;
    private final ExecutorService downloaders;
    private final ExecutorService extractors;
    private final int perHost;

    /**
     * Class for crawls websites.
     *
     * @param downloader  allows to download pages and extract links from them.
     * @param downloaders the maximum number of simultaneously loaded pages.
     * @param extractors  the maximum number of pages from which links are extracted at the same time.
     * @param perHost     the maximum number of pages loaded simultaneously from one host.
     *                    To determine the host, use the getHost method of the URLUtils class from the tests.
     */
    public WebCrawler(final Downloader downloader, final int downloaders, final int extractors, final int perHost) {
        this.downloader = downloader;
        this.downloaders = newFixedThreadPool(downloaders);
        this.extractors = newFixedThreadPool(extractors);
        this.perHost = perHost;
    }

    /**
     * Downloads website up to specified depth.
     *
     * @param url   start <a href="http://tools.ietf.org/html/rfc3986">URL</a>.
     * @param depth download depth.
     * @return download result.
     */
    @Override
    public Result download(final String url, final int depth) {
        // :NOTE: extract to class
        SiteDescent myDownloader = new SiteDescent();

        return myDownloader.download(url, depth);
    }

    /**
     * Closes this web-crawler, relinquishing any allocated resources.
     */
    @Override
    public void close() {
        // https://docs.oracle.com/en/java/javase/19/docs/api/java.base/java/util/concurrent/ExecutorService.html
        int i = 0;
        while (!(downloaders.isShutdown() && extractors.isShutdown())
                && i < MAX_ITERS_FOR_CLOSE) {
            i++;
            downloaders.shutdownNow();
            extractors.shutdownNow();
        }
        if (i == MAX_ITERS_FOR_CLOSE) {
            if (!downloaders.isShutdown()) {
                System.err.println("Cannot shut down downloaders");
            }
            if (!extractors.isShutdown()) {
                System.err.println("Cannot shut down extractors");
            }
        }
    }

    private static void checkArgs(final String[] args) {
        if (args == null) {
            throw new IllegalArgumentException("There are no arguments.");
        }
        if (args.length > 5) {
            throw new IllegalArgumentException("There are more then five arguments");
        } else if (args.length < 1) {
            throw new IllegalArgumentException("There are less then two arguments.");
        }
        for (String arg : args) {
            if (arg == null) {
                throw new IllegalArgumentException("Please enter arguments correct.");
            }
        }
    }

    /**
     * Get the command line, create new WebCrawler and surf sites in depth.
     *
     * @param args args-type: "url [depth [downloads [extractors [perHost]]]]"
     */
    public static void main(String[] args) {
        // :NOTE: WebCrawler url [depth [downloads [extractors [perHost]]]]
        try {
            checkArgs(args);
            try (Crawler webCrawler = new WebCrawler(
                    new CachingDownloader(TIME_SCALE),
                    args.length > 2 ? Integer.parseInt(args[2]) : 1,
                    args.length > 3 ? Integer.parseInt(args[3]) : 1,
                    args.length > 4 ? Integer.parseInt(args[4]) : 1)) {
                webCrawler.download(args[0], args.length > 1 ? Integer.parseInt(args[1]) : 1);
                // print Result
            }
        } catch (IOException e) {
            System.err.println("Problem in CachingDownloader");
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }
    }

    private class SiteDescent {
        private final Map<String, IOException> exceptions;
        private final Set<String> downloaded;
        private final Set<String> extracted;
        private final Set<String> visited;
        private final Phaser phaser;

        private SiteDescent() {
            this.exceptions = new ConcurrentHashMap<>();
            this.downloaded = ConcurrentHashMap.newKeySet();
            this.extracted = ConcurrentHashMap.newKeySet();
            this.visited = ConcurrentHashMap.newKeySet();
            this.phaser = new Phaser(1);
        }

        private Result download(final String url, final int depth) {
            final Set<String> pagesForWait = ConcurrentHashMap.newKeySet();
            pagesForWait.add(url);
            visited.add(url);
            for (int i = 0; i < depth; i++) {
                if (i != 0) {
                    pagesForWait.clear();
                    pagesForWait.addAll(extracted);
                    extracted.clear();
                }
                for (final String u : pagesForWait) {
                    launchDownload(u, depth - i);
                }
                phaser.arriveAndAwaitAdvance();
            }

            return new Result(new ArrayList<>(downloaded), exceptions);
        }

        private void launchDownload(final String url, final int depth) {
            phaser.register();
            downloaders.submit(newDownloaders(url, depth));
        }

        private Runnable newDownloaders(final String url, final int depth) {
            return () -> {
                try {
                    Document page = downloader.download(url);
                    downloaded.add(url);
                    if (depth != 1) {
                        phaser.register();
                        extractors.submit(newExtractors(page));
                    }
                } catch (final IOException e) {
                    exceptions.put(url, e);
                } finally {
                    phaser.arriveAndDeregister();
                }
            };
        }

        private Runnable newExtractors(final Document page) {
            return () -> {
                try {
                    for (String u : page.extractLinks()) {
                        if (!visited.contains(u)) {
                            visited.add(u);
                            extracted.add(u);
                        }
                    }
                } catch (final IOException e) {
                    System.err.println("Problem with extracts links from downloaded URL: " + e.getMessage());
                } finally {
                    phaser.arriveAndDeregister();
                }
            };
        }

    }
}
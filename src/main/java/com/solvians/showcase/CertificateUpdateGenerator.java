package com.solvians.showcase;

import com.solvians.showcase.task.CertificateUpdateTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Stream;

/**
 * Generates certificate updates concurrently.
 */
public class CertificateUpdateGenerator {

    private final int threads;
    private final int quotes;

    public CertificateUpdateGenerator(int threads, int quotes) {
        if (threads <= 0) {
            throw new IllegalArgumentException(
                    "Number of threads must be positive");
        }

        if (quotes < 0) {
            throw new IllegalArgumentException(
                    "Number of quotes must not be negative");
        }

        this.threads = threads;
        this.quotes = quotes;
    }

    /**
     * Generates certificate update objects.
     */
    public Stream<CertificateUpdate> generateQuotes() {
        CertificateUpdateTask task = new CertificateUpdateTask();
        return generate(task::generateUpdate);
    }

    /**
     * Generates certificate updates in CSV format.
     */
    public Stream<String> generateCsvLines() {
        return generate(new CertificateUpdateTask());
    }

    private <T> Stream<T> generate(Callable<T> task) {
        if (quotes == 0) {
            return Stream.empty();
        }

        ExecutorService executor =
                Executors.newFixedThreadPool(threads);

        try {
            List<Callable<List<T>>> tasks = new ArrayList<>();

            for (int i = 0; i < threads; i++) {
                tasks.add(() -> {
                    List<T> batch = new ArrayList<>();

                    for (int j = 0; j < quotes; j++) {
                        if (Thread.currentThread().isInterrupted()) {
                            throw new InterruptedException();
                        }

                        batch.add(task.call());
                    }

                    return batch;
                });
            }

            List<T> results = new ArrayList<>();

            for (Future<List<T>> future : executor.invokeAll(tasks)) {
                results.addAll(future.get());
            }

            return results.stream();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Certificate update generation interrupted", e);

        } catch (ExecutionException e) {
            throw new IllegalStateException(
                    "Certificate update generation failed", e.getCause());

        } finally {
            executor.shutdownNow();
        }
    }
}
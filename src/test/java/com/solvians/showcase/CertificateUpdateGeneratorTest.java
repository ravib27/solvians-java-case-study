package com.solvians.showcase;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CertificateUpdateGeneratorTest {

    @Test
    public void generateQuotes() {
        CertificateUpdateGenerator certificateUpdateGenerator = new CertificateUpdateGenerator(10, 100);
        Stream<CertificateUpdate> quotes = certificateUpdateGenerator.generateQuotes();
        assertNotNull(quotes);
        assertEquals(10 * 100, quotes.count());

    }

    @Test
    void shouldGenerateQuotesUsingSingleThread() {
        CertificateUpdateGenerator generator =
                new CertificateUpdateGenerator(1, 100);

        assertEquals(100, generator.generateQuotes().count());
    }

    @Test
    void shouldGenerateExpectedNumberOfCsvLines() {
        CertificateUpdateGenerator generator =
                new CertificateUpdateGenerator(10, 100);

        assertEquals(1_000, generator.generateCsvLines().count());
    }

    @Test
    void shouldGenerateValidCsvLines() {
        CertificateUpdateGenerator generator =
                new CertificateUpdateGenerator(4, 25);

        List<String> results = generator.generateCsvLines()
                .collect(Collectors.toList());

        assertEquals(100, results.size());

        for (String result : results) {
            assertNotNull(result);

            String[] fields = result.split(",", -1);

            assertEquals(6, fields.length);

            for (String field : fields) {
                assertFalse(field.isBlank());
            }
        }
    }

    @Test
    void shouldGenerateNonNullCertificateUpdates() {
        CertificateUpdateGenerator generator =
                new CertificateUpdateGenerator(4, 25);

        List<CertificateUpdate> updates = generator.generateQuotes()
                .collect(Collectors.toList());

        assertEquals(100, updates.size());
        assertTrue(updates.stream().allMatch(
                Objects::nonNull));
    }

    @Test
    void shouldReturnEmptyStreamForZeroQuotes() {
        CertificateUpdateGenerator generator =
                new CertificateUpdateGenerator(4, 0);

        assertEquals(0, generator.generateQuotes().count());
        assertEquals(0, generator.generateCsvLines().count());
    }

    @Test
    void shouldRejectZeroThreads() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CertificateUpdateGenerator(0, 100));
    }

    @Test
    void shouldRejectNegativeThreads() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CertificateUpdateGenerator(-1, 100));
    }

    @Test
    void shouldRejectNegativeQuotes() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CertificateUpdateGenerator(4, -1));
    }

    @Test
    void shouldSupportRepeatedGeneration() {
        CertificateUpdateGenerator generator =
                new CertificateUpdateGenerator(2, 10);

        assertEquals(20, generator.generateQuotes().count());
        assertEquals(20, generator.generateQuotes().count());
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void shouldPreserveInterruptStatusWhenGenerationIsInterrupted() {
        CertificateUpdateGenerator generator =
                new CertificateUpdateGenerator(2, 10);

        try {
            Thread.currentThread().interrupt();

            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    generator::generateQuotes);

            assertInstanceOf(InterruptedException.class, exception.getCause());

            assertTrue(Thread.currentThread().isInterrupted());

        } finally {
            // Clear the interrupt status to avoid affecting subsequent tests.
            Thread.interrupted();
        }
    }
}
package com.solvians.showcase;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class AppTest {

    // Original supplied test.
    @Test
    public void expectTwoIntArgs() {
        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> {
            App.main(new String[]{"xxx"});
        });
        NumberFormatException numbers = Assertions.assertThrows(NumberFormatException.class, () -> {
            App.main(new String[]{"xxx", "zzz"});
        });
        numbers = Assertions.assertThrows(NumberFormatException.class, () -> {
            App.main(new String[]{"10", "zzz"});
        });
        assertEquals("For input string: \"zzz\"", numbers.getMessage());
    }

    @Test
    void shouldPrintSingleCertificateUpdate() {
        String output = captureOutput("1", "1");

        assertEquals(1, output.lines().count());
    }

    @Test
    void shouldPrintExpectedNumberOfCertificateUpdates() {
        String output = captureOutput("10", "100");

        assertEquals(1_000, output.lines().count());
    }

    @Test
    void shouldPrintCertificateUpdatesInCsvFormat() {
        String output = captureOutput("4", "25");

        assertEquals(100, output.lines().count());

        output.lines().forEach(line -> {
            assertTrue(line.matches(
                    "\\d+,[A-Z]{2}[A-Z0-9]{9}[0-9],"
                            + "\\d+\\.\\d{2},\\d+,"
                            + "\\d+\\.\\d{2},\\d+"));
        });
    }

    @Test
    void shouldPrintNothingWhenQuotesAreZero() {
        String output = captureOutput("4", "0");

        assertTrue(output.isEmpty());
    }

    @Test
    void shouldRejectMissingArguments() {
        assertThrows(
                IllegalArgumentException.class,
                () -> App.main(new String[]{}));
    }

    @Test
    void shouldRejectZeroThreads() {
        assertThrows(
                IllegalArgumentException.class,
                () -> App.main(new String[]{"0", "100"}));
    }

    @Test
    void shouldRejectNegativeThreads() {
        assertThrows(
                IllegalArgumentException.class,
                () -> App.main(new String[]{"-1", "100"}));
    }

    @Test
    void shouldRejectNegativeQuotes() {
        assertThrows(
                IllegalArgumentException.class,
                () -> App.main(new String[]{"4", "-1"}));
    }

    private String captureOutput(String... args) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (PrintStream printStream =
                     new PrintStream(output, true, StandardCharsets.UTF_8)) {

            System.setOut(printStream);

            App.main(args);

            return output.toString(StandardCharsets.UTF_8);

        } finally {
            System.setOut(originalOut);
        }
    }
}
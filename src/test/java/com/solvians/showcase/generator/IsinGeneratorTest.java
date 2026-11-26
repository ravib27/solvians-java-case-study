
package com.solvians.showcase.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class IsinGeneratorTest {

    private final IsinGenerator generator = new IsinGenerator();


    @ParameterizedTest
    @CsvSource({
            "DE123456789, 6",
            "US037833100, 5",
            "US594918104, 5",
            "GB000263494, 6",
            "US02079K305, 9",
            "AA000000006, 3",
            "AA000000003, 0",
            "AAAAAAAAAAA, 9",
            "ZZZZZZZZZZZ, 6",
            "ZZ999999999, 7",
            "AZ0Z9A1B2C3, 5"
    })
    void shouldCalculateCorrectCheckDigit(
            String baseIsin, int expectedCheckDigit) {

        int actualCheckDigit =
                generator.calculateCheckDigit(baseIsin);

        assertEquals(expectedCheckDigit, actualCheckDigit);
    }


    @Test
    void shouldGenerateIsinWithCorrectLength() {
        String isin = generator.generate();

        assertEquals(12, isin.length());
    }

    @Test
    void shouldGenerateIsinWithCorrectFormat() {
        String isin = generator.generate();

        assertTrue(isin.matches("[A-Z]{2}[A-Z0-9]{9}[0-9]"));
    }

    @Test
    void shouldGenerateIsinsWithValidCheckDigits() {
        for (int i = 0; i < 1000; i++) {
            String isin = generator.generate();

            assertTrue(
                    isin.matches("[A-Z]{2}[A-Z0-9]{9}[0-9]"),
                    "Invalid ISIN format: " + isin);

            int expectedCheckDigit =
                    generator.calculateCheckDigit(isin.substring(0, 11));

            int actualCheckDigit = isin.charAt(11) - '0';

            assertEquals(expectedCheckDigit, actualCheckDigit,
                    "Invalid check digit for ISIN: " + isin);
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "DE123",
            "DE1234567896",
            "12123456789",
            "de123456789",
            "DE12345@789",
            "DE12345 789",
            "DE12345678a"
    })
    void shouldRejectInvalidBaseIsin(String baseIsin) {
        assertThrows(IllegalArgumentException.class,
                () -> generator.calculateCheckDigit(baseIsin));
    }
}

package com.solvians.showcase.generator;

import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

/**
 * Generates random ISINs with a valid check digit.
 * This class is stateless and thread-safe.
 */
public final class IsinGenerator {

    private static final String LETTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final String ALPHANUMERIC =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final int PREFIX_LENGTH = 2;
    private static final int IDENTIFIER_LENGTH = 9;
    private static final int ISIN_LENGTH = 12;

    private static final Pattern BASE_ISIN_PATTERN =
            Pattern.compile("[A-Z]{2}[A-Z0-9]{9}");

    /**
     * Generates a 12-character ISIN consisting of two uppercase
     * letters, nine alphanumeric characters and a check digit.
     *
     * @return generated ISIN
     */
    public String generate() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        StringBuilder isin = new StringBuilder(ISIN_LENGTH);

        for (int i = 0; i < PREFIX_LENGTH; i++) {
            isin.append(LETTERS.charAt(
                    random.nextInt(LETTERS.length())));
        }

        for (int i = 0; i < IDENTIFIER_LENGTH; i++) {
            isin.append(ALPHANUMERIC.charAt(
                    random.nextInt(ALPHANUMERIC.length())));
        }

        int checkDigit = calculateCheckDigit(isin.toString());

        return isin.append(checkDigit).toString();
    }

    /**
     * Calculates the ISIN check digit using the Luhn algorithm.
     *
     * @param baseIsin the first 11 characters of an ISIN
     * @return check digit between 0 and 9
     * @throws IllegalArgumentException if the base ISIN is invalid
     */
    public int calculateCheckDigit(String baseIsin) {
        if (baseIsin == null
                || !BASE_ISIN_PATTERN.matcher(baseIsin).matches()) {
            throw new IllegalArgumentException(
                    "Base ISIN must contain two uppercase letters "
                            + "followed by nine alphanumeric characters");
        }

        StringBuilder digits = new StringBuilder(baseIsin.length() * 2);

        // Convert letters to their numeric equivalents: A=10, Z=35.
        for (char character : baseIsin.toCharArray()) {
            if (character >= 'A' && character <= 'Z') {
                digits.append(character - 'A' + 10);
            } else {
                digits.append(character);
            }
        }

        int sum = 0;
        boolean doubleDigit = true;

        // Starting from the right, double every alternate digit.
        for (int i = digits.length() - 1; i >= 0; i--) {
            int digit = digits.charAt(i) - '0';

            if (doubleDigit) {
                digit *= 2;
            }

            // Add individual digits of the resulting number.
            sum += digit / 10 + digit % 10;

            doubleDigit = !doubleDigit;
        }

        return (10 - sum % 10) % 10;
    }
}

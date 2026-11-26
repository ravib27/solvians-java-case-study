package com.solvians.showcase;

import com.solvians.showcase.generator.IsinGenerator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

/**
 * Immutable representation of a certificate update.
 */
public final class CertificateUpdate {

    private static final int PRICE_SCALE = 2;

    private static final int MIN_PRICE_CENTS = 10_000;
    private static final int MAX_PRICE_CENTS = 20_000;

    private static final BigDecimal MIN_PRICE =
            BigDecimal.valueOf(MIN_PRICE_CENTS, PRICE_SCALE);

    private static final BigDecimal MAX_PRICE =
            BigDecimal.valueOf(MAX_PRICE_CENTS, PRICE_SCALE);

    private static final int MIN_SIZE = 1_000;
    private static final int MAX_BID_SIZE = 5_000;
    private static final int MAX_ASK_SIZE = 10_000;

    private static final Pattern ISIN_PATTERN =
            Pattern.compile("[A-Z]{2}[A-Z0-9]{9}[0-9]");

    private static final IsinGenerator ISIN_GENERATOR =
            new IsinGenerator();

    private final long timestamp;
    private final String isin;
    private final BigDecimal bidPrice;
    private final int bidSize;
    private final BigDecimal askPrice;
    private final int askSize;

    /**
     * Creates a randomly generated certificate update.
     */
    public CertificateUpdate() {
        this(
                System.currentTimeMillis(),
                ISIN_GENERATOR.generate(),
                generatePrice(),
                ThreadLocalRandom.current().nextInt(
                        MIN_SIZE, MAX_BID_SIZE + 1),
                generatePrice(),
                ThreadLocalRandom.current().nextInt(
                        MIN_SIZE, MAX_ASK_SIZE + 1)
        );
    }

    /**
     * Creates a certificate update with explicitly supplied values.
     */
    public CertificateUpdate(
            long timestamp,
            String isin,
            BigDecimal bidPrice,
            int bidSize,
            BigDecimal askPrice,
            int askSize) {

        if (timestamp < 0) {
            throw new IllegalArgumentException(
                    "Timestamp must not be negative");
        }

        if (isin == null || !ISIN_PATTERN.matcher(isin).matches()) {
            throw new IllegalArgumentException(
                    "Invalid ISIN format");
        }

        this.timestamp = timestamp;
        this.isin = isin;
        this.bidPrice = validatePrice(bidPrice, "Bid price");
        this.bidSize = validateSize(bidSize, MAX_BID_SIZE, "Bid size");
        this.askPrice = validatePrice(askPrice, "Ask price");
        this.askSize = validateSize(askSize, MAX_ASK_SIZE, "Ask size");
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getIsin() {
        return isin;
    }

    public BigDecimal getBidPrice() {
        return bidPrice;
    }

    public int getBidSize() {
        return bidSize;
    }

    public BigDecimal getAskPrice() {
        return askPrice;
    }

    public int getAskSize() {
        return askSize;
    }

    /**
     * Returns the certificate update in CSV format.
     */
    public String toCsv() {
        return timestamp + ","
                + isin + ","
                + bidPrice.toPlainString() + ","
                + bidSize + ","
                + askPrice.toPlainString() + ","
                + askSize;
    }

    @Override
    public String toString() {
        return toCsv();
    }

    private static BigDecimal generatePrice() {
        int cents = ThreadLocalRandom.current()
                .nextInt(MIN_PRICE_CENTS, MAX_PRICE_CENTS + 1);

        return BigDecimal.valueOf(cents, PRICE_SCALE);
    }

    private static BigDecimal validatePrice(
            BigDecimal price, String fieldName) {

        if (price == null
                || price.compareTo(MIN_PRICE) < 0
                || price.compareTo(MAX_PRICE) > 0) {

            throw new IllegalArgumentException(
                    fieldName + " must be between "
                            + MIN_PRICE + " and " + MAX_PRICE);
        }

        try {
            return price.setScale(
                    PRICE_SCALE, RoundingMode.UNNECESSARY);

        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(
                    fieldName + " must not exceed two decimal places", e);
        }
    }

    private static int validateSize(
            int size, int maxSize, String fieldName) {

        if (size < MIN_SIZE || size > maxSize) {
            throw new IllegalArgumentException(
                    fieldName + " must be between "
                            + MIN_SIZE + " and " + maxSize);
        }

        return size;
    }
}
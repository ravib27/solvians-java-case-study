package com.solvians.showcase;

import com.solvians.showcase.generator.IsinGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CertificateUpdateTest {

    private static final long TIMESTAMP = 1352122280502L;
    private static final String ISIN = "DE1234567896";

    private static final BigDecimal BID_PRICE =
            new BigDecimal("101.23");

    private static final BigDecimal ASK_PRICE =
            new BigDecimal("103.45");

    private static final int BID_SIZE = 1_000;
    private static final int ASK_SIZE = 1_000;

    private static final String EXPECTED_CSV =
            "1352122280502,DE1234567896,101.23,1000,103.45,1000";

    @Test
    void shouldCreateCertificateUpdateWithExpectedValues() {
        CertificateUpdate update = createValidUpdate();

        assertAll(
                () -> assertEquals(TIMESTAMP, update.getTimestamp()),
                () -> assertEquals(ISIN, update.getIsin()),
                () -> assertEquals(BID_PRICE, update.getBidPrice()),
                () -> assertEquals(BID_SIZE, update.getBidSize()),
                () -> assertEquals(ASK_PRICE, update.getAskPrice()),
                () -> assertEquals(ASK_SIZE, update.getAskSize())
        );
    }

    @Test
    void shouldFormatCertificateUpdateAsCsv() {
        CertificateUpdate update = createValidUpdate();

        assertEquals(EXPECTED_CSV, update.toCsv());
    }

    @Test
    void shouldReturnCsvRepresentationFromToString() {
        CertificateUpdate update = createValidUpdate();

        assertEquals(EXPECTED_CSV, update.toString());
    }

    @ParameterizedTest
    @CsvSource({
            "100.00, 1000, 100.00, 1000",
            "200.00, 5000, 200.00, 10000",
            "100.00, 5000, 200.00, 1000",
            "200.00, 1000, 100.00, 10000"
    })
    void shouldAcceptInclusiveBoundaryValues(
            String bidPrice,
            int bidSize,
            String askPrice,
            int askSize) {

        CertificateUpdate update = new CertificateUpdate(
                TIMESTAMP,
                ISIN,
                new BigDecimal(bidPrice),
                bidSize,
                new BigDecimal(askPrice),
                askSize
        );

        assertAll(
                () -> assertEquals(
                        new BigDecimal(bidPrice), update.getBidPrice()),
                () -> assertEquals(bidSize, update.getBidSize()),
                () -> assertEquals(
                        new BigDecimal(askPrice), update.getAskPrice()),
                () -> assertEquals(askSize, update.getAskSize())
        );
    }

    @ParameterizedTest
    @CsvSource({
            "100, 100.00",
            "100.5, 100.50",
            "101.230, 101.23",
            "200, 200.00"
    })
    void shouldNormalizePriceToTwoDecimalPlaces(
            String input, String expected) {

        BigDecimal price = new BigDecimal(input);

        CertificateUpdate update = new CertificateUpdate(
                TIMESTAMP, ISIN, price, BID_SIZE, price, ASK_SIZE
        );

        assertAll(
                () -> assertEquals(
                        new BigDecimal(expected), update.getBidPrice()),
                () -> assertEquals(
                        new BigDecimal(expected), update.getAskPrice())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "99.99",
            "200.01",
            "101.234",
            "-100.00"
    })
    void shouldRejectInvalidBidPrice(String price) {
        assertThrows(IllegalArgumentException.class,
                () -> new CertificateUpdate(
                        TIMESTAMP,
                        ISIN,
                        new BigDecimal(price),
                        BID_SIZE,
                        ASK_PRICE,
                        ASK_SIZE
                ));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "99.99",
            "200.01",
            "103.456",
            "-100.00"
    })
    void shouldRejectInvalidAskPrice(String price) {
        assertThrows(IllegalArgumentException.class,
                () -> new CertificateUpdate(
                        TIMESTAMP,
                        ISIN,
                        BID_PRICE,
                        BID_SIZE,
                        new BigDecimal(price),
                        ASK_SIZE
                ));
    }

    @Test
    void shouldRejectNullPrices() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new CertificateUpdate(
                                TIMESTAMP, ISIN,
                                null, BID_SIZE,
                                ASK_PRICE, ASK_SIZE
                        )),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new CertificateUpdate(
                                TIMESTAMP, ISIN,
                                BID_PRICE, BID_SIZE,
                                null, ASK_SIZE
                        ))
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {999, 5001, -1})
    void shouldRejectInvalidBidSize(int bidSize) {
        assertThrows(IllegalArgumentException.class,
                () -> new CertificateUpdate(
                        TIMESTAMP, ISIN,
                        BID_PRICE, bidSize,
                        ASK_PRICE, ASK_SIZE
                ));
    }

    @ParameterizedTest
    @ValueSource(ints = {999, 10001, -1})
    void shouldRejectInvalidAskSize(int askSize) {
        assertThrows(IllegalArgumentException.class,
                () -> new CertificateUpdate(
                        TIMESTAMP, ISIN,
                        BID_PRICE, BID_SIZE,
                        ASK_PRICE, askSize
                ));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "DE123",
            "DE123456789",
            "de1234567896",
            "DE123456789A",
            "121234567896",
            "DE12345@7896"
    })
    void shouldRejectInvalidIsinFormat(String isin) {
        assertThrows(IllegalArgumentException.class,
                () -> new CertificateUpdate(
                        TIMESTAMP, isin,
                        BID_PRICE, BID_SIZE,
                        ASK_PRICE, ASK_SIZE
                ));
    }

    @Test
    void shouldRejectNegativeTimestamp() {
        assertThrows(IllegalArgumentException.class,
                () -> new CertificateUpdate(
                        -1L, ISIN,
                        BID_PRICE, BID_SIZE,
                        ASK_PRICE, ASK_SIZE
                ));
    }

    @Test
    void shouldAcceptEpochTimestamp() {
        CertificateUpdate update = new CertificateUpdate(
                0L, ISIN,
                BID_PRICE, BID_SIZE,
                ASK_PRICE, ASK_SIZE
        );

        assertEquals(0L, update.getTimestamp());
    }

    @Test
    void shouldFormatCsvWithoutGroupingSeparators() {
        CertificateUpdate update = new CertificateUpdate(
                TIMESTAMP,
                ISIN,
                new BigDecimal("100"),
                5_000,
                new BigDecimal("200"),
                10_000
        );

        assertEquals(
                "1352122280502,DE1234567896,100.00,5000,200.00,10000",
                update.toCsv()
        );
    }


    @Test
    void shouldGenerateUpdateUsingDefaultConstructor() {
        long before = System.currentTimeMillis();

        CertificateUpdate update = new CertificateUpdate();

        long after = System.currentTimeMillis();

        assertNotNull(update);

        assertTrue(update.getTimestamp() >= before);
        assertTrue(update.getTimestamp() <= after);

        assertNotNull(update.getIsin());
        assertTrue(update.getIsin().matches(
                "[A-Z]{2}[A-Z0-9]{9}[0-9]"));

        assertTrue(update.getBidPrice().compareTo(
                new BigDecimal("100.00")) >= 0);

        assertTrue(update.getBidPrice().compareTo(
                new BigDecimal("200.00")) <= 0);

        assertTrue(update.getAskPrice().compareTo(
                new BigDecimal("100.00")) >= 0);

        assertTrue(update.getAskPrice().compareTo(
                new BigDecimal("200.00")) <= 0);

        assertEquals(2, update.getBidPrice().scale());
        assertEquals(2, update.getAskPrice().scale());

        assertTrue(update.getBidSize() >= 1_000);
        assertTrue(update.getBidSize() <= 5_000);

        assertTrue(update.getAskSize() >= 1_000);
        assertTrue(update.getAskSize() <= 10_000);
    }

    @Test
    void shouldGenerateValidIsinCheckDigit() {
        CertificateUpdate update = new CertificateUpdate();

        String isin = update.getIsin();

        int expectedCheckDigit = new IsinGenerator()
                .calculateCheckDigit(isin.substring(0, 11));

        int actualCheckDigit = isin.charAt(11) - '0';

        assertEquals(expectedCheckDigit, actualCheckDigit);
    }

    @Test
    void shouldGenerateSixCsvFieldsUsingDefaultConstructor() {
        CertificateUpdate update = new CertificateUpdate();

        String[] fields = update.toCsv().split(",", -1);

        assertEquals(6, fields.length);

        assertEquals(
                String.valueOf(update.getTimestamp()), fields[0]);

        assertEquals(update.getIsin(), fields[1]);
        assertEquals(update.getBidPrice().toPlainString(), fields[2]);
        assertEquals(String.valueOf(update.getBidSize()), fields[3]);
        assertEquals(update.getAskPrice().toPlainString(), fields[4]);
        assertEquals(String.valueOf(update.getAskSize()), fields[5]);
    }


    private CertificateUpdate createValidUpdate() {
        return new CertificateUpdate(
                TIMESTAMP,
                ISIN,
                BID_PRICE,
                BID_SIZE,
                ASK_PRICE,
                ASK_SIZE
        );
    }
}
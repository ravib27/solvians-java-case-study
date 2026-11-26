package com.solvians.showcase.task;

import com.solvians.showcase.CertificateUpdate;
import com.solvians.showcase.generator.IsinGenerator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;

class CertificateUpdateTaskTest {

    private final CertificateUpdateTask task =
            new CertificateUpdateTask();

    @Test
    void shouldImplementCallable() throws Exception {

        assertNotNull(((Callable<String>) task).call());
    }

    @Test
    void shouldGenerateSixCsvFields() {
        String result = task.call();

        assertNotNull(result);
        assertEquals(6, result.split(",", -1).length);
    }

    @Test
    void shouldGenerateCertificateUpdate() {
        CertificateUpdate update = task.generateUpdate();

        assertNotNull(update);
        assertNotNull(update.getIsin());
        assertNotNull(update.getBidPrice());
        assertNotNull(update.getAskPrice());
    }

    @Test
    void shouldGenerateCurrentTimestamp() {
        long before = System.currentTimeMillis();

        String[] fields = task.call().split(",");

        long after = System.currentTimeMillis();
        long timestamp = Long.parseLong(fields[0]);

        assertTrue(timestamp >= before);
        assertTrue(timestamp <= after);
    }

    @Test
    void shouldGenerateValidIsin() {
        String isin = task.call().split(",")[1];

        assertTrue(isin.matches(
                "[A-Z]{2}[A-Z0-9]{9}[0-9]"));

        int expectedCheckDigit = new IsinGenerator()
                .calculateCheckDigit(isin.substring(0, 11));

        assertEquals(expectedCheckDigit, isin.charAt(11) - '0');
    }

    @Test
    void shouldGenerateValidPricesAndSizes() {
        for (int i = 0; i < 1_000; i++) {
            String[] fields = task.call().split(",");

            BigDecimal bidPrice = new BigDecimal(fields[2]);
            int bidSize = Integer.parseInt(fields[3]);

            BigDecimal askPrice = new BigDecimal(fields[4]);
            int askSize = Integer.parseInt(fields[5]);

            assertTrue(bidPrice.compareTo(
                    new BigDecimal("100.00")) >= 0);

            assertTrue(bidPrice.compareTo(
                    new BigDecimal("200.00")) <= 0);

            assertTrue(askPrice.compareTo(
                    new BigDecimal("100.00")) >= 0);

            assertTrue(askPrice.compareTo(
                    new BigDecimal("200.00")) <= 0);

            assertEquals(2, bidPrice.scale());
            assertEquals(2, askPrice.scale());

            assertTrue(bidSize >= 1_000 && bidSize <= 5_000);
            assertTrue(askSize >= 1_000 && askSize <= 10_000);
        }
    }
}
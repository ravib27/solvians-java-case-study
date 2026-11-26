
package com.solvians.showcase;

import java.util.Arrays;

/**
 * Entry point for the certificate update feed generator.
 */
public class App {

    public App(String threads, String quotes) {
        // Retained for backward compatibility.
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException(
                    "Expect at least number of threads and number of quotes. "
                            + "But got: " + Arrays.toString(args));
        }

        int threads = Integer.parseInt(args[0]);
        int quotes = Integer.parseInt(args[1]);

        CertificateUpdateGenerator certificateUpdateGenerator =
                new CertificateUpdateGenerator(threads, quotes);

        certificateUpdateGenerator.generateCsvLines()
                .forEach(System.out::println);
    }
}

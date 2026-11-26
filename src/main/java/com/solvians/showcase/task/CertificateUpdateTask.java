package com.solvians.showcase.task;

import com.solvians.showcase.CertificateUpdate;

import java.util.concurrent.Callable;

/**
 * Generates a single certificate update in CSV format.
 */
public final class CertificateUpdateTask implements Callable<String> {

    @Override
    public String call() {
        return generateUpdate().toCsv();
    }

    public CertificateUpdate generateUpdate() {
        return new CertificateUpdate();
    }
}
package com.monitor.metrics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CpuMetricsTest {

    @Test
    void testCalculateCpuUsage() {
        long[] prev = new long[] {100, 0, 100, 400, 0, 0, 0, 0};
        long[] curr = new long[] {150, 0, 150, 450, 0, 0, 0, 0};
        double usage = CpuMetrics.calculateCpuUsage(prev, curr);
        assertEquals(66.67, Math.round(usage * 100.0) / 100.0);
    }
}

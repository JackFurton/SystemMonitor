package com.monitor.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FormatUtilTest {

    @Test
    void testFormatBytes() {
        assertEquals("512 B", FormatUtil.formatBytes(512));
        assertEquals("1.00 KB", FormatUtil.formatBytes(1024));
    }

    @Test
    void testFormatPercent() {
        assertEquals("12.35%", FormatUtil.formatPercent(12.345));
    }

    @Test
    void testFormatBitRate() {
        assertEquals("500 bps", FormatUtil.formatBitRate(500));
        assertEquals("1.50 kbps", FormatUtil.formatBitRate(1500));
    }

    @Test
    void testFormatTemperature() {
        assertEquals("N/A", FormatUtil.formatTemperature(0));
        assertEquals("36.60°C", FormatUtil.formatTemperature(36.6));
    }
}

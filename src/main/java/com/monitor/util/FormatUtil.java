package com.monitor.util;

import java.text.DecimalFormat;

/**
 * Utility class for formatting numbers. The previous implementation used a
 * single static {@link DecimalFormat} instance which is not thread safe. This
 * caused garbled output when multiple threads accessed the formatting methods
 * concurrently (for example the scheduled collector and web requests). To
 * avoid this issue we use a ThreadLocal to provide each thread with its own
 * instance of DecimalFormat.
 */

public class FormatUtil {
    
    private static final ThreadLocal<DecimalFormat> DF =
            ThreadLocal.withInitial(() -> new DecimalFormat("0.00"));
    
    /**
     * Format bytes into a human-readable string with appropriate units (KB, MB, GB, etc.)
     */
    public static String formatBytes(long bytes) {
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = "KMGTPE".charAt(exp-1) + "";
        return DF.get().format(bytes / Math.pow(unit, exp)) + " " + pre + "B";
    }
    
    /**
     * Format a double as a percentage with two decimal places
     */
    public static String formatPercent(double value) {
        return DF.get().format(value) + "%";
    }
    
    /**
     * Format bit rate into a human-readable string (Kbps, Mbps, Gbps, etc.)
     */
    public static String formatBitRate(long bitsPerSecond) {
        int unit = 1000; // Network speeds typically use 1000 (not 1024)
        if (bitsPerSecond < unit) return bitsPerSecond + " bps";
        
        int exp = (int) (Math.log(bitsPerSecond) / Math.log(unit));
        String pre = "kMGTPE".charAt(exp-1) + "";
        return DF.get().format(bitsPerSecond / Math.pow(unit, exp)) + " " + pre + "bps";
    }
    
    /**
     * Format a temperature value in degrees Celsius
     */
    public static String formatTemperature(double celsius) {
        if (celsius <= 0) return "N/A";
        return DF.get().format(celsius) + "°C";
    }
}


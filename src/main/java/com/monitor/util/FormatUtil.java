package com.monitor.util;

import java.text.DecimalFormat;

public class FormatUtil {
    
    private static final DecimalFormat df = new DecimalFormat("0.00");
    
    /**
     * Format bytes into a human-readable string with appropriate units (KB, MB, GB, etc.)
     */
    public static String formatBytes(long bytes) {
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = "KMGTPE".charAt(exp-1) + "";
        return df.format(bytes / Math.pow(unit, exp)) + " " + pre + "B";
    }
    
    /**
     * Format a double as a percentage with two decimal places
     */
    public static String formatPercent(double value) {
        return df.format(value) + "%";
    }
    
    /**
     * Format bit rate into a human-readable string (Kbps, Mbps, Gbps, etc.)
     */
    public static String formatBitRate(long bitsPerSecond) {
        int unit = 1000; // Network speeds typically use 1000 (not 1024)
        if (bitsPerSecond < unit) return bitsPerSecond + " bps";
        
        int exp = (int) (Math.log(bitsPerSecond) / Math.log(unit));
        String pre = "kMGTPE".charAt(exp-1) + "";
        return df.format(bitsPerSecond / Math.pow(unit, exp)) + " " + pre + "bps";
    }
    
    /**
     * Format a temperature value in degrees Celsius
     */
    public static String formatTemperature(double celsius) {
        if (celsius <= 0) return "N/A";
        return df.format(celsius) + "°C";
    }
}
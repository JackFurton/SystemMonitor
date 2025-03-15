package com.monitor.metrics;

import oshi.hardware.GlobalMemory;

public class MemoryMetrics {
    private GlobalMemory memory;
    private long totalMemory;
    private long availableMemory;
    private long usedMemory;
    private double memoryUsagePercent;
    
    public void collectMetrics() {
        if (SystemMetrics.hardware == null) {
            throw new IllegalStateException("System hardware not initialized");
        }
        
        memory = SystemMetrics.hardware.getMemory();
        totalMemory = memory.getTotal();
        availableMemory = memory.getAvailable();
        usedMemory = totalMemory - availableMemory;
        memoryUsagePercent = ((double) usedMemory / totalMemory) * 100;
    }
    
    public void displayMetrics() {
        System.out.println("Memory Usage: " + String.format("%.2f", memoryUsagePercent) + "%");
        System.out.println("Total Memory: " + formatBytes(totalMemory));
        System.out.println("Used Memory: " + formatBytes(usedMemory));
        System.out.println("Available Memory: " + formatBytes(availableMemory));
    }
    
    private String formatBytes(long bytes) {
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = "KMGTPE".charAt(exp-1) + "";
        return String.format("%.2f %sB", bytes / Math.pow(unit, exp), pre);
    }
    
    public double getMemoryUsagePercent() {
        return memoryUsagePercent;
    }
    
    public long getTotalMemory() {
        return totalMemory;
    }
    
    public long getAvailableMemory() {
        return availableMemory;
    }
    
    public long getUsedMemory() {
        return usedMemory;
    }
}
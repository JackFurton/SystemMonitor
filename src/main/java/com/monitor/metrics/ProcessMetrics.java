package com.monitor.metrics;

import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

public class ProcessMetrics {
    private List<OSProcess> processes;
    private int displayCount = 5; // Number of top processes to display
    
    public void collectMetrics() {
        if (SystemMetrics.systemInfo == null) {
            throw new IllegalStateException("System info not initialized");
        }
        
        OperatingSystem os = SystemMetrics.systemInfo.getOperatingSystem();
        List<OSProcess> allProcesses = os.getProcesses();
        
        // Sort by memory usage (resident set size)
        allProcesses.sort(Comparator.comparingLong(OSProcess::getResidentSetSize).reversed());
        
        // Get top processes
        int count = Math.min(displayCount, allProcesses.size());
        processes = new ArrayList<>(allProcesses.subList(0, count));
    }
    
    public void displayMetrics() {
        System.out.println("\nTop Memory-Consuming Processes:");
        System.out.println("------------------------------");
        System.out.printf("%-7s %-30s %-10s %-10s %-10s%n", 
                "PID", "Name", "Memory", "CPU %", "Threads");
        
        for (OSProcess process : processes) {
            System.out.printf("%-7d %-30s %-10s %-10.1f %-10d%n",
                    process.getProcessID(),
                    truncate(process.getName(), 30),
                    formatBytes(process.getResidentSetSize()),
                    process.getProcessCpuLoadCumulative() * 100,
                    process.getThreadCount());
        }
    }
    
    private String truncate(String str, int length) {
        if (str.length() <= length) {
            return str;
        }
        return str.substring(0, length - 3) + "...";
    }
    
    private String formatBytes(long bytes) {
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = "KMGTPE".charAt(exp-1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
    
    public void setDisplayCount(int count) {
        this.displayCount = count;
    }
    
    public List<OSProcess> getProcesses() {
        return processes;
    }
}
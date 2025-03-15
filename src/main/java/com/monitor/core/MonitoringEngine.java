package com.monitor.core;

import com.monitor.metrics.CpuMetrics;
import com.monitor.metrics.MemoryMetrics;
import com.monitor.metrics.ProcessMetrics;
import com.monitor.metrics.SystemMetrics;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MonitoringEngine {
    private final ScheduledExecutorService scheduler;
    private final SystemMetrics systemMetrics;
    private final CpuMetrics cpuMetrics;
    private final MemoryMetrics memoryMetrics;
    private final ProcessMetrics processMetrics;
    
    public MonitoringEngine() {
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.systemMetrics = new SystemMetrics();
        this.cpuMetrics = new CpuMetrics();
        this.memoryMetrics = new MemoryMetrics();
        this.processMetrics = new ProcessMetrics();
    }
    
    public void startMonitoring() {
        // Initialize the system
        systemMetrics.initialize();
        
        // Schedule metrics collection every 2 seconds (to allow for process data collection)
        scheduler.scheduleAtFixedRate(this::collectAndDisplayMetrics, 0, 2, TimeUnit.SECONDS);
        
        // Shutdown hook to clean up resources
        Runtime.getRuntime().addShutdownHook(new Thread(this::stopMonitoring));
    }
    
    private void collectAndDisplayMetrics() {
        try {
            System.out.println("\n===== System Monitor =====");
            
            // Collect and display CPU metrics
            cpuMetrics.collectMetrics();
            cpuMetrics.displayMetrics();
            
            // Collect and display Memory metrics
            memoryMetrics.collectMetrics();
            memoryMetrics.displayMetrics();
            
            // Collect and display Process metrics
            processMetrics.collectMetrics();
            processMetrics.displayMetrics();
            
            System.out.println("=========================");
        } catch (Exception e) {
            System.err.println("Error collecting metrics: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void stopMonitoring() {
        System.out.println("Stopping monitoring...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }
    
    /**
     * Set the number of processes to display in the process metrics.
     * 
     * @param count Number of top processes to display
     */
    public void setProcessDisplayCount(int count) {
        if (count < 1) {
            System.err.println("Process count must be at least 1. Using default.");
            return;
        }
        processMetrics.setDisplayCount(count);
    }
}
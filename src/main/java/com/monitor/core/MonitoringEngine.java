package com.monitor.core;

import com.monitor.metrics.CpuMetrics;
import com.monitor.metrics.MemoryMetrics;
import com.monitor.metrics.ProcessMetrics;
import com.monitor.metrics.SystemMetrics;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MonitoringEngine {
    private final ScheduledExecutorService scheduler;
    private final SystemMetrics systemMetrics;
    private final CpuMetrics cpuMetrics;
    private final MemoryMetrics memoryMetrics;
    private final ProcessMetrics processMetrics;
    
    private int refreshRateSeconds = 2; // Default refresh rate
    private ScheduledFuture<?> monitoringTask;
    
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
        
        // Schedule metrics collection at the configured refresh rate
        monitoringTask = scheduler.scheduleAtFixedRate(
            this::collectAndDisplayMetrics, 
            0, 
            refreshRateSeconds, 
            TimeUnit.SECONDS
        );
        
        // Display refresh rate information
        System.out.println("Refresh rate: " + refreshRateSeconds + " seconds");
        
        // Shutdown hook to clean up resources
        Runtime.getRuntime().addShutdownHook(new Thread(this::stopMonitoring));
    }
    
    private void collectAndDisplayMetrics() {
        try {
            // Clear screen for better visibility (works on most terminals)
            System.out.print("\033[H\033[2J");
            System.out.flush();
            
            System.out.println("===== System Monitor ===== (Refresh: " + refreshRateSeconds + "s)");
            
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
            System.out.println("Press Ctrl+C to exit");
        } catch (Exception e) {
            System.err.println("Error collecting metrics: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void stopMonitoring() {
        System.out.println("Stopping monitoring...");
        if (monitoringTask != null) {
            monitoringTask.cancel(false);
        }
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
    
    /**
     * Set the refresh rate for metric collection and display.
     * 
     * @param seconds Refresh rate in seconds (minimum 1)
     */
    public void setRefreshRate(int seconds) {
        if (seconds < 1) {
            System.err.println("Refresh rate must be at least 1 second. Using default.");
            return;
        }
        this.refreshRateSeconds = seconds;
    }
}
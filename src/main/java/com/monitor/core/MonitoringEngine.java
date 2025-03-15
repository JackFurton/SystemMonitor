package com.monitor.core;

import com.monitor.metrics.CpuMetrics;
import com.monitor.metrics.MemoryMetrics;
import com.monitor.metrics.SystemMetrics;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MonitoringEngine {
    private final ScheduledExecutorService scheduler;
    private final SystemMetrics systemMetrics;
    private final CpuMetrics cpuMetrics;
    private final MemoryMetrics memoryMetrics;
    
    public MonitoringEngine() {
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.systemMetrics = new SystemMetrics();
        this.cpuMetrics = new CpuMetrics();
        this.memoryMetrics = new MemoryMetrics();
    }
    
    public void startMonitoring() {
        // Initialize the system
        systemMetrics.initialize();
        
        // Schedule metrics collection every 1 second
        scheduler.scheduleAtFixedRate(this::collectAndDisplayMetrics, 0, 1, TimeUnit.SECONDS);
        
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
            
            System.out.println("=========================");
        } catch (Exception e) {
            System.err.println("Error collecting metrics: " + e.getMessage());
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
}
package com.monitor.core;

import com.monitor.metrics.CpuMetrics;
import com.monitor.metrics.DiskMetrics;
import com.monitor.metrics.GpuMetrics;
import com.monitor.metrics.MemoryMetrics;
import com.monitor.metrics.NetworkMetrics;
import com.monitor.metrics.ProcessMetrics;
import com.monitor.metrics.SystemMetrics;
import com.monitor.metrics.TemperatureMetrics;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class MonitoringEngine {
    private final SystemMetrics systemMetrics;
    private final CpuMetrics cpuMetrics;
    private final MemoryMetrics memoryMetrics;
    private final ProcessMetrics processMetrics;
    private final DiskMetrics diskMetrics;
    private final GpuMetrics gpuMetrics;
    private final NetworkMetrics networkMetrics;
    private final TemperatureMetrics temperatureMetrics;
    
    private int refreshRateSeconds = 2; // Default refresh rate
    private boolean consoleOutput = false; // Disable console output by default in web mode
    
    public MonitoringEngine() {
        this.systemMetrics = new SystemMetrics();
        this.cpuMetrics = new CpuMetrics();
        this.memoryMetrics = new MemoryMetrics();
        this.processMetrics = new ProcessMetrics();
        this.diskMetrics = new DiskMetrics();
        this.gpuMetrics = new GpuMetrics();
        this.networkMetrics = new NetworkMetrics();
        this.temperatureMetrics = new TemperatureMetrics();
    }
    
    @PostConstruct
    public void init() {
        // Initialize the system
        systemMetrics.initialize();
        
        // Display refresh rate information
        System.out.println("Metrics collection rate: " + refreshRateSeconds + " seconds");
        
        // Initial collection
        collectMetrics();
        
        // Start the scheduled collection
        System.out.println("Metrics collection started");
    }
    
    @Scheduled(fixedDelayString = "${metrics.refresh-rate:2000}")
    public void scheduledMetricsCollection() {
        collectAndDisplayMetrics();
    }
    
    public void collectMetrics() {
        try {
            // Collect CPU metrics
            cpuMetrics.collectMetrics();
            
            // Collect Memory metrics
            memoryMetrics.collectMetrics();
            
            // Collect Process metrics
            processMetrics.collectMetrics();
            
            // Collect Disk metrics
            diskMetrics.collectMetrics();
            
            // Collect GPU metrics
            gpuMetrics.collectMetrics();
            
            // Collect Network metrics with error handling
            try {
                networkMetrics.collectMetrics();
            } catch (Exception e) {
                System.err.println("Error collecting network metrics: " + e.getMessage());
                // Continue with other metrics
            }
            
            // Collect Temperature metrics with error handling
            try {
                temperatureMetrics.collectMetrics();
            } catch (Exception e) {
                System.err.println("Error collecting temperature metrics: " + e.getMessage());
                // Continue with other metrics
            }
        } catch (Exception e) {
            System.err.println("Error collecting metrics: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void collectAndDisplayMetrics() {
        collectMetrics();
        
        if (consoleOutput) {
            try {
                // Clear screen for better visibility (works on most terminals)
                System.out.print("\033[H\033[2J");
                System.out.flush();
                
                System.out.println("===== System Monitor ===== (Refresh: " + refreshRateSeconds + "s)");
                
                // Display CPU metrics
                cpuMetrics.displayMetrics();
                
                // Display Memory metrics
                memoryMetrics.displayMetrics();
                
                // Display Process metrics
                processMetrics.displayMetrics();
                
                // Display Disk metrics
                diskMetrics.displayMetrics();
                
                // Display GPU metrics
                gpuMetrics.displayMetrics();
                
                // Display Network metrics
                networkMetrics.displayMetrics();
                
                // Display Temperature metrics
                temperatureMetrics.displayMetrics();
                
                System.out.println("=========================");
                System.out.println("Press Ctrl+C to exit");
            } catch (Exception e) {
                System.err.println("Error displaying metrics: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    @PreDestroy
    public void stopMonitoring() {
        System.out.println("Stopping monitoring...");
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
    
    /**
     * Enable or disable console output.
     * 
     * @param enabled Whether to enable console output
     */
    public void setConsoleOutput(boolean enabled) {
        this.consoleOutput = enabled;
    }
    
    // Getter methods for REST controllers
    public SystemMetrics getSystemMetrics() {
        return systemMetrics;
    }
    
    public CpuMetrics getCpuMetrics() {
        return cpuMetrics;
    }
    
    public MemoryMetrics getMemoryMetrics() {
        return memoryMetrics;
    }
    
    public ProcessMetrics getProcessMetrics() {
        return processMetrics;
    }
    
    public DiskMetrics getDiskMetrics() {
        return diskMetrics;
    }
    
    public GpuMetrics getGpuMetrics() {
        return gpuMetrics;
    }
    
    public NetworkMetrics getNetworkMetrics() {
        return networkMetrics;
    }
    
    public TemperatureMetrics getTemperatureMetrics() {
        return temperatureMetrics;
    }
    
    public int getRefreshRateSeconds() {
        return refreshRateSeconds;
    }
}
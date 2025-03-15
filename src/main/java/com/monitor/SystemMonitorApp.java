package com.monitor;

import com.monitor.core.MonitoringEngine;
import com.monitor.metrics.ProcessMetrics;

public class SystemMonitorApp {
    public static void main(String[] args) {
        System.out.println("Starting System Monitor...");
        
        // Create monitoring engine
        MonitoringEngine engine = new MonitoringEngine();
        
        // Parse command line arguments
        parseArgs(args, engine);
        
        // Start monitoring
        engine.startMonitoring();
    }
    
    private static void parseArgs(String[] args, MonitoringEngine engine) {
        if (args.length > 0) {
            try {
                // Check for process count flag: -p or --processes
                for (int i = 0; i < args.length; i++) {
                    if ((args[i].equals("-p") || args[i].equals("--processes")) && i + 1 < args.length) {
                        int processCount = Integer.parseInt(args[i + 1]);
                        System.out.println("Setting process display count to: " + processCount);
                        engine.setProcessDisplayCount(processCount);
                        i++; // Skip the next argument since we've processed it
                    }
                }
            } catch (NumberFormatException e) {
                System.err.println("Error parsing command line arguments: " + e.getMessage());
                printUsage();
            }
        }
    }
    
    private static void printUsage() {
        System.out.println("Usage: java -cp \"build:lib/*\" com.monitor.SystemMonitorApp [options]");
        System.out.println("Options:");
        System.out.println("  -p, --processes <count>   Number of top processes to display (default: 5)");
    }
}
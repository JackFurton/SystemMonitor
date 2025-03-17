package com.monitor;

import com.monitor.core.MonitoringEngine;
import com.monitor.util.PcapNetworkUtil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.Primary;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnableScheduling
public class SystemMonitorApp {
    // Configure logging to silence MacNetworkParams errors on macOS
    static {
        // This will silence the getaddrinfo errors in OSHI's MacNetworkParams class
        System.setProperty("org.slf4j.simpleLogger.log.oshi.software.os.mac.MacNetworkParams", "OFF");
    }
    
    private static int processCount = 5; // Default value
    private static int refreshRate = 2; // Default value in seconds
    
    public static void main(String[] args) {
        System.out.println("Starting System Monitor with Web Interface...");
        
        // Parse command line arguments
        parseArgs(args);
        
        // Set the refresh rate as a system property for Spring to use
        System.setProperty("METRICS_REFRESH_RATE", String.valueOf(refreshRate * 1000));
        
        // Start Spring Boot application
        ConfigurableApplicationContext context = SpringApplication.run(SystemMonitorApp.class, args);
        
        // Get the MonitoringEngine bean and configure it
        MonitoringEngine engine = context.getBean(MonitoringEngine.class);
        engine.setProcessDisplayCount(processCount);
        engine.setRefreshRate(refreshRate);
        
        // Register shutdown hook to clean up resources
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Stopping monitoring and cleaning up resources...");
            
            // Stop pcap packet capture if it's running
            try {
                PcapNetworkUtil.stopNetworkMonitoring();
            } catch (Exception e) {
                System.err.println("Error stopping network monitoring: " + e.getMessage());
            }
        }));
    }
    
    @Bean
    public ApplicationListener<ApplicationReadyEvent> onApplicationReady() {
        return event -> {
            System.out.println("System Monitor is running!");
            System.out.println("Web interface available at: http://localhost:8080");
            System.out.println("API endpoints:");
            System.out.println("- http://localhost:8080/api/cpu");
            System.out.println("- http://localhost:8080/api/memory");
            System.out.println("- http://localhost:8080/api/processes");
            System.out.println("- http://localhost:8080/api/system");
            System.out.println("- http://localhost:8080/api/disks");
            System.out.println("- http://localhost:8080/api/gpus");
            System.out.println("- http://localhost:8080/api/network");
            System.out.println("- http://localhost:8080/api/temperature");
            System.out.println("- http://localhost:8080/api/all");
        };
    }
    
    private static void parseArgs(String[] args) {
        if (args.length > 0) {
            try {
                for (int i = 0; i < args.length; i++) {
                    // Check for process count flag: -p or --processes
                    if ((args[i].equals("-p") || args[i].equals("--processes")) && i + 1 < args.length) {
                        processCount = Integer.parseInt(args[i + 1]);
                        System.out.println("Setting process display count to: " + processCount);
                        i++; // Skip the next argument since we've processed it
                    }
                    
                    // Check for refresh rate flag: -r or --refresh
                    else if ((args[i].equals("-r") || args[i].equals("--refresh")) && i + 1 < args.length) {
                        refreshRate = Integer.parseInt(args[i + 1]);
                        if (refreshRate < 1) {
                            System.err.println("Refresh rate must be at least 1 second. Using default (2s).");
                            refreshRate = 2;
                        } else {
                            System.out.println("Setting refresh rate to: " + refreshRate + " seconds");
                        }
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
        System.out.println("Usage: java -jar system-monitor.jar [options]");
        System.out.println("Options:");
        System.out.println("  -p, --processes <count>   Number of top processes to display (default: 5)");
        System.out.println("  -r, --refresh <seconds>   Refresh rate in seconds (default: 2)");
    }
}
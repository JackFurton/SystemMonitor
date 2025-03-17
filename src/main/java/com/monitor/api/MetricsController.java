package com.monitor.api;

import com.monitor.core.MonitoringEngine;
import com.monitor.util.FormatUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import oshi.software.os.OSProcess;

@RestController
@RequestMapping("/api")
public class MetricsController {

    private final MonitoringEngine monitoringEngine;

    @Autowired
    public MetricsController(MonitoringEngine monitoringEngine) {
        this.monitoringEngine = monitoringEngine;
    }

    /**
     * Main API endpoint for all system metrics
     */
    @GetMapping({"/", "/all", "/metrics"})
    public ResponseEntity<Map<String, Object>> getAllMetrics() {
        try {
            // Ensure metrics are collected
            monitoringEngine.collectMetrics();
        
        Map<String, Object> response = new HashMap<>();
        
        // System information
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("os", monitoringEngine.getSystemMetrics().getSystemInfo().getOperatingSystem().toString());
        systemInfo.put("refreshRate", monitoringEngine.getRefreshRateSeconds());
        systemInfo.put("timestamp", System.currentTimeMillis());
        
        // Check if running with elevated permissions
        boolean isRunningWithSudo = false;
        try {
            String username = System.getProperty("user.name");
            // Check for root or admin user
            boolean isElevatedUser = "root".equals(username) || username.toLowerCase().startsWith("admin");
            
            // Also check if network interfaces have been loaded successfully via our specialized PcapNetworkUtil
            boolean hasNetworkInterfaces = false;
            if (monitoringEngine.getNetworkMetrics().getNetworkInfo() != null && 
                monitoringEngine.getNetworkMetrics().getNetworkInfo().size() > 1) {
                // More than just the global info means we have interfaces
                hasNetworkInterfaces = true;
            }
            
            isRunningWithSudo = isElevatedUser || hasNetworkInterfaces;
        } catch (Exception e) {
            System.err.println("Error checking sudo status: " + e.getMessage());
            // Ignore - default to false
        }
        systemInfo.put("runningWithSudo", isRunningWithSudo);
        response.put("system", systemInfo);
        
        // CPU metrics
        Map<String, Object> cpuMetrics = new HashMap<>();
        double cpuUsage = monitoringEngine.getCpuMetrics().getCpuUsage();
        cpuMetrics.put("usage", cpuUsage);
        cpuMetrics.put("usageFormatted", FormatUtil.formatPercent(cpuUsage));
        cpuMetrics.put("cores", monitoringEngine.getCpuMetrics().getProcessor().getLogicalProcessorCount());
        response.put("cpu", cpuMetrics);
        
        // Memory metrics
        Map<String, Object> memoryMetrics = new HashMap<>();
        double memUsage = monitoringEngine.getMemoryMetrics().getMemoryUsagePercent();
        long totalMemory = monitoringEngine.getMemoryMetrics().getTotalMemory();
        long usedMemory = monitoringEngine.getMemoryMetrics().getUsedMemory();
        long availableMemory = monitoringEngine.getMemoryMetrics().getAvailableMemory();
        
        memoryMetrics.put("usage", memUsage);
        memoryMetrics.put("usageFormatted", FormatUtil.formatPercent(memUsage));
        memoryMetrics.put("total", totalMemory);
        memoryMetrics.put("totalFormatted", FormatUtil.formatBytes(totalMemory));
        memoryMetrics.put("used", usedMemory);
        memoryMetrics.put("usedFormatted", FormatUtil.formatBytes(usedMemory));
        memoryMetrics.put("available", availableMemory);
        memoryMetrics.put("availableFormatted", FormatUtil.formatBytes(availableMemory));
        response.put("memory", memoryMetrics);
        
        // Process metrics
        List<OSProcess> processes = monitoringEngine.getProcessMetrics().getProcesses();
        List<Map<String, Object>> processList = processes.stream()
            .map(process -> {
                Map<String, Object> processMap = new HashMap<>();
                long memory = process.getResidentSetSize();
                double cpu = process.getProcessCpuLoadCumulative() * 100;
                
                processMap.put("pid", process.getProcessID());
                processMap.put("name", process.getName());
                processMap.put("memory", memory);
                processMap.put("memoryFormatted", FormatUtil.formatBytes(memory));
                processMap.put("cpu", cpu);
                processMap.put("cpuFormatted", FormatUtil.formatPercent(cpu));
                processMap.put("threads", process.getThreadCount());
                return processMap;
            })
            .collect(Collectors.toList());
        response.put("processes", processList);
        
        // Disk metrics
        List<Map<String, Object>> diskInfo = monitoringEngine.getDiskMetrics().getDiskInfo();
        response.put("disks", diskInfo);
        
        // GPU metrics
        List<Map<String, Object>> gpuInfo = monitoringEngine.getGpuMetrics().getGpuInfo();
        response.put("gpus", gpuInfo);
        
        // Network metrics
        List<Map<String, Object>> networkInfo = monitoringEngine.getNetworkMetrics().getNetworkInfo();
        
        // Create a properly structured network response
        Map<String, Object> networkData = new HashMap<>();
        
        // Add connection stats from the first entry (if it exists)
        if (!networkInfo.isEmpty()) {
            try {
                // Create a defensive copy to avoid concurrent modification
                List<Map<String, Object>> networkInfoCopy = new ArrayList<>(networkInfo);
                
                Map<String, Object> globalInfo = new HashMap<>(networkInfoCopy.get(0));
                networkData.putAll(globalInfo);
                
                // Create interfaces list from remaining entries with defensive copy
                List<Map<String, Object>> interfaces = new ArrayList<>();
                if (networkInfoCopy.size() > 1) {
                    for (int i = 1; i < networkInfoCopy.size(); i++) {
                        interfaces.add(new HashMap<>(networkInfoCopy.get(i)));
                    }
                }
                
                // Mark if sudo is needed for full network monitoring
                if (!isRunningWithSudo && System.getProperty("os.name").toLowerCase().contains("mac")) {
                    for (Map<String, Object> netInterface : interfaces) {
                        netInterface.put("requiresSudo", true);
                    }
                }
                
                networkData.put("interfaces", interfaces);
            } catch (Exception e) {
                System.err.println("Error processing network info: " + e.getMessage());
                e.printStackTrace();
                // Provide empty interfaces list if error occurs
                networkData.put("interfaces", new ArrayList<>());
            }
        }
        
        response.put("network", networkData);
        
        // Temperature metrics
        Map<String, Object> temperatureInfo = monitoringEngine.getTemperatureMetrics().getTemperatureInfo();
        response.put("temperature", temperatureInfo);
        
        return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error generating metrics response: " + e.getMessage());
            e.printStackTrace();
            
            // Return a simple error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error generating metrics: " + e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    // Single endpoint for backward compatibility with legacy endpoint paths
    @GetMapping({
        "/cpu", 
        "/memory", 
        "/processes", 
        "/system", 
        "/disks", 
        "/gpus", 
        "/network", 
        "/temperature"
    })
    public ResponseEntity<Map<String, Object>> getLegacyMetrics() {
        return getAllMetrics();
    }
}
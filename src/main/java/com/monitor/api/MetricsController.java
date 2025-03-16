package com.monitor.api;

import com.monitor.core.MonitoringEngine;
import com.monitor.util.FormatUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        // Ensure metrics are collected
        monitoringEngine.collectMetrics();
        
        Map<String, Object> response = new HashMap<>();
        
        // System information
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("os", monitoringEngine.getSystemMetrics().getSystemInfo().getOperatingSystem().toString());
        systemInfo.put("refreshRate", monitoringEngine.getRefreshRateSeconds());
        systemInfo.put("timestamp", System.currentTimeMillis());
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
        
        return ResponseEntity.ok(response);
    }
    
    // Legacy endpoints redirect to the main endpoint for backward compatibility
    
    @GetMapping("/cpu")
    public ResponseEntity<Map<String, Object>> getCpuMetrics() {
        return getAllMetrics();
    }

    @GetMapping("/memory")
    public ResponseEntity<Map<String, Object>> getMemoryMetrics() {
        return getAllMetrics();
    }

    @GetMapping("/processes")
    public ResponseEntity<Map<String, Object>> getProcessMetrics() {
        return getAllMetrics();
    }

    @GetMapping("/system")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        return getAllMetrics();
    }
}
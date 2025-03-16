package com.monitor.web;

import com.monitor.core.MonitoringEngine;
import com.monitor.util.FormatUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class DashboardController {

    private final MonitoringEngine monitoringEngine;

    @Autowired
    public DashboardController(MonitoringEngine monitoringEngine) {
        this.monitoringEngine = monitoringEngine;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        // Ensure metrics are collected
        monitoringEngine.collectMetrics();
        
        // Add refresh rate to model
        model.addAttribute("refreshRate", monitoringEngine.getRefreshRateSeconds());
        
        // Add system information
        model.addAttribute("os", monitoringEngine.getSystemMetrics().getSystemInfo().getOperatingSystem().toString());
        
        // Add CPU metrics
        model.addAttribute("cpuUsage", FormatUtil.formatPercent(monitoringEngine.getCpuMetrics().getCpuUsage()));
        model.addAttribute("cpuCores", monitoringEngine.getCpuMetrics().getProcessor().getLogicalProcessorCount());
        
        // Add memory metrics
        model.addAttribute("memoryUsage", FormatUtil.formatPercent(monitoringEngine.getMemoryMetrics().getMemoryUsagePercent()));
        model.addAttribute("totalMemory", FormatUtil.formatBytes(monitoringEngine.getMemoryMetrics().getTotalMemory()));
        
        // Add current time
        model.addAttribute("currentTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        return "dashboard";
    }
}
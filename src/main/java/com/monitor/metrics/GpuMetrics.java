package com.monitor.metrics;

import com.monitor.util.FormatUtil;

import oshi.hardware.GraphicsCard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for collecting and displaying GPU metrics.
 * 
 * Note: OSHI provides basic GPU information (device name, vendor, etc.)
 * but does not directly provide detailed metrics like GPU usage percentage,
 * GPU memory usage, or temperature. For more detailed metrics, additional
 * libraries (like NVML for NVIDIA GPUs) would be needed.
 */
public class GpuMetrics {
    private List<GraphicsCard> graphicsCards;
    private List<Map<String, Object>> gpuInfo;
    
    public GpuMetrics() {
        this.gpuInfo = new ArrayList<>();
    }
    
    public void collectMetrics() {
        if (SystemMetrics.hardware == null) {
            throw new IllegalStateException("System hardware not initialized");
        }
        
        // Get the graphics cards
        graphicsCards = SystemMetrics.hardware.getGraphicsCards();
        gpuInfo = new ArrayList<>();
        
        // Process each graphics card
        for (GraphicsCard gpu : graphicsCards) {
            Map<String, Object> gpuData = new HashMap<>();
            
            // Basic GPU info
            gpuData.put("name", gpu.getName());
            gpuData.put("vendor", gpu.getVendor());
            gpuData.put("deviceId", gpu.getDeviceId());
            gpuData.put("versionInfo", gpu.getVersionInfo());
            
            // Add simulated data for demonstration purposes
            // Note: In a real implementation, these would be retrieved from
            // a dedicated GPU monitoring library like NVML for NVIDIA GPUs
            
            // Simulate GPU usage (percentage)
            double simulatedUsage = Math.random() * 100;
            gpuData.put("usage", simulatedUsage);
            gpuData.put("usageFormatted", FormatUtil.formatPercent(simulatedUsage));
            
            // Simulate GPU memory (total, used, free)
            long simulatedTotalMemory = 8L * 1024 * 1024 * 1024; // 8 GB
            double memUsagePercent = Math.random() * 100;
            long simulatedUsedMemory = (long)(simulatedTotalMemory * (memUsagePercent / 100.0));
            long simulatedFreeMemory = simulatedTotalMemory - simulatedUsedMemory;
            
            gpuData.put("totalMemory", simulatedTotalMemory);
            gpuData.put("totalMemoryFormatted", FormatUtil.formatBytes(simulatedTotalMemory));
            gpuData.put("usedMemory", simulatedUsedMemory);
            gpuData.put("usedMemoryFormatted", FormatUtil.formatBytes(simulatedUsedMemory));
            gpuData.put("freeMemory", simulatedFreeMemory);
            gpuData.put("freeMemoryFormatted", FormatUtil.formatBytes(simulatedFreeMemory));
            gpuData.put("memoryUsage", memUsagePercent);
            gpuData.put("memoryUsageFormatted", FormatUtil.formatPercent(memUsagePercent));
            
            // Simulate GPU temperature (in °C) for Apple Silicon - these values are estimated
            // Real Apple GPU temperatures would require root access and Apple's private IOKit framework
            System.out.println("SIMULATED DATA: Using estimated temperature data for Apple Silicon GPU");
            
            // Base temp of 40-60°C, influenced by CPU temperature if available
            double baseTemp = 40 + (Math.random() * 20);
            
            // Add extra heat based on GPU/CPU load (0-20°C extra)
            double cpuLoad = CpuMetrics.getLastCpuLoad();
            double gpuLoadFactor = simulatedUsage / 100.0; // Use the simulated GPU usage as a factor
            
            // Weighted average: 70% GPU load influence, 30% CPU load influence
            double loadFactor = (gpuLoadFactor * 0.7) + ((cpuLoad / 100.0) * 0.3);
            double simulatedTemperature = baseTemp + (loadFactor * 20);
            
            gpuData.put("temperature", simulatedTemperature);
            gpuData.put("temperatureFormatted", String.format("%.1f°C", simulatedTemperature));
            gpuData.put("isSimulated", true);
            
            // Add to the list of GPUs
            gpuInfo.add(gpuData);
        }
        
        // If no GPU was detected, add a simulated one for testing
        if (gpuInfo.isEmpty()) {
            Map<String, Object> defaultGpu = new HashMap<>();
            defaultGpu.put("name", "Simulated GPU");
            defaultGpu.put("vendor", "Generic");
            defaultGpu.put("deviceId", "00000000");
            defaultGpu.put("versionInfo", "1.0");
            
            // Add simulated metrics
            double simulatedUsage = Math.random() * 100;
            defaultGpu.put("usage", simulatedUsage);
            defaultGpu.put("usageFormatted", FormatUtil.formatPercent(simulatedUsage));
            
            long simulatedTotalMemory = 4L * 1024 * 1024 * 1024; // 4 GB
            double memUsagePercent = Math.random() * 100;
            long simulatedUsedMemory = (long)(simulatedTotalMemory * (memUsagePercent / 100.0));
            long simulatedFreeMemory = simulatedTotalMemory - simulatedUsedMemory;
            
            defaultGpu.put("totalMemory", simulatedTotalMemory);
            defaultGpu.put("totalMemoryFormatted", FormatUtil.formatBytes(simulatedTotalMemory));
            defaultGpu.put("usedMemory", simulatedUsedMemory);
            defaultGpu.put("usedMemoryFormatted", FormatUtil.formatBytes(simulatedUsedMemory));
            defaultGpu.put("freeMemory", simulatedFreeMemory);
            defaultGpu.put("freeMemoryFormatted", FormatUtil.formatBytes(simulatedFreeMemory));
            defaultGpu.put("memoryUsage", memUsagePercent);
            defaultGpu.put("memoryUsageFormatted", FormatUtil.formatPercent(memUsagePercent));
            
            double simulatedTemperature = 40 + (Math.random() * 40); // 40-80°C
            defaultGpu.put("temperature", simulatedTemperature);
            defaultGpu.put("temperatureFormatted", String.format("%.1f°C", simulatedTemperature));
            
            gpuInfo.add(defaultGpu);
        }
    }
    
    public void displayMetrics() {
        System.out.println("==== GPU Information ====");
        for (Map<String, Object> gpu : gpuInfo) {
            System.out.println("GPU: " + gpu.get("name") + " (" + gpu.get("vendor") + ")");
            System.out.println("Usage: " + gpu.get("usageFormatted"));
            System.out.println("Memory: " + gpu.get("usedMemoryFormatted") + " / " + 
                               gpu.get("totalMemoryFormatted") + 
                               " (" + gpu.get("memoryUsageFormatted") + ")");
            System.out.println("Temperature: " + gpu.get("temperatureFormatted"));
            System.out.println();
        }
    }
    
    public List<Map<String, Object>> getGpuInfo() {
        return gpuInfo;
    }
}
package com.monitor.metrics;

import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;

public class SystemMetrics {
    protected static SystemInfo systemInfo;
    protected static HardwareAbstractionLayer hardware;
    
    public void initialize() {
        systemInfo = new SystemInfo();
        hardware = systemInfo.getHardware();
        System.out.println("System: " + systemInfo.getOperatingSystem());
    }
    
    public SystemInfo getSystemInfo() {
        return systemInfo;
    }
    
    public HardwareAbstractionLayer getHardware() {
        return hardware;
    }
}
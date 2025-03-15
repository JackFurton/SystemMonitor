package com.monitor;

import com.monitor.core.MonitoringEngine;

public class SystemMonitorApp {
    public static void main(String[] args) {
        System.out.println("Starting System Monitor...");
        MonitoringEngine engine = new MonitoringEngine();
        engine.startMonitoring();
    }
}
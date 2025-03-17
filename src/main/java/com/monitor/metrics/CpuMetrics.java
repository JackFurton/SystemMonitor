package com.monitor.metrics;

import oshi.hardware.CentralProcessor;
import oshi.hardware.CentralProcessor.TickType;

import java.text.DecimalFormat;

public class CpuMetrics {
    private static final DecimalFormat df = new DecimalFormat("0.00");
    private CentralProcessor processor;
    private long[] prevTicks;
    private long[] currTicks;
    private double cpuUsage;
    private static double lastCpuLoad = 0.0; // Static to share between instances
    
    public void collectMetrics() {
        if (SystemMetrics.hardware == null) {
            throw new IllegalStateException("System hardware not initialized");
        }
        
        processor = SystemMetrics.hardware.getProcessor();
        prevTicks = currTicks != null ? currTicks : processor.getSystemCpuLoadTicks();
        // Sleep a bit to get a difference
        try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        currTicks = processor.getSystemCpuLoadTicks();
        
        long user = currTicks[TickType.USER.getIndex()] - prevTicks[TickType.USER.getIndex()];
        long nice = currTicks[TickType.NICE.getIndex()] - prevTicks[TickType.NICE.getIndex()];
        long sys = currTicks[TickType.SYSTEM.getIndex()] - prevTicks[TickType.SYSTEM.getIndex()];
        long idle = currTicks[TickType.IDLE.getIndex()] - prevTicks[TickType.IDLE.getIndex()];
        long iowait = currTicks[TickType.IOWAIT.getIndex()] - prevTicks[TickType.IOWAIT.getIndex()];
        long irq = currTicks[TickType.IRQ.getIndex()] - prevTicks[TickType.IRQ.getIndex()];
        long softirq = currTicks[TickType.SOFTIRQ.getIndex()] - prevTicks[TickType.SOFTIRQ.getIndex()];
        long steal = currTicks[TickType.STEAL.getIndex()] - prevTicks[TickType.STEAL.getIndex()];
        
        long totalCpu = user + nice + sys + idle + iowait + irq + softirq + steal;
        
        cpuUsage = totalCpu > 0 ? 100d * (totalCpu - idle) / totalCpu : 0d;
        
        // Update the static last CPU load for use by other components
        lastCpuLoad = cpuUsage;
    }
    
    public void displayMetrics() {
        System.out.println("CPU Usage: " + df.format(cpuUsage) + "%");
        System.out.println("CPU Cores: " + processor.getLogicalProcessorCount());
    }
    
    public double getCpuUsage() {
        return cpuUsage;
    }
    
    public CentralProcessor getProcessor() {
        return processor;
    }
    
    /**
     * Get the last CPU load percentage (0-100)
     * This is used by other components like TemperatureMetrics to adjust simulations
     */
    public static double getLastCpuLoad() {
        return lastCpuLoad;
    }
}
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

        cpuUsage = calculateCpuUsage(prevTicks, currTicks);

        // Update the static last CPU load for use by other components
        lastCpuLoad = cpuUsage;
    }

    /**
     * Calculate CPU usage percent from previous and current tick arrays.
     */
    static double calculateCpuUsage(long[] previous, long[] current) {
        long user = current[TickType.USER.getIndex()] - previous[TickType.USER.getIndex()];
        long nice = current[TickType.NICE.getIndex()] - previous[TickType.NICE.getIndex()];
        long sys = current[TickType.SYSTEM.getIndex()] - previous[TickType.SYSTEM.getIndex()];
        long idle = current[TickType.IDLE.getIndex()] - previous[TickType.IDLE.getIndex()];
        long iowait = current[TickType.IOWAIT.getIndex()] - previous[TickType.IOWAIT.getIndex()];
        long irq = current[TickType.IRQ.getIndex()] - previous[TickType.IRQ.getIndex()];
        long softirq = current[TickType.SOFTIRQ.getIndex()] - previous[TickType.SOFTIRQ.getIndex()];
        long steal = current[TickType.STEAL.getIndex()] - previous[TickType.STEAL.getIndex()];

        long totalCpu = user + nice + sys + idle + iowait + irq + softirq + steal;
        return totalCpu > 0 ? 100d * (totalCpu - idle) / totalCpu : 0d;
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
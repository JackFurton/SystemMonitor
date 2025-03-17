package com.monitor.metrics;

import com.monitor.util.FormatUtil;

import oshi.hardware.HWDiskStore;
import oshi.hardware.HWPartition;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiskMetrics {
    private List<HWDiskStore> diskStores;
    private List<Map<String, Object>> diskInfo;
    private Map<String, Long> previousReadBytes;
    private Map<String, Long> previousWriteBytes;
    private Map<String, Long> readBytesRate;
    private Map<String, Long> writeBytesRate;
    private long lastUpdateTime;
    
    public DiskMetrics() {
        this.diskInfo = new ArrayList<>();
        this.previousReadBytes = new HashMap<>();
        this.previousWriteBytes = new HashMap<>();
        this.readBytesRate = new HashMap<>();
        this.writeBytesRate = new HashMap<>();
        this.lastUpdateTime = System.currentTimeMillis();
    }
    
    public void collectMetrics() {
        if (SystemMetrics.hardware == null) {
            throw new IllegalStateException("System hardware not initialized");
        }
        
        long currentTime = System.currentTimeMillis();
        double timeDiffSeconds = (currentTime - lastUpdateTime) / 1000.0;
        
        // Get the disk stores
        diskStores = SystemMetrics.hardware.getDiskStores();
        diskInfo = new ArrayList<>();
        
        // Process each disk store
        for (HWDiskStore store : diskStores) {
            Map<String, Object> disk = new HashMap<>();
            String diskId = store.getName();
            
            // Calculate read/write rates
            if (previousReadBytes.containsKey(diskId) && timeDiffSeconds > 0) {
                long readDiff = store.getReadBytes() - previousReadBytes.get(diskId);
                long writeDiff = store.getWriteBytes() - previousWriteBytes.get(diskId);
                
                readBytesRate.put(diskId, (long)(readDiff / timeDiffSeconds));
                writeBytesRate.put(diskId, (long)(writeDiff / timeDiffSeconds));
            }
            
            // Store current values for next calculation
            previousReadBytes.put(diskId, store.getReadBytes());
            previousWriteBytes.put(diskId, store.getWriteBytes());
            
            // Basic disk info
            disk.put("name", store.getName());
            disk.put("model", store.getModel());
            disk.put("serial", store.getSerial());
            disk.put("size", store.getSize());
            disk.put("sizeFormatted", FormatUtil.formatBytes(store.getSize()));
            
            // I/O stats
            disk.put("reads", store.getReads());
            disk.put("writes", store.getWrites());
            disk.put("readBytes", store.getReadBytes());
            disk.put("readBytesFormatted", FormatUtil.formatBytes(store.getReadBytes()));
            disk.put("writeBytes", store.getWriteBytes());
            disk.put("writeBytesFormatted", FormatUtil.formatBytes(store.getWriteBytes()));
            
            // Read/Write rates
            long readRate = readBytesRate.getOrDefault(diskId, 0L);
            long writeRate = writeBytesRate.getOrDefault(diskId, 0L);
            disk.put("readRate", readRate);
            disk.put("readRateFormatted", FormatUtil.formatBytes(readRate) + "/s");
            disk.put("writeRate", writeRate);
            disk.put("writeRateFormatted", FormatUtil.formatBytes(writeRate) + "/s");
            
            // Get partitions
            List<Map<String, Object>> partitionsList = new ArrayList<>();
            for (HWPartition partition : store.getPartitions()) {
                Map<String, Object> part = new HashMap<>();
                part.put("identification", partition.getIdentification());
                part.put("name", partition.getName());
                part.put("type", partition.getType());
                part.put("mountPoint", partition.getMountPoint());
                part.put("size", partition.getSize());
                part.put("sizeFormatted", FormatUtil.formatBytes(partition.getSize()));
                
                // Get filesystem details for this partition
                File fileSystem = new File(partition.getMountPoint());
                if (fileSystem.exists()) {
                    long totalSpace = fileSystem.getTotalSpace();
                    long freeSpace = fileSystem.getFreeSpace();
                    long usableSpace = fileSystem.getUsableSpace();
                    double usedPercent = totalSpace > 0 ? 
                            100.0 * (totalSpace - freeSpace) / totalSpace : 0.0;
                    
                    part.put("totalSpace", totalSpace);
                    part.put("totalSpaceFormatted", FormatUtil.formatBytes(totalSpace));
                    part.put("freeSpace", freeSpace);
                    part.put("freeSpaceFormatted", FormatUtil.formatBytes(freeSpace));
                    part.put("usableSpace", usableSpace);
                    part.put("usableSpaceFormatted", FormatUtil.formatBytes(usableSpace));
                    part.put("usedPercent", usedPercent);
                    part.put("usedPercentFormatted", FormatUtil.formatPercent(usedPercent));
                }
                
                partitionsList.add(part);
            }
            disk.put("partitions", partitionsList);
            
            diskInfo.add(disk);
        }
        
        lastUpdateTime = currentTime;
    }
    
    public void displayMetrics() {
        System.out.println("==== Disk Information ====");
        for (Map<String, Object> disk : diskInfo) {
            System.out.println("Disk: " + disk.get("name") + " (" + disk.get("model") + ")");
            System.out.println("Size: " + disk.get("sizeFormatted"));
            System.out.println("I/O: Read " + disk.get("readRateFormatted") + 
                    ", Write " + disk.get("writeRateFormatted"));
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> partitions = (List<Map<String, Object>>) disk.get("partitions");
            System.out.println("Partitions: " + partitions.size());
            
            for (Map<String, Object> partition : partitions) {
                if (partition.containsKey("totalSpace") && (Long)partition.get("totalSpace") > 0) {
                    System.out.println("  [" + partition.get("name") + "] " + 
                            partition.get("mountPoint") + ": " + 
                            partition.get("usedPercentFormatted") + " used, " + 
                            partition.get("freeSpaceFormatted") + " free of " + 
                            partition.get("totalSpaceFormatted"));
                }
            }
            System.out.println();
        }
    }
    
    public List<Map<String, Object>> getDiskInfo() {
        return diskInfo;
    }
}
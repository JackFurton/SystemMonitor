package com.monitor.util;

import org.pcap4j.core.PcapAddress;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.Pcaps;
import org.pcap4j.core.BpfProgram.BpfCompileMode;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.UdpPacket;
import org.pcap4j.packet.namednumber.IpNumber;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility class for working with pcap to get actual network interface information.
 * This uses JNI to interact with the native pcap libraries and get real network stats.
 */
public class PcapNetworkUtil {
    // Store network statistics per interface
    private static final Map<String, InterfaceStats> interfaceStatsMap = new HashMap<>();
    private static final Map<String, PcapHandle> handleMap = new HashMap<>();
    private static ExecutorService executor = Executors.newCachedThreadPool();
    private static boolean monitoringActive = false;
    private static boolean executorShutdown = false;
    
    /**
     * Get real network interfaces using pcap
     */
    public static List<Map<String, Object>> getNetworkInterfaces() {
        List<Map<String, Object>> interfaceList = new ArrayList<>();
        
        try {
            // Get all interfaces from pcap
            List<PcapNetworkInterface> allDevs = Pcaps.findAllDevs();
            
            if (allDevs == null || allDevs.isEmpty()) {
                System.err.println("No network interfaces found with pcap");
                return interfaceList;
            }
            
            for (PcapNetworkInterface dev : allDevs) {
                try {
                    // Skip loopback and inactive interfaces
                    if (dev.isLoopBack()) {
                        continue;
                    }
                    
                    Map<String, Object> netData = new HashMap<>();
                    String name = dev.getName();
                    
                    // Basic network interface info
                    netData.put("name", name);
                    netData.put("displayName", dev.getDescription() != null ? 
                            dev.getDescription() : name);
                    
                    // Get MAC address
                    byte[] macAddr = dev.getLinkLayerAddresses().isEmpty() ? 
                            null : dev.getLinkLayerAddresses().get(0).getAddress();
                    
                    StringBuilder macBuilder = new StringBuilder();
                    if (macAddr != null) {
                        for (byte b : macAddr) {
                            macBuilder.append(String.format("%02X:", b & 0xff));
                        }
                        if (macBuilder.length() > 0) {
                            macBuilder.deleteCharAt(macBuilder.length() - 1);
                        }
                    }
                    netData.put("macAddress", macBuilder.length() > 0 ? 
                            macBuilder.toString() : "Unknown");
                    
                    // MTU - use a default value since pcap4j doesn't expose this directly
                    netData.put("mtu", 1500);
                    
                    // Speed - estimate based on interface type
                    long speed = estimateInterfaceSpeed(name, dev.getDescription());
                    netData.put("speed", speed);
                    netData.put("speedFormatted", FormatUtil.formatBitRate(speed));
                    
                    // Determine if connected based on having addresses
                    boolean hasAddresses = !dev.getAddresses().isEmpty();
                    netData.put("connected", hasAddresses);
                    
                    // Get IP addresses
                    List<String> ipv4Addresses = new ArrayList<>();
                    List<String> ipv6Addresses = new ArrayList<>();
                    
                    for (PcapAddress addr : dev.getAddresses()) {
                        InetAddress inetAddr = addr.getAddress();
                        if (inetAddr instanceof Inet4Address) {
                            ipv4Addresses.add(inetAddr.getHostAddress());
                        } else if (inetAddr instanceof Inet6Address) {
                            ipv6Addresses.add(inetAddr.getHostAddress());
                        }
                    }
                    
                    netData.put("ipv4Addresses", ipv4Addresses.toArray(new String[0]));
                    netData.put("ipv6Addresses", ipv6Addresses.toArray(new String[0]));
                    
                    // Get interface statistics - ensure monitoring is active
                    if (!monitoringActive) {
                        startNetworkMonitoring();
                    }
                    
                    // Get the interface stats
                    InterfaceStats stats = interfaceStatsMap.getOrDefault(name, new InterfaceStats());
                    
                    // Add traffic statistics
                    netData.put("packetsRecv", stats.packetsReceived.get());
                    netData.put("packetsSent", stats.packetsSent.get()); 
                    netData.put("bytesRecv", stats.bytesReceived.get());
                    netData.put("bytesRecvFormatted", FormatUtil.formatBytes(stats.bytesReceived.get()));
                    netData.put("bytesSent", stats.bytesSent.get());
                    netData.put("bytesSentFormatted", FormatUtil.formatBytes(stats.bytesSent.get()));
                    
                    // Calculate rates
                    netData.put("downloadRate", stats.downloadRateBytesPerSec.get());
                    netData.put("downloadRateFormatted", 
                            FormatUtil.formatBytes(stats.downloadRateBytesPerSec.get()) + "/s");
                    netData.put("uploadRate", stats.uploadRateBytesPerSec.get());
                    netData.put("uploadRateFormatted", 
                            FormatUtil.formatBytes(stats.uploadRateBytesPerSec.get()) + "/s");
                    
                    // Add to the list
                    interfaceList.add(netData);
                } catch (Exception e) {
                    System.err.println("Error processing interface " + dev.getName() + ": " + e.getMessage());
                }
            }
        } catch (PcapNativeException e) {
            System.err.println("Error getting network interfaces with pcap: " + e.getMessage());
        }
        
        return interfaceList;
    }
    
    /**
     * Start packet capture on all available network interfaces
     */
    public static void startNetworkMonitoring() {
        if (monitoringActive) {
            return;
        }
        
        try {
            // If executor was shutdown, create a new one
            if (executorShutdown) {
                executor = Executors.newCachedThreadPool();
                executorShutdown = false;
            }
            
            List<PcapNetworkInterface> devs = Pcaps.findAllDevs();
            if (devs == null || devs.isEmpty()) {
                return;
            }
            
            System.out.println("Starting network monitoring on " + devs.size() + " interfaces");
            monitoringActive = true;
            
            for (PcapNetworkInterface dev : devs) {
                if (dev.isLoopBack()) {
                    continue;
                }
                
                final String ifName = dev.getName();
                
                // Initialize stats for this interface if not present
                if (!interfaceStatsMap.containsKey(ifName)) {
                    interfaceStatsMap.put(ifName, new InterfaceStats());
                }
                
                try {
                    // Start a new packet capture thread for this interface
                    executor.execute(() -> {
                        try {
                            // Open a handle to capture packets
                            PcapHandle handle = dev.openLive(65536, PromiscuousMode.PROMISCUOUS, 10);
                            handleMap.put(ifName, handle);
                            
                            // Set a filter to capture only IP traffic
                            handle.setFilter("ip", BpfCompileMode.OPTIMIZE);
                            
                            // Start the packet capture
                            while (monitoringActive) {
                                try {
                                    Packet packet = handle.getNextPacketEx();
                                    processPacket(packet, ifName);
                                } catch (TimeoutException e) {
                                    // Timeout is normal, just continue
                                } catch (Exception e) {
                                    if (monitoringActive) {
                                        System.err.println("Error capturing packet on " + ifName + ": " + e.getMessage());
                                    }
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Error starting capture on " + ifName + ": " + e.getMessage());
                        }
                    });
                } catch (Exception e) {
                    System.err.println("Error scheduling capture for " + ifName + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error starting network monitoring: " + e.getMessage());
        }
    }
    
    /**
     * Stop packet capture on all interfaces
     */
    public static void stopNetworkMonitoring() {
        monitoringActive = false;
        
        System.out.println("Stopping network monitoring...");
        
        // Close all pcap handles
        for (PcapHandle handle : handleMap.values()) {
            try {
                handle.close();
                System.out.println("Closed pcap handle");
            } catch (Exception e) {
                // Ignore errors on shutdown
            }
        }
        handleMap.clear();
        
        // Shutdown the executor
        try {
            executor.shutdownNow();
            executorShutdown = true;
            System.out.println("Network thread pool shutdown");
        } catch (Exception e) {
            System.err.println("Error shutting down executor: " + e.getMessage());
        }
    }
    
    /**
     * Process a captured packet
     */
    private static void processPacket(Packet packet, String interfaceName) {
        if (packet == null || !interfaceStatsMap.containsKey(interfaceName)) {
            return;
        }
        
        InterfaceStats stats = interfaceStatsMap.get(interfaceName);
        int packetSize = packet.length();
        
        // Check if this is an IP packet
        if (packet.contains(IpV4Packet.class)) {
            IpV4Packet ipPacket = packet.get(IpV4Packet.class);
            
            // Determine if it's inbound or outbound based on addresses
            // This is a simplified approach - in reality, determining direction is more complex
            boolean isInbound = true;  // Default to inbound
            
            try {
                // Check protocol type (TCP, UDP, or other)
                boolean isTcp = ipPacket.getHeader().getProtocol() == IpNumber.TCP && packet.contains(TcpPacket.class);
                boolean isUdp = ipPacket.getHeader().getProtocol() == IpNumber.UDP && packet.contains(UdpPacket.class);
                
                // Get protocol-specific details if needed
                if (isTcp) {
                    TcpPacket tcpPacket = packet.get(TcpPacket.class);
                    // Could extract TCP-specific info here if needed
                } else if (isUdp) {
                    UdpPacket udpPacket = packet.get(UdpPacket.class);
                    // Could extract UDP-specific info here if needed
                }
                
                // Update interface statistics (same logic for all protocols)
                if (isInbound) {
                    stats.bytesReceived.addAndGet(packetSize);
                    stats.packetsReceived.incrementAndGet();
                    stats.downloadRateBytesPerSec.addAndGet(packetSize / 2); // Rough estimate
                } else {
                    stats.bytesSent.addAndGet(packetSize);
                    stats.packetsSent.incrementAndGet();
                    stats.uploadRateBytesPerSec.addAndGet(packetSize / 2); // Rough estimate
                }
            } catch (Exception e) {
                System.err.println("Error processing packet: " + e.getMessage());
            }
        }
    }
    
    /**
     * Estimate interface speed based on name and description
     */
    private static long estimateInterfaceSpeed(String name, String description) {
        String lowerName = name.toLowerCase();
        String lowerDesc = description != null ? description.toLowerCase() : "";
        
        // Ethernet connections (usually 1 Gbps)
        if (lowerName.startsWith("eth") || lowerName.startsWith("en") && !lowerName.startsWith("ens")) {
            return 1_000_000_000L;
        }
        
        // Wi-Fi connections (usually 150 Mbps - 866 Mbps)
        if (lowerName.startsWith("wlan") || lowerName.startsWith("wifi") || 
                lowerDesc.contains("wireless") || lowerDesc.contains("wifi") || 
                lowerName.startsWith("en") && (lowerDesc.contains("wireless") || lowerDesc.contains("wifi"))) {
            return 300_000_000L;
        }
        
        // Default to 100 Mbps
        return 100_000_000L;
    }
    
    /**
     * Class to store interface statistics
     */
    private static class InterfaceStats {
        // Traffic counters
        AtomicLong bytesReceived = new AtomicLong(0);
        AtomicLong bytesSent = new AtomicLong(0);
        AtomicLong packetsReceived = new AtomicLong(0);
        AtomicLong packetsSent = new AtomicLong(0);
        
        // Rate counters (bytes per second)
        AtomicLong downloadRateBytesPerSec = new AtomicLong(0);
        AtomicLong uploadRateBytesPerSec = new AtomicLong(0);
        
        // Last update time
        long lastUpdateTime = System.currentTimeMillis();
    }
}
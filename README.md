# Java System Monitor

A comprehensive Java application for monitoring system resources with real-time tracking of CPU, memory, disk, network, and temperature metrics. Features both console and web interfaces.

## Features

- Real-time CPU usage monitoring
- Memory usage statistics with utilization charts
- Process monitoring (top memory-consuming processes)
- Disk usage monitoring with I/O rates and partition details
- GPU monitoring with usage, memory, and temperature metrics
- Network monitoring with bandwidth usage and connection stats
- Temperature sensors for CPU, GPU, and fan speeds 
- Customizable refresh rate
- Responsive web interface with dark mode support
- Complete REST API for system metrics
- Real-time updating charts for CPU and memory usage
- Cross-platform support (Windows, macOS, Linux)
- Extensible architecture for adding custom metrics

## Requirements

- Java 11 or higher
- Maven (for building with dependencies)
- Internet connection (for downloading dependencies)

## How to Run

1. Make the build script executable (if needed):
   ```
   chmod +x build.sh
   ```

2. Run the build script:
   ```
   ./build.sh
   ```

The script will:
- Build the application with Maven
- Run the Spring Boot application

### Platform-Specific Considerations

#### macOS

For macOS, some features require elevated permissions to access hardware information:

```
sudo ./build.sh
```

Benefits of running with sudo:
- Complete network interface monitoring using packet capture (pcap)
- More detailed system information
- Higher access level for hardware sensors

**Note about Apple Silicon (M1/M2/M3):** Some metrics are not directly accessible on Apple Silicon chips:
- **Temperature data:** CPU and GPU temperatures are estimated as Apple doesn't provide official APIs for these sensors
- **GPU metrics:** Usage and memory metrics are simulated as Apple's Metal API requires special permissions
- The dashboard will mark estimated values with an "EST" indicator

#### Linux

For Linux distributions, sudo may be required for:
- Network packet capture
- Raw hardware access
- Some temperature sensors

#### Windows

On Windows, run the Command Prompt as Administrator for:
- Complete hardware access
- Network interface monitoring

### Command Line Options

You can customize the System Monitor with the following options:

- `-p, --processes <count>`: Set the number of top processes to display (default: 5)
- `-r, --refresh <seconds>`: Set the refresh rate in seconds (default: 2)

Examples:
```
./build.sh -- -p 10                  # Show top 10 processes
./build.sh -- -r 5                   # Refresh every 5 seconds
./build.sh -- -p 8 -r 3              # Show top 8 processes, refresh every 3 seconds
```

Note: Use double dash (`--`) to separate Maven arguments from application arguments.

## Web Interface

The web interface is available at http://localhost:8080 when the application is running.

Features:
- Modern, responsive dashboard using Bootstrap
- Real-time charts for CPU and memory usage
- Detailed process table with memory and CPU usage
- Automatic refreshing based on configured refresh rate

## REST API

The main API endpoint provides all system metrics in a single request:

- `GET /api/all` - Complete metrics with formatted values

For backwards compatibility, the following endpoints are also available (they all return the complete metrics):

- `GET /api/` - Main API endpoint
- `GET /api/metrics` - Alias for the main endpoint
- `GET /api/cpu` - Legacy endpoint
- `GET /api/memory` - Legacy endpoint
- `GET /api/processes` - Legacy endpoint
- `GET /api/system` - Legacy endpoint

Example curl command:
```bash
curl http://localhost:8080/api/all
```

### API Response Format

```json
{
  "system": {
    "os": "Apple macOS 15.3.1 build 24D70",
    "refreshRate": 2,
    "timestamp": 1710547892573,
    "runningWithSudo": true
  },
  "cpu": {
    "usage": 39.71,
    "usageFormatted": "39.71%",
    "cores": 11
  },
  "memory": {
    "usage": 69.94,
    "usageFormatted": "69.94%",
    "total": 19327352832,
    "totalFormatted": "18.00 GB",
    "used": 13517832192,
    "usedFormatted": "12.59 GB",
    "available": 5809520640,
    "availableFormatted": "5.41 GB"
  },
  "processes": [
    {
      "pid": 9824,
      "name": "Google Chrome Helper (Renderer)",
      "memory": 581844992,
      "memoryFormatted": "554.88 MB",
      "cpu": 0.68,
      "cpuFormatted": "0.68%",
      "threads": 21
    },
    // Additional processes...
  ],
  "disks": [
    {
      "name": "disk0",
      "model": "APPLE SSD AP0512Q",
      "size": 500277790720,
      "sizeFormatted": "465.92 GB",
      "reads": 892345,
      "writes": 359842,
      "readBytes": 28546704384,
      "readBytesFormatted": "26.59 GB",
      "writeBytes": 15487325184,
      "writeBytesFormatted": "14.42 GB", 
      "readRate": 524288,
      "readRateFormatted": "512.00 KB/s",
      "writeRate": 131072,
      "writeRateFormatted": "128.00 KB/s",
      "partitions": [
        {
          "name": "disk0s1",
          "mountPoint": "/",
          "size": 494384795648,
          "sizeFormatted": "460.43 GB",
          "totalSpace": 494384795648,
          "totalSpaceFormatted": "460.43 GB",
          "freeSpace": 92536807424,
          "freeSpaceFormatted": "86.18 GB",
          "usableSpace": 92536807424,
          "usableSpaceFormatted": "86.18 GB",
          "usedPercent": 81.28,
          "usedPercentFormatted": "81.28%"
        }
        // Additional partitions...
      ]
    }
    // Additional disks...
  ],
  "gpus": [
    {
      "name": "Apple M3 Pro",
      "vendor": "Apple",
      "deviceId": "0x106b",
      "isSimulated": true,
      "usage": 45.8,
      "usageFormatted": "45.80%",
      "totalMemory": 8589934592,
      "totalMemoryFormatted": "8.00 GB",
      "usedMemory": 3004112896,
      "usedMemoryFormatted": "2.80 GB",
      "freeMemory": 5585821696,
      "freeMemoryFormatted": "5.20 GB",
      "memoryUsage": 35.0,
      "memoryUsageFormatted": "35.00%",
      "temperature": 68.5,
      "temperatureFormatted": "68.5°C"
    }
  ],
  "temperature": {
    "cpu": {
      "model": "Apple M3 Pro",
      "temperature": 52.8,
      "temperatureFormatted": "52.8°C",
      "isSimulated": true
    },
    "gpus": [
      {
        "name": "Apple M3 Pro",
        "temperature": 65.2,
        "temperatureFormatted": "65.2°C",
        "isSimulated": true
      }
    ]
  },
  "network": {
    "interfaces": [
      {
        "name": "en0",
        "displayName": "Apple Wi-Fi",
        "ipv4Addresses": ["192.168.1.100"],
        "downloadRate": 1258000,
        "downloadRateFormatted": "1.26 MB/s",
        "uploadRate": 345000,
        "uploadRateFormatted": "345.00 KB/s",
        "requiresSudo": true,
        "isSimulated": false
      }
    ]
  }
}
```

### Notes on Data Accuracy

The API provides real-time system metrics, with some platform-specific limitations:

1. **Apple Silicon (M1/M2/M3) devices:**
   - Temperature data (CPU/GPU) is estimated (`isSimulated: true`)
   - GPU metrics are simulated (`isSimulated: true`)
   - Network metrics require sudo for full detail (`requiresSudo: true`)

2. **All platforms:**
   - Some metrics require elevated permissions for complete accuracy
   - The API indicates simulated data with `isSimulated` properties
```

## Project Structure

- `SystemMonitorApp.java` - Main Spring Boot application entry point
- `core/MonitoringEngine.java` - Core monitoring scheduler
- `metrics/` - Various system metrics collectors:
  - `SystemMetrics.java` - Base system information
  - `CpuMetrics.java` - CPU usage statistics
  - `MemoryMetrics.java` - Memory usage statistics
  - `ProcessMetrics.java` - Process monitoring (memory, CPU usage)
  - `DiskMetrics.java` - Disk usage and I/O statistics
  - `GpuMetrics.java` - GPU monitoring (usage, memory, temperature)
  - `NetworkMetrics.java` - Network interface and bandwidth monitoring
  - `TemperatureMetrics.java` - Temperature sensors for hardware components
- `util/` - Utility classes:
  - `FormatUtil.java` - Formatting utilities for bytes, percentages, etc.
  - `PcapNetworkUtil.java` - JNI wrapper for packet capture via pcap4j
- `api/` - REST API controllers
- `web/` - Web UI controllers
- `resources/templates/` - Thymeleaf HTML templates
- `resources/static/` - Static resources (CSS, JavaScript)

## Technical Implementation Details

### JNI for Native Hardware Access

The application uses Java Native Interface (JNI) through several libraries:

1. **OSHI (Operating System & Hardware Information)**: 
   - Primary library for system metrics
   - Cross-platform hardware information

2. **pcap4j (Packet Capture for Java)**:
   - JNI wrapper around native pcap libraries
   - Used for real network interface monitoring
   - Requires sudo/admin access on most platforms

3. **Simulated Data**:
   - Used where native access isn't available (Apple Silicon sensors)
   - Marked with `isSimulated` flags in API responses
   - Displayed with "EST" indicators in the UI

## Extending the Monitor

The system is designed to be extensible. To add new metrics:

1. Create a new class in the `metrics` package
2. Implement the collection and display logic
3. Add the new metric to the `MonitoringEngine` class
4. Update the REST API and web UI as needed

## Future Enhancements

- Advanced process filtering and sorting options
- Data persistence for historical analysis
- Alert thresholds for high resource usage
- Export metrics to CSV/JSON files
- User authentication for the web interface
- Customizable dashboard layouts
- Dark mode for better viewing in low-light environments
- Mobile companion app for remote monitoring
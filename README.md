# Java System Monitor

A simple, extensible Java application for monitoring system resources like CPU and memory usage, with both console and web interfaces.

## Features

- Real-time CPU usage monitoring
- Memory usage statistics
- Process monitoring (top memory-consuming processes)
- Customizable refresh rate
- **NEW: Web interface with responsive dashboard**
- **NEW: REST API for system metrics**
- **NEW: Real-time charts for CPU and memory usage**
- Extensible architecture for adding new metrics

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

API response format:
```json
{
  "system": {
    "os": "Apple macOS 15.3.1 build 24D70",
    "refreshRate": 2,
    "timestamp": 1710547892573
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
    /* more processes... */
  ]
}
```

## Project Structure

- `SystemMonitorApp.java` - Main Spring Boot application entry point
- `core/MonitoringEngine.java` - Core monitoring scheduler
- `metrics/` - Various system metrics collectors:
  - `SystemMetrics.java` - Base system information
  - `CpuMetrics.java` - CPU usage statistics
  - `MemoryMetrics.java` - Memory usage statistics
  - `ProcessMetrics.java` - Process monitoring (memory, CPU usage)
- `api/` - REST API controllers
- `web/` - Web UI controllers
- `resources/templates/` - Thymeleaf HTML templates
- `resources/static/` - Static resources (CSS, JavaScript)

## Extending the Monitor

The system is designed to be extensible. To add new metrics:

1. Create a new class in the `metrics` package
2. Implement the collection and display logic
3. Add the new metric to the `MonitoringEngine` class
4. Update the REST API and web UI as needed

## Future Enhancements

- Disk usage monitoring
- Network traffic monitoring
- Advanced process filtering and sorting options
- Data persistence for historical analysis
- Alert thresholds for high resource usage
- Export metrics to CSV/JSON files
- User authentication for the web interface
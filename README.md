# Java System Monitor

A simple, extensible Java application for monitoring system resources like CPU and memory usage.

## Features

- Real-time CPU usage monitoring
- Memory usage statistics
- Extensible architecture for adding new metrics
- Console-based display with regular updates

## Requirements

- Java 11 or higher
- Internet connection (for the initial download of dependencies)

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
- Download required dependencies
- Compile the Java code
- Run the application

## Project Structure

- `SystemMonitorApp.java` - Main application entry point
- `core/MonitoringEngine.java` - Core monitoring scheduler
- `metrics/` - Various system metrics collectors:
  - `SystemMetrics.java` - Base system information
  - `CpuMetrics.java` - CPU usage statistics
  - `MemoryMetrics.java` - Memory usage statistics

## Extending the Monitor

The system is designed to be extensible. To add new metrics:

1. Create a new class in the `metrics` package
2. Implement the collection and display logic
3. Add the new metric to the `MonitoringEngine` class

## Future Enhancements

- Disk usage monitoring
- Network traffic monitoring
- Process monitoring
- Data persistence
- Web or GUI interface
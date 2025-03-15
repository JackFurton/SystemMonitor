#!/bin/bash

# Create lib directory if it doesn't exist
mkdir -p lib

# Check if OSHI is already downloaded
if [ ! -f "lib/oshi-core-6.4.8.jar" ]; then
    echo "Downloading OSHI Core..."
    curl -L "https://repo1.maven.org/maven2/com/github/oshi/oshi-core/6.4.8/oshi-core-6.4.8.jar" -o "lib/oshi-core-6.4.8.jar"
fi

# Check if JNA is already downloaded
if [ ! -f "lib/jna-5.13.0.jar" ]; then
    echo "Downloading JNA..."
    curl -L "https://repo1.maven.org/maven2/net/java/dev/jna/jna/5.13.0/jna-5.13.0.jar" -o "lib/jna-5.13.0.jar"
fi

# Check if JNA Platform is already downloaded
if [ ! -f "lib/jna-platform-5.13.0.jar" ]; then
    echo "Downloading JNA Platform..."
    curl -L "https://repo1.maven.org/maven2/net/java/dev/jna/jna-platform/5.13.0/jna-platform-5.13.0.jar" -o "lib/jna-platform-5.13.0.jar"
fi

# Check if SLF4J is already downloaded
if [ ! -f "lib/slf4j-api-2.0.9.jar" ]; then
    echo "Downloading SLF4J API..."
    curl -L "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.9/slf4j-api-2.0.9.jar" -o "lib/slf4j-api-2.0.9.jar"
fi

# Check if SLF4J Simple is already downloaded
if [ ! -f "lib/slf4j-simple-2.0.9.jar" ]; then
    echo "Downloading SLF4J Simple..."
    curl -L "https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/2.0.9/slf4j-simple-2.0.9.jar" -o "lib/slf4j-simple-2.0.9.jar"
fi

# Create build directory
mkdir -p build

# Compile Java code
echo "Compiling Java code..."
javac -d build -cp "lib/*" src/main/java/com/monitor/metrics/*.java src/main/java/com/monitor/core/*.java src/main/java/com/monitor/*.java

# Check if compilation was successful
if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    
    # Run the application
    echo "Running the application..."
    java -cp "build:lib/*" com.monitor.SystemMonitorApp
else
    echo "Compilation failed."
fi
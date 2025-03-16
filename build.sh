#!/bin/bash

# Check if mvn command exists
if ! command -v mvn &> /dev/null; then
    echo "Maven is not installed or not in the PATH. Please install Maven to build the application."
    exit 1
fi

# Parse command line arguments for Maven
MAVEN_ARGS=""
APP_ARGS=""
parsing_app_args=false

for arg in "$@"; do
    if [ "$arg" = "--" ]; then
        # Everything after "--" is passed to the application
        parsing_app_args=true
        continue
    fi
    
    if [ "$parsing_app_args" = true ]; then
        APP_ARGS="$APP_ARGS $arg"
    else
        MAVEN_ARGS="$MAVEN_ARGS $arg"
    fi
done

# Build the application with Maven
echo "Building the application with Maven..."
mvn clean package $MAVEN_ARGS

# Check if the build was successful
if [ $? -eq 0 ]; then
    echo "Build successful!"
    
    # Run the application with Spring Boot
    echo "Starting the System Monitor with Web Interface..."
    echo "Web UI will be available at http://localhost:8080"
    
    # If we have application arguments, pass them to the Spring Boot app
    if [ -n "$APP_ARGS" ]; then
        echo "Running with arguments: $APP_ARGS"
        java -jar target/system-monitor-1.0-SNAPSHOT.jar $APP_ARGS
    else
        java -jar target/system-monitor-1.0-SNAPSHOT.jar
    fi
else
    echo "Build failed."
    exit 1
fi
// Dashboard.js - System Monitor Web UI

// Chart objects
let cpuChart;
let memoryChart;

// Historical data for charts
const cpuHistory = Array(30).fill(0);
const memoryHistory = Array(30).fill(0);

// Last update timestamp
let lastUpdateTime = 0;

// Colors for charts and UI
const cpuColor = {
    primary: 'rgba(13, 110, 253, 1)',
    light: 'rgba(13, 110, 253, 0.1)'
};
const memoryColor = {
    primary: 'rgba(25, 135, 84, 1)',
    light: 'rgba(25, 135, 84, 0.1)'
};

// Initialize the dashboard
document.addEventListener('DOMContentLoaded', function() {
    // Initialize charts
    initCharts();
    
    // Initialize connection status
    document.getElementById('connectionStatus').style.display = 'none';
    
    // Initialize network badge
    document.getElementById('networkBadge').textContent = 'Initializing...';
    
    // Get the refresh rate from the page (in seconds)
    const refreshRateElement = document.querySelector('#refreshRate span');
    const refreshRate = refreshRateElement ? parseInt(refreshRateElement.textContent) * 1000 : 2000;
    
    // Initial data load
    fetchMetrics();
    
    // Set up regular polling
    setInterval(fetchMetrics, refreshRate);
    
    // Add additional interval to clear connection error if data loading works
    setInterval(function() {
        // If we've received data recently (within the last 10 seconds), make sure error is cleared
        if (Date.now() - lastUpdateTime < 10000 && lastUpdateTime > 0) {
            document.getElementById('connectionStatus').style.display = 'none';
        }
    }, 5000); // Check every 5 seconds
    
    // Add animations for the cards
    document.querySelectorAll('.card').forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-5px)';
            this.style.boxShadow = '0 8px 16px rgba(0, 0, 0, 0.2)';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
            this.style.boxShadow = '0 4px 6px rgba(0, 0, 0, 0.1)';
        });
    });
});

// Initialize charts
function initCharts() {
    // Common chart options
    const commonOptions = {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
            y: {
                min: 0,
                max: 100,
                display: false
            },
            x: {
                display: false
            }
        },
        plugins: {
            legend: {
                display: false
            },
            tooltip: {
                enabled: false
            }
        },
        animation: {
            duration: 500
        }
    };
    
    // CPU Chart
    const cpuCtx = document.getElementById('cpuChart').getContext('2d');
    cpuChart = new Chart(cpuCtx, {
        type: 'line',
        data: {
            labels: Array(30).fill(''),
            datasets: [{
                label: 'CPU %',
                data: cpuHistory,
                borderColor: cpuColor.primary,
                backgroundColor: cpuColor.light,
                borderWidth: 2,
                tension: 0.3,
                fill: true
            }]
        },
        options: commonOptions
    });
    
    // Memory Chart
    const memoryCtx = document.getElementById('memoryChart').getContext('2d');
    memoryChart = new Chart(memoryCtx, {
        type: 'line',
        data: {
            labels: Array(30).fill(''),
            datasets: [{
                label: 'Memory %',
                data: memoryHistory,
                borderColor: memoryColor.primary,
                backgroundColor: memoryColor.light,
                borderWidth: 2,
                tension: 0.3,
                fill: true
            }]
        },
        options: commonOptions
    });
}

// Fetch metrics from the API
// Track consecutive failures to prevent repeated retries for network and temperature
let networkFailCount = 0;
let temperatureFailCount = 0;
const MAX_FAIL_COUNT = 3;
// Add a timeout to prevent indefinite loading
const FETCH_TIMEOUT = 10000; // 10 seconds
// Flag to track if we're running with sudo
let isRunningSudo = false;

function fetchMetrics() {
    console.log('Fetching metrics...');
    // Set up an abort controller to handle timeouts
    const controller = new AbortController();
    const timeoutId = setTimeout(function() { controller.abort(); }, FETCH_TIMEOUT);

    console.log('Starting fetch from /api/all');
    fetch('/api/all', { signal: controller.signal })
        .then(function(response) {
            clearTimeout(timeoutId);
            console.log('Response received:', response.status);
            if (!response.ok) {
                throw new Error('HTTP error! Status: ' + response.status);
            }
            return response.json().catch(function(e) {
                console.error('JSON parse error:', e);
                throw new Error('Failed to parse JSON response');
            });
        })
        .then(function(data) {
            console.log('Received API data successfully', data);
            
            try {
                // Update timestamp for last refresh
                lastUpdateTime = data.system.timestamp;
                
                // Hide any connection error message
                document.getElementById('connectionStatus').style.display = 'none';
                
                // Log the data we're about to use
                console.log('CPU data:', data.cpu);
                console.log('Memory data:', data.memory);
                
                // Update all the core metrics
                if (data.system) updateSystemInfo(data.system);
                if (data.cpu) updateCpuMetrics(data.cpu);
                if (data.memory) updateMemoryMetrics(data.memory);
                if (data.processes) updateProcessTable(data.processes);
                if (data.disks) updateDiskInfo(data.disks);
                if (data.gpus) updateGpuInfo(data.gpus);
                if (data.network) updateNetworkInfo(data.network);
                if (data.temperature) updateTemperatureInfo(data.temperature);
            } catch (e) {
                console.error("Error updating metrics:", e);
            }
            
            // Check for sudo either from system property or detected network interfaces
            if (data.system && data.system.runningWithSudo === true) {
                isRunningSudo = true;
            }
            
            // Set the badge status
            const networkBadge = document.getElementById('networkBadge');
            if (networkBadge) {
                if (isRunningSudo) {
                    networkBadge.classList.remove('bg-light', 'text-primary');
                    networkBadge.classList.add('bg-success', 'text-white');
                    networkBadge.textContent = 'Full Access';
                } else {
                    networkBadge.classList.remove('bg-success', 'text-white');
                    networkBadge.classList.add('bg-light', 'text-primary');
                    networkBadge.textContent = 'Sudo Required';
                }
            }
                        
            // This is a duplicate section - remove it to avoid double updates
            
            // We already set the badge above, so we don't need this section
            
            // Add a subtle animation to the cards to show they were updated
            document.querySelectorAll('.card').forEach(function(card) {
                card.classList.add('updated');
                setTimeout(function() { card.classList.remove('updated'); }, 300);
            });
        })
        .catch(function(error) {
            clearTimeout(timeoutId);
            console.error('Error fetching metrics:', error);
            // Show an error message on the UI
            console.error('Error fetching metrics - will display connection error');
            const connectionStatus = document.getElementById('connectionStatus');
            if (connectionStatus) {
                connectionStatus.textContent = 'Connection Error';
                connectionStatus.className = 'badge bg-danger';
                connectionStatus.style.display = 'inline-block';
            }
        });
}

// Update system info display
function updateSystemInfo(system) {
    // Clear any error messages when we get successful system info
    const systemInfoEl = document.getElementById('systemInfo');
    systemInfoEl.textContent = system.os;
    systemInfoEl.classList.remove('text-danger');
    
    // Check for sudo access
    if (system.runningWithSudo) {
        // Add a shield icon to show we have sudo access
        systemInfoEl.innerHTML += ' <i class="bi bi-shield-check text-success" title="Running with sudo"></i>';
        // Set global flag so other functions know we have sudo
        isRunningSudo = true;
    }
    
    // Update refresh rate info if it's different than what's displayed
    const refreshRateElement = document.querySelector('#refreshRate span');
    if (refreshRateElement && parseInt(refreshRateElement.textContent) !== system.refreshRate) {
        refreshRateElement.textContent = system.refreshRate;
    }
    
    // Update last refresh time
    const lastRefreshElement = document.getElementById('lastRefresh');
    if (lastRefreshElement) {
        const date = new Date(system.timestamp);
        lastRefreshElement.textContent = date.toLocaleTimeString();
    }
}

// Update CPU metrics display
function updateCpuMetrics(cpu) {
    console.log("Updating CPU metrics:", cpu);
    try {
        // Display formatted values where available
        document.getElementById('cpuUsage').textContent = cpu.usageFormatted || (cpu.usage.toFixed(2) + '%');
        document.getElementById('cpuCores').textContent = cpu.cores + ' cores';
        
        // Update progress indicator if it exists
        const cpuProgressBar = document.getElementById('cpuProgressBar');
        if (cpuProgressBar) {
            cpuProgressBar.style.width = cpu.usage + '%';
            
            // Adjust color based on usage
            if (cpu.usage > 80) {
                cpuProgressBar.classList.remove('bg-success', 'bg-warning');
                cpuProgressBar.classList.add('bg-danger');
            } else if (cpu.usage > 50) {
                cpuProgressBar.classList.remove('bg-success', 'bg-danger');
                cpuProgressBar.classList.add('bg-warning');
            } else {
                cpuProgressBar.classList.remove('bg-warning', 'bg-danger');
                cpuProgressBar.classList.add('bg-success');
            }
        }
        
        // Update chart
        cpuHistory.push(cpu.usage);
        cpuHistory.shift();
        cpuChart.data.datasets[0].data = cpuHistory;
        cpuChart.update('none'); // Update without animation for smoother updates
    } catch (e) {
        console.error("Error updating CPU metrics:", e);
    }
}

// Update memory metrics display
function updateMemoryMetrics(memory) {
    console.log("Updating memory metrics:", memory);
    try {
        // Display formatted values where available
        document.getElementById('memoryUsage').textContent = memory.usageFormatted || (memory.usage.toFixed(2) + '%');
        
        // Update the progress bar
        const memoryProgressBar = document.getElementById('memoryProgressBar');
        if (memoryProgressBar) {
            memoryProgressBar.style.width = memory.usage + '%';
            
            // Adjust color based on usage
            if (memory.usage > 80) {
                memoryProgressBar.classList.remove('bg-success', 'bg-warning');
                memoryProgressBar.classList.add('bg-danger');
            } else if (memory.usage > 60) {
                memoryProgressBar.classList.remove('bg-success', 'bg-danger');
                memoryProgressBar.classList.add('bg-warning');
            } else {
                memoryProgressBar.classList.remove('bg-warning', 'bg-danger');
                memoryProgressBar.classList.add('bg-success');
            }
        }
        
        // Update memory details using formatted values
        const memoryDetails = `Total: ${memory.totalFormatted} | Used: ${memory.usedFormatted} | Available: ${memory.availableFormatted}`;
        document.getElementById('memoryDetails').textContent = memoryDetails;
        
        // Update chart
        memoryHistory.push(memory.usage);
        memoryHistory.shift();
        memoryChart.data.datasets[0].data = memoryHistory;
        memoryChart.update('none'); // Update without animation for smoother updates
    } catch (e) {
        console.error("Error updating memory metrics:", e);
    }
}

// Update process table
function updateProcessTable(processes) {
    console.log("Updating process table:", processes);
    try {
        const tableBody = document.getElementById('processTable');
        if (!tableBody) return;
        
        tableBody.innerHTML = '';
        
        processes.forEach(function(process) {
            const row = document.createElement('tr');
            
            // Highlight high CPU or memory usage
            if (process.cpu > 80 || process.memory > 1000000000) {
                row.classList.add('table-warning');
            }
            
            // Use the formatted values from the API
            row.innerHTML = `
                <td>${process.pid}</td>
                <td>${process.name}</td>
                <td>${process.memoryFormatted}</td>
                <td>${process.cpuFormatted}</td>
                <td>${process.threads}</td>
            `;
            
            tableBody.appendChild(row);
        });
    } catch (e) {
        console.error("Error updating process table:", e);
    }
}

// Update disk information
function updateDiskInfo(disks) {
    console.log("Updating disk info:", disks);
    try {
        const diskContainer = document.getElementById('diskContainer');
        if (!diskContainer) return;
        
        diskContainer.innerHTML = '';
        
        if (!disks || disks.length === 0) {
            diskContainer.innerHTML = '<div class="alert alert-info">No disk information available</div>';
            return;
        }
    
    // Create disk overview cards
    for (var i = 0; i < disks.length; i++) {
        var disk = disks[i];
        var diskCard = document.createElement('div');
        diskCard.classList.add('mb-4');
        
        // Basic disk info
        diskCard.innerHTML = `
            <h5><i class="bi bi-hdd me-2"></i>${disk.name} (${disk.model || 'Unknown'})</h5>
            <p class="mb-2">Size: ${disk.sizeFormatted}</p>
            <p class="mb-2">I/O: Read ${disk.readRateFormatted}, Write ${disk.writeRateFormatted}</p>
        `;
        
        // Create partition cards if available
        if (disk.partitions && disk.partitions.length > 0) {
            const partitionsContainer = document.createElement('div');
            partitionsContainer.classList.add('ms-3');
            
            disk.partitions.forEach(partition => {
                // Only show partitions with valid mount points and usage info
                if (partition.mountPoint && partition.totalSpace && partition.totalSpace > 0) {
                    const partCard = document.createElement('div');
                    partCard.classList.add('card', 'mb-2');
                    
                    // Determine color class based on usage
                    let colorClass = 'bg-success';
                    if (partition.usedPercent > 85) {
                        colorClass = 'bg-danger';
                    } else if (partition.usedPercent > 70) {
                        colorClass = 'bg-warning';
                    }
                    
                    partCard.innerHTML = `
                        <div class="card-body p-2">
                            <p class="mb-1"><strong>${partition.mountPoint}</strong></p>
                            <div class="progress mb-2" style="height: 8px;">
                                <div class="progress-bar ${colorClass}" role="progressbar" 
                                    style="width: ${partition.usedPercent}%;" 
                                    aria-valuenow="${partition.usedPercent}" 
                                    aria-valuemin="0" 
                                    aria-valuemax="100">
                                </div>
                            </div>
                            <p class="small text-muted mb-0">
                                ${partition.usedPercentFormatted} used 
                                (${partition.freeSpaceFormatted} free of ${partition.totalSpaceFormatted})
                            </p>
                        </div>
                    `;
                    
                    partitionsContainer.appendChild(partCard);
                }
            });
            
            if (partitionsContainer.children.length > 0) {
                diskCard.appendChild(partitionsContainer);
            } else {
                diskCard.innerHTML += '<p class="text-muted">No mountable partitions found</p>';
            }
        }
        
        // Add a separator between disks
        const separator = document.createElement('hr');
        diskCard.appendChild(separator);
        
        diskContainer.appendChild(diskCard);
    }
    } catch (e) {
        console.error("Error updating disk info:", e);
    }
}

// Update GPU information
function updateGpuInfo(gpus) {
    console.log("Updating GPU info:", gpus);
    try {
        const gpuContainer = document.getElementById('gpuContainer');
        if (!gpuContainer) return;
        
        gpuContainer.innerHTML = '';
        
        if (!gpus || gpus.length === 0) {
            gpuContainer.innerHTML = '<div class="alert alert-info">No GPU information available</div>';
            return;
        }
    
    // Create GPU cards
    gpus.forEach(function(gpu) {
        const gpuCard = document.createElement('div');
        gpuCard.classList.add('mb-4');
        
        // Determine temperature color class
        let temperatureClass = 'text-success';
        if (gpu.temperature > 80) {
            temperatureClass = 'text-danger';
        } else if (gpu.temperature > 70) {
            temperatureClass = 'text-warning';
        }
        
        // Basic GPU info
        gpuCard.innerHTML = `
            <div class="d-flex justify-content-between align-items-start">
                <h5><i class="bi bi-gpu-card me-2"></i>${gpu.name}</h5>
                <span class="badge ${temperatureClass}">${gpu.temperatureFormatted}</span>
            </div>
            <p class="text-muted small mb-2">Vendor: ${gpu.vendor || 'Unknown'}</p>
        `;
        
        // Create usage metric rows
        const metricsContainer = document.createElement('div');
        metricsContainer.classList.add('mt-3');
        
        // GPU Usage
        const gpuUsageRow = document.createElement('div');
        gpuUsageRow.classList.add('mb-3');
        
        let usageColorClass = 'bg-success';
        if (gpu.usage > 80) {
            usageColorClass = 'bg-danger';
        } else if (gpu.usage > 60) {
            usageColorClass = 'bg-warning';
        }
        
        gpuUsageRow.innerHTML = `
            <div class="d-flex justify-content-between mb-1">
                <span>GPU Usage</span>
                <span>${gpu.usageFormatted}</span>
            </div>
            <div class="progress" style="height: 10px;">
                <div class="progress-bar ${usageColorClass}" role="progressbar" 
                    style="width: ${gpu.usage}%;" 
                    aria-valuenow="${gpu.usage}" 
                    aria-valuemin="0" 
                    aria-valuemax="100">
                </div>
            </div>
        `;
        
        metricsContainer.appendChild(gpuUsageRow);
        
        // GPU Memory
        const gpuMemoryRow = document.createElement('div');
        gpuMemoryRow.classList.add('mb-3');
        
        let memoryColorClass = 'bg-success';
        if (gpu.memoryUsage > 80) {
            memoryColorClass = 'bg-danger';
        } else if (gpu.memoryUsage > 60) {
            memoryColorClass = 'bg-warning';
        }
        
        gpuMemoryRow.innerHTML = `
            <div class="d-flex justify-content-between mb-1">
                <span>Memory Usage</span>
                <span>${gpu.memoryUsageFormatted}</span>
            </div>
            <div class="progress" style="height: 10px;">
                <div class="progress-bar ${memoryColorClass}" role="progressbar" 
                    style="width: ${gpu.memoryUsage}%;" 
                    aria-valuenow="${gpu.memoryUsage}" 
                    aria-valuemin="0" 
                    aria-valuemax="100">
                </div>
            </div>
            <div class="small text-muted mt-1">
                ${gpu.usedMemoryFormatted} / ${gpu.totalMemoryFormatted}
            </div>
        `;
        
        metricsContainer.appendChild(gpuMemoryRow);
        
        // Add the metrics container to the GPU card
        gpuCard.appendChild(metricsContainer);
        
        // Add a divider between GPUs
        if (gpus.length > 1) {
            const separator = document.createElement('hr');
            gpuCard.appendChild(separator);
        }
        
        gpuContainer.appendChild(gpuCard);
    }); // Close the forEach
    } catch (e) {
        console.error("Error updating GPU info:", e);
    }
}

// Update Network information
function updateNetworkInfo(networkData) {
    console.log("Updating network info:", networkData);
    try {
        const networkContainer = document.getElementById('networkContainer');
        if (!networkContainer) return;
        
        // Clear existing content
        networkContainer.innerHTML = '';
        
        if (!networkData || Object.keys(networkData).length === 0) {
            networkContainer.innerHTML = '<div class="alert alert-info">No network information available</div>';
            return;
        }
    
        // Get global network info (first item in array)
        const globalInfo = networkData;
        
        // Create hostname/domain header
        const globalInfoCard = document.createElement('div');
        globalInfoCard.classList.add('mb-3');
        
        // Display hostname/domain if available
        let hostnameDisplay = globalInfo.hostname || 'Unknown Host';
        let domainDisplay = globalInfo.domain && globalInfo.domain !== 'Unknown' ? 
                           `Domain: ${globalInfo.domain}` : '';
        let gatewayDisplay = globalInfo.ipv4DefaultGateway && globalInfo.ipv4DefaultGateway !== 'Unknown' ? 
                            `Gateway: ${globalInfo.ipv4DefaultGateway}` : '';
        
        if (domainDisplay && gatewayDisplay) {
            globalInfoCard.innerHTML = `
                <h5>${hostnameDisplay}</h5>
                <p class="text-muted small mb-3">${domainDisplay} | ${gatewayDisplay}</p>
            `;
        } else if (domainDisplay || gatewayDisplay) {
            globalInfoCard.innerHTML = `
                <h5>${hostnameDisplay}</h5>
                <p class="text-muted small mb-3">${domainDisplay}${gatewayDisplay}</p>
            `;
        } else {
            globalInfoCard.innerHTML = `<h5>${hostnameDisplay}</h5>`;
        }
        
        // Display TCP connection stats if available
        if (globalInfo.tcp) {
            const tcpStatsCard = document.createElement('div');
            tcpStatsCard.classList.add('card', 'mb-3');
            
            // Add more detailed TCP stats
            tcpStatsCard.innerHTML = `
                <div class="card-body p-2">
                    <h6 class="card-title">TCP/IP Statistics</h6>
                    <div class="row">
                        <div class="col-md-4">
                            <p class="small mb-1 fw-bold">Connections</p>
                            <p class="small mb-1">Established: ${globalInfo.tcp.connectionsEstablished || 0}</p>
                            <p class="small mb-1">Active: ${globalInfo.tcp.connectionsActive || 0}</p>
                            <p class="small mb-1">Passive: ${globalInfo.tcp.connectionsPassive || 0}</p>
                        </div>
                        <div class="col-md-4">
                            <p class="small mb-1 fw-bold">Segments</p>
                            <p class="small mb-1">Sent: ${globalInfo.tcp.segmentsSent?.toLocaleString() || 0}</p>
                            <p class="small mb-1">Received: ${globalInfo.tcp.segmentsReceived?.toLocaleString() || 0}</p>
                            <p class="small mb-1">Retransmitted: ${globalInfo.tcp.segmentsRetransmitted?.toLocaleString() || 0}</p>
                        </div>
                        <div class="col-md-4">
                            <p class="small mb-1 fw-bold">Errors</p>
                            <p class="small mb-1">Failures: ${globalInfo.tcp.connectionsFailures?.toLocaleString() || 0}</p>
                            <p class="small mb-1">In Errors: ${globalInfo.tcp.inErrors?.toLocaleString() || 0}</p>
                            <p class="small mb-1">Out Resets: ${globalInfo.tcp.outResets?.toLocaleString() || 0}</p>
                        </div>
                    </div>
                </div>
            `;
            globalInfoCard.appendChild(tcpStatsCard);
        }
        
        // Add UDP stats if available
        if (globalInfo.udp) {
            const udpStatsCard = document.createElement('div');
            udpStatsCard.classList.add('card', 'mb-3');
            udpStatsCard.innerHTML = `
                <div class="card-body p-2">
                    <h6 class="card-title">UDP Statistics</h6>
                    <div class="row">
                        <div class="col-md-6">
                            <p class="small mb-1">Datagrams Sent: ${globalInfo.udp.datagramsSent?.toLocaleString() || 0}</p>
                            <p class="small mb-1">Datagrams Received: ${globalInfo.udp.datagramsReceived?.toLocaleString() || 0}</p>
                        </div>
                        <div class="col-md-6">
                            <p class="small mb-1">No Port: ${globalInfo.udp.datagramsNoPort?.toLocaleString() || 0}</p>
                            <p class="small mb-1">Receive Errors: ${globalInfo.udp.datagramsReceivedErrors?.toLocaleString() || 0}</p>
                        </div>
                    </div>
                </div>
            `;
            globalInfoCard.appendChild(udpStatsCard);
        }
        
        networkContainer.appendChild(globalInfoCard);
        
        // Network interfaces from the interfaces array
        if (globalInfo.interfaces && globalInfo.interfaces.length > 0) {
            const interfacesContainer = document.createElement('div');
            interfacesContainer.classList.add('mt-4');
            
            let activeInterfacesFound = false;
            
            // Show only connected interfaces
            for (let i = 0; i < globalInfo.interfaces.length; i++) {
                const networkInterface = globalInfo.interfaces[i];
                const isConnected = networkInterface.connected;
                
                // Only show connected interfaces with IP addresses to reduce clutter
                if (isConnected && networkInterface.ipv4Addresses && networkInterface.ipv4Addresses.length > 0) {
                    activeInterfacesFound = true;
                    
                    const interfaceCard = document.createElement('div');
                    interfaceCard.classList.add('card', 'mb-3');
                    
                    // Interface name and IP
                    let ipAddresses = '';
                    if (networkInterface.ipv4Addresses && networkInterface.ipv4Addresses.length > 0) {
                        networkInterface.ipv4Addresses.forEach(ip => {
                            ipAddresses += `<span class="badge bg-secondary me-1">${ip}</span>`;
                        });
                    }
                    
                    // Add IPv6 addresses if available
                    let ipv6Addresses = '';
                    if (networkInterface.ipv6Addresses && networkInterface.ipv6Addresses.length > 0) {
                        networkInterface.ipv6Addresses.forEach(ip => {
                            // Truncate long IPv6 addresses for display
                            const displayIp = ip.length > 20 ? ip.substring(0, 18) + '...' : ip;
                            ipv6Addresses += `<span class="badge bg-info text-dark me-1" title="${ip}">${displayIp}</span>`;
                        });
                    }
                    
                    // Interface card header with name and IPs
                    const cardHeader = document.createElement('div');
                    cardHeader.classList.add('card-header', 'py-2', 'px-3');
                    cardHeader.innerHTML = `
                        <div class="d-flex justify-content-between align-items-center">
                            <span>${networkInterface.displayName || networkInterface.name}</span>
                            <div>${ipAddresses}</div>
                        </div>
                    `;
                    
                    if (ipv6Addresses) {
                        cardHeader.innerHTML += `
                            <div class="d-flex justify-content-end mt-1">
                                <small class="text-muted me-1">IPv6:</small>${ipv6Addresses}
                            </div>
                        `;
                    }
                    
                    interfaceCard.appendChild(cardHeader);
                    
                    // Interface card body with traffic and packet data
                    const cardBody = document.createElement('div');
                    cardBody.classList.add('card-body', 'p-3');
                    
                    // Transfer rate displays with progress bars
                    cardBody.innerHTML = `
                        <div class="row g-2">
                            <div class="col-md-6">
                                <div class="d-flex justify-content-between">
                                    <span class="small">Download:</span>
                                    <span class="small fw-bold">${networkInterface.downloadRateFormatted}</span>
                                </div>
                                <div class="progress" style="height: 8px;">
                                    <div class="progress-bar bg-success" style="width: 100%"></div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="d-flex justify-content-between">
                                    <span class="small">Upload:</span>
                                    <span class="small fw-bold">${networkInterface.uploadRateFormatted}</span>
                                </div>
                                <div class="progress" style="height: 8px;">
                                    <div class="progress-bar bg-primary" style="width: 100%"></div>
                                </div>
                            </div>
                        </div>
                    `;
                    
                    // Add detailed packet statistics section
                    const packetStats = document.createElement('div');
                    packetStats.classList.add('card', 'mt-2', 'border-light');
                    
                    const packetHeader = document.createElement('div');
                    packetHeader.classList.add('card-header', 'py-1', 'bg-light');
                    packetHeader.innerHTML = '<span class="small fw-bold">Packet Statistics</span>';
                    
                    const packetBody = document.createElement('div');
                    packetBody.classList.add('card-body', 'py-2', 'px-3');
                    
                    // Format packet counts and calculate packet size averages
                    const packetsRecv = networkInterface.packetsRecv || 0;
                    const packetsSent = networkInterface.packetsSent || 0;
                    const bytesRecv = networkInterface.bytesRecv || 0;
                    const bytesSent = networkInterface.bytesSent || 0;
                    
                    const avgPacketSizeRecv = packetsRecv > 0 ? Math.round(bytesRecv / packetsRecv) : 0;
                    const avgPacketSizeSent = packetsSent > 0 ? Math.round(bytesSent / packetsSent) : 0;
                    
                    packetBody.innerHTML = `
                        <div class="row g-2">
                            <div class="col-md-6">
                                <div class="small">
                                    <div class="d-flex justify-content-between">
                                        <span>Packets Received:</span>
                                        <span class="fw-bold">${packetsRecv.toLocaleString()}</span>
                                    </div>
                                    <div class="d-flex justify-content-between">
                                        <span>Total Received:</span>
                                        <span class="fw-bold">${networkInterface.bytesRecvFormatted}</span>
                                    </div>
                                    <div class="d-flex justify-content-between">
                                        <span>Avg. Packet Size:</span>
                                        <span class="fw-bold">${avgPacketSizeRecv} bytes</span>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="small">
                                    <div class="d-flex justify-content-between">
                                        <span>Packets Sent:</span>
                                        <span class="fw-bold">${packetsSent.toLocaleString()}</span>
                                    </div>
                                    <div class="d-flex justify-content-between">
                                        <span>Total Sent:</span>
                                        <span class="fw-bold">${networkInterface.bytesSentFormatted}</span>
                                    </div>
                                    <div class="d-flex justify-content-between">
                                        <span>Avg. Packet Size:</span>
                                        <span class="fw-bold">${avgPacketSizeSent} bytes</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    `;
                    
                    packetStats.appendChild(packetHeader);
                    packetStats.appendChild(packetBody);
                    cardBody.appendChild(packetStats);
                    
                    // Add hardware info at the bottom
                    cardBody.innerHTML += `
                        <div class="small text-muted mt-2">
                            <div class="row">
                                <div class="col-md-6">
                                    <span><i class="bi bi-link-45deg"></i> NIC Speed: ${networkInterface.speedFormatted}</span>
                                </div>
                                <div class="col-md-6">
                                    <span><i class="bi bi-cpu"></i> MTU: ${networkInterface.mtu || 'Unknown'}</span>
                                </div>
                            </div>
                            <div><i class="bi bi-ethernet"></i> MAC: ${networkInterface.macAddress}</div>
                        </div>
                    `;
                    
                    interfaceCard.appendChild(cardBody);
                    interfacesContainer.appendChild(interfaceCard);
                }
            }
            
            if (!activeInterfacesFound) {
                interfacesContainer.innerHTML = '<div class="alert alert-warning">No active network interfaces detected</div>';
            }
            
            networkContainer.appendChild(interfacesContainer);
        } else {
            networkContainer.innerHTML += '<div class="alert alert-warning">No network interfaces available</div>';
        }
    } catch (e) {
        console.error("Error updating network info:", e);
        const networkContainer = document.getElementById('networkContainer');
        if (networkContainer) {
            networkContainer.innerHTML = '<div class="alert alert-danger">Error processing network data</div>';
        }
    }
}

// Update Temperature information
function updateTemperatureInfo(temperature) {
    console.log("Updating temperature data:", temperature);
    try {
        const temperatureContainer = document.getElementById('temperatureContainer');
        if (!temperatureContainer) return;
        
        // Clear existing content
        temperatureContainer.innerHTML = '';
        
        if (!temperature || Object.keys(temperature).length === 0) {
            temperatureContainer.innerHTML = '<div class="alert alert-info">No temperature information available</div>';
            return;
        }
        
        // CPU temperature
        if (temperature.cpu) {
            const cpuTemp = temperature.cpu;
            const cpuCard = document.createElement('div');
            cpuCard.classList.add('mb-4');
            
            // Determine temperature class
            let tempClass = 'text-success';
            let tempBadgeClass = 'bg-success';
            
            if (cpuTemp.temperature > 85) {
                tempClass = 'text-danger';
                tempBadgeClass = 'bg-danger';
            } else if (cpuTemp.temperature > 70) {
                tempClass = 'text-warning';
                tempBadgeClass = 'bg-warning';
            }
            
            // Check if Apple Silicon temperature is simulated
            const isAppleSilicon = cpuTemp.model && cpuTemp.model.includes('Apple');
            const isSimulated = isAppleSilicon && cpuTemp.temperature > 0;
            
            // CPU info with simulation indicator for Apple Silicon
            cpuCard.innerHTML = `
                <div class="d-flex justify-content-between align-items-center mb-2">
                    <h5 class="mb-0"><i class="bi bi-cpu me-2"></i>CPU</h5>
                    <div>
                        ${isSimulated ? '<span class="badge bg-secondary me-1" title="Estimated temperature">EST</span>' : ''}
                        <span class="badge ${tempBadgeClass}">${cpuTemp.temperatureFormatted}</span>
                    </div>
                </div>
                <p class="text-muted small mb-3">${cpuTemp.model || 'Unknown CPU'}</p>
            `;
            
            // CPU details
            const cpuDetails = document.createElement('div');
            cpuDetails.classList.add('small', 'text-muted');
            cpuDetails.innerHTML = `
                <div class="row">
                    <div class="col-6">Physical cores: ${cpuTemp.physicalCores || 'N/A'}</div>
                    <div class="col-6">Logical cores: ${cpuTemp.logicalCores || 'N/A'}</div>
                </div>
            `;
            
            if (cpuTemp.voltage) {
                cpuDetails.innerHTML += `<div class="mt-1">Voltage: ${cpuTemp.voltageFormatted}</div>`;
            }
            
            cpuCard.appendChild(cpuDetails);
            temperatureContainer.appendChild(cpuCard);
        }
        
        // Fan speeds
        if (temperature.fans && temperature.fans.length > 0) {
            const fansCard = document.createElement('div');
            fansCard.classList.add('card', 'mb-3');
            
            let fansHtml = `
                <div class="card-header py-2">
                    <i class="bi bi-fan me-1"></i> Fans
                </div>
                <div class="card-body p-2">
                    <div class="row">
            `;
            
            temperature.fans.forEach(fan => {
                fansHtml += `
                    <div class="col-md-4 col-6">
                        <div class="text-center">
                            <div class="small">#${fan.id}</div>
                            <div class="fw-bold">${fan.rpmFormatted}</div>
                        </div>
                    </div>
                `;
            });
            
            fansHtml += `
                    </div>
                </div>
            `;
            
            fansCard.innerHTML = fansHtml;
            temperatureContainer.appendChild(fansCard);
        }
        
        // GPU temperatures
        if (temperature.gpus && temperature.gpus.length > 0) {
            const gpusContainer = document.createElement('div');
            gpusContainer.classList.add('mt-3');
            
            const gpusHeading = document.createElement('h6');
            gpusHeading.innerHTML = '<i class="bi bi-gpu-card me-1"></i> GPUs';
            gpusContainer.appendChild(gpusHeading);
            
            temperature.gpus.forEach(gpu => {
                const gpuTemp = document.createElement('div');
                gpuTemp.classList.add('d-flex', 'justify-content-between', 'align-items-center', 'mt-2');
                
                // Determine temperature class
                let tempClass = 'bg-success';
                if (gpu.temperature > 85) {
                    tempClass = 'bg-danger';
                } else if (gpu.temperature > 70) {
                    tempClass = 'bg-warning text-dark';
                }
                
                // Check if Apple GPU temperature is simulated
                const isAppleGPU = gpu.name && gpu.name.includes('Apple');
                const isSimulated = isAppleGPU && gpu.temperature > 0;
                
                gpuTemp.innerHTML = `
                    <span class="small">${gpu.name}</span>
                    <div>
                        ${isSimulated ? '<span class="badge bg-secondary me-1" title="Estimated temperature">EST</span>' : ''}
                        <span class="badge ${tempClass}">${gpu.temperatureFormatted}</span>
                    </div>
                `;
                
                gpusContainer.appendChild(gpuTemp);
            });
            
            temperatureContainer.appendChild(gpusContainer);
        }
    } catch (e) {
        console.error('Error processing temperature data:', e);
        const temperatureContainer = document.getElementById('temperatureContainer');
        if (temperatureContainer) {
            temperatureContainer.innerHTML = '<div class="alert alert-warning">Error processing temperature data</div>';
        }
    }
}
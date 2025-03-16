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
    
    // Get the refresh rate from the page (in seconds)
    const refreshRateElement = document.querySelector('#refreshRate span');
    const refreshRate = refreshRateElement ? parseInt(refreshRateElement.textContent) * 1000 : 2000;
    
    // Initial data load
    fetchMetrics();
    
    // Set up regular polling
    setInterval(fetchMetrics, refreshRate);
    
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
function fetchMetrics() {
    fetch('/api/all')
        .then(response => response.json())
        .then(data => {
            // Update timestamp for last refresh
            lastUpdateTime = data.system.timestamp;
            
            // Update the UI with the new data
            updateSystemInfo(data.system);
            updateCpuMetrics(data.cpu);
            updateMemoryMetrics(data.memory);
            updateProcessTable(data.processes);
            
            // Add a subtle animation to the cards to show they were updated
            document.querySelectorAll('.card').forEach(card => {
                card.classList.add('updated');
                setTimeout(() => card.classList.remove('updated'), 300);
            });
        })
        .catch(error => {
            console.error('Error fetching metrics:', error);
            // Show an error message on the UI
            document.getElementById('systemInfo').innerHTML = '<span class="text-danger"><i class="bi bi-exclamation-triangle"></i> Connection error</span>';
        });
}

// Update system info display
function updateSystemInfo(system) {
    document.getElementById('systemInfo').textContent = system.os;
    
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
}

// Update memory metrics display
function updateMemoryMetrics(memory) {
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
    const memoryDetails = `Total: ${memory.totalFormatted} | 
                          Used: ${memory.usedFormatted} | 
                          Available: ${memory.availableFormatted}`;
    document.getElementById('memoryDetails').textContent = memoryDetails;
    
    // Update chart
    memoryHistory.push(memory.usage);
    memoryHistory.shift();
    memoryChart.data.datasets[0].data = memoryHistory;
    memoryChart.update('none'); // Update without animation for smoother updates
}

// Update process table
function updateProcessTable(processes) {
    const tableBody = document.getElementById('processTable');
    if (!tableBody) return;
    
    tableBody.innerHTML = '';
    
    processes.forEach(process => {
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
}
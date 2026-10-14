param(
    [switch]$WithTunnel = $true,
    [int]$Port = 8085
)

$baseDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $baseDir

if (!(Test-Path "$baseDir\logs")) {
    New-Item -ItemType Directory -Path "$baseDir\logs" | Out-Null
}

$jarPath = "$baseDir\target\public-transport-management-system-1.0.0.jar"
if (!(Test-Path $jarPath)) {
    Write-Host "[PTMS] Executable JAR not found. Building now..." -ForegroundColor Yellow
    mvn clean package -DskipTests
}

# Stop any currently running instance on the port
$existing = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
if ($existing) {
    Write-Host "[PTMS] Detected existing listener on port $Port (PID: $($existing.OwningProcess)). Stopping it first..." -ForegroundColor Yellow
    & "$baseDir\stop-service.ps1"
    Start-Sleep -Seconds 1
}

Write-Host "[PTMS] Starting PTMS Java Web Server in background on port $Port..." -ForegroundColor Cyan

$env:PORT = "$Port"
$javaCmd = "javaw -Dfile.encoding=UTF-8 -jar `"$jarPath`" --server"
$javaRes = Invoke-CimMethod -ClassName Win32_Process -MethodName Create -Arguments @{
    CommandLine = $javaCmd
    CurrentDirectory = $baseDir
}
$javaPid = $javaRes.ProcessId
$javaPid | Out-File -FilePath "$baseDir\logs\ptms.pid" -Encoding ascii
Write-Host "[PTMS] Java Web Server started as fully decoupled background process (PID: $javaPid)." -ForegroundColor Green

# Wait a moment for server to bind port
Start-Sleep -Seconds 2

# Verify server response
try {
    $res = Invoke-RestMethod -Uri "http://localhost:$Port/api/state" -TimeoutSec 3 -ErrorAction Stop
    Write-Host "[PTMS] Local Dashboard verified active at: http://localhost:$Port" -ForegroundColor Green
} catch {
    Write-Host "[PTMS] Server process running, initializing endpoints..." -ForegroundColor Yellow
}

if ($WithTunnel) {
    Write-Host "[PTMS] Starting Cloudflare public tunnel in background..." -ForegroundColor Cyan
    
    # Remove previous URL file if any
    $urlFile = "$baseDir\logs\public-tunnel-url.txt"
    if (Test-Path $urlFile) { Remove-Item $urlFile -Force -ErrorAction SilentlyContinue }

    $tunnelCmd = "node `"$baseDir\scripts\tunnel.mjs`""
    $tunnelRes = Invoke-CimMethod -ClassName Win32_Process -MethodName Create -Arguments @{
        CommandLine = $tunnelCmd
        CurrentDirectory = $baseDir
    }
    $tunnelPid = $tunnelRes.ProcessId
    $tunnelPid | Out-File -FilePath "$baseDir\logs\tunnel.pid" -Encoding ascii
    Write-Host "[PTMS] Cloudflare Tunnel started as decoupled background process (PID: $tunnelPid)." -ForegroundColor Green

    # Wait for tunnel URL
    $maxWait = 15
    while ($maxWait -gt 0 -and !(Test-Path $urlFile)) {
        Start-Sleep -Seconds 1
        $maxWait--
    }

    if (Test-Path $urlFile) {
        $url = (Get-Content $urlFile).Trim()
        Write-Host ""
        Write-Host "==================================================================" -ForegroundColor Green
        Write-Host "   SUCCESS: PTMS IS NOW ACCESSIBLE IN THE BACKGROUND!" -ForegroundColor Green
        Write-Host "   Local URL:   http://localhost:$Port" -ForegroundColor White
        Write-Host "   Public URL:  $url" -ForegroundColor Cyan
        Write-Host "==================================================================" -ForegroundColor Green
        Write-Host " [!] You can now safely close this terminal and Antigravity IDE." -ForegroundColor Yellow
        Write-Host "     The app and tunnel will keep running in the background." -ForegroundColor Yellow
        Write-Host "     Run '.\status-service.bat' to check health anytime." -ForegroundColor Gray
        Write-Host "     Run '.\stop-service.bat' to stop the background services." -ForegroundColor Gray
        Write-Host ""
    } else {
        Write-Host "[PTMS] Tunnel is still connecting. Check '.\status-service.bat' in a few seconds." -ForegroundColor Yellow
    }
}

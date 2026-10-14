$baseDir = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host "================== PTMS SERVICE STATUS ==================" -ForegroundColor Cyan

# 1. Web server check
$conn = Get-NetTCPConnection -LocalPort 8085 -State Listen -ErrorAction SilentlyContinue
if ($conn) {
    $proc = Get-Process -Id $conn.OwningProcess -ErrorAction SilentlyContinue
    Write-Host "[WEB SERVER] Status:  RUNNING (Active)" -ForegroundColor Green
    Write-Host "[WEB SERVER] Local:   http://localhost:8085" -ForegroundColor Green
    Write-Host "[WEB SERVER] Process: PID $($conn.OwningProcess) ($($proc.ProcessName))" -ForegroundColor Gray

    try {
        $res = Invoke-RestMethod -Uri "http://localhost:8085/api/state" -TimeoutSec 2 -ErrorAction Stop
        Write-Host "[WEB SERVER] Health:  OK (Responding to API calls)" -ForegroundColor Green
    } catch {
        Write-Host "[WEB SERVER] Health:  Unresponsive ($($_.Exception.Message))" -ForegroundColor Yellow
    }
} else {
    Write-Host "[WEB SERVER] Status:  STOPPED (Port 8085 not listening)" -ForegroundColor Red
}

Write-Host "---------------------------------------------------------" -ForegroundColor Gray

# 2. Cloudflare tunnel check
$urlFile = "$baseDir\logs\public-tunnel-url.txt"
$tPidFile = "$baseDir\logs\tunnel.pid"
$tunnelRunning = $false

if (Test-Path $tPidFile) {
    $tId = (Get-Content $tPidFile).Trim()
    if ($tId -and (Get-Process -Id $tId -ErrorAction SilentlyContinue)) {
        $tunnelRunning = $true
    }
}

if ($tunnelRunning -and (Test-Path $urlFile)) {
    $url = (Get-Content $urlFile).Trim()
    Write-Host "[TUNNEL]     Status:  ONLINE (Active)" -ForegroundColor Green
    Write-Host "[TUNNEL]     Public:  $url" -ForegroundColor Cyan
    Write-Host "[TUNNEL]     Share:   Anyone can open this link from browser/mobile!" -ForegroundColor Gray
} else {
    Write-Host "[TUNNEL]     Status:  OFFLINE" -ForegroundColor Yellow
}

Write-Host "=========================================================" -ForegroundColor Cyan

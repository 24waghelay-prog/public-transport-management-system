$baseDir = Split-Path -Parent $MyInvocation.MyCommand.Path

# 1. Stop Java server from PID file
$pidFile = "$baseDir\logs\ptms.pid"
if (Test-Path $pidFile) {
    $procId = (Get-Content $pidFile).Trim()
    if ($procId) {
        try {
            Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
            Write-Host "[PTMS] Stopped Java server process (PID: $procId)." -ForegroundColor Yellow
        } catch {}
    }
    Remove-Item $pidFile -Force -ErrorAction SilentlyContinue
}

# 2. Stop Tunnel from PID file
$tunnelPidFile = "$baseDir\logs\tunnel.pid"
if (Test-Path $tunnelPidFile) {
    $tId = (Get-Content $tunnelPidFile).Trim()
    if ($tId) {
        try {
            Stop-Process -Id $tId -Force -ErrorAction SilentlyContinue
            Write-Host "[PTMS] Stopped Cloudflare tunnel process (PID: $tId)." -ForegroundColor Yellow
        } catch {}
    }
    Remove-Item $tunnelPidFile -Force -ErrorAction SilentlyContinue
}

# 3. Clean up any process listening on port 8085
$conn = Get-NetTCPConnection -LocalPort 8085 -State Listen -ErrorAction SilentlyContinue
if ($conn) {
    try {
        Stop-Process -Id $conn.OwningProcess -Force -ErrorAction SilentlyContinue
        Write-Host "[PTMS] Freed port 8085 from PID $($conn.OwningProcess)." -ForegroundColor Yellow
    } catch {}
}

# 4. Clean up any lingering cloudflared tunnel runners
Get-Process -Name "cloudflared" -ErrorAction SilentlyContinue | ForEach-Object {
    try {
        Stop-Process -Id $_.Id -Force -ErrorAction SilentlyContinue
    } catch {}
}

# 5. Clean up URL file
$urlFile = "$baseDir\logs\public-tunnel-url.txt"
if (Test-Path $urlFile) {
    Remove-Item $urlFile -Force -ErrorAction SilentlyContinue
}

Write-Host "[PTMS] All background PTMS services stopped successfully." -ForegroundColor Green

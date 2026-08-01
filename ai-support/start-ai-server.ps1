param([switch]$Quiet)

$port = 8000
$dir = "E:\Users\Rodrigo\Documents\Rendly\ai-support\python"

# Matar proceso anterior
$proc = netstat -ano | Where-Object { $_ -match ":$port " }
if ($proc) {
    $pid = ($proc -split '\s+')[-1]
    if ($pid -match '^\d+$') { Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue }
    Start-Sleep -Seconds 2
}

$env:PYTHONPATH = $dir
$process = Start-Process -NoNewWindow -FilePath "python" -ArgumentList "-m uvicorn main:app --host 0.0.0.0 --port $port" -PassThru

Start-Sleep -Seconds 3

try {
    $r = Invoke-WebRequest -Uri "http://localhost:$port/health" -UseBasicParsing -TimeoutSec 5
    if (-not $Quiet) {
        Write-Host "=== Mercora AI Server ===" -ForegroundColor Green
        Write-Host "Local:  http://localhost:$port" -ForegroundColor Cyan
        Write-Host "Red:    http://192.168.1.16:$port" -ForegroundColor Cyan
        Write-Host "Estado: Online (PID: $($process.Id))" -ForegroundColor Green
        Write-Host "Detener: stop-ai-server.ps1" -ForegroundColor Yellow
    }
} catch {
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
}

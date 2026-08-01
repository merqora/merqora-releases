$port = 8000
$proc = netstat -ano | Where-Object { $_ -match ":$port " }
if ($proc) {
    $pid = ($proc -split '\s+')[-1]
    if ($pid -match '^\d+$') {
        Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
        Write-Host "Servidor AI detenido (PID: $pid)" -ForegroundColor Yellow
    }
} else {
    Write-Host "No hay servidor corriendo en puerto $port" -ForegroundColor Gray
}

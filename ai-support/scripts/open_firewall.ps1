# Script para abrir el firewall de Windows para Rendly AI Support
# DEBE EJECUTARSE COMO ADMINISTRADOR

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Configurando Firewall para Rendly AI" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Verificar si se ejecuta como admin
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host "ERROR: Este script debe ejecutarse como Administrador!" -ForegroundColor Red
    Write-Host "Click derecho -> Ejecutar como administrador" -ForegroundColor Yellow
    exit 1
}

# Eliminar reglas existentes
Write-Host "`nEliminando reglas anteriores..." -ForegroundColor Yellow
Remove-NetFirewallRule -DisplayName "Rendly AI Support*" -ErrorAction SilentlyContinue
Remove-NetFirewallRule -DisplayName "Python*" -ErrorAction SilentlyContinue

# Crear regla para puerto 8000 TCP
Write-Host "Creando regla para puerto 8000 TCP..." -ForegroundColor Yellow
New-NetFirewallRule -DisplayName "Rendly AI Support - TCP 8000" `
    -Direction Inbound `
    -Protocol TCP `
    -LocalPort 8000 `
    -Action Allow `
    -Profile Any `
    -RemoteAddress Any `
    -Description "Permite conexiones al servidor AI de Rendly desde cualquier dispositivo"

# Crear regla para permitir Python
Write-Host "Creando regla para Python..." -ForegroundColor Yellow
$pythonPath = "$env:LOCALAPPDATA\Programs\Python\Python314\python.exe"
if (Test-Path $pythonPath) {
    New-NetFirewallRule -DisplayName "Rendly AI Support - Python" `
        -Direction Inbound `
        -Program $pythonPath `
        -Action Allow `
        -Profile Any `
        -RemoteAddress Any `
        -Description "Permite conexiones entrantes a Python para Rendly AI"
}

# Crear regla para ICMP (ping) - útil para diagnóstico
Write-Host "Habilitando ping (ICMP)..." -ForegroundColor Yellow
netsh advfirewall firewall add rule name="Rendly AI Support - Ping" protocol=icmpv4:8,any dir=in action=allow

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "  Firewall configurado correctamente!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

# Verificar
Write-Host "`nReglas creadas:" -ForegroundColor Cyan
Get-NetFirewallRule -DisplayName "Rendly AI Support*" | Select-Object DisplayName, Enabled, Direction, Action | Format-Table -AutoSize

Write-Host "`nAhora reinicia el servidor AI y prueba desde tu telefono." -ForegroundColor Yellow

# Verificar la regla
Write-Host "`nVerificando regla..." -ForegroundColor Cyan
Get-NetFirewallRule -DisplayName "Rendly AI Support*" | Format-Table Name, DisplayName, Enabled, Direction, Action

Write-Host "`nPresiona cualquier tecla para salir..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

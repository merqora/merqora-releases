# Script para descargar Gradle Wrapper
Write-Host "Descargando Gradle Wrapper..." -ForegroundColor Green

$wrapperUrl = "https://raw.githubusercontent.com/gradle/gradle/master/gradle/wrapper/gradle-wrapper.jar"
$wrapperDir = "gradle\wrapper"
$wrapperJar = "$wrapperDir\gradle-wrapper.jar"

# Crear directorio si no existe
if (-not (Test-Path $wrapperDir)) {
    New-Item -ItemType Directory -Path $wrapperDir -Force | Out-Null
}

# Descargar wrapper JAR
try {
    Invoke-WebRequest -Uri $wrapperUrl -OutFile $wrapperJar
    Write-Host "✅ Gradle Wrapper descargado exitosamente" -ForegroundColor Green
    Write-Host ""
    Write-Host "Ahora puedes compilar con:" -ForegroundColor Yellow
    Write-Host "  .\gradlew.bat assembleDebug" -ForegroundColor Cyan
} catch {
    Write-Host "❌ Error al descargar: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "Alternativa: Descarga manualmente desde:" -ForegroundColor Yellow
    Write-Host "  https://raw.githubusercontent.com/gradle/gradle/master/gradle/wrapper/gradle-wrapper.jar" -ForegroundColor Cyan
    Write-Host "Y colócalo en: gradle\wrapper\gradle-wrapper.jar" -ForegroundColor Cyan
}

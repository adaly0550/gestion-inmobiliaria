# iniciar-entorno-ngrok.ps1
# Script para iniciar el servidor Spring Boot en 127.0.0.1:3000 y levantar ngrok por IPv4 evitando ERR_NGROK_8012

$ErrorActionPreference = "SilentlyContinue"
$ProjectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ProjectDir

$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot"
$MvnCmd = "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.1.3\plugins\maven\lib\maven3\bin\mvn.cmd"

Write-Host "1. Verificando si el servidor local responde en http://127.0.0.1:3000 ..." -ForegroundColor Cyan
$ServerUp = $false
try {
    $resp = Invoke-WebRequest -Uri "http://127.0.0.1:3000" -UseBasicParsing -TimeoutSec 2
    if ($resp.StatusCode -eq 200) {
        $ServerUp = $true
        Write-Host "   -> Servidor activo en 127.0.0.1:3000 (HTTP 200)." -ForegroundColor Green
    }
} catch {}

if (-not $ServerUp) {
    Write-Host "   -> Iniciando servidor Spring Boot en segundo plano (puerto 3000)..." -ForegroundColor Yellow
    Start-Process -FilePath $MvnCmd -ArgumentList "spring-boot:run" -WorkingDirectory $ProjectDir -WindowStyle Minimized
    for ($i = 0; $i -lt 20; $i++) {
        Start-Sleep -Seconds 2
        try {
            $r = Invoke-WebRequest -Uri "http://127.0.0.1:3000" -UseBasicParsing -TimeoutSec 2
            if ($r.StatusCode -eq 200) {
                Write-Host "   -> Servidor listo en http://127.0.0.1:3000" -ForegroundColor Green
                break
            }
        } catch {}
    }
}

Write-Host "2. Iniciando tunel ngrok apuntando a IPv4 (127.0.0.1:3000) para evitar ERR_NGROK_8012..." -ForegroundColor Cyan
ngrok http 127.0.0.1:3000

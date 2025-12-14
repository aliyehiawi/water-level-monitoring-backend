# Mosquitto MQTT Broker Management Script
# This script helps you start/stop Mosquitto and set environment variables

param(
    [Parameter(Position=0)]
    [ValidateSet("start", "stop", "status", "test", "config")]
    [string]$Action = "status"
)

$configPath = "$env:USERPROFILE\scoop\apps\mosquitto\current\mosquitto.conf"

switch ($Action) {
    "start" {
        Write-Host "Starting Mosquitto MQTT broker..." -ForegroundColor Green
        $process = Get-Process -Name "mosquitto" -ErrorAction SilentlyContinue
        if ($process) {
            Write-Host "Mosquitto is already running (PID: $($process.Id))" -ForegroundColor Yellow
        } else {
            Start-Process -FilePath "mosquitto" -ArgumentList "-c", "`"$configPath`"" -WindowStyle Hidden
            Start-Sleep -Seconds 2
            $process = Get-Process -Name "mosquitto" -ErrorAction SilentlyContinue
            if ($process) {
                Write-Host "Mosquitto started successfully (PID: $($process.Id))" -ForegroundColor Green
            } else {
                Write-Host "Failed to start Mosquitto" -ForegroundColor Red
            }
        }
    }
    
    "stop" {
        Write-Host "Stopping Mosquitto MQTT broker..." -ForegroundColor Yellow
        $processes = Get-Process -Name "mosquitto" -ErrorAction SilentlyContinue
        if ($processes) {
            $processes | Stop-Process -Force
            Write-Host "Mosquitto stopped" -ForegroundColor Green
        } else {
            Write-Host "Mosquitto is not running" -ForegroundColor Yellow
        }
    }
    
    "status" {
        Write-Host "=== Mosquitto Status ===" -ForegroundColor Cyan
        $process = Get-Process -Name "mosquitto" -ErrorAction SilentlyContinue
        if ($process) {
            Write-Host "Status: RUNNING (PID: $($process.Id))" -ForegroundColor Green
        } else {
            Write-Host "Status: NOT RUNNING" -ForegroundColor Red
        }
        
        $port1883 = netstat -an | Select-String "1883.*LISTENING"
        if ($port1883) {
            Write-Host "Port 1883: LISTENING" -ForegroundColor Green
        } else {
            Write-Host "Port 1883: NOT LISTENING" -ForegroundColor Red
        }
        
        Write-Host "`nBroker URL: tcp://localhost:1883" -ForegroundColor Cyan
        Write-Host "Config: $configPath" -ForegroundColor Cyan
    }
    
    "test" {
        Write-Host "Testing MQTT connection..." -ForegroundColor Cyan
        $process = Get-Process -Name "mosquitto" -ErrorAction SilentlyContinue
        if (-not $process) {
            Write-Host "Error: Mosquitto is not running. Start it first with: .\start-mosquitto.ps1 start" -ForegroundColor Red
            exit 1
        }
        
        Write-Host "Publishing test message..." -ForegroundColor Yellow
        mosquitto_pub -h localhost -t "test/topic" -m "Test message from $(Get-Date)" 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host "Test message published successfully!" -ForegroundColor Green
        } else {
            Write-Host "Failed to publish test message" -ForegroundColor Red
        }
    }
    
    "config" {
        Write-Host "=== Mosquitto Configuration ===" -ForegroundColor Cyan
        Write-Host "Config file: $configPath" -ForegroundColor Yellow
        Write-Host "`nRelevant settings:" -ForegroundColor Cyan
        Get-Content $configPath | Select-String -Pattern "^[^#]*listener|^[^#]*allow_anonymous" | ForEach-Object {
            Write-Host "  $($_.Line.Trim())" -ForegroundColor White
        }
    }
}

Write-Host "`n=== Quick Commands ===" -ForegroundColor Cyan
Write-Host "Set MQTT environment variables:" -ForegroundColor Yellow
Write-Host '  $env:MQTT_BROKER_URL = "tcp://localhost:1883"' -ForegroundColor White
Write-Host '  $env:MQTT_CLIENT_ID = "water-level-backend-dev"' -ForegroundColor White
Write-Host "`nRun your application:" -ForegroundColor Yellow
Write-Host "  .\gradlew.bat bootRun" -ForegroundColor White


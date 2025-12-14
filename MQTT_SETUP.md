# MQTT Broker Setup Guide

This guide explains how to set up an MQTT broker for the Water Level Monitoring Backend.

## Overview

The backend uses MQTT for real-time communication with hardware devices:

- **Pump Control**: Backend publishes pump start commands to hardware
- **Threshold Updates**: Backend publishes threshold changes to hardware
- **Sensor Data**: Hardware publishes sensor readings to backend

## MQTT Topics

### Outbound (Backend → Hardware)

- `devices/{deviceKey}/pump/start` - Pump start command
- `devices/{deviceKey}/thresholds/update` - Threshold update

### Inbound (Hardware → Backend)

- `devices/+/sensor/data` - Sensor data from hardware (wildcard subscription)

## Recommended: Local Mosquitto Installation (Windows)

**Best for**: Local development and testing

This is the recommended setup for developers working on this project locally. It provides full control, no external dependencies, and works offline.

### Step 1: Install Scoop Package Manager

Scoop is a command-line installer for Windows that makes installing Mosquitto easy.

1. **Open PowerShell** (regular user, no admin needed):

   ```powershell
   # Check if Scoop is already installed
   scoop --version
   ```
2. **If Scoop is not installed**, run:

   ```powershell
   Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser -Force
   Invoke-RestMethod -Uri https://get.scoop.sh | Invoke-Expression
   ```
3. **Verify installation**:

   ```powershell
   scoop --version
   ```

### Step 2: Install Mosquitto

```powershell
scoop install mosquitto
```

This will:

- Download and install Mosquitto MQTT broker
- Install MQTT client tools (`mosquitto_pub`, `mosquitto_sub`)
- Configure basic settings
- Add Mosquitto to your PATH

**Installation Location**: `C:\Users\<YourUsername>\scoop\apps\mosquitto\current`

### Step 3: Configure Mosquitto

The configuration file is located at: `C:\Users\<YourUsername>\scoop\apps\mosquitto\current\mosquitto.conf`

The default configuration should already have:

```
listener 1883
allow_anonymous true
```

If these settings are missing, add them to the config file.

**Note for Hardware Access**: If you need hardware devices to connect to your local broker, you'll need to configure Mosquitto to listen on all network interfaces. See [Step 8: Configure for Hardware Device Access](#step-8-configure-for-hardware-device-access) for details.

### Step 4: Start Mosquitto

**Option A: Using the Helper Script (Recommended)**

The project includes a helper script `start-mosquitto.ps1` in the project root:

```powershell
# Check status
.\start-mosquitto.ps1 status

# Start Mosquitto
.\start-mosquitto.ps1 start

# Stop Mosquitto
.\start-mosquitto.ps1 stop

# Test connection
.\start-mosquitto.ps1 test

# View configuration
.\start-mosquitto.ps1 config
```

**Option B: Manual Start**

```powershell
# Start Mosquitto in background
$configPath = "$env:USERPROFILE\scoop\apps\mosquitto\current\mosquitto.conf"
Start-Process -FilePath "mosquitto" -ArgumentList "-c", "`"$configPath`"" -WindowStyle Hidden
```

**Option C: Run in Foreground (for debugging)**

```powershell
mosquitto -c "$env:USERPROFILE\scoop\apps\mosquitto\current\mosquitto.conf"
```

### Step 5: Verify Mosquitto is Running

```powershell
# Check if process is running
Get-Process -Name "mosquitto" -ErrorAction SilentlyContinue

# Check if port 1883 is listening
netstat -an | Select-String "1883.*LISTENING"
```

You should see:

- Mosquitto process running
- Port 1883 in LISTENING state

### Step 6: Configure Your Application

Set environment variables to use local Mosquitto:

```powershell
$env:MQTT_BROKER_URL = "tcp://localhost:1883"
$env:MQTT_CLIENT_ID = "water-level-backend-dev"
```

**For permanent setup** (optional):

```powershell
[System.Environment]::SetEnvironmentVariable('MQTT_BROKER_URL', 'tcp://localhost:1883', 'User')
[System.Environment]::SetEnvironmentVariable('MQTT_CLIENT_ID', 'water-level-backend-dev', 'User')
```

Then restart your terminal/IDE.

### Step 7: Test the Setup

**Important**: If you get "command not found" errors for `mosquitto_pub` or `mosquitto_sub`:

1. **Check if Scoop is in PATH**:

   ```powershell
   $env:Path -split ';' | Select-String "scoop"
   ```

   If nothing appears, Scoop may not have been installed correctly.
2. **Try opening a new PowerShell terminal**. Scoop automatically adds itself to PATH during installation, and new terminals will have the updated PATH.
3. **If still not working**, verify Scoop installation:

   ```powershell
   scoop list
   ```

**Test 1: Basic Connection Test**

```powershell
# Subscribe to a test topic (leave this running)
mosquitto_sub -h localhost -t "test/topic" -v

# In another terminal, publish a message
mosquitto_pub -h localhost -t "test/topic" -m "Hello MQTT!"
```

You should see "Hello MQTT!" appear in the subscriber window.

**Test 2: Test with Your Application Topics**

```powershell
# Subscribe to sensor data (leave this running)
mosquitto_sub -h localhost -t "devices/+/sensor/data" -v

# In another terminal, publish test sensor data
mosquitto_pub -h localhost -t "devices/test-device/sensor/data" -m '{"device_key":"test-key","water_level":50.5,"pump_status":"ON","timestamp":"2024-01-01T12:00:00"}'
```

**Test 3: Run Your Application**

```powershell
# Make sure environment variables are set
$env:MQTT_BROKER_URL = "tcp://localhost:1883"
$env:MQTT_CLIENT_ID = "water-level-backend-dev"

# Run the application
.\gradlew.bat bootRun
```

Check the application logs for MQTT connection messages. You should see:

```
MQTT connection established
Connected to broker: tcp://localhost:1883
```

### Quick Reference Commands

```powershell
# Start Mosquitto
.\start-mosquitto.ps1 start

# Check status
.\start-mosquitto.ps1 status

# Stop Mosquitto
.\start-mosquitto.ps1 stop

# Test connection
.\start-mosquitto.ps1 test

# Subscribe to sensor data
mosquitto_sub -h localhost -t "devices/+/sensor/data" -v

# Publish test message
mosquitto_pub -h localhost -t "devices/test-device/sensor/data" -m '{"device_key":"test-key","water_level":50.5,"pump_status":"ON","timestamp":"2024-01-01T12:00:00"}'
```

### Step 8: Configure for Hardware Device Access

**Real-world scenario**: Your hardware devices (Arduino, ESP32, Raspberry Pi, etc.) need to connect to the MQTT broker running on your laptop. This requires additional configuration.

#### Step 8.1: Find Your Laptop's Local IP Address

Hardware devices cannot use `localhost` - they need your laptop's actual IP address on the local network.

**Find your IP address:**

```powershell
# Quick method
ipconfig | Select-String "IPv4"

# More detailed method
Get-NetIPAddress -AddressFamily IPv4 | Where-Object {$_.InterfaceAlias -notlike "*Loopback*"} | Select-Object IPAddress, InterfaceAlias
```

You'll see something like:

```
IPAddress      InterfaceAlias
---------      --------------
192.168.1.100  Wi-Fi
```

**Note your IP address** (e.g., `192.168.1.100`) - you'll need this for hardware configuration.

#### Step 8.2: Configure Mosquitto to Listen on All Network Interfaces

By default, Mosquitto might only listen on `localhost` (127.0.0.1), which prevents external devices from connecting. You need to configure it to listen on all network interfaces.

1. **Edit the Mosquitto config file:**

   ```powershell
   notepad "$env:USERPROFILE\scoop\apps\mosquitto\current\mosquitto.conf"
   ```
2. **Update the listener configuration** to listen on all interfaces:

   ```
   listener 1883 0.0.0.0
   allow_anonymous true
   ```

   Or explicitly bind to all interfaces:

   ```
   listener 1883
   bind_address 0.0.0.0
   allow_anonymous true
   ```
3. **Save the file and restart Mosquitto:**

   ```powershell
   .\start-mosquitto.ps1 stop
   .\start-mosquitto.ps1 start
   ```

#### Step 8.3: Configure Windows Firewall

Windows Firewall will block incoming connections by default. You need to allow port 1883.

**Option A: PowerShell (Run as Administrator)**

```powershell
New-NetFirewallRule -DisplayName "MQTT Mosquitto" -Direction Inbound -LocalPort 1883 -Protocol TCP -Action Allow
```

**Option B: Windows Firewall GUI**

1. Open **Windows Security** → **Firewall & network protection**
2. Click **Advanced settings**
3. Click **Inbound Rules** → **New Rule**
4. Select **Port** → **Next**
5. Select **TCP** → Enter port `1883` → **Next**
6. Select **Allow the connection** → **Next**
7. Check all profiles (Domain, Private, Public) → **Next**
8. Name: `MQTT Mosquitto` → **Finish**

#### Step 8.4: Verify Mosquitto is Listening on All Interfaces

Check that Mosquitto is listening on `0.0.0.0:1883` (all interfaces), not just `127.0.0.1:1883`:

```powershell
netstat -an | Select-String "1883.*LISTENING"
```

You should see:

```
TCP    0.0.0.0:1883           0.0.0.0:0              LISTENING
```

If you only see `127.0.0.1:1883`, Mosquitto is only listening on localhost. Check your config file and restart.

#### Step 8.5: Configure Your Hardware Devices

On your hardware device (Arduino, ESP32, etc.), configure the MQTT broker URL to use your laptop's IP address:

```
tcp://YOUR_LAPTOP_IP:1883
```

**Example:** If your laptop IP is `192.168.1.100`:

```
tcp://192.168.1.100:1883
```

**Hardware Configuration Example (Arduino/ESP32):**

```cpp
const char* mqtt_server = "192.168.1.100";  // Your laptop IP
const int mqtt_port = 1883;
```

#### Step 8.6: Test from Another Device

Test that external devices can connect:

**From another computer/phone on the same WiFi:**

```bash
# Subscribe to test topic
mosquitto_sub -h 192.168.1.100 -t "test/topic" -v

# Publish test message
mosquitto_pub -h 192.168.1.100 -t "test/topic" -m "Hello from hardware!"
```

If this works, your hardware devices should be able to connect too.

#### Step 8.7: Update Application Configuration

Your Spring Boot application can still use `localhost` (it's running on the same machine), but if you want to be explicit:

```powershell
# Application can still use localhost (same machine)
$env:MQTT_BROKER_URL = "tcp://localhost:1883"

# Or use your IP address (works the same)
$env:MQTT_BROKER_URL = "tcp://192.168.1.100:1883"
```

Both work since the application runs on the same machine as Mosquitto.

#### Important Considerations

**Network Requirements:**

- ✅ Hardware devices must be on the **same local network** (same WiFi/LAN) as your laptop
- ✅ Both devices must be able to reach each other (no network isolation/VLAN separation)
- ⚠️ Your laptop's IP address may change if you reconnect to WiFi

**IP Address Changes:**
If your laptop's IP changes, you'll need to update your hardware configuration. Solutions:

1. **Set a static IP** on your laptop (via Windows network settings)
2. **Use DHCP reservation** on your router (reserve an IP for your laptop's MAC address)
3. **Use hostname** (if your network supports mDNS/Bonjour, e.g., `laptop-name.local`)

**Security Notes:**

- `allow_anonymous true` is fine for local development/testing
- For production, enable authentication:
  ```powershell
  # Create password file
  mosquitto_passwd -c "$env:USERPROFILE\scoop\apps\mosquitto\current\passwd" username

  # Update config
  # allow_anonymous false
  # password_file C:\Users\<YourUsername>\scoop\apps\mosquitto\current\passwd
  ```

**Quick Setup Summary:**

```powershell
# 1. Find your IP
$ip = (Get-NetIPAddress -AddressFamily IPv4 | Where-Object {$_.InterfaceAlias -notlike "*Loopback*"}).IPAddress
Write-Host "Your laptop IP: $ip"

# 2. Update Mosquitto config (edit manually)
# File: $env:USERPROFILE\scoop\apps\mosquitto\current\mosquitto.conf
# Change: listener 1883
# To: listener 1883 0.0.0.0

# 3. Allow firewall (run as Administrator)
New-NetFirewallRule -DisplayName "MQTT Mosquitto" -Direction Inbound -LocalPort 1883 -Protocol TCP -Action Allow

# 4. Restart Mosquitto
.\start-mosquitto.ps1 stop
.\start-mosquitto.ps1 start

# 5. Verify listening on all interfaces
netstat -an | Select-String "1883.*LISTENING"

# 6. Configure hardware with: tcp://$ip:1883
```

### Troubleshooting Local Setup

**Mosquitto won't start:**

- Check if port 1883 is already in use: `netstat -an | Select-String "1883"`
- Verify config file exists: `Test-Path "$env:USERPROFILE\scoop\apps\mosquitto\current\mosquitto.conf"`
- Check Windows Firewall isn't blocking port 1883

**Can't find mosquitto commands:**

- Restart PowerShell to refresh PATH
- Verify Scoop installation: `scoop list`
- Check PATH: `$env:Path | Select-String "scoop"`

**Connection refused:**

- Ensure Mosquitto is running: `Get-Process -Name "mosquitto"`
- Verify port is listening: `netstat -an | Select-String "1883.*LISTENING"`
- Check broker URL is correct: `tcp://localhost:1883` (for local) or `tcp://YOUR_IP:1883` (for hardware)

**Hardware cannot connect to local broker:**

- Verify Mosquitto is listening on `0.0.0.0:1883` (not just `127.0.0.1:1883`)
- Check Windows Firewall allows port 1883: `Get-NetFirewallRule -DisplayName "*MQTT*"`
- Ensure hardware is on the same network as your laptop
- Verify your laptop's IP address hasn't changed: `ipconfig | Select-String "IPv4"`
- Test from another device on the same network: `mosquitto_sub -h YOUR_IP -t "test/topic" -v`
- Check if your network has client isolation enabled (some public WiFi networks block device-to-device communication)

## Testing MQTT Connection

### Using MQTT Client Tools (mosquitto_pub/mosquitto_sub)

**Subscribe to sensor data** (leave this running to see incoming messages):

```powershell
mosquitto_sub -h localhost -t "devices/+/sensor/data" -v
```

**Publish test sensor data**:

```powershell
mosquitto_pub -h localhost -t "devices/test-device/sensor/data" -m '{"device_key":"test-key","water_level":50.5,"pump_status":"ON","timestamp":"2024-01-01T12:00:00"}'
```

**Subscribe to pump control commands**:

```powershell
mosquitto_sub -h localhost -t "devices/+/pump/start" -v
```

**Subscribe to threshold updates**:

```powershell
mosquitto_sub -h localhost -t "devices/+/thresholds/update" -v
```

### Testing Full Flow

1. **Start Mosquitto** (if using local):

   ```powershell
   .\start-mosquitto.ps1 start
   ```
2. **Subscribe to all device topics** (Terminal 1):

   ```powershell
   mosquitto_sub -h localhost -t "devices/+/#" -v
   ```
3. **Start your application** (Terminal 2):

   ```powershell
   $env:MQTT_BROKER_URL = "tcp://localhost:1883"
   $env:MQTT_CLIENT_ID = "water-level-backend-dev"
   .\gradlew.bat bootRun
   ```
4. **Publish test sensor data** (Terminal 3):

   ```powershell
   mosquitto_pub -h localhost -t "devices/test-device/sensor/data" -m '{"device_key":"test-key","water_level":50.5,"pump_status":"ON","timestamp":"2024-01-01T12:00:00"}'
   ```
5. **Verify**:

   - Terminal 1 should show the published message
   - Application logs should show the message was received and processed
   - WebSocket clients should receive the update (if connected)

## Troubleshooting

### Connection Issues

**"Connection refused" or "Connection timeout":**

- Verify Mosquitto is running: `.\start-mosquitto.ps1 status`
- Check port 1883 is listening: `netstat -an | Select-String "1883.*LISTENING"`
- Verify broker URL format: `tcp://localhost:1883` (not `http://` or missing `tcp://`)
- Check Windows Firewall isn't blocking port 1883
- For remote brokers, check network connectivity

**"Connection lost" or frequent disconnections:**

- Check Mosquitto logs (if running in foreground)
- Verify network stability
- Check if multiple clients are using the same client ID
- Review MQTT keep-alive settings

### Authentication Failures

**"Not authorized" or "Bad username or password":**

- Verify username/password if broker requires authentication
- Check broker authentication configuration
- Ensure client ID is unique (each connection needs unique ID)
- For local Mosquitto, ensure `allow_anonymous true` is set in config

### Message Not Received

**Messages published but not received:**

- Verify topic names match exactly (case-sensitive)
- Check subscription patterns (wildcards: `+` for single level, `#` for multi-level)
- Verify QoS levels (backend uses QoS 1)
- Ensure subscriber is connected before publishing
- Check if messages are being filtered by broker ACL rules

**Application not receiving sensor data:**

- Verify application is subscribed to: `devices/+/sensor/data`
- Check application logs for MQTT connection status
- Verify message format matches expected JSON structure
- Test with `mosquitto_sub` to verify messages are reaching the broker

### Common Issues

**"mosquitto: command not found" or "mosquitto_pub/mosquitto_sub: command not found":**

This happens when Scoop's shims directory isn't in your PATH. Try these solutions:

1. **Check if Scoop is in PATH**:

   ```powershell
   $env:Path -split ';' | Select-String "scoop"
   ```

   If nothing appears, Scoop may not have been installed correctly.
2. **Try opening a new PowerShell terminal**. Scoop automatically adds itself to PATH during installation. If you installed Scoop/Mosquitto in an existing terminal, that terminal won't have the updated PATH until you restart it.
3. **Verify Scoop installation**:

   ```powershell
   scoop list
   scoop list mosquitto
   ```
4. **If still not working**, check if Scoop is in your User PATH:

   ```powershell
   [System.Environment]::GetEnvironmentVariable("Path","User") -split ';' | Select-String "scoop"
   ```

   If nothing appears, Scoop may not have been installed correctly. Reinstall Scoop.
5. **Reinstall Mosquitto** (if needed):

   ```powershell
   scoop uninstall mosquitto
   scoop install mosquitto
   ```

**Port 1883 already in use:**

- Find process using port: `netstat -ano | Select-String "1883"`
- Stop the conflicting process or use a different port
- Update Mosquitto config to use different port and update `MQTT_BROKER_URL`

**Mosquitto won't start:**

- Check config file exists: `Test-Path "$env:USERPROFILE\scoop\apps\mosquitto\current\mosquitto.conf"`
- Verify config syntax (no typos in `listener` or `allow_anonymous`)
- Try running in foreground to see error messages: `mosquitto -c "$env:USERPROFILE\scoop\apps\mosquitto\current\mosquitto.conf"`

**Application connects but messages don't flow:**

- Verify both publisher and subscriber are connected to same broker
- Check topic names match exactly
- Verify JSON payload is valid
- Check application logs for MQTT errors

## Configuration in Application

The application is already configured to use MQTT. You just need to set environment variables.

### Environment Variables

**Required for local development:**

```powershell
$env:MQTT_BROKER_URL = "tcp://localhost:1883"
$env:MQTT_CLIENT_ID = "water-level-backend-dev"
```

**Optional (only if broker requires authentication):**

```powershell
$env:MQTT_USERNAME = "your_username"
$env:MQTT_PASSWORD = "your_password"
```

### Configuration Files

The application uses Spring Boot profiles for configuration:

- **`application-dev.yml`**: Requires `MQTT_BROKER_URL` to be set (use `tcp://localhost:1883` for local Mosquitto)
- **`application-prod.yml`**: Requires `MQTT_BROKER_URL` to be set

**Set the environment variable to use local Mosquitto:**

```powershell
$env:MQTT_BROKER_URL = "tcp://localhost:1883"
```

### Configuration Structure

```yaml
spring:
  mqtt:
    broker:
      url: ${MQTT_BROKER_URL:tcp://localhost:1883}
    client:
      id: ${MQTT_CLIENT_ID:water-level-backend-dev}
    username: ${MQTT_USERNAME:}
    password: ${MQTT_PASSWORD:}
    retry:
      max-attempts: ${MQTT_RETRY_MAX_ATTEMPTS:3}
      initial-delay-ms: ${MQTT_RETRY_INITIAL_DELAY_MS:1000}
      max-delay-ms: ${MQTT_RETRY_MAX_DELAY_MS:10000}
      multiplier: ${MQTT_RETRY_MULTIPLIER:2.0}
```

**Note**: The default is `tcp://localhost:1883` assuming you're running local Mosquitto. Always set `MQTT_BROKER_URL` explicitly for clarity.

### Quick Start Checklist

1. ✅ Install Scoop (if not already installed)
2. ✅ Install Mosquitto: `scoop install mosquitto`
3. ✅ Start Mosquitto: `.\start-mosquitto.ps1 start`
4. ✅ Set environment variables:
   ```powershell
   $env:MQTT_BROKER_URL = "tcp://localhost:1883"
   $env:MQTT_CLIENT_ID = "water-level-backend-dev"
   ```
5. ✅ Run application: `.\gradlew.bat bootRun`
6. ✅ Test: Publish a message and verify it's received

## Additional Resources

### Documentation

- [MQTT Protocol Specification](https://mqtt.org/mqtt-specification/)
- [Scoop Package Manager](https://scoop.sh/)

### Tools

- [MQTT.fx](https://mqttfx.jensd.de/) - Desktop MQTT client
- [MQTT Explorer](http://mqtt-explorer.com/) - MQTT topic browser

### Project Files

- `start-mosquitto.ps1` - Helper script for managing local Mosquitto
- `application-dev.yml` - Development MQTT configuration
- `application-prod.yml` - Production MQTT configuration

## Summary

For local development, the recommended setup is:

1. **Install Mosquitto locally** using Scoop (see "Recommended: Local Mosquitto Installation" above)
2. **Use the helper script** (`start-mosquitto.ps1`) to manage Mosquitto
3. **Set environment variables** to point to `tcp://localhost:1883`
4. **Test with mosquitto_pub/sub** commands
5. **Run your application** and verify MQTT connection in logs

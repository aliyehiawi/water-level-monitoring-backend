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

## Free MQTT Broker Options

### Option 1: Eclipse Mosquitto (Public Test Broker)
**Best for**: Development and testing

- **URL**: `tcp://test.mosquitto.org:1883`
- **Authentication**: None required
- **Limitations**: 
  - Public, not secure
  - No persistence
  - May have connection limits
- **Setup**: No setup required, ready to use

**Configuration**:
```bash
MQTT_BROKER_URL=tcp://test.mosquitto.org:1883
MQTT_CLIENT_ID=water-level-backend
MQTT_USERNAME=
MQTT_PASSWORD=
```

### Option 2: HiveMQ Cloud (Free Tier)
**Best for**: Production or secure development

- **URL**: `tcp://broker.hivemq.com:1883` (public) or your HiveMQ Cloud URL
- **Authentication**: Optional (username/password for Cloud)
- **Limitations**: 
  - Free tier has connection limits
  - Public broker is unsecured
- **Setup**:
  1. Visit [HiveMQ Cloud](https://www.hivemq.com/cloud/)
  2. Sign up for free tier
  3. Create a cluster
  4. Get your broker URL and credentials

**Configuration**:
```bash
MQTT_BROKER_URL=tcp://your-cluster.hivemq.cloud:1883
MQTT_CLIENT_ID=water-level-backend
MQTT_USERNAME=your_username
MQTT_PASSWORD=your_password
```

### Option 3: Self-Hosted Mosquitto
**Best for**: Full control and security

**Installation (Ubuntu/Debian)**:
```bash
sudo apt-get update
sudo apt-get install mosquitto mosquitto-clients
sudo systemctl start mosquitto
sudo systemctl enable mosquitto
```

**Configuration** (`/etc/mosquitto/mosquitto.conf`):
```
listener 1883
allow_anonymous true  # Set to false for production
```

**For Production** (with authentication):
1. Create password file:
   ```bash
   sudo mosquitto_passwd -c /etc/mosquitto/passwd username
   ```

2. Update config:
   ```
   listener 1883
   allow_anonymous false
   password_file /etc/mosquitto/passwd
   ```

3. Restart:
   ```bash
   sudo systemctl restart mosquitto
   ```

**Configuration**:
```bash
MQTT_BROKER_URL=tcp://your-server:1883
MQTT_CLIENT_ID=water-level-backend
MQTT_USERNAME=username
MQTT_PASSWORD=password
```

## Testing MQTT Connection

### Using MQTT Client (mosquitto_pub/mosquitto_sub)

**Subscribe to sensor data**:
```bash
mosquitto_sub -h test.mosquitto.org -t "devices/+/sensor/data" -v
```

**Publish test message**:
```bash
mosquitto_pub -h test.mosquitto.org -t "devices/test-device/sensor/data" \
  -m '{"device_key":"test-key","water_level":50.5,"pump_status":"ON","timestamp":"2024-01-01T12:00:00"}'
```

### Using Online MQTT Client
1. Visit [HiveMQ WebSocket Client](http://www.hivemq.com/demos/websocket-client/)
2. Connect to your broker
3. Subscribe to topics
4. Publish test messages

## Production Recommendations

1. **Use Authentication**: Always use username/password in production
2. **Use TLS/SSL**: Configure MQTT over TLS (port 8883) for secure communication
3. **Use Private Broker**: Self-hosted or cloud broker with access control
4. **Monitor Connections**: Set up monitoring for broker health
5. **Backup Configuration**: Keep broker configuration in version control

## Troubleshooting

### Connection Issues
- Check firewall rules (port 1883 or 8883)
- Verify broker URL format: `tcp://host:port`
- Check network connectivity
- Review broker logs

### Authentication Failures
- Verify username/password
- Check broker authentication configuration
- Ensure client ID is unique

### Message Not Received
- Verify topic names match exactly
- Check QoS levels (backend uses QoS 1)
- Review subscription patterns
- Check broker logs

## Configuration in Application

Update `application.yml` or environment variables:

```yaml
spring:
  mqtt:
    broker:
      url: ${MQTT_BROKER_URL:tcp://test.mosquitto.org:1883}
    client:
      id: ${MQTT_CLIENT_ID:water-level-backend}
    username: ${MQTT_USERNAME:}
    password: ${MQTT_PASSWORD:}
    retry:
      max-attempts: ${MQTT_RETRY_MAX_ATTEMPTS:3}
      initial-delay-ms: ${MQTT_RETRY_INITIAL_DELAY_MS:1000}
      max-delay-ms: ${MQTT_RETRY_MAX_DELAY_MS:10000}
      multiplier: ${MQTT_RETRY_MULTIPLIER:2.0}
```

## Additional Resources

- [Eclipse Mosquitto Documentation](https://mosquitto.org/documentation/)
- [HiveMQ Documentation](https://www.hivemq.com/docs/)
- [MQTT Protocol Specification](https://mqtt.org/mqtt-specification/)


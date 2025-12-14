# Water Level Monitoring Backend - Tutorial

This tutorial provides step-by-step guides for common tasks and workflows in the Water Level Monitoring Backend system.

## Table of Contents

1. [Getting Started](#getting-started)
2. [User Registration and Authentication](#user-registration-and-authentication)
3. [Device Management](#device-management)
4. [Monitoring Water Levels](#monitoring-water-levels)
5. [Pump Control](#pump-control)
6. [Threshold Configuration](#threshold-configuration)
7. [Real-time Updates via WebSocket](#real-time-updates-via-websocket)
8. [MQTT Integration](#mqtt-integration)
9. [Admin Operations](#admin-operations)

## Getting Started

### Prerequisites

- Java 21 or higher installed
- Gradle 8.0 or higher
- MQTT broker (see [MQTT_SETUP.md](MQTT_SETUP.md) for local Mosquitto installation)

### Initial Setup

1. **Clone and build the project:**
   ```bash
   git clone <repository-url>
   cd water-level-monitoring-backend
   ./gradlew build
   ```

2. **Set environment variables:**
   ```bash
   export JWT_SECRET=$(openssl rand -hex 32)
   ```

3. **Run the application:**
   ```bash
   ./gradlew bootRun
   ```

4. **Verify the application is running:**
   - Health check: `http://localhost:8080/api/actuator/health`
   - Swagger UI: `http://localhost:8080/api/swagger-ui.html`

## User Registration and Authentication

### Step 1: Register a New User

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "securepassword123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 86400,
  "user": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "role": "USER",
    "createdAt": "2024-01-01T10:00:00"
  }
}
```

**Notes:**
- The response includes a JWT token that should be saved for subsequent requests
- Token expires in 24 hours (86400 seconds)
- New users are created with `USER` role by default

### Step 2: Login

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "securepassword123"
  }'
```

**Response:** Same format as registration response with a new JWT token.

### Step 3: Get Current User Information

**Request:**
```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Device Management

### Registering a Device (Admin Only)

**Prerequisites:** You must be logged in as an ADMIN user.

**Request:**
```bash
curl -X POST http://localhost:8080/api/devices/register \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Water Tank 1",
    "minThreshold": 10.0,
    "maxThreshold": 80.0
  }'
```

**Response:**
```json
{
  "id": 1,
  "name": "Water Tank 1",
  "deviceKey": "550e8400-e29b-41d4-a716-446655440000",
  "minThreshold": 10.0,
  "maxThreshold": 80.0,
  "adminId": 1,
  "adminUsername": "admin_user",
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:00:00"
}
```

**Important:** Save the `deviceKey` - it's required for hardware device configuration.

### Listing All Devices

**Request:**
```bash
curl -X GET "http://localhost:8080/api/devices?page=0&size=20" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

**Response:**
```json
{
  "content": [...],
  "totalElements": 5,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

## Monitoring Water Levels

### Understanding Water Level Data

Water level data is automatically collected from hardware devices via MQTT. The system stores:
- Current water level (0.0 - 999.99)
- Pump status (ON, OFF, UNKNOWN)
- Timestamp of the reading

### Viewing Historical Data

Historical data can be accessed through the database or by implementing a dedicated endpoint. The data is stored in the `water_level_data` table with the following structure:
- `device_id`: Reference to the device
- `water_level`: Numeric value
- `pump_status`: Enum value
- `timestamp`: When the reading was taken

## Pump Control

### Manual Pump Start (Admin Only)

**Request:**
```bash
curl -X POST http://localhost:8080/api/devices/1/pump/start \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

**Response:**
```json
{
  "message": "Pump start command sent successfully",
  "deviceId": 1
}
```

**How it works:**
1. Backend validates device ownership
2. Publishes MQTT command to `devices/{deviceKey}/pump/start`
3. Hardware device receives command and starts pump
4. Hardware sends confirmation via sensor data message

### Check Pump Status

**Request:**
```bash
curl -X GET http://localhost:8080/api/devices/1/pump/status \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

**Response:**
```json
{
  "pumpStatus": "ON",
  "deviceId": 1,
  "lastUpdate": "2024-01-01T10:05:00"
}
```

## Threshold Configuration

### Understanding Thresholds

Thresholds define the safe operating range for water levels:
- **Min Threshold**: Minimum acceptable water level
- **Max Threshold**: Maximum acceptable water level

When water level goes below min or above max, the system can trigger alerts or automated actions.

### Get Current Thresholds

**Request:**
```bash
curl -X GET http://localhost:8080/api/devices/1/thresholds \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

**Response:**
```json
{
  "minThreshold": 10.0,
  "maxThreshold": 80.0,
  "deviceId": 1
}
```

### Update Thresholds

**Request:**
```bash
curl -X PUT http://localhost:8080/api/devices/1/thresholds \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "minThreshold": 15.0,
    "maxThreshold": 85.0
  }'
```

**Response:**
```json
{
  "minThreshold": 15.0,
  "maxThreshold": 85.0,
  "deviceId": 1
}
```

**How it works:**
1. Backend validates thresholds (min < max, within valid range)
2. Updates database
3. Publishes MQTT message to `devices/{deviceKey}/thresholds/update`
4. Hardware device receives and applies new thresholds

## Real-time Updates via WebSocket

### Connecting to WebSocket

**JavaScript Example:**
```javascript
const socket = new SockJS('http://localhost:8080/api/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
  console.log('Connected: ' + frame);
  
  // Subscribe to device updates
  stompClient.subscribe('/topic/device/1', function(message) {
    const update = JSON.parse(message.body);
    console.log('Water level update:', update);
    // Update UI with new data
  });
});
```

### Message Format

**Sensor Update:**
```json
{
  "type": "sensor_update",
  "deviceId": 1,
  "waterLevel": 45.5,
  "pumpStatus": "ON",
  "timestamp": "2024-01-01T10:05:00"
}
```

**Threshold Update:**
```json
{
  "type": "threshold_updated",
  "deviceId": 1,
  "minThreshold": 15.0,
  "maxThreshold": 85.0,
  "timestamp": "2024-01-01T10:05:00"
}
```

## MQTT Integration

### Hardware Device Setup

1. **Get Device Key:** Register device via API to receive `deviceKey`
2. **Configure MQTT Client:** Connect to MQTT broker using device key
3. **Subscribe to Commands:**
   - `devices/{deviceKey}/pump/start` - Pump control commands
   - `devices/{deviceKey}/thresholds/update` - Threshold updates

4. **Publish Sensor Data:**
   - Topic: `devices/{deviceKey}/sensor/data`
   - Format:
     ```json
     {
       "device_key": "550e8400-e29b-41d4-a716-446655440000",
       "water_level": 45.5,
       "pump_status": "ON",
       "timestamp": "2024-01-01T10:05:00"
     }
     ```

### MQTT Message Examples

**Pump Start Command (from backend):**
```json
{
  "command": "START",
  "timestamp": "2024-01-01T10:00:00Z",
  "initiatedBy": 1
}
```

**Threshold Update Command (from backend):**
```json
{
  "minThreshold": 15.0,
  "maxThreshold": 85.0,
  "timestamp": "2024-01-01T10:00:00Z",
  "updatedBy": 1
}
```

For detailed MQTT setup, see [MQTT_SETUP.md](MQTT_SETUP.md).

## Admin Operations

### Promoting a User to Admin

**Request:**
```bash
curl -X PUT http://localhost:8080/api/users/2/promote \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

**Response:**
```json
{
  "id": 2,
  "username": "john_doe",
  "email": "john@example.com",
  "role": "ADMIN",
  "createdAt": "2024-01-01T10:00:00"
}
```

### Listing All Users

**Request:**
```bash
curl -X GET "http://localhost:8080/api/users?page=0&size=20" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

### Deleting a User

**Request:**
```bash
curl -X DELETE http://localhost:8080/api/users/2 \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

**Note:** Users with associated devices cannot be deleted. Delete devices first.

## Common Workflows

### Complete Setup Workflow

1. **Register as admin user** (first user should be manually promoted)
2. **Register devices** for each water tank/monitoring point
3. **Configure hardware devices** with device keys
4. **Set thresholds** for each device
5. **Connect frontend** to WebSocket for real-time updates
6. **Monitor** water levels and pump status

### Daily Operations

1. **Monitor water levels** via WebSocket or API
2. **Adjust thresholds** if needed based on conditions
3. **Manually control pump** if automated system needs override
4. **Review audit logs** for security and compliance

### Troubleshooting

**Device not receiving commands:**
- Verify MQTT broker connection
- Check device key matches registered device
- Verify MQTT topic subscriptions

**WebSocket not receiving updates:**
- Check WebSocket connection status
- Verify device is sending sensor data
- Check CORS configuration for frontend origin

**Authentication issues:**
- Verify JWT token is not expired
- Check token is included in Authorization header
- Ensure user has required role (ADMIN for admin endpoints)

## Best Practices

1. **Security:**
   - Always use HTTPS in production
   - Rotate JWT secrets regularly
   - Use strong passwords
   - Keep device keys secure

2. **MQTT:**
   - Use authenticated MQTT broker in production
   - Implement retry logic in hardware devices
   - Monitor MQTT connection status

3. **Monitoring:**
   - Set up alerts for threshold violations
   - Monitor pump status regularly
   - Review audit logs periodically

4. **Performance:**
   - Use pagination for large data sets
   - Implement caching where appropriate
   - Monitor database query performance

## Additional Resources

- [API Documentation](README.md#-api-documentation) - Complete API reference
- [MQTT Setup Guide](MQTT_SETUP.md) - Detailed MQTT configuration
- [Swagger UI](http://localhost:8080/api/swagger-ui.html) - Interactive API documentation


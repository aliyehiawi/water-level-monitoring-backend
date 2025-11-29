# Water Level Monitoring System - Tutorial

This tutorial will guide you through setting up, using, and extending the Water Level Monitoring Backend API.

## Table of Contents
1. [System Overview](#system-overview)
2. [Quick Start](#quick-start)
3. [User Management](#user-management)
4. [Device Registration](#device-registration)
5. [Hardware Integration](#hardware-integration)
6. [Dashboard Usage](#dashboard-usage)
7. [Admin Operations](#admin-operations)
8. [Development Guide](#development-guide)

## System Overview

The Water Level Monitoring System consists of:
- **Backend API**: Spring Boot application with JWT security
- **Frontend UI**: User dashboard for monitoring and control
- **Hardware Devices**: IoT sensors that monitor water levels and control pumps

### Architecture Flow
```
Hardware Device → Backend API ← Frontend UI
       ↓              ↓           ↑
   Auto Pump     Database    User Actions
```

## Quick Start

### 1. Start the Application
```bash
./gradlew bootRun
```

### 2. Access H2 Console
- URL: `http://localhost:8080/api/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: `password`

### 3. View API Documentation
- Swagger UI: `http://localhost:8080/api/swagger-ui.html`

## User Management

### Creating the First Admin User

Since the system requires an admin to register devices, you need to create the first admin user manually or through a setup endpoint.

**Option 1: Database Insertion**
```sql
INSERT INTO users (username, email, password, role, created_at) 
VALUES ('admin', 'admin@example.com', '$2a$10$encrypted_password', 'ADMIN', NOW());
```

**Option 2: Registration + Manual Role Update**
1. Register through `/api/auth/register`
2. Update role in database: `UPDATE users SET role = 'ADMIN' WHERE username = 'admin';`

### User Registration Flow
```bash
# Register new user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "securepassword"
  }'
```

### User Login
```bash
# Login and get JWT token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "securepassword"
  }'

# Response includes JWT token
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "john_doe",
  "role": "USER"
}
```

## Device Registration

### Admin Registers Device
Only admins can register new devices. The system generates a unique device key.

```bash
# Register new device (Admin only)
curl -X POST http://localhost:8080/api/devices/register \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Pool Water Monitor",
    "location": "Backyard Pool"
  }'

# Response with device credentials
{
  "deviceId": "12345",
  "deviceKey": "a1b2c3d4-e5f6-7890-abcd-1234567890ab",
  "name": "Pool Water Monitor",
  "message": "Device registered successfully. Store the device key securely."
}
```

### Device Key Storage
The frontend will:
1. Receive the device key from the backend
2. Communicate with hardware via Bluetooth
3. Store the device key in hardware memory

## Hardware Integration

### Device Authentication
Hardware devices authenticate using their device key:

```bash
# Hardware sends sensor data
curl -X POST http://localhost:8080/api/data/sensor \
  -H "Content-Type: application/json" \
  -d '{
    "deviceKey": "a1b2c3d4-e5f6-7890-abcd-1234567890ab",
    "waterLevel": 75.5,
    "minThreshold": 30.0,
    "maxThreshold": 90.0,
    "pumpStatus": "OFF",
    "timestamp": "2023-11-29T10:30:00Z"
  }'
```

### Polling for Manual Pump Control
Hardware polls every few seconds to check for manual pump commands:

```bash
# Check for manual pump start flag
curl -X GET http://localhost:8080/api/data/pump-flag/a1b2c3d4-e5f6-7890-abcd-1234567890ab

# Response
{
  "manualStart": true,
  "timestamp": "2023-11-29T10:35:00Z"
}

# If flag is true, hardware starts pump and resets flag
curl -X PUT http://localhost:8080/api/data/pump-flag/a1b2c3d4-e5f6-7890-abcd-1234567890ab/reset \
  -H "Content-Type: application/json"
```

## Dashboard Usage

### Viewing Device Data
Users can view data for devices they have access to:

```bash
# Get accessible devices
curl -X GET http://localhost:8080/api/dashboard/devices \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Get water level history for a device
curl -X GET http://localhost:8080/api/dashboard/data/12345?page=0&size=50 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Get current readings
curl -X GET http://localhost:8080/api/dashboard/current/12345 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### WebSocket Live Updates
Connect to WebSocket for real-time updates:

```javascript
// Frontend WebSocket connection
const socket = new SockJS('/websocket');
const stompClient = Stomp.over(socket);

stompClient.connect({
  'Authorization': 'Bearer ' + token
}, function(frame) {
  // Subscribe to device updates
  stompClient.subscribe('/topic/device/12345', function(message) {
    const data = JSON.parse(message.body);
    updateDashboard(data);
  });
});
```

## Admin Operations

### Threshold Management
Admins can update device thresholds:

```bash
# Update thresholds
curl -X PUT http://localhost:8080/api/devices/12345/thresholds \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "minThreshold": 25.0,
    "maxThreshold": 85.0
  }'
```

### Manual Pump Control
Admins can manually start pumps:

```bash
# Start pump manually
curl -X POST http://localhost:8080/api/devices/12345/pump/start \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN"
```

### User Management
Admins can manage other users:

```bash
# List all users
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN"

# Promote user to admin
curl -X PUT http://localhost:8080/api/users/67890/promote \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN"

# Delete user
curl -X DELETE http://localhost:8080/api/users/67890 \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN"
```

## Development Guide

### Adding New Features

1. **Create Entity Classes**
```java
@Entity
@Table(name = "new_feature")
public class NewFeature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // ... other fields
}
```

2. **Create Repository**
```java
public interface NewFeatureRepository extends JpaRepository<NewFeature, Long> {
    // Custom queries
}
```

3. **Create Service Layer**
```java
@Service
@Transactional
public class NewFeatureService {
    // Business logic
}
```

4. **Create Controller**
```java
@RestController
@RequestMapping("/api/new-feature")
@PreAuthorize("hasRole('ADMIN')")
public class NewFeatureController {
    // API endpoints
}
```

### Testing Strategy

1. **Unit Tests**
```java
@ExtendWith(MockitoExtension.class)
class NewFeatureServiceTest {
    @Mock
    private NewFeatureRepository repository;
    
    @Test
    void shouldCreateNewFeature() {
        // Test implementation
    }
}
```

2. **Integration Tests**
```java
@SpringBootTest
@AutoConfigureTestDatabase
class NewFeatureIntegrationTest {
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateFeatureViaApi() {
        // Integration test
    }
}
```

### Security Considerations

1. **Always validate input data**
2. **Use @PreAuthorize for method-level security**
3. **Log security events for auditing**
4. **Validate device keys for hardware endpoints**

### Performance Optimization

1. **Use pagination for large datasets**
2. **Implement caching for frequently accessed data**
3. **Optimize database queries with proper indexing**
4. **Use WebSocket for real-time updates instead of polling**

### Monitoring and Logging

The system uses structured logging with MDC (Mapped Diagnostic Context):

```java
// In your service classes
MDC.put("userId", currentUser.getId().toString());
MDC.put("deviceId", device.getId().toString());
log.info("User {} accessed device {}", userId, deviceId);
MDC.clear();
```

### Troubleshooting

**Common Issues:**

1. **JWT Token Expired**
   - Error: `401 Unauthorized`
   - Solution: Refresh token or login again

2. **Device Key Invalid**
   - Error: `403 Forbidden`
   - Solution: Verify device key is correct and device exists

3. **Database Connection Issues**
   - Error: `Connection refused`
   - Solution: Check H2 console or database configuration

4. **WebSocket Connection Failed**
   - Error: `Connection failed`
   - Solution: Verify CORS settings and authentication

For more detailed information, check the application logs in the `logs/` directory.

## Next Steps

1. Implement the frontend React/Angular application
2. Set up production database (MySQL/PostgreSQL)
3. Configure SSL/TLS for HTTPS
4. Set up monitoring and alerting
5. Implement backup and recovery procedures

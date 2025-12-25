# Water Level Monitoring Backend API

A comprehensive backend system for monitoring water levels with automated pump control, user management, and real-time data monitoring.

## 📋 Features

- **User Management**: Registration, authentication, and role-based access control (USER/ADMIN)
- **Device Management**: Device registration with secure key-based authentication
- **Real-time Monitoring**: WebSocket-based live data streaming
- **Automated Control**: Threshold-based pump automation
- **Manual Control**: Admin override for pump operations
- **Audit Logging**: Comprehensive logging and audit trails
- **API Documentation**: Interactive Swagger/OpenAPI documentation

## 🏗️ Architecture

### Database Schema
- **Users**: User accounts with role-based permissions
- **Devices**: Registered monitoring devices with unique keys
- **WaterLevelData**: Historical sensor readings and thresholds
- **PumpControl**: Manual pump control flags

### Security
- JWT-based authentication
- Role-based authorization (USER/ADMIN)
- Device key validation for hardware communication
- Comprehensive audit logging

## 🚀 Getting Started

### Quick Start (Recommended - Docker)

**Prerequisites:**
- Docker Desktop installed and running

**Steps:**

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd water-level-monitoring-backend
   ```

2. **Start the application**
   ```bash
   docker compose up --build
   ```

3. **Access the application**
   - API Base URL: `http://localhost:8080/api`
   - Swagger UI: `http://localhost:8080/api/swagger-ui.html`
   - OpenAPI JSON: `http://localhost:8080/api/api-docs`

**What this does:**
- Builds the Spring Boot application inside Docker (no Java/Gradle needed locally)
- Starts the backend API container
- Starts the Mosquitto MQTT broker container
- Uses H2 in-memory database (same as local dev)

**Stop the application:**
```bash
docker compose down
```

For detailed Docker instructions, see [DOCKER_COMPOSE.md](DOCKER_COMPOSE.md).

### Alternative: Local Development Setup

**Prerequisites:**
- Java 21 or higher
- Gradle 8.0 or higher
- MQTT broker (see [MQTT_SETUP.md](MQTT_SETUP.md) for local Mosquitto installation)

**Steps:**

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd water-level-monitoring-backend
   ```

2. **Set required environment variable**
   ```bash
   # Windows PowerShell
   $env:JWT_SECRET = "your-secret-key-here-min-32-chars"
   
   # Linux/Mac
   export JWT_SECRET="your-secret-key-here-min-32-chars"
   ```

3. **Run the application**
   ```bash
   ./gradlew bootRun
   ```

4. **Access the application**
   - API Base URL: `http://localhost:8080/api`
   - Swagger UI: `http://localhost:8080/api/swagger-ui.html`
   - H2 Console: `http://localhost:8080/api/h2-console`

### Development Commands

```bash
# Format code
./gradlew spotlessApply

# Run tests with coverage
./gradlew test

# Run all quality checks
./gradlew verify

# Generate documentation
./gradlew javadoc
```

## 📚 API Documentation

### Interactive Documentation
- **Swagger UI**: `http://localhost:8080/api/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/api/api-docs`

### Authentication Endpoints
- `POST /api/auth/register` - User registration (returns JWT token)
- `POST /api/auth/login` - User login (returns JWT token)
- `GET /api/auth/me` - Get current authenticated user

### Device Management (Admin Only)
- `POST /api/devices/register` - Register new device (returns device key)
- `GET /api/devices` - List all devices
- `DELETE /api/devices/{id}` - Delete device

### Threshold Management (Admin Only)
- `GET /api/devices/{deviceId}/thresholds` - Get current thresholds
- `PUT /api/devices/{deviceId}/thresholds` - Update thresholds (publishes to MQTT)

### Pump Control (Admin Only)
- `POST /api/devices/{deviceId}/pump/start` - Manually start pump (publishes to MQTT)
- `GET /api/devices/{deviceId}/pump/status` - Get current pump status

### Water Level Data (Authenticated Users)
- `GET /api/devices/{deviceId}/water-level-data` - Get paginated historical water level data for a device

### User Management (Admin Only)
- `GET /api/users` - List all users
- `PUT /api/users/{id}/promote` - Promote user to admin
- `DELETE /api/users/{id}` - Delete user

### WebSocket Endpoints
- `WS /api/ws` - WebSocket endpoint for real-time updates
- Topics:
  - `/topic/device/{deviceId}` - Device updates (sensor data, pump status, thresholds)

### Postman Collection
Import the Postman collection from `postman/Water-Level-Monitoring-API.postman_collection.json` for easy API testing.

## 🔧 Configuration

### Spring Boot Profiles (Best Practice)

The application uses **Spring Boot profiles** for configuration management:

- **`application.yml`** - Base configuration (shared technical constants)
- **`application-dev.yml`** - Development profile (safe defaults, can be committed)
- **`application-prod.yml`** - Production profile (structure only, no secrets)

**How it works:**
1. Non-sensitive defaults → YAML files (committed to Git)
2. Secrets & environment-specific values → Environment variables (NOT committed)

### Running the Application

**Development:**
```bash
# Uses 'dev' profile automatically (default)
./gradlew bootRun

# No need to manually set profile - dev is the default for local development
```

**Production:**
```bash
# Set required environment variables, then:
export SPRING_PROFILES_ACTIVE=prod
java -jar app.jar
```

### Environment Variables

**Required (Secrets - must be set in all environments):**
- `JWT_SECRET` - JWT signing secret (minimum 32 characters)
  - Generate with: `openssl rand -hex 32` (Linux/Mac)
  - Or use any string with at least 32 characters
  - **Note:** Docker Compose sets this automatically, no manual setup needed

**Optional (for production-like local testing):**
- `DB_URL` - Database connection URL (e.g., `jdbc:postgresql://host:5432/db`)
- `DB_USERNAME` - Database username
- `DB_PASSWORD` - Database password
- `MQTT_BROKER_URL` - MQTT broker URL
- `MQTT_CLIENT_ID` - MQTT client ID
- `CORS_ALLOWED_ORIGINS` - Comma-separated allowed origins
- `WEBSOCKET_ALLOWED_ORIGINS` - Comma-separated allowed origins

**Optional (override defaults if needed):**
- `SERVER_PORT` - Server port (default: 8080)
- `JWT_EXPIRATION` - Token expiration in milliseconds (default: 86400000)
- `SPRINGDOC_SERVER_URL` - OpenAPI server URL
- `SPRINGDOC_CONTACT_EMAIL` - Contact email for API docs

See `application-prod.yml` for production requirements and `application-dev.yml` for development defaults.

### Development Configuration

The `dev` profile (`application-dev.yml`) includes:
- H2 in-memory database (default)
- H2 console enabled
- Debug logging
- Local MQTT broker (defaults to `tcp://localhost:1883`)
- Localhost CORS origins
- Swagger UI and OpenAPI docs are publicly accessible

**To run locally (without Docker):**
1. Set `JWT_SECRET` environment variable (required, minimum 32 characters)
2. Start MQTT broker locally (see [MQTT_SETUP.md](MQTT_SETUP.md))
3. Optionally set `MQTT_BROKER_URL` if using a different broker
4. Run: `./gradlew bootRun` (automatically uses `dev` profile)

**To run with Docker:**
- No setup needed - just run `docker compose up --build`
- Docker handles MQTT broker automatically

### Production Configuration

The `prod` profile (`application-prod.yml`) is available for production-like local testing:
- PostgreSQL database (via `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`)
- Production MQTT broker (via `MQTT_BROKER_URL`, `MQTT_CLIENT_ID`)
- Production CORS origins (via `CORS_ALLOWED_ORIGINS`)
- All secrets via environment variables

**To run with prod profile locally:**
1. Set all required environment variables
2. Set `SPRING_PROFILES_ACTIVE=prod`
3. Run: `./gradlew bootRun`

### MQTT Configuration

See [MQTT_SETUP.md](MQTT_SETUP.md) for detailed setup instructions.

**Development:** Uses local Mosquitto broker (`tcp://localhost:1883`) by default. See [MQTT_SETUP.md](MQTT_SETUP.md) for installation and setup instructions.

### Logging
Comprehensive logging is configured:
- **Application logs**: `logs/application.log` - General application logs
- **Security logs**: `logs/security.log` - Authentication and authorization events
- **Audit logs**: `logs/audit.log` - Admin actions and critical operations

Log levels:
- **Development**: DEBUG for application, INFO for root
- **Production**: INFO for application, WARN for root

## 🧪 Testing

### Running Tests
```bash
# Run all tests
./gradlew test

# Run with coverage report
./gradlew jacocoTestReport

# View coverage report
open build/jacocoHtml/index.html
```

### Test Structure
- **Unit Tests**: Service layer tests (`src/test/java/com/example/waterlevel/service/`)
- **Controller Tests**: REST endpoint tests using MockMvc (`src/test/java/com/example/waterlevel/controller/`)
- **Integration Tests**: Full flow tests (`src/test/java/com/example/waterlevel/integration/`)

### Test Coverage
The project uses JaCoCo for code coverage reporting. Aim for at least 80% coverage.

### Postman Collection
Import the Postman collection for manual API testing:
1. Import `postman/Water-Level-Monitoring-API.postman_collection.json`
2. Import `postman/Water-Level-Monitoring-API.postman_environment.json`
3. Set `base_url` environment variable
4. Login to automatically set `jwt_token` variable

## 📊 Code Quality

### Static Analysis
- **Spotless**: Code formatting with Google Java Format
- **Checkstyle**: Code style validation
- **SonarQube**: Code quality and security analysis

### Pre-commit Hooks
```bash
# Apply formatting
./gradlew spotlessApply

# Run tests
./gradlew test
```

## 🚀 CI/CD Pipeline

The GitHub Actions workflow automatically:
1. Code formatting validation (`spotlessCheck`)
2. Static analysis (`checkstyle`)
3. Unit tests (`test`)
4. Code coverage (`jacocoTestReport`)
5. SonarQube analysis (`sonar`)

**For local SonarQube analysis:**
1. Get your SonarCloud token: https://sonarcloud.io → My Account → Security
2. Set environment variable:
   ```bash
   export SONAR_TOKEN=your-token-here
   ```
3. Run: `./gradlew sonar`

**Note:** The organization key is already configured in `build.gradle` (it's public). Only the token needs to be set per user.

## 🤝 Contributing

1. Follow conventional commit format
2. Ensure code is properly formatted (`./gradlew spotlessApply`)
3. Add tests for new features
4. Update documentation

### Commit Message Format
```
type(scope): description

feat(auth): add JWT token refresh endpoint
fix(device): resolve device key validation issue
docs(readme): update installation instructions
```

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📖 Additional Documentation

- **[DOCKER_COMPOSE.md](DOCKER_COMPOSE.md)** - Docker multi-container setup guide (recommended)
- **[MQTT_SETUP.md](MQTT_SETUP.md)** - MQTT broker setup for local development
- **[tutorial.md](tutorial.md)** - Step-by-step tutorial and API usage guides
- **[Postman Collection](postman/)** - API testing collection

## 🔐 Security Best Practices

1. **Never commit secrets**: Use environment variables for all sensitive data
2. **Strong JWT secret**: Generate with `openssl rand -hex 32`
3. **MQTT authentication**: Use username/password for MQTT broker when connecting to external brokers
4. **Database security**: Use strong passwords and restrict database access
5. **CORS configuration**: Restrict allowed origins appropriately

## 🔧 Troubleshooting

### Docker Issues

**Port already in use:**
- Check if port 8080 or 1883 is already in use
- Stop conflicting services or change ports in `docker-compose.yml`

**Container won't start:**
- Check logs: `docker compose logs backend`
- Ensure Docker Desktop is running
- Try rebuilding: `docker compose up --build`

**MQTT connection issues:**
- Verify Mosquitto container is running: `docker compose ps`
- Check Mosquitto logs: `docker compose logs mosquitto`

### Local Development Issues

**JWT_SECRET error:**
- Ensure `JWT_SECRET` is set and has at least 32 characters
- Check environment variable is set correctly: `echo $JWT_SECRET` (Linux/Mac) or `$env:JWT_SECRET` (PowerShell)

**MQTT connection refused:**
- Ensure MQTT broker is running (see [MQTT_SETUP.md](MQTT_SETUP.md))
- Verify `MQTT_BROKER_URL` is correct (default: `tcp://localhost:1883`)

**Port 8080 already in use:**
- Change port: Set `SERVER_PORT` environment variable to a different port
- Or stop the service using port 8080

**Swagger UI shows "Access Denied":**
- Swagger UI is now publicly accessible by default
- If you see access denied, check application logs for security configuration errors

## 📞 Support

For support and questions:
- Create an issue in the repository
- Check the [tutorial.md](tutorial.md) for detailed guides
- Review API documentation at `/swagger-ui.html`
- Check [MQTT_SETUP.md](MQTT_SETUP.md) for MQTT configuration help
- Check [DOCKER_COMPOSE.md](DOCKER_COMPOSE.md) for Docker-specific help

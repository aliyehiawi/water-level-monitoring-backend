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

### Prerequisites
- Java 21 or higher
- Gradle 8.0 or higher

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd water-level-monitoring-backend
   ```

2. **Run the application**
   ```bash
   ./gradlew bootRun
   ```

3. **Access the application**
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

### Authentication Endpoints
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `GET /api/auth/me` - Get current user

### Device Management (Admin)
- `POST /api/devices/register` - Register new device
- `GET /api/devices` - List all devices
- `DELETE /api/devices/{id}` - Delete device

### Data Collection (Hardware)
- `POST /api/data/sensor` - Submit sensor data
- `GET /api/data/pump-flag/{deviceKey}` - Check manual pump flag
- `PUT /api/data/pump-flag/{deviceKey}/reset` - Reset pump flag

### Dashboard (All Users)
- `GET /api/dashboard/devices` - Get accessible devices
- `GET /api/dashboard/data/{deviceId}` - Get water level history

For complete API documentation, visit the Swagger UI at `/swagger-ui.html`

## 🔧 Configuration

### Database
The application uses H2 in-memory database by default. For production:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/waterlevel
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

### JWT Configuration
```yaml
spring:
  security:
    jwt:
      secret: ${JWT_SECRET}
      expiration: ${JWT_EXPIRATION:86400000}
```

**Note:** The default JWT secret in `application.yml` is for development only. Always use environment variables in production.

### Production Profile
Use the `prod` profile for production deployments:
```bash
java -jar app.jar --spring.profiles.active=prod
```

Ensure all required environment variables are set:
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`

### Logging
Logs are configured with MDC for tracing:
- Application logs: `logs/application.log`
- Security logs: `logs/security.log`
- Audit logs: `logs/audit.log`

## 🧪 Testing

```bash
# Run unit tests
./gradlew test

# Run with coverage report
./gradlew jacocoTestReport

# View coverage report
open build/jacocoHtml/index.html
```

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

## 🚀 Deployment

### CI/CD Pipeline
1. Code formatting validation (`spotlessCheck`)
2. Static analysis (`checkstyle`)
3. Unit tests (`test`)
4. Code coverage (`jacocoTestReport`)
5. SonarQube analysis (`sonar`)

### Environment Variables

For development, you can use the default values. For production, set these environment variables:

```bash
# Required for production
export JWT_SECRET=your_jwt_secret_key 
export DB_URL=jdbc:mysql://localhost:3306/waterlevel
export DB_USERNAME=your_db_username
export DB_PASSWORD=your_db_password
export H2_CONSOLE_ENABLED=false
export SPRING_PROFILES_ACTIVE=prod

# Optional
export JWT_EXPIRATION=86400000
export SERVER_PORT=8080
```

**See [env-setup.md](env-setup.md) for detailed setup instructions.**

**Note:** Copy `env.example` to `.env` and fill in your values (`.env` is gitignored).

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

## 📞 Support

For support and questions:
- Create an issue in the repository
- Check the [tutorial.md](tutorial.md) for detailed guides
- Review API documentation at `/swagger-ui.html`

# Deployment Guide

This guide covers deploying the Water Level Monitoring Backend to Clever Cloud.

## Prerequisites

- Clever Cloud account
- Git repository with the code
- MQTT broker (see [MQTT_SETUP.md](MQTT_SETUP.md))

## Clever Cloud Deployment

### 1. Create Application

1. Log in to [Clever Cloud](https://www.clever-cloud.com/)
2. Create a new application
3. Select "Java + Maven/Gradle" runtime
4. Connect your Git repository

### 2. Configure Environment Variables

Set the following environment variables in Clever Cloud dashboard:

**Required:**
- `DB_URL` - Database connection URL (Clever Cloud provides PostgreSQL addon)
- `DB_USERNAME` - Database username
- `DB_PASSWORD` - Database password
- `JWT_SECRET` - Generate with: `openssl rand -hex 32`
- `SPRING_PROFILES_ACTIVE=prod`

**Optional:**
- `JWT_EXPIRATION` - Default: 86400000 (24 hours)
- `SERVER_PORT` - Default: 8080
- `MQTT_BROKER_URL` - MQTT broker URL
- `MQTT_CLIENT_ID` - MQTT client ID
- `MQTT_USERNAME` - MQTT username (if required)
- `MQTT_PASSWORD` - MQTT password (if required)
- `CORS_ALLOWED_ORIGINS` - Comma-separated origins
- `WEBSOCKET_ALLOWED_ORIGINS` - Comma-separated origins

### 3. Add PostgreSQL Database

1. In Clever Cloud dashboard, add a PostgreSQL addon
2. The database credentials will be automatically set as environment variables
3. Update `DB_URL` to use the provided PostgreSQL URL

### 4. Configure Build

The `clevercloud.json` file is already configured:
- Build command: `gradle build -x test`
- Deploy: JAR file from `build/libs/`
- Post-deploy: Run with `prod` profile

### 5. Deploy

1. Push your code to the connected Git repository
2. Clever Cloud will automatically build and deploy
3. Monitor deployment logs in the dashboard

## Post-Deployment

### Verify Deployment

1. Check health endpoint: `https://your-app.cleverapps.io/api/actuator/health`
2. Access Swagger UI: `https://your-app.cleverapps.io/api/swagger-ui.html`
3. Test authentication endpoints

### MQTT Configuration

1. Set up MQTT broker (see [MQTT_SETUP.md](MQTT_SETUP.md))
2. Configure MQTT environment variables
3. Test MQTT connectivity

### Database Migration

The application uses JPA with `ddl-auto: validate` in production:
- Ensure database schema matches entity definitions
- For initial setup, temporarily use `create` or `update`
- Switch back to `validate` after schema is created

## Troubleshooting

### Build Failures
- Check build logs in Clever Cloud dashboard
- Verify Gradle wrapper is included in repository
- Ensure all dependencies are resolvable

### Runtime Errors
- Check application logs
- Verify all required environment variables are set
- Test database connectivity

### MQTT Connection Issues
- Verify MQTT broker URL and credentials
- Check firewall rules
- Review MQTT broker logs

## Environment Variables Reference

See `application-prod.yml` for complete production configuration requirements.

## Additional Resources

- [Clever Cloud Documentation](https://www.clever-cloud.com/doc/)
- [Spring Boot Production Deployment](https://docs.spring.io/spring-boot/docs/current/reference/html/deployment.html)
- [MQTT Setup Guide](MQTT_SETUP.md)


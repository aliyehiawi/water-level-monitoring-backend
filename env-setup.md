# Environment Variables Setup Guide

This guide explains how to configure environment variables for the Water Level Monitoring Backend.

## Quick Start

### Development (Default)
For local development, you can use the default values in `application.yml`. No environment variables are required.

### Production
For production, you **MUST** set the following environment variables:

## Required Environment Variables

### Database Configuration
```bash
DB_URL=jdbc:mysql://localhost:3306/waterlevel
DB_USERNAME=your_db_username
DB_PASSWORD=your_secure_password
```

### Security Configuration
```bash
# Generate a strong JWT secret (32+ characters recommended)
# On Linux/Mac: openssl rand -hex 32
# On Windows: use PowerShell: -join ((48..57) + (65..90) + (97..122) | Get-Random -Count 32 | % {[char]$_})
JWT_SECRET=your_very_secure_random_secret_key_here
JWT_EXPIRATION=86400000  # 24 hours in milliseconds
```

### Application Configuration
```bash
# Disable H2 console in production
H2_CONSOLE_ENABLED=false

# Use production profile
SPRING_PROFILES_ACTIVE=prod
```

## Setting Environment Variables

### Windows (PowerShell)
```powershell
# Set for current session
$env:JWT_SECRET="your_secret_here"
$env:DB_PASSWORD="your_password_here"

# Set permanently (User level)
[System.Environment]::SetEnvironmentVariable("JWT_SECRET", "your_secret_here", "User")
[System.Environment]::SetEnvironmentVariable("DB_PASSWORD", "your_password_here", "User")
```

### Windows (Command Prompt)
```cmd
set JWT_SECRET=your_secret_here
set DB_PASSWORD=your_password_here
```

### Linux/Mac (Bash)
```bash
# Set for current session
export JWT_SECRET="your_secret_here"
export DB_PASSWORD="your_password_here"

# Set permanently (add to ~/.bashrc or ~/.zshrc)
echo 'export JWT_SECRET="your_secret_here"' >> ~/.bashrc
echo 'export DB_PASSWORD="your_password_here"' >> ~/.bashrc
source ~/.bashrc
```

### Docker
```bash
# Using docker run
docker run -e JWT_SECRET=your_secret -e DB_PASSWORD=your_password your-image

# Using docker-compose.yml
services:
  app:
    environment:
      - JWT_SECRET=${JWT_SECRET}
      - DB_PASSWORD=${DB_PASSWORD}
```

### Docker Compose
1. Copy `docker-compose.example.yml` to `docker-compose.yml`
2. Copy `env.example` to `.env` and fill in your values:
```bash
JWT_SECRET=your_secret_here
DB_URL=jdbc:mysql://db:3306/waterlevel
DB_USERNAME=waterlevel_user
DB_PASSWORD=secure_password
SPRING_PROFILES_ACTIVE=prod
H2_CONSOLE_ENABLED=false
```
3. Run: `docker-compose up -d`

## Generating Secure Secrets

### JWT Secret Generation

**Linux/Mac:**
```bash
openssl rand -hex 32
```

**Windows (PowerShell):**
```powershell
-join ((48..57) + (65..90) + (97..122) | Get-Random -Count 64 | % {[char]$_})
```

**Online (if needed):**
- Use a secure random string generator
- Minimum 32 characters recommended
- Mix of letters, numbers, and special characters

## Running with Environment Variables

### Using Spring Profiles
```bash
# Development (uses defaults from application.yml)
./gradlew bootRun

# Production (requires all env vars to be set)
java -jar build/libs/water-level-monitoring-backend-*.jar --spring.profiles.active=prod
```

### Verifying Environment Variables
```bash
# Check if variables are set
echo $JWT_SECRET  # Linux/Mac
echo %JWT_SECRET%  # Windows CMD
$env:JWT_SECRET    # Windows PowerShell
```

## Security Best Practices

1. **Never commit `.env` files** - They are already in `.gitignore`
2. **Use strong secrets** - Minimum 32 characters for JWT secrets
3. **Rotate secrets regularly** - Especially if compromised
4. **Use different secrets** - Different values for dev, staging, and production
5. **Use secret management** - Consider using:
   - AWS Secrets Manager
   - HashiCorp Vault
   - Kubernetes Secrets
   - Azure Key Vault

## Troubleshooting

### Application won't start
- Check that all required environment variables are set
- Verify variable names match exactly (case-sensitive)
- Check for typos in variable values

### "Invalid JWT secret" errors
- Ensure JWT_SECRET is set and not empty
- Verify secret is long enough (32+ characters recommended)
- Check for special characters that might need escaping

### Database connection errors
- Verify DB_URL format is correct
- Check DB_USERNAME and DB_PASSWORD are correct
- Ensure database server is accessible

## Example Production Setup

```bash
# Set all required variables
export SPRING_PROFILES_ACTIVE=prod
export JWT_SECRET=$(openssl rand -hex 32)
export DB_URL=jdbc:mysql://db.example.com:3306/waterlevel
export DB_USERNAME=waterlevel_prod
export DB_PASSWORD=$(openssl rand -base64 24)
export H2_CONSOLE_ENABLED=false

# Run application
java -jar water-level-monitoring-backend.jar
```


# Docker Compose (Multi-container) - Local Run

This repo can be run locally as a **multi-container** application:

- **backend**: Spring Boot API
- **mosquitto**: MQTT broker

## Prerequisites

- Docker Desktop installed + running

## Run

From the project root:

```powershell
docker compose up --build
```

Wait until the logs show the backend started.

## Access the APIs

- **Swagger UI**: `http://localhost:8080/api/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/api/api-docs`

### Where to run commands (beginner-friendly)

- You can run **all commands in your normal PC terminal** (PowerShell). Docker Desktop just needs to be running.
- You do **not** need to open a terminal inside Docker Desktop.
- If you need multiple terminals, open **two PowerShell windows** (or two VS Code terminals).

### If you want to run a command inside a container

Use:

```powershell
docker compose exec <service_name> <command>
```

Service names in this project: `backend`, `mosquitto`

## Test (quick checks)

### 1) Check containers

```powershell
docker compose ps
```

### 2) Test MQTT broker (publish/subscribe)

Terminal A (subscribe):

```powershell
docker compose exec mosquitto mosquitto_sub -h localhost -t "devices/+/sensor/data" -v
```

Terminal B (publish):

```powershell
docker compose exec mosquitto mosquitto_pub -h localhost -t "devices/test-device/sensor/data" -m '{"device_key":"test-device","water_level":50.5}'
```

**Note**: Use **single quotes** (`'`) for the JSON message in PowerShell to avoid escaping issues.

## Stop

```powershell
docker compose down
```

## Clean reset (also removes DB data)

```powershell
docker compose down -v
```


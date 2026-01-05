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

## MQTT Broker

The MQTT broker (Mosquitto) is automatically started and configured:
- **Broker URL**: `tcp://mosquitto:1883` (internal Docker network)
- **Backend connection**: Automatically configured via `MQTT_BROKER_URL` environment variable
- **No setup required**: Ready to use immediately after `docker compose up`

You can test the MQTT broker using the commands below.

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
docker compose exec mosquitto mosquitto_pub -h localhost -t "devices/<DEVICE_KEY>/sensor/data" -m '{"device_key":"<DEVICE_KEY>","water_level":50.5,"pump_status":"ON"}'
```

**Note**: Use **single quotes** (`'`) for the JSON message in PowerShell to avoid escaping issues.

**Important**:
- Replace `<DEVICE_KEY>` with a real device key returned from `POST /api/devices/register` (it must exist in the database).

## Stop

```powershell
docker compose down
```

## Clean reset (also removes DB data)

```powershell
docker compose down -v
```


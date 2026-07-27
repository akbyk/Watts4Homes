# VoltWise

VoltWise is a real-time IoT energy analytics and budget auditing platform. It ingests live appliance-level telemetry from home sensors, tracks per-home energy usage and cost against a configurable budget in real time, applies tariff and anomaly rules, and sends AI-generated behavioral advisories by email when a home crosses a usage threshold or an appliance starts drawing an unsafe amount of power.

The system is built as two independent Spring Boot applications connected exclusively through Kafka, backed by Apache Ignite for hot-path operational state and PostgreSQL for durable, permanent records.

## Architecture

```
[Telemetry Sensors] --publishes wattage--> [Kafka: telemetry-stream]
                                                   |
                                                   v
                                        [Core: Telemetry Processing]
                                                   |
                                                   v
                                        [Ignite: home-state, appliance-breach]
                                                   |
                                                   v
                                     [Core: Tariff and Anomaly Rules]
                                        |                        |
                                        v                        v
                          [Postgres: event_log]      [Core: AI Notification]
                                                          |            |
                                                          v            v
                                                     [Gemini]   [Postgres: ai_recommendations]
                                                          |            |
                                                          v            v
                                                     (email dispatch to contact_email)

[Core: Home Registration] --writes--> [Postgres: homes, appliances]
                          --publishes--> [Kafka: home-registration] --> [Telemetry Sensors registry]
```

### Components

- **VoltWise Core** — a Spring Boot modular monolith organized by feature (`homes`, `telemetry`, `rules`, `notifications`). It owns the REST API, the Kafka consumers/producers, all Postgres persistence, and all Ignite state management.
- **VoltWise Telemetry Sensors** — a standalone Spring Boot application that simulates appliance-level power draw for every registered home. It has no dependency on Core and communicates exclusively through Kafka.
- **Apache Ignite** — holds the current operational state of the system: running usage/cost totals per home and consecutive-breach counters per appliance. This is the sub-millisecond read/write path and is treated as disposable — nothing here needs to survive a restart.
- **PostgreSQL** — the system of record. Home and appliance configuration, the permanent audit trail of every breach/anomaly/penalty event, generated AI advisories, and daily consumption snapshots all live here.
- **Apache Kafka** — the only channel of communication between Core and Sensors. Both topics are keyed by `home_id` so that all messages for a given home are processed in order by the same consumer thread.
- **Gemini** — generates the Turkish-language behavioral advisory sent to homeowners when a rule fires. A resilient fallback advisory is used automatically if the call fails, times out, or is rate-limited.

## Prerequisites

- Java 21
- Maven 3.9+
- Docker and Docker Compose v2
- A Gemini API key
- SMTP credentials for outbound email (any provider; disposable test accounts such as Ethereal work well for local development)

## Getting Started

### 1. Start the infrastructure

```bash
cd voltwise
docker compose up -d
```

This brings up Kafka (KRaft mode, no Zookeeper), a single-node Ignite instance, and PostgreSQL. Verify each service is healthy before continuing:

```bash
docker exec voltwise-postgres pg_isready -U voltwise -d voltwise
docker exec voltwise-kafka /opt/kafka/bin/kafka-broker-api-versions.sh --bootstrap-server localhost:9092
curl "http://localhost:8080/ignite?cmd=version"
```

### 2. Configure environment variables

Core and Sensors read all connection details and secrets from environment variables — nothing is hardcoded. Create `.env.core`:

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=voltwise
export DB_USER=voltwise
export DB_PASSWORD=voltwise_dev_password
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export IGNITE_ADDRESS=localhost:10800
export GEMINI_API_KEY=your-gemini-api-key
export MAIL_HOST=your-smtp-host
export MAIL_PORT=587
export MAIL_USERNAME=your-smtp-username
export MAIL_PASSWORD=your-smtp-password
```

And `.env.sensors`:

```bash
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export SERVER_PORT=8082
```

Neither file is committed to source control.

### 3. Run Core

```bash
cd voltwise-core
source ../.env.core
mvn clean install
mvn spring-boot:run
```

On startup, Core applies its database migrations, connects to Kafka, Ignite, and Postgres, and exposes its API. Swagger documentation is available at `http://localhost:8081/swagger-ui.html`.

### 4. Run Telemetry Sensors

```bash
cd voltwise-sensors
source ../.env.sensors
mvn clean install
mvn spring-boot:run
```

Sensors starts independently of Core and immediately begins listening for home registrations. It has no REST API of its own.

## How It Works

### Registering a home

A `POST /api/homes` request persists the home and its appliances to PostgreSQL in a single transaction. Only after that transaction commits does Core publish a registration event to Kafka, which Telemetry Sensors consumes to add the home to its in-memory simulation registry. From that point on, Sensors generates a realistic, fluctuating wattage reading for every appliance roughly every five seconds and publishes it to the telemetry stream.

### Processing telemetry

Core consumes every telemetry reading and atomically updates the home's running usage and cost totals in Ignite, converting the tariff to the penalty rate the moment a home crosses its full budget. In parallel, each appliance's reading is checked against its configured safe limit; three consecutive over-limit readings mark the appliance anomalous, and any reading back under the limit resets the counter immediately.

Every threshold crossing — an 80 percent budget warning, a 100 percent breach with penalty tariff activation, or an anomalous appliance — is logged once, durably, to PostgreSQL, and never fires more than once per threshold for the same home.

### Notifying homeowners

Each of those events triggers an asynchronous call into the AI Notification module, which gathers the home's current state, builds a Turkish-language prompt describing the situation in concrete terms, and asks Gemini to generate a short, personalized, actionable piece of advice. The generated text (or a fallback message if Gemini is unavailable) is persisted and emailed to the home's registered contact address. This work always runs off the main telemetry-processing thread, so a slow or failing AI call never affects ingestion.

### Reading system state

Two read endpoints serve different needs and deliberately read from different stores:

- `GET /api/homes/{homeId}/status` and `GET /api/homes/status` return live, low-latency state — accumulated usage and cost, tariff state, and per-appliance breach status — sourced exclusively from Ignite.
- `GET /api/homes/{homeId}/trend` returns daily aggregated usage and cost, sourced exclusively from PostgreSQL. A scheduled job rolls up Ignite's running totals into a daily snapshot row for each home.

## API Reference

| Method | Path | Description | Source |
|---|---|---|---|
| POST | `/api/homes` | Register a new home with its appliances | Postgres |
| GET | `/api/homes/{homeId}/status` | Live status for one home | Ignite |
| GET | `/api/homes/status` | Live status for all homes | Ignite |
| GET | `/api/homes/{homeId}/trend` | Daily aggregated consumption history | Postgres |

Full request/response schemas are available through Swagger once Core is running.

## Database Schema

| Table | Purpose |
|---|---|
| `homes` | Registered homes and their billing configuration |
| `appliances` | Appliances belonging to a home and their safe wattage limits |
| `event_log` | Permanent audit trail of every breach, anomaly, and penalty activation |
| `ai_recommendations` | Every AI-generated (or fallback) advisory, with email delivery status |
| `consumption_snapshots` | Daily rollup of usage and cost per home, used for historical trend charts |

Schema is managed entirely through Flyway migrations in `voltwise-core/src/main/resources/db/migration`.

## Kafka Topics

| Topic | Producer | Consumer | Keyed by |
|---|---|---|---|
| `home-registration` | Core | Sensors | `home_id` |
| `telemetry-stream` | Sensors | Core | `home_id` |

Both payload contracts are documented as JSON Schema in the `contracts/` directory.

## Ignite State

| Cache | Key | Purpose |
|---|---|---|
| `home-state` | `home_id` | Running usage, cost, and tariff state for a home |
| `appliance-breach` | `home_id:appliance_id` | Consecutive over-limit reading count and current status for an appliance |

Ignite holds no data that isn't reconstructible from Postgres on first use; nothing here is treated as permanent.

## Project Structure

```
voltwise/
├── docker-compose.yml
├── contracts/
├── docs/
├── voltwise-core/
│   └── src/main/java/com/voltwise/core/
│       ├── homes/
│       ├── telemetry/
│       ├── rules/
│       ├── notifications/
│       ├── scheduling/
│       └── config/
└── voltwise-sensors/
    └── src/main/java/com/voltwise/sensors/
        ├── registry/
        ├── listener/
        ├── simulation/
        └── event/
```

## Design Principles

- **Kafka is the only channel between Core and Sensors.** Neither service calls the other's API directly.
- **Ignite before Postgres, always, on the hot path.** Every rule that fires confirms its Ignite write before attempting the corresponding Postgres log write, with failures in one isolated from the other.
- **No secret is ever hardcoded.** All credentials and connection details are resolved from environment variables.
- **Notifications never block ingestion.** AI and email work runs on a dedicated thread pool, separate from the Kafka consumer thread.

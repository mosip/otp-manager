# AGENTS.md

This file provides guidance to AI agents when working with code in this repository.
## Project Overview

**kernel-otpmanager-service** is a Spring Boot microservice (part of the [MOSIP](https://mosip.io) platform) that generates and validates One-Time Passwords. It exposes two REST endpoints under `/v1/otpmanager` and runs on port 8085. The service depends on a Spring Config Server in non-local environments.

## Build & Run

All Maven commands must be run from `kernel/kernel-otpmanager-service/`:

```bash
# Build (skip GPG signing)
cd kernel
mvn clean install -Dgpg.skip=true

# Run tests only
cd kernel/kernel-otpmanager-service
mvn test

# Run a single test class
mvn test -Dtest=OtpGeneratorServiceTest

# Run the service locally (requires Spring Config Server or local profile)
java -jar -Dspring.profiles.active=local target/kernel-otpmanager-service-*.jar
```

## Key Configuration

Local configuration lives in `src/main/resources/application-local.properties`. Critical properties:

| Property | Default | Description |
|---|---|---|
| `mosip.kernel.otp.default-length` | `6` | OTP digit length |
| `mosip.kernel.otp.expiry-time` | `120` | Seconds until OTP expires |
| `mosip.kernel.otp.key-freeze-time` | `7200` | Seconds a key is frozen after max failed attempts |
| `mosip.kernel.otp.validation-attempt-threshold` | `3` | Failed attempts before key freeze |

The `local` Spring profile bypasses real OTP validation — any request with OTP `111111` (configurable via `local.env.otp`) returns success.

## Architecture

```
OtpGeneratorController  →  OtpGenerator SPI  →  OtpGeneratorServiceImpl
OtpValidatorController  →  OtpValidator SPI  →  OtpValidatorServiceImpl
                                                      ↓
                                               OtpRepository (JPA)
                                               OtpManagerUtils
                                               OtpProvider (HMAC)
```

**Storage model:** The `otp_transaction` table (`otp` schema) does not store the OTP or key in plaintext:
- `id` (PK) = `HMAC-SHA512(key + ":" + otp)` — the composite hash used for validation lookup
- `ref_id` = `HMAC-SHA512(key)` — hashed reference key used for retrieval
- Validation retrieves the latest record by `ref_id` and compares the freshly computed `id` hash

**OTP generation** (`OtpProvider`) uses HMAC-SHA512 seeded with the current timestamp via `PasscodeGenerator`.

**Validation flow** (`OtpValidatorServiceImpl`):
1. Lookup by `ref_id` hash — throws `RequiredKeyNotFoundException` if absent
2. Check OTP expiry against `generated_dtimes`
3. Check if key is frozen (`status_code = KEY_FREEZED`)
4. Increment `validation_retry_count`; freeze key when threshold is reached
5. On success: delete the record and return success

**Beans with `@RefreshScope`**: `OtpValidatorServiceImpl` and `OtpManagerUtils` support hot-reload of config from the Config Server.

## Database

PostgreSQL is required for non-test environments. Schema: `mosip_otp`, table schema: `otp`.

```sql
CREATE DATABASE mosip_otp;
CREATE SCHEMA otp;
-- then run: db_scripts/mosip_otp/ddl/otp-otp_transaction.sql
```

Tests use H2 in-memory via `src/test/resources/application.properties`. The in-memory schema is initialized from `src/main/resources/schema.sql`.

## API Endpoints

| Method | Path | Auth Roles |
|---|---|---|
| `POST` | `/otp/generate` | `INDIVIDUAL`, `REGISTRATION_ADMIN`, `ID_AUTHENTICATION`, etc. |
| `GET` | `/otp/validate?key=...&otp=...` | same roles |

Swagger UI: `http://localhost:8085/v1/otpmanager/swagger-ui.html`

## Dependencies

The service pulls shared MOSIP libraries via `kernel-bom` (BOM-managed versions):
- `kernel-core` — SPI interfaces (`OtpGenerator`, `OtpValidator`), `HMACUtils2`, `BaseRepository`
- `kernel-dataaccess-hibernate` — Hibernate/JPA configuration
- `kernel-logger-logback` — structured logging
- `kernel-auth-adapter` — MOSIP auth adapter (included via `openapi-doc-generate-profile` for doc generation; built into the JAR by default)

## JaCoCo / Sonar

Coverage excludes `config`, `dto`, `entity`, `exception`, `constant` packages. Run Sonar analysis with:

```bash
mvn verify -Psonar -Dsonar.token=<token>
```
# OTP Manager

## Overview
The OTP Manager Service is a core component responsible for the secure generation, validation, and lifecycle management of One-Time Passwords (OTPs). It serves as a critical trust anchor for authentication flows within the MOSIP ecosystem, ensuring that OTPs are generated with high entropy and validated strictly against expiration and usage policies.

Refer [otp-manager](https://docs.mosip.io/1.1.5/modules/kernel/common-services-functionality) for more details.

## Features
- **OTP Generation**: Supports numeric and alphanumeric OTPs with configurable length.
- **OTP Validation**: Validates OTPs against the generating key, ensuring they are used within the allowed time window.
- **Configurable Policies**: Allows configuration of OTP expiry time, freeze time, and max validation attempts.
- **Notification Integration**: Can trigger SMS/Email notifications (via integration with other services).
- **Security**: Implements throttling and account freeze mechanisms to prevent brute-force attacks.

## Services
- **kernel-otpmanager-service**: The core microservice that exposes REST APIs for generating and validating OTPs.

> **Note**: Use Mosip Auth Adaptor for authentication and authorization to access the Rest APIs.

## Local Setup
There are three ways to set up the OTP Manager service locally:
1. [Local Deployment](#local-deployment) (Running the JAR file)
2. [Local Setup using docker image](#local-setup-using-docker-image)
3. [Local Setup by building docker image](#local-setup-by-building-docker-image)

## Pre-requisites
- JDK 21 or higher
- Maven 3.9.x
- PostgreSQL 10 or higher
- Docker (for Docker-based setup)

## Database Setup
The OTP Manager service requires a PostgreSQL database.

**Clone the Repository**
   ```bash
   git clone https://github.com/mosip/otp-manager.git
   ```

**Option 1: Using Deployment Script (Recommended)**
1. Navigate to the `db_scripts/mosip_otp` directory.
2. Run the `deploy.sh` script.

   ```bash
   cd db_scripts/mosip_otp
   ./deploy.sh
   ```

**Option 2: Manual Setup**
1. Create a database
   Log into postgresql and create a database for the OTP Manager service.
   ```sql
   CREATE DATABASE mosip_otp;
   ```
2. Create a schema
   Log into postgresql and create a schema for the OTP Manager service.
   ```sql
   CREATE SCHEMA otp;
   ```
3. Run the SQL scripts provided in the `db_scripts/mosip_otp` directory to create the necessary tables.

## Configurations
The service configuration can be found in `kernel/kernel-otpmanager-service/src/main/resources/application-local.properties`. Key configurations include:

- `mosip.kernel.otp.default-length`: Default length of the OTP (e.g., 6).
- `mosip.kernel.otp.expiry-time`: Time in seconds before an OTP expires.
- `mosip.kernel.otp.key-freeze-time`: Time in seconds to freeze a key after multiple failed attempts.
- `mosip.kernel.otp.validation-attempt-threshold`: Number of allowed validation attempts before blocking.
- `mosip.kernel.otp.min-key-length`: Minimum length of the key.
- `mosip.kernel.otp.max-key-length`: Maximum length of the key.

## Local Deployment
1. **Clone the Repository**
   ```bash
   git clone https://github.com/mosip/otp-manager.git
   ```

2. **Build the Project**
   Navigate to the kernel directory and build the project.
   ```bash
   cd kernel
   mvn clean install -Dgpg.skip=true
   ```


3. **Run the Service**
   Navigate to the service directory and run the application.
   ```bash
   cd kernel-otpmanager-service
   java -jar target/kernel-otpmanager-service-*.jar
   ```

## Local Setup using docker image
1. Pull the docker image from the docker hub:
   ```bash
   docker pull mosipid/kernel-otpmanager-service:latest
   ```
2. Run the Docker container:
   ```bash
   docker run -d --name otpmanager-service \
     -p 8085:8085 \
     -e active_profile_env=local \
     mosip/kernel-otpmanager-service
   ```

## Local Setup by building docker image
1. **Clone the Repository**
   ```bash
   git clone https://github.com/mosip/otp-manager.git
   ```
2. **Build the Docker image**
   Navigate to the service directory and build the image.
   ```bash
   cd kernel/kernel-otpmanager-service
   docker build -t mosip/kernel-otpmanager-service .
   ```
3. **Run the Docker container**
   ```bash
   docker run -d --name otpmanager-service \
     -p 8085:8085 \
     -e active_profile_env=local \
     mosip/kernel-otpmanager-service
   ```

## Deployment
Scripts for deployment are available in the `deploy` directory.

## Upgrade
Upgrade scripts for the database are available in the `db_upgrade_scripts` directory.

## Documentation

### API Documentation
API endpoints, base URL (kernel-otpmanager-service), and mock server details are available via Stoplight and Swagger documentation: [API Documentation](https://mosip.github.io/documentation/1.2.0/kernel-otpmanager-service.html)

### Product Documentation
To know more about OTP Manager in the perspective of functional and use cases you can refer to our main document: [OTP Manager Documentation](https://docs.mosip.io/1.1.5/modules/kernel/common-services-functionality)

## Contribution & Community
We welcome contributions from everyone!

[Check here](https://docs.mosip.io/1.2.0/community/code-contributions) to learn how you can contribute code to this application.

If you have any questions or run into issues while trying out the application, feel free to post them in the [MOSIP Community](https://community.mosip.io/) — we’ll be happy to help you out.

[GitHub Issues](https://github.com/mosip/otp-manager/issues)

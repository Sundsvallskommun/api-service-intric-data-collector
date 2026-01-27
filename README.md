# AI Data Collector

_A component responsible for collecting and synchronizing data from various sources (such as Confluence) to AI
services (Eneo). This module processes content changes via webhooks and scheduled jobs, transforming and uploading data
to AI knowledge bases._

## Getting Started

### Prerequisites

- **Java 21 or higher**
- **Maven**
- **MariaDB**
- **Git**
- **[Dependent Microservices](#dependencies)**

### Installation

1. **Clone the repository:**

   ```bash
   git clone git@github.com:Sundsvallskommun/api-service-ai-data-collector.git
   ```
2. **Configure the application:**

   Before running the application, you need to set up configuration settings.
   See [Configuration](#Configuration)

   **Note:** Ensure all required configurations are set; otherwise, the application may fail to start.

3. **Ensure dependent services are running:**

   If this microservice depends on other services, make sure they are up and accessible.
   See [Dependencies](#dependencies) for more details.

4. **Build and run the application:**

   ```bash
   mvn spring-boot:run
   ```

## Dependencies

This microservice depends on the following services:

- **Confluence**
  - **Purpose:** Source system for content pages that are synchronized to the AI knowledge base.
  - **Additional Notes:** Requires webhook configuration to receive real-time updates for page changes.
- **Eneo**
  - **Purpose:** AI service where content is uploaded as info blobs for knowledge base management.
  - **Additional Notes:** Requires OAuth2 client credentials and API key for authentication.

Ensure that these services are running and properly configured before starting this microservice.

## API Documentation

Access the API documentation via Swagger UI:

- **Swagger UI:** [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

Alternatively, refer to the `openapi.yml` file located in the project's resource directory for the OpenAPI
specification.

## Usage

### API Endpoints

Refer to the [API Documentation](#api-documentation) for detailed information on available endpoints.

### Example Request

```bash
curl -X POST http://localhost:8080/2281/confluence/webhook-event \
  -H "Content-Type: application/json" \
  -d '{"event": "page_updated", "page": {"id": 12345}}'
```

## Configuration

Configuration is crucial for the application to run successfully. Ensure all necessary settings are configured in
`application.yml`.

### Key Configuration Parameters

- **Server Port:**

  ```yaml
  server:
    port: 8080
  ```
- **Database Settings:**

  ```yaml
  spring:
    datasource:
      url: jdbc:mariadb://localhost:3306/your_database
      username: your_db_username
      password: your_db_password
  ```
- **Eneo Configuration:**

  ```yaml
  integration:
    eneo:
      municipalities:
        2281:
          url: https://eneo-api-url
          api-key: your-api-key
      oauth2:
        token-url: https://oauth-token-url
        client-id: your-client-id
        client-secret: your-client-secret
        authorization-grant-type: client_credentials
      connect-timeout-in-seconds: 120
      read-timeout-in-seconds: 120
  ```
- **Confluence Configuration:**

  ```yaml
  integration:
    confluence:
      environments:
        2281:
          base-url: https://confluence-url
          basic-auth:
            username: confluence-user
            password: confluence-password
          scheduling:
            enabled: true
            cron-expression: '0 0 0/2 * * *'
          webhook:
            enabled: true
            security:
              enabled: true
              secret: your-webhook-secret
          mappings:
            - root-id: 12345
              eneo-group-id: uuid-of-eneo-group
          blacklisted-root-ids:
            - 67890
  ```

### Database Initialization

The project is set up with [Flyway](https://github.com/flyway/flyway) for database migrations. Flyway is disabled by
default so you will have to enable it to automatically populate the database schema upon application startup.

```yaml
spring:
  flyway:
    enabled: true
```

- **No additional setup is required** for database initialization, as long as the database connection settings are
  correctly configured.

### Additional Notes

- **Application Profiles:**

  Use Spring profiles (`dev`, `prod`, etc.) to manage different configurations for different environments.

- **Logging Configuration:**

  Adjust logging levels if necessary.

## Contributing

Contributions are welcome! Please
see [CONTRIBUTING.md](https://github.com/Sundsvallskommun/.github/blob/main/.github/CONTRIBUTING.md) for guidelines.

## License

This project is licensed under the [MIT License](LICENSE).

## Code status

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-ai-data-collector&metric=alert_status)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-ai-data-collector)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-ai-data-collector&metric=reliability_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-ai-data-collector)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-ai-data-collector&metric=security_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-ai-data-collector)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-ai-data-collector&metric=sqale_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-ai-data-collector)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-ai-data-collector&metric=vulnerabilities)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-ai-data-collector)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-ai-data-collector&metric=bugs)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-ai-data-collector)

---

© 2024 Sundsvalls kommun

# Infectious Disease Bulletin Backend

This is the backend service for the Infectious Disease Bulletin application, built using Spring Boot. It provides APIs for fetching and processing disease-related data and interacts with a PostgreSQL database. The service is containerized with Docker and deployed using Amazon ECS, and its CI/CD pipeline is managed through GitHub Actions.

## Table of Contents
- [Prerequisites](#prerequisites)
- [Project Setup](#project-setup)
- [Database Configuration](#database-configuration)
- [Running Locally](#running-locally)
- [Building and Running with Docker](#building-and-running-with-docker)
- [Deploying to AWS](#deploying-to-aws)
- [API Endpoints](#api-endpoints)
- [Environment Variables](#environment-variables)
- [Scheduled Data Fetching](#scheduled-data-fetching)
- [Caching](#caching)
- [Monitoring and Logging](#monitoring-and-logging)
- [Technologies Used](#technologies-used)
- [License](#license)

## Prerequisites
- JDK 17
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL Database

## Project Setup
1. Clone the repository:
   ```bash
   git clone https://github.com/giopasaribu/infectious-disease-bulletin.git
   cd infectious-disease-bulletin
   ```
2. Install dependencies:
   ```bash
   mvn clean install
   ```

## Database Configuration
The project uses PostgreSQL as the database. You need to set up the database and configure connection details in your `application.properties` or environment variables.

```properties
# Database settings
spring.datasource.url=jdbc:postgresql://<database-host>:5432/disease
spring.datasource.username=postgres
spring.datasource.password=<your-password>
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA and Hibernate settings
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

## Running Locally
To run the application locally, use:
```bash
mvn spring-boot:run
```
The application will start at `http://localhost:8080`.

## Building and Running with Docker
The project uses multi-stage Docker builds. Here's how to build and run the Docker container:

1. Build the Docker image:
   ```bash
   docker build -t infectious-disease-bulletin .
   ```
2. Run the Docker container:
   ```bash
   docker run -p 8080:8080 --env-file .env infectious-disease-bulletin
   ```

## Deploying to AWS
The project is deployed on AWS using ECS (Elastic Container Service) with ECR (Elastic Container Registry) for container storage.

1. **Push the Docker image to ECR**:
    - Tag your Docker image:
      ```bash
      docker tag infectious-disease-bulletin:latest <your-ecr-repo-url>:latest
      ```
    - Push the Docker image:
      ```bash
      docker push <your-ecr-repo-url>:latest
      ```
2. **Deploy using ECS**:
    - The GitHub Actions workflow automatically deploys the backend to ECS upon pushing to the main branch.

## API Endpoints
### Disease Data Endpoints
- **GET /api/disease/get**: Fetches disease data.
- **POST /api/disease/pull-async**: Pull data from api (on-demand).
- **POST /api/disease/invalidate-cache**: Invalidate disease cache (on-demand).

## Environment Variables
The backend uses the following environment variables:
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `INFECTIOUS_DISEASE_RESOURCE_ID`
- `DATA_FETCH_CRON`

These can be defined in the `application.properties` file or as environment variables in your deployment environment.

## Scheduled Data Fetching
The application fetches data from an external source using a scheduled task defined in the `DiseaseService`. This is controlled by the `data.fetch.cron` environment variable.

Example cron expression:
```
data.fetch.cron=0 0 1 * * ?
```

## Caching
The project uses Spring's caching mechanism to cache disease data. The cache is invalidated when the scheduled data fetch runs.

## Monitoring and Logging
The application uses SLF4J and Logback for logging. Logs can be found in the container logs when deployed on AWS ECS.

## Technologies Used
- **Spring Boot**: Backend framework
- **PostgreSQL**: Database
- **Docker**: Containerization
- **AWS ECS & ECR**: Deployment
- **GitHub Actions**: CI/CD pipeline

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

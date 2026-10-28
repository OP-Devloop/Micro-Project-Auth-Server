# Micro-Project Auth Server

A RESTful authentication service built with Spring Boot. It provides
user authentication, JWT issuance, public-key discovery, and user
management for the webshop microservice project.

The service is designed to issue RSA-signed JSON Web Tokens (JWTs) that
other services can validate using the public key published by the JWKS
endpoint.

## Features

-   Username-and-password authentication
-   RSA-signed JWT generation
-   JSON Web Key Set (JWKS) endpoint for public-key discovery
-   User management through a REST API
-   Endpoint for retrieving the currently authenticated user's account
-   Endpoint for retrieving current JWT session information
-   Password hashing using BCrypt
-   Spring Security and OAuth2 Resource Server support for JWT
    authentication
-   OpenAPI documentation through Springdoc and Swagger UI
-   PostgreSQL persistence with Spring Data JPA
-   Automated tests with Spring Boot Test, Spring Security Test, and H2

## Tech stack

  -----------------------------------------------------------------------
  Technology                          Purpose
  ----------------------------------- -----------------------------------
  Java 17                             Programming language

  Spring Boot 3.4.5                   Application framework

  Spring Web                          REST API

  Spring Security                     Authentication and authorization

  Spring Security OAuth2 Resource     JWT authentication and validation
  Server                              

  Spring Data JPA                     Database access

  PostgreSQL                          Runtime database

  H2                                  Test database

  Springdoc OpenAPI 2.8.6             OpenAPI documentation and Swagger
                                      UI

  MapStruct 1.6.3                     Object mapping

  Lombok                              Boilerplate-code reduction

  Maven                               Build and dependency management

  JUnit / Spring Boot Test            Automated testing
  -----------------------------------------------------------------------

## Prerequisites

Install or have access to:

-   JDK 17
-   PostgreSQL
-   Maven, or the Maven Wrapper included in the repository
-   RSA public and private keys in the format expected by the
    application's JWT configuration

## Getting started

### 1. Clone the repository

``` bash
git clone <YOUR-REPOSITORY-URL>
cd Micro-Project-Auth-Server
```

Replace `<YOUR-REPOSITORY-URL>` with the repository's Git URL.

### 2. Configure environment variables

The application reads its database and JWT settings from environment
variables. The table below lists the variables used by the current
`application.properties` configuration.

  ----------------------------------------------------------------------------
  Variable                   Description             Default
  -------------------------- ----------------------- -------------------------
  `DB_URL`                   PostgreSQL JDBC         Required
                             connection URL          

  `DB_USERNAME`              PostgreSQL username     Required

  `DB_PASSWORD`              PostgreSQL password     Required

  `PORT`                     Server port             `8080`

  `JWT_ISSUER`               JWT issuer claim        `http://localhost:8080`

  `JWT_EXPIRATION_MINUTES`   JWT lifetime in minutes `60`

  `JWT_KEY_ID`               JWT signing-key         `jwt-key-1`
                             identifier              

  `JWT_PUBLIC_KEY`           RSA public key used for Required
                             JWT verification        

  `JWT_PRIVATE_KEY`          RSA private key used to Required
                             sign JWTs               
  ----------------------------------------------------------------------------

Example configuration for Windows PowerShell:

``` powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/authdb"
$env:DB_USERNAME = "your-database-username"
$env:DB_PASSWORD = "your-database-password"
$env:JWT_PUBLIC_KEY = "<your-rsa-public-key>"
$env:JWT_PRIVATE_KEY = "<your-rsa-private-key>"

.\mvnw.cmd spring-boot:run
```

Replace the example credentials and key placeholders with your own
values. Create the database before starting the application. The key
values must use the format expected by the application's JWT
configuration.

On macOS or Linux, start the application with:

``` bash
export DB_URL="jdbc:postgresql://localhost:5432/authdb"
export DB_USERNAME="your-database-username"
export DB_PASSWORD="your-database-password"
export JWT_PUBLIC_KEY="<your-rsa-public-key>"
export JWT_PRIVATE_KEY="<your-rsa-private-key>"

./mvnw spring-boot:run
```

If you do not have the Maven Wrapper, use `mvn spring-boot:run`.

### 3. Open API documentation

When the application is running locally with the default Springdoc
paths, open:

-   Swagger UI: <http://localhost:8080/swagger-ui/index.html>
-   OpenAPI specification: <http://localhost:8080/v3/api-docs>

The default server port is `8080`. Set `PORT` to use a different port.

## API endpoints

The following endpoints are implemented by the service. Whether an
endpoint requires authentication depends on the application's Spring
Security configuration.

### Authentication

  -----------------------------------------------------------------------
  Method                  Endpoint                Description
  ----------------------- ----------------------- -----------------------
  `POST`                  `/auth/login`           Authenticates a user
                                                  and returns a JWT with
                                                  token/session details.

  `GET`                   `/auth/jwks`            Publishes the JSON Web
                                                  Key Set containing
                                                  public key material for
                                                  JWT verification.

  `GET`                   `/auth/me`              Returns session details
                                                  for the authenticated
                                                  JWT, including
                                                  remaining lifetime,
                                                  subject, and roles.
  -----------------------------------------------------------------------

### User management

  -----------------------------------------------------------------------
  Method                  Endpoint                Description
  ----------------------- ----------------------- -----------------------
  `GET`                   `/appusers`             Retrieves the user
                                                  collection.

  `GET`                   `/appusers/{id}`        Retrieves a user by
                                                  database ID.

  `GET`                   `/appusers/me`          Retrieves the user
                                                  record associated with
                                                  the authenticated
                                                  identity.

  `POST`                  `/appusers`             Creates a user account
                                                  and returns
                                                  `201 Created`.
  -----------------------------------------------------------------------

### Log in

**Request**

``` http
POST /auth/login
Content-Type: application/json
```

``` json
{
  "username": "your-username",
  "password": "your-password"
}
```

A successful login returns an authentication result. The response
includes the access token and session details.

**Example response**

``` json
{
  "accessToken": "<JWT>",
  "expiresIn": 3600,
  "subject": "your-username",
  "roles": [
    "USER"
  ]
}
```

The values are examples; the actual token, subject, roles, and
expiration depend on the authentication result and configuration.

### Get session information

**Request**

``` http
GET /auth/me
Authorization: Bearer <JWT>
```

**Example response**

``` json
{
  "expiresIn": 2450,
  "subject": "your-username",
  "roles": [
    "USER"
  ]
}
```

`expiresIn` is the number of seconds remaining until the JWT expires.
`subject` is the identity in the JWT subject claim, and `roles` contains
the roles included in the token. The remaining lifetime is returned as
zero if the token has no expiration timestamp.

### Retrieve the current user

**Request**

``` http
GET /appusers/me
Authorization: Bearer <JWT>
```

This endpoint looks up the user record using the authenticated identity.
The request must be authenticated when the endpoint is protected by the
application's security configuration.

### Create a user

**Request**

``` http
POST /appusers
Content-Type: application/json
```

The request body must match the user-creation DTO accepted by the
application. On success, the endpoint returns the created user with HTTP
`201 Created`.

## JWT authentication

The service uses JSON Web Tokens to represent authenticated identities.
JWTs are signed with an RSA private key; consuming services can use the
corresponding public key to verify signatures.

Relevant token claims include:

-   **Subject (`sub`)** --- identifies the authenticated user.
-   **Issuer (`iss`)** --- identifies the token issuer.
-   **Expiration (`exp`)** --- specifies when the token expires.
-   **Roles** --- carries role information used by the application.

The `/auth/jwks` endpoint publishes public key material so compatible
services can discover the key used to validate signatures. A consuming
service should validate the signature and relevant claims, including
issuer and expiration.

## API gateway integration

This service is designed to work with the webshop API gateway.

A typical login flow is:

1.  The frontend sends login credentials to the gateway.
2.  The gateway forwards the login request to this authentication
    service.
3.  The authentication service validates the credentials and issues a
    JWT.
4.  The gateway can place the JWT in an `HttpOnly` cookie for
    browser-based authentication.
5.  Later requests pass through the gateway, which handles the cookie
    and forwards requests to the appropriate backend service.

The `HttpOnly` cookie behavior belongs to the gateway integration; it is
not the same as the authentication service returning a JWT in its login
response. For deployment, configure cookie attributes such as `Secure`
and `SameSite` appropriately and consider CSRF protections when using
cookies.

## Running tests

Run the test suite with Maven.

**Windows**

``` powershell
.\mvnw.cmd test
```

**macOS or Linux**

``` bash
./mvnw test
```

The project includes Spring Boot Test and Spring Security Test. H2 is
available for tests that need an in-memory database.

## Security considerations

-   Never store passwords as plain text.
-   Keep database credentials and RSA private keys out of source
    control.
-   Use HTTPS in production.
-   Validate JWT signatures, issuer, expiration, and other required
    claims.
-   Restrict CORS to trusted origins.
-   Ensure each endpoint has the intended authentication and
    authorization rules.
-   Configure cookie security attributes and CSRF protections when using
    cookie-based authentication.
-   Do not log passwords, private keys, or complete authentication
    tokens.
-   Use a deliberate signing-key rotation strategy for production.

## Database notes

The current configuration uses PostgreSQL and sets Hibernate schema
management to `update`. SQL logging is enabled. For production
deployments, consider using a database migration tool such as Flyway or
Liquibase to manage schema changes deliberately.

## Project structure

The project separates responsibilities across components such as:

-   **Controllers** --- expose REST endpoints.
-   **Services** --- implement authentication and application logic.
-   **DTOs** --- represent API request and response data.
-   **Security configuration** --- configures authentication,
    authorization, and JWT validation.
-   **Entities and repositories** --- represent and persist user data.
-   **Mappers** --- convert between objects where MapStruct is used.
-   **Tests** --- verify application behavior.

Refer to the source code for the exact package structure and
implementation details.

## License

No license is specified in this README. Add a license file and update
this section if you intend to distribute or permit reuse of the project.

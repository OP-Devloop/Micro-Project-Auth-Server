# Micro-Project Auth Server

The auth service handles authentication and user management for the
webshop backend. It validates user credentials, issues RSA-signed JSON
Web Tokens (JWTs), and publishes public key information so other
services can verify tokens.

## Contents

- [Responsibilities](#responsibilities)
- [System overview](#system-overview)
- [Technologies](#technologies)
- [Database](#database)
- [Port](#port)
- [Environment variables](#environment-variables)
- [Running the application locally](#running-the-application-locally)
- [Endpoints](#endpoints)
- [Login and JWT](#login-and-jwt)
- [Gateway integration](#gateway-integration)
- [Tests](#tests)
- [Other repositories](#other-repositories)

## Responsibilities

The auth service has four main jobs:

1. **Authenticates users** by validating submitted usernames and
   passwords.
2. **Issues JWTs** after successful authentication.
3. **Publishes public key information** through `/auth/jwks` so other
   services can verify token signatures.
4. **Manages user accounts** through REST endpoints.

The service stores user data in PostgreSQL. Browser cookie handling
belongs to the API gateway, not this service.

## System overview

``` text
+----------+       +--------------------+       +--------------------+
| Frontend | ----> |  Webshop Gateway   | ----> |    Auth service    |
|  :5173   |       |       :8090        |       |       :8080        |
+----------+       +--------------------+       +---------+----------+
                                                          |
                                                +---------v----------+
                                                |    PostgreSQL      |
                                                +--------------------+
```

These are the local development ports used by the webshop setup. The
frontend should normally call the gateway rather than call backend
services directly.

## Technologies

| Area                             | Technology                                       |
|----------------------------------|--------------------------------------------------|
| Language                         | Java 17                                          |
| Framework                        | Spring Boot 3.4.5                                |
| REST API                         | Spring Web                                       |
| Authentication and authorization | Spring Security                                  |
| JWT validation                   | Spring Security OAuth2 Resource Server           |
| Persistence                      | Spring Data JPA                                  |
| Runtime database                 | PostgreSQL                                       |
| Test database                    | H2                                               |
| API documentation                | Springdoc OpenAPI 2.8.6                          |
| Object mapping                   | MapStruct 1.6.3                                  |
| Boilerplate reduction            | Lombok                                           |
| Build tool                       | Maven / Maven Wrapper                            |
| Testing                          | JUnit, Spring Boot Test and Spring Security Test |

## Database

The service uses **PostgreSQL** through Spring Data JPA. Its connection
is configured with `DB_URL`, `DB_USERNAME` and `DB_PASSWORD`.

The current configuration uses Hibernate's `update` schema mode and
enables SQL logging. For production, consider using Flyway or Liquibase
to manage schema changes. H2 is included as a test dependency.

## Port

The service listens on port **8080** by default. Set `PORT` to use a
different port.

## Environment variables

  ----------------------------------------------------------------------------
Variable Default Description
  -------------------------- ------------------------- -----------------------
`DB_URL`                   Required PostgreSQL JDBC
connection URL

`DB_USERNAME`              Required PostgreSQL username

`DB_PASSWORD`              Required PostgreSQL password

`PORT`                     `8080`                    Port the service
listens on

`JWT_ISSUER`               `http://localhost:8080`   JWT issuer claim

`JWT_EXPIRATION_MINUTES`   `60`                      JWT lifetime in minutes

`JWT_KEY_ID`               `jwt-key-1`               Signing-key identifier

`JWT_PUBLIC_KEY`           Required RSA public key used for
JWT verification

`JWT_PRIVATE_KEY`          Required RSA private key used to
sign JWTs
  ----------------------------------------------------------------------------

Keep credentials and private signing keys out of source control. Key
values must use the format expected by the application's JWT
configuration. Environment variables override defaults in
`application.properties`.

## Running the application locally

### Prerequisites

- Java 17 (JDK)
- PostgreSQL
- Database and JWT environment variables configured
- Maven, or the Maven Wrapper included in the repository

### Configure environment variables

Windows PowerShell:

``` powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/authdb"
$env:DB_USERNAME = "your-database-username"
$env:DB_PASSWORD = "your-database-password"
$env:JWT_PUBLIC_KEY = "<your-rsa-public-key>"
$env:JWT_PRIVATE_KEY = "<your-rsa-private-key>"
```

macOS and Linux:

``` bash
export DB_URL="jdbc:postgresql://localhost:5432/authdb"
export DB_USERNAME="your-database-username"
export DB_PASSWORD="your-database-password"
export JWT_PUBLIC_KEY="<your-rsa-public-key>"
export JWT_PRIVATE_KEY="<your-rsa-private-key>"
```

Create the database first, and replace the placeholders with your own
values.

### Start the service

Windows:

``` powershell
.\mvnw.cmd spring-boot:run
```

macOS and Linux:

``` bash
./mvnw spring-boot:run
```

Alternatively, run `mvn spring-boot:run` if Maven is installed, or start
the application from IntelliJ IDEA.

The service should be available at `http://localhost:8080`.

### API documentation

With the default Springdoc paths:

- Swagger UI: <http://localhost:8080/swagger-ui/index.html>
- OpenAPI specification: <http://localhost:8080/v3/api-docs>

## Endpoints

Authentication and authorization requirements depend on the Spring
Security configuration.

  -----------------------------------------------------------------------
Method Path Description
  ----------------------- ----------------------- -----------------------
`POST`                  `/auth/login`           Authenticates a user
and returns a JWT with
session details.

`GET`                   `/auth/jwks`            Publishes the public
JSON Web Key Set for
JWT verification.

`GET`                   `/auth/me`              Returns session details
for the authenticated
JWT.

`GET`                   `/appusers`             Retrieves the user
collection.

`GET`                   `/appusers/{id}`        Retrieves a user by
database ID.

`GET`                   `/appusers/me`          Retrieves the user
record for the
authenticated identity.

`POST`                  `/appusers`             Creates a user account
and returns
`201 Created`.
  -----------------------------------------------------------------------

## Login and JWT

Login request:

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

Example response:

``` json
{
  "accessToken": "<JWT>",
  "expiresIn": 3600,
  "subject": "your-username",
  "roles": ["USER"]
}
```

The values are examples; the actual token and session details depend on
the authentication result.

To retrieve session information:

``` http
GET /auth/me
Authorization: Bearer <JWT>
```

Example response:

``` json
{
  "expiresIn": 2450,
  "subject": "your-username",
  "roles": ["USER"]
}
```

`expiresIn` is the number of seconds remaining until the token expires.
`subject` is taken from the JWT subject claim, and `roles` contains the
roles included in the token. If the JWT has no expiration timestamp,
`expiresIn` is `0`.

The `/auth/jwks` endpoint publishes public key material for signature
verification. The private signing key must never be exposed.

## Gateway integration

The auth service is designed to work with the webshop gateway:

1. The frontend sends login credentials to the gateway.
2. The gateway forwards the login request to this service.
3. The auth service validates the credentials and issues a signed JWT.
4. The gateway can store the JWT in an `HttpOnly` cookie named
   `accessToken`.
5. For later requests, the gateway can read the cookie and forward the
   token as `Authorization: Bearer <token>`.

The auth service issues tokens; the gateway manages the browser cookie.
Configure cookie attributes such as `Secure` and `SameSite`
appropriately for the deployment environment.

## Tests

Run the test suite from the project root.

Windows:

``` powershell
.\mvnw.cmd test
```

macOS and Linux:

``` bash
./mvnw test
```

H2 is available as a test-scoped database dependency.

## Other repositories

  --------------------------------------------------------------------------------------------------------------------------------
Part Repository
  ----------------------------------- --------------------------------------------------------------------------------------------
Auth service (this repository)      [Micro-Project-Auth-Server](https://github.com/OP-Devloop/Micro-Project-Auth-Server)

Gateway                             [webshop-gateway](https://github.com/L2025S/webshop-gateway)

Product
service                     [micro-project-product-service](https://github.com/L2025S/micro-project-product-service)

Order
service                       [Micro-ProjectOrder-Service](https://github.com/Yunus-CanYucel/Micro-ProjectOrder-Service)

Mail service                        [Mail-Service](https://github.com/FredrikAlmen93/Mail-Service)

Frontend                            [webshop-frontend](https://github.com/L2025S/webshop-frontend)
--------------------------------------------------------------------------------------------------------------------------------

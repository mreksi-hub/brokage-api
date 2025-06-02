# Brokage Firm Backend API

## Project Overview

This project provides a backend API for a brokage firm, enabling employees to manage stock orders for their customers. The system supports creating, listing, deleting, and matching stock orders, along to managing customer assets. It features a robust security model with distinct roles for customers (who can manage their own data) and administrators (who have full control over all data). All data is persisted in a database.

## Features

* **Order Management:**
    * **Create Order:** Submit new stock orders (BUY/SELL) for a given customer, asset, size, and price. Orders are initially `PENDING`.
    * **List Orders:** Retrieve orders for a specific customer within a defined date range.
    * **Cancel Order:** Cancel a `PENDING` order. Orders with `MATCHED` or `CANCELED` status cannot be canceled.
    * **Match Order (Admin Only):** Administrators can manually match `PENDING` orders. This operation updates the order's status to `MATCHED` and adjusts the `size` and `usableSize` of associated assets (including TRY and the traded asset) for the customer. Orders are not matched against each other in the sense of finding a counter-order in the book; rather, it's a unilateral "match" operation to finalize a pending order.

* **Asset Management:**
    * **List Assets:** Retrieve all assets held by a given customer.

* **Authentication & Authorization:**
    * **Login Endpoint:** Allows users (customers and admins) to authenticate and obtain JWT tokens.
    * **Role-Based Access Control:**
        * **Customers:** Can only access and manipulate their own order and asset data.
        * **Admins:** Have full access to manage all customer data (orders and assets).
    * All API endpoints are secured and require proper authorization (Bearer token).

* **Financial Logic:**
    * **TRY Asset:** `TRY` is treated as an asset and its balance is managed within the asset table. All buy/sell operations are against `TRY`.
    * **Usable Size Check:** When creating a new order, the system verifies if the customer has sufficient `usableSize` of `TRY` (for BUY orders) or the `asset` to be sold (for SELL orders). The `usableSize` is updated accordingly upon order creation.
    * **Usable Size Update on Cancellation:** When an order is canceled, the `usableSize` of the relevant asset is adjusted back.
    * **Asset Updates on Matching:** Upon matching an order, both the `TRY` asset's and the traded asset's `size` and `usableSize` values are updated for the customer involved.

## Technology Stack

* **Language:** Java
* **Framework:** Spring Boot
* **Database:** H2 Database
* **ORM:** Spring Data JPA / Hibernate
* **Security:** Spring Security (JWT-based authentication)
* **API Documentation:** Springdoc-openapi (Swagger UI)
* **Logging:** SLF4J with Logback
* **Build Tool:** Maven
* **Testing:** JUnit 5, Mockito, Spring Boot Test

## Data Model

The application uses the following entities stored in the database:

* **`Asset`**:
    * `customerId` (UUID/String): Unique identifier of the customer owning the asset.
    * `assetName` (String): Name of the stock (e.g., "GOLD", "BTC", "TRY").
    * `size` (BigDecimal): Total quantity of the asset held.
    * `usableSize` (BigDecimal): Quantity of the asset available for new orders (not locked by pending orders).

* **`Order`**:
    * `customerId` (UUID/String): Unique identifier of the customer who placed the order.
    * `assetName` (String): Name of the asset being traded.
    * `orderSide` (Enum `SideType`): `BUY` or `SELL`.
    * `size` (BigDecimal): Total quantity of shares in the order.
    * `price` (BigDecimal): Price per share.
    * `status` (Enum `StatusType`): `PENDING`, `MATCHED`, or `CANCELED`.
    * `createDate` (ZonedDateTime): Timestamp of order creation.

* **`User`**: (Implicitly, for authentication)
    * `id` (UUID/String)
    * `email` (String - used as username)
    * `password` (String - encoded)
    * `roles` (String - e.g., "ROLE_CUSTOMER", "ADMIN")
    * `customerId` (UUID/String - link to the customer data)

### Enums

* **`SideType`**:
    * `BUY`
    * `SELL`
* **`StatusType`**:
    * `PENDING`
    * `MATCHED`
    * `CANCELED`

## Project Structure and Design Principles

The project is structured following common Spring Boot best practices to ensure maintainability, scalability, and testability:

* **Layered Architecture:**
    * **`rest` package:** Contains controllers, DTOs (Request/Response Models), and REST-specific exceptions.
    * **`service` package:** Holds business logic, orchestrating interactions between DAOs and performing validations.
    * **`core.data` package:** Includes DAOs (Data Access Objects) for interacting with the database entities.
    * **`core.data.entity` package:** Defines JPA entities (`@Entity`).
    * **`security` package:** Manages authentication (JWT generation, validation) and authorization.
    * **`utility` package:** Contains enums, constants, and utility methods.
    * **`rest.validation` package:** Houses custom validators for business rules.
* **Database Interaction:** Uses Spring Data JPA repositories for simplified data access.
* **Transaction Management:** `@Transactional` annotations are used to ensure atomicity of business operations.
* **Exception Handling:** Custom exceptions and a global exception handler (if implemented) provide consistent error responses.
* **Validation:** JSR 303 (Jakarta Bean Validation) annotations for request payload validation, complemented by custom business logic validators.
* **Security:** JWTs for stateless authentication, with roles for granular access control.

## How to Build and Run

### Prerequisites

* Java 17 or higher
* Maven 3.6+

### Build the Project

Navigate to the root directory of the project where `pom.xml` is located and run:

```bash
mvn clean install
```

This command will compile the code, run unit tests, and package the application into a JAR file in the `target/` directory.

### Run the Application

You can run the Spring Boot application using the generated JAR file:

```bash
java -jar target/Brokage-api-0.0.1-SNAPSHOT.jar
```

Alternatively, if you have Maven installed, you can run it directly using the Spring Boot Maven plugin:

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080/brokage` by default.

### Accessing Swagger UI (API Documentation)

Once the application is running, you can access the Swagger UI at:

`http://localhost:8080/brokage/swagger-ui/index.html`

This interface provides interactive documentation for all available API endpoints, allowing you to test them directly.

### Accessing H2 Database Console

You can access the H2 in-memory database console at:

`http://localhost:8080/brokage/h2-console`

**JDBC URL:** `jdbc:h2:file:~/brokage` (or whatever is configured in `application.properties`/`application-test.properties`)
**Username:** `root`
**Password:** `rasit`

---

## Pre-defined Users and Assets (from `data.sql`)

The application comes with pre-defined users and their initial asset balances, loaded from `src/main/resources/data.sql` on startup.

### Pre-defined Customers:

| First Name | Last Name | Username | Password | Role     | Customer ID                            |
| :--------- | :-------- | :------- | :------- | :------- | :------------------------------------- |
| rasit      | eksi      | rasit    | rasit    | ADMIN    | `bcf9bf9b-036d-4de3-a0ae-f55aef01e398` |
| alice      | smith     | alice    | alice    | CUSTOMER | `39aeef68-f97b-4c05-8385-cfe4c0f49b5b` |
| bob        | chan      | bob      | bob      | CUSTOMER | `98bba09f-c6b0-4b9c-932c-8ae422c57a7d` |
| eve        | mallory   | eve      | eve      | CUSTOMER | `768cb8d2-c29a-4f2c-afc5-2c790384d1f7` |

*All passwords are the same as their respective usernames.*

### Initial Assets:

**Customer: `39aeef68-f97b-4c05-8385-cfe4c0f49b5b` (alice)**
* `TRY`: 10000 (usable: 10000)
* `Gold`: 400 (usable: 400)
* `MSFT`: 350 (usable: 350)

**Customer: `98bba09f-c6b0-4b9c-932c-8ae422c57a7d` (bob)**
* `TRY`: 10000 (usable: 10000)
* `Gold`: 200 (usable: 200)
* `NVDA`: 500 (usable: 500)
* `AAPL`: 300 (usable: 300)

**Customer: `768cb8d2-c29a-4f2c-afc5-2c790384d1f7` (eve)**
* `TRY`: 10000 (usable: 10000)
* `Gold`: 50 (usable: 50)
* `AAPL`: 20 (usable: 20)
* `BTC`: 100 (usable: 100)
* `MSFT`: 120 (usable: 120)

### How to Use the API with Pre-defined Users:

1.  **Login:** Use the `/v1/auth/login` endpoint (via Swagger UI or a tool like Postman/cURL) with one of the pre-defined usernames and passwords.
2.  **Copy Token:** Copy the `token` (JWT) from the successful login response.
3.  **Authorize in Swagger UI:** Click the "Authorize" button in Swagger UI, select "BearerAuth", and paste your copied JWT token into the `Value` field (e.g., `<your_jwt_token>`).
4.  **Call Endpoints:** You can now call other protected endpoints. For admin users, you can also set the `X-Customer-ID` header if you wish to act on behalf of another customer.

---

## Unit and Integration Tests

The project includes comprehensive unit and integration tests.

* **Unit Tests:** Located in `src/test/java/com/rasit/brokage/...`, these tests focus on individual components (e.g., `OrderServiceTest`, `AuthenticationServiceTest`) by mocking their dependencies.
* **Integration Tests:** Located in `src/test/java/com/rasit/brokage/...IT.java` (e.g., `BrokageApplicationIT`), these tests start a slice or the entire Spring application context and verify the interaction between multiple components, often using an in-memory database and `TestRestTemplate` to simulate HTTP requests.

### Running Tests

To run all tests (unit and integration):

```bash
mvn test
```

To run only unit tests:

```bash
mvn test -DskipITs
```

To run only integration tests (requires `failsafe` plugin configuration, or a naming convention for ITs like `*IT.java` for the `maven-failsafe-plugin`):

```bash
mvn verify
```

---

## API Endpoints (Summary)

All endpoints are prefixed with `brokage/v1`.

| Method | Path                 | Description                                           | Authorization    |
| :----- |:---------------------| :---------------------------------------------------- |:-----------------|
| `POST` | `/auth/login`        | Authenticate user and get JWT token.                  | Public           |
| `POST` | `/auth/login/reload` | Refresh JWT token.                                    | Public           |
| `POST` | `/order`             | Create a new stock order.                             | Customer / Admin |
| `GET`  | `/order/list`        | List orders for a customer by date range.             | Customer / Admin |
| `DELETE`| `/order`             | Cancel a pending order by ID.                         | Customer / Admin |
| `GET`  | `/asset/list`        | List assets for a customer.                           | Customer / Admin |
| `POST` | `/order/match`       | Match a pending order (Admin only).                   | Admin Only       |

**Note:** For `X-Customer-ID` header, only `ADMIN` users can use it to specify a target customer. Regular `ROLE_CUSTOMER` users will have their `customerId` resolved from their JWT token.

---

## Future Enhancements (Ideas)
* **Real-time Matching Engine:** Implement a more sophisticated order book and matching algorithm where BUY and SELL orders are automatically matched against each other based on price-time priority.
* **WebSockets:** Provide real-time updates on order status and trades.
* **Advanced Authentication and Authorization:**
  * **Login Endpoint Security & Rate Limiting:**
    * **Rate Limiting:** Implement rate limiting on the `/auth/login` (login) endpoint to prevent brute-force attacks and reduce load from malicious requests.
    * **Account Lockout:** Implement an account lockout mechanism after a certain number of failed login attempts.
    * **CAPTCHA/reCAPTCHA:** Integrate CAPTCHA or reCAPTCHA challenges for suspicious login attempts.
    * **MFA (Multi-Factor Authentication):** Add support for multi-factor authentication for enhanced security.
  * **OAuth 2.0 Integration:** Migrate from custom JWT implementation to a full OAuth 2.0 flow (e.g., with Spring Security OAuth2 or an external identity provider like Keycloak/Auth0). This provides standardized token management, scopes, and potentially more grant types.
* **Architectural Enhancements:**
  * **Event Sourcing:** Implement an event-sourcing pattern where all changes to the system's state are stored as a sequence of events. This provides a complete audit log, strong consistency, and facilitates rebuilding state.
  * **CQRS (Command Query Responsibility Segregation):** Separate the read (query) model from the write (command) model. This allows for optimized data stores and models for both operations, improving performance and scalability, especially for complex reads.
* **More Organized Relational Database Schema:**
  * **Index Optimization:** Analyze and add database indexes as necessary for improved query performance on frequently accessed columns (e.g., `customerId`, `createDate`, `orderId`).
* **Robust Error Handling:** More granular error codes and messages.
* **Performance Optimization:** Implement caching, message queues for high-throughput scenarios.
* **Monitoring & Alerting:** Integrate with Prometheus/Grafana or similar tools.

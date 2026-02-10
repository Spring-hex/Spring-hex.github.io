---
title: Testing
parent: Guide
nav_order: 5
---

# Testing

Spring-Hex provides commands for generating and running tests. This guide covers creating feature tests (integration tests) and unit tests, as well as executing your test suite.

## Test Types

Spring-Hex supports two categories of tests:

- **Feature Tests** — Integration tests using `@SpringBootTest` and MockMvc for testing HTTP endpoints and full application context
- **Unit Tests** — Isolated tests using Mockito for testing individual components

## Generating Feature Tests

Create a feature test for a controller or component:

```bash
spring-hex make:test OrderController
```

This generates a test class with Spring Boot testing infrastructure.

### Generated Feature Test Template

**File:** `src/test/java/com/example/app/infrastructure/web/order/OrderControllerTest.java`

```java
package com.example.app.infrastructure.web.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testEndpoint() throws Exception {
        // TODO: Implement your feature test
        mockMvc.perform(get("/api/orders"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
```

### Example Feature Test Implementation

```java
package com.example.app.infrastructure.web.order;

import com.example.app.domain.order.model.OrderId;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createOrder_WithValidRequest_ReturnsCreated() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(
            "customer-123",
            List.of(
                new OrderLineRequest("product-1", 2, 29.99),
                new OrderLineRequest("product-2", 1, 49.99)
            ),
            "123 Main St, City, State 12345"
        );

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.orderId", notNullValue()));
    }

    @Test
    void getOrder_WithExistingId_ReturnsOrder() throws Exception {
        String orderId = "order-123";

        mockMvc.perform(get("/api/orders/{id}", orderId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId", is(orderId)))
            .andExpect(jsonPath("$.orderLines", hasSize(greaterThan(0))));
    }

    @Test
    void getOrder_WithNonExistentId_ReturnsNotFound() throws Exception {
        String nonExistentId = "non-existent-id";

        mockMvc.perform(get("/api/orders/{id}", nonExistentId))
            .andExpect(status().isNotFound());
    }

    @Test
    void createOrder_WithInvalidCustomer_ReturnsBadRequest() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(
            null, // Invalid: null customer
            List.of(new OrderLineRequest("product-1", 2, 29.99)),
            "123 Main St"
        );

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
}
```

## Generating Unit Tests

Create a unit test with Mockito:

```bash
spring-hex make:test OrderService --unit
```

This generates a test class with mocking infrastructure.

### Generated Unit Test Template

**File:** `src/test/java/com/example/app/domain/order/service/OrderServiceTest.java`

```java
package com.example.app.domain.order.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    // TODO: Add @Mock dependencies

    @BeforeEach
    void setUp() {
        // TODO: Set up test data
    }

    @Test
    void testMethod() {
        // TODO: Implement your unit test
        fail("Not yet implemented");
    }
}
```

### Example Unit Test Implementation

```java
package com.example.app.domain.order.command;

import com.example.app.domain.customer.model.Customer;
import com.example.app.domain.customer.model.CustomerId;
import com.example.app.domain.customer.port.out.CustomerRepository;
import com.example.app.domain.order.exception.CustomerNotFoundException;
import com.example.app.domain.order.model.Order;
import com.example.app.domain.order.model.OrderId;
import com.example.app.domain.order.port.out.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateOrderCommandHandlerTest {

    @InjectMocks
    private CreateOrderCommandHandler handler;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    private CreateOrderCommand validCommand;
    private Customer existingCustomer;

    @BeforeEach
    void setUp() {
        existingCustomer = new Customer(
            new CustomerId("customer-123"),
            "john@example.com",
            "John Doe"
        );

        validCommand = new CreateOrderCommand(
            "customer-123",
            List.of(
                new OrderLineDto("product-1", 2, 29.99),
                new OrderLineDto("product-2", 1, 49.99)
            ),
            "123 Main St, City, State 12345"
        );
    }

    @Test
    void handle_WithValidCommand_CreatesOrder() {
        // Arrange
        when(customerRepository.findById("customer-123"))
            .thenReturn(Optional.of(existingCustomer));

        Order savedOrder = mock(Order.class);
        when(savedOrder.getId()).thenReturn(new OrderId("order-123"));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        OrderId result = handler.handle(validCommand);

        // Assert
        assertNotNull(result);
        assertEquals("order-123", result.getValue());
        verify(customerRepository).findById("customer-123");
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void handle_WithNonExistentCustomer_ThrowsException() {
        // Arrange
        when(customerRepository.findById("customer-123"))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            CustomerNotFoundException.class,
            () -> handler.handle(validCommand)
        );

        verify(customerRepository).findById("customer-123");
        verify(orderRepository, never()).save(any());
    }

    @Test
    void handle_WithEmptyOrderLines_ThrowsException() {
        // Arrange
        CreateOrderCommand invalidCommand = new CreateOrderCommand(
            "customer-123",
            List.of(), // Empty order lines
            "123 Main St"
        );

        when(customerRepository.findById("customer-123"))
            .thenReturn(Optional.of(existingCustomer));

        // Act & Assert
        assertThrows(
            IllegalArgumentException.class,
            () -> handler.handle(invalidCommand)
        );
    }
}
```

## Running Tests

Spring-Hex provides commands to run your test suite using the detected build tool (Maven or Gradle).

### Run All Tests

Execute the entire test suite:

```bash
spring-hex run:test
```

This runs:
- **Maven:** `mvn test` or `./mvnw test`
- **Gradle:** `gradle test` or `./gradlew test`

### Run Unit Tests Only

Execute only unit tests:

```bash
spring-hex run:test --unit
```

This filters tests based on naming conventions or package patterns:
- Tests in packages containing `unit`
- Test classes ending with `UnitTest`

Maven example:

```bash
mvn test -Dtest=**/*UnitTest
```

Gradle example:

```bash
gradle test --tests '*UnitTest'
```

### Run Feature Tests Only

Execute only feature/integration tests:

```bash
spring-hex run:test --feature
```

This filters for:
- Tests in packages containing `feature` or `integration`
- Test classes ending with `FeatureTest` or `IntegrationTest`
- Tests annotated with `@SpringBootTest`

Maven example:

```bash
mvn test -Dtest=**/*FeatureTest,**/*IntegrationTest
```

Gradle example:

```bash
gradle test --tests '*FeatureTest' --tests '*IntegrationTest'
```

### Build Tool Detection

Spring-Hex auto-detects your build tool and uses the appropriate wrapper if available:

1. Checks for `mvnw` (Maven Wrapper) or `gradlew` (Gradle Wrapper)
2. Falls back to `mvn` or `gradle` if wrapper not found
3. Uses the wrapper for reproducible builds across environments

## Test Organization

Spring-Hex follows standard Java test conventions:

### Directory Structure

```
src/
├── main/java/com/example/app/
│   └── domain/order/
│       ├── model/Order.java
│       ├── command/CreateOrderCommandHandler.java
│       └── service/OrderService.java
└── test/java/com/example/app/
    ├── domain/order/
    │   ├── command/CreateOrderCommandHandlerTest.java  (unit)
    │   └── service/OrderServiceTest.java                (unit)
    └── infrastructure/web/order/
        └── OrderControllerTest.java                     (feature)
```

### Naming Conventions

- **Feature Tests:** `{ClassName}Test.java` or `{ClassName}FeatureTest.java`
- **Unit Tests:** `{ClassName}Test.java` or `{ClassName}UnitTest.java`
- Test methods: `methodName_scenario_expectedResult()`

## Test Dependencies

Ensure your `pom.xml` or `build.gradle` includes necessary test dependencies:

### Maven (pom.xml)

```xml
<dependencies>
    <!-- Spring Boot Test -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- JUnit 5 -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- Mockito -->
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- MockMvc -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
</dependencies>
```

### Gradle (build.gradle)

```groovy
dependencies {
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.junit.jupiter:junit-jupiter'
    testImplementation 'org.mockito:mockito-core'
}
```

## Best Practices

1. **Follow AAA Pattern** — Arrange, Act, Assert structure in tests
2. **One assertion per test** — Or closely related assertions
3. **Test behavior, not implementation** — Focus on outcomes, not internal details
4. **Use descriptive test names** — `createOrder_WithValidRequest_ReturnsCreated`
5. **Isolate unit tests** — Mock all external dependencies
6. **Use test data builders** — For complex object creation
7. **Clean up after tests** — Especially in feature tests with databases
8. **Avoid test interdependence** — Each test should run independently
9. **Use appropriate test type** — Unit tests for logic, feature tests for integration
10. **Maintain test coverage** — Aim for meaningful coverage of critical paths

## Continuous Integration

Spring-Hex test commands work seamlessly in CI/CD pipelines:

```yaml
# GitHub Actions example
name: Tests
on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Run tests
        run: spring-hex run:test
```

## Related Commands

- `spring-hex make:test <ClassName>` — Generate feature test
- `spring-hex make:test <ClassName> --unit` — Generate unit test
- `spring-hex run:test` — Run all tests
- `spring-hex run:test --unit` — Run unit tests only
- `spring-hex run:test --feature` — Run feature tests only

---
title: Greenfield Project
parent: Tutorials
nav_order: 1
---

# Greenfield Project Tutorial

This tutorial demonstrates building a complete e-commerce Order Management system from scratch using hexagonal architecture with Spring-Hex CLI.

## Prerequisites

- JDK 17 or higher
- Spring-Hex CLI installed
- Your favorite IDE (IntelliJ IDEA, VS Code, etc.)

## Step 1: Create a Spring Boot Project

Navigate to [start.spring.io](https://start.spring.io) and configure your project:

- **Project**: Maven
- **Language**: Java
- **Spring Boot**: 3.2.0 or higher
- **Group**: com.example
- **Artifact**: order-management
- **Package name**: com.example.ordermanagement
- **Dependencies**: Spring Web, Spring Data JPA, Lombok, H2 Database

Download and extract the project, then open it in your IDE.

## Step 2: Initialize Spring-Hex

Navigate to your project root and run the initialization command:

```bash
cd order-management
spring-hex init
```

**Output:**
```
Spring-Hex initialized successfully!
Configuration file created: .hex/config.yml
Base package: com.example.ordermanagement

You can now start generating hexagonal architecture components.
Try: spring-hex make:module Order
```

This creates the `.hex/config.yml` configuration file with default settings for your project structure.

## Step 3: Generate the Mediator Pattern Infrastructure

Before creating domain modules, generate the mediator pattern infrastructure for handling commands and queries:

```bash
spring-hex make:mediator
```

**Output:**
```
Generated: src/main/java/com/example/ordermanagement/shared/mediator/CommandBus.java
Generated: src/main/java/com/example/ordermanagement/shared/mediator/QueryBus.java
Generated: src/main/java/com/example/ordermanagement/shared/mediator/CommandHandler.java
Generated: src/main/java/com/example/ordermanagement/shared/mediator/QueryHandler.java
Generated: src/main/java/com/example/ordermanagement/shared/mediator/SimpleCommandBus.java
Generated: src/main/java/com/example/ordermanagement/shared/mediator/SimpleQueryBus.java
Generated: src/main/java/com/example/ordermanagement/shared/domain/AggregateRoot.java
Generated: src/main/java/com/example/ordermanagement/config/DomainConfig.java
Generated: src/main/java/com/example/ordermanagement/config/MediatorConfig.java

Mediator pattern infrastructure created successfully!
```

### What Was Generated?

The mediator pattern provides a decoupled way to handle commands and queries:

- **CommandBus**: Dispatches commands to their handlers
- **QueryBus**: Dispatches queries to their handlers
- **CommandHandler<T>**: Interface for command handlers
- **QueryHandler<T, R>**: Interface for query handlers
- **SimpleCommandBus**: Default implementation using Spring's ApplicationContext
- **SimpleQueryBus**: Default implementation using Spring's ApplicationContext
- **AggregateRoot**: Base class for domain aggregates
- **Configuration classes**: Spring beans for dependency injection

## Step 4: Generate the Order Module

Now generate a complete bounded context for the Order aggregate:

```bash
spring-hex make:module Order
```

**Output:**
```
Generated: src/main/java/com/example/ordermanagement/order/model/Order.java
Generated: src/main/java/com/example/ordermanagement/order/model/OrderId.java
Generated: src/main/java/com/example/ordermanagement/order/command/CreateOrderCommand.java
Generated: src/main/java/com/example/ordermanagement/order/command/CreateOrderCommandHandler.java
Generated: src/main/java/com/example/ordermanagement/order/query/GetOrderQuery.java
Generated: src/main/java/com/example/ordermanagement/order/query/GetOrderQueryHandler.java
Generated: src/main/java/com/example/ordermanagement/order/port/OrderRepository.java
Generated: src/main/java/com/example/ordermanagement/order/adapter/persistence/JpaOrderRepository.java
Generated: src/main/java/com/example/ordermanagement/order/adapter/persistence/OrderEntity.java
Generated: src/main/java/com/example/ordermanagement/order/adapter/persistence/OrderMapper.java
Generated: src/main/java/com/example/ordermanagement/order/adapter/web/OrderController.java
Generated: src/main/java/com/example/ordermanagement/order/event/OrderCreatedEvent.java
Generated: src/main/java/com/example/ordermanagement/order/event/OrderCreatedEventListener.java

Order module created successfully!
```

### Generated Code Examples

#### Order Aggregate

```java
// src/main/java/com/example/ordermanagement/order/model/Order.java
package com.example.ordermanagement.order.model;

import com.example.ordermanagement.shared.domain.AggregateRoot;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder
public class Order extends AggregateRoot {
    private OrderId id;
    private String customerName;
    private String status;
    private LocalDateTime createdAt;

    @Builder.Default
    private List<String> items = new ArrayList<>();

    public void markAsShipped() {
        this.status = "SHIPPED";
    }

    public void addItem(String item) {
        this.items.add(item);
    }
}
```

#### CreateOrderCommandHandler

```java
// src/main/java/com/example/ordermanagement/order/command/CreateOrderCommandHandler.java
package com.example.ordermanagement.order.command;

import com.example.ordermanagement.order.model.Order;
import com.example.ordermanagement.order.model.OrderId;
import com.example.ordermanagement.order.port.OrderRepository;
import com.example.ordermanagement.shared.mediator.CommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateOrderCommandHandler implements CommandHandler<CreateOrderCommand> {

    private final OrderRepository orderRepository;

    @Override
    public void handle(CreateOrderCommand command) {
        Order order = Order.builder()
                .id(new OrderId(UUID.randomUUID().toString()))
                .customerName(command.customerName())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        orderRepository.save(order);
    }
}
```

#### OrderController

```java
// src/main/java/com/example/ordermanagement/order/adapter/web/OrderController.java
package com.example.ordermanagement.order.adapter.web;

import com.example.ordermanagement.order.command.CreateOrderCommand;
import com.example.ordermanagement.order.query.GetOrderQuery;
import com.example.ordermanagement.shared.mediator.CommandBus;
import com.example.ordermanagement.shared.mediator.QueryBus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    @PostMapping
    public ResponseEntity<Void> createOrder(@RequestBody CreateOrderCommand command) {
        commandBus.dispatch(command);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getOrder(@PathVariable String id) {
        Object order = queryBus.dispatch(new GetOrderQuery(id));
        return ResponseEntity.ok(order);
    }
}
```

## Step 5: Add an UpdateOrder Command

Add additional commands to extend functionality:

```bash
spring-hex make:command UpdateOrder -a order
```

**Output:**
```
Generated: src/main/java/com/example/ordermanagement/order/command/UpdateOrderCommand.java
Generated: src/main/java/com/example/ordermanagement/order/command/UpdateOrderCommandHandler.java

UpdateOrder command created successfully!
```

The generated command uses a Java record:

```java
// src/main/java/com/example/ordermanagement/order/command/UpdateOrderCommand.java
package com.example.ordermanagement.order.command;

public record UpdateOrderCommand(
    String id,
    String customerName,
    String status
) {}
```

## Step 6: Add a ListOrders Query

Generate a query to list all orders:

```bash
spring-hex make:query ListOrders -a order
```

**Output:**
```
Generated: src/main/java/com/example/ordermanagement/order/query/ListOrdersQuery.java
Generated: src/main/java/com/example/ordermanagement/order/query/ListOrdersQueryHandler.java

ListOrders query created successfully!
```

Query handler example:

```java
// src/main/java/com/example/ordermanagement/order/query/ListOrdersQueryHandler.java
package com.example.ordermanagement.order.query;

import com.example.ordermanagement.order.port.OrderRepository;
import com.example.ordermanagement.shared.mediator.QueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ListOrdersQueryHandler implements QueryHandler<ListOrdersQuery, List<Object>> {

    private final OrderRepository orderRepository;

    @Override
    public List<Object> handle(ListOrdersQuery query) {
        return orderRepository.findAll().stream()
                .map(order -> order)
                .toList();
    }
}
```

## Step 7: Add an OrderShipped Event

Generate domain events for event-driven architecture:

```bash
spring-hex make:event OrderShipped -a order
```

**Output:**
```
Generated: src/main/java/com/example/ordermanagement/order/event/OrderShippedEvent.java
Generated: src/main/java/com/example/ordermanagement/order/event/OrderShippedEventListener.java

OrderShipped event created successfully!
```

Event example:

```java
// src/main/java/com/example/ordermanagement/order/event/OrderShippedEvent.java
package com.example.ordermanagement.order.event;

import java.time.LocalDateTime;

public record OrderShippedEvent(
    String orderId,
    String trackingNumber,
    LocalDateTime shippedAt
) {}
```

Event listener:

```java
// src/main/java/com/example/ordermanagement/order/event/OrderShippedEventListener.java
package com.example.ordermanagement.order.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderShippedEventListener {

    @EventListener
    public void handleOrderShipped(OrderShippedEvent event) {
        log.info("Order shipped: {} with tracking number: {}",
                event.orderId(), event.trackingNumber());

        // Send notification email
        // Update inventory
        // Create shipping record
    }
}
```

## Step 8: Create a Database Migration

Generate a database migration file:

```bash
spring-hex make:migration create_orders_table
```

**Output:**
```
Generated: src/main/resources/db/migration/V1__create_orders_table.sql

Migration file created successfully!
```

## Step 9: Final Project Structure

After generating all components, your project structure looks like this:

```
order-management/
├── src/main/java/com/example/ordermanagement/
│   ├── config/
│   │   ├── DomainConfig.java
│   │   └── MediatorConfig.java
│   ├── shared/
│   │   ├── domain/
│   │   │   └── AggregateRoot.java
│   │   └── mediator/
│   │       ├── CommandBus.java
│   │       ├── CommandHandler.java
│   │       ├── QueryBus.java
│   │       ├── QueryHandler.java
│   │       ├── SimpleCommandBus.java
│   │       └── SimpleQueryBus.java
│   └── order/
│       ├── model/
│       │   ├── Order.java
│       │   └── OrderId.java
│       ├── command/
│       │   ├── CreateOrderCommand.java
│       │   ├── CreateOrderCommandHandler.java
│       │   ├── UpdateOrderCommand.java
│       │   └── UpdateOrderCommandHandler.java
│       ├── query/
│       │   ├── GetOrderQuery.java
│       │   ├── GetOrderQueryHandler.java
│       │   ├── ListOrdersQuery.java
│       │   └── ListOrdersQueryHandler.java
│       ├── port/
│       │   └── OrderRepository.java
│       ├── adapter/
│       │   ├── persistence/
│       │   │   ├── JpaOrderRepository.java
│       │   │   ├── OrderEntity.java
│       │   │   └── OrderMapper.java
│       │   └── web/
│       │       └── OrderController.java
│       └── event/
│           ├── OrderCreatedEvent.java
│           ├── OrderCreatedEventListener.java
│           ├── OrderShippedEvent.java
│           └── OrderShippedEventListener.java
├── src/main/resources/
│   └── db/migration/
│       └── V1__create_orders_table.sql
└── .hex/
    └── config.yml
```

## Next Steps

1. Implement business logic in your aggregate and handlers
2. Add validation to commands using Bean Validation annotations
3. Configure H2 database connection in `application.yml`
4. Add more aggregates using `spring-hex make:module Product`, `make:module Customer`, etc.
5. Write unit tests for handlers and integration tests for controllers
6. Add API documentation using SpringDoc OpenAPI

## Key Takeaways

- Spring-Hex automates hexagonal architecture scaffolding
- The mediator pattern decouples command/query execution from controllers
- Each module is a complete bounded context with clear separation of concerns
- Domain logic lives in aggregates, infrastructure concerns in adapters
- Events enable loose coupling between modules

---
title: Quick Start
parent: Getting Started
nav_order: 3
---

# Quick Start

This tutorial walks you through creating a new Spring Boot project and generating a complete hexagonal architecture module in under 5 minutes.

## Step 1: Create a Spring Boot Project

Visit [start.spring.io](https://start.spring.io) and create a new project with:

- **Project**: Maven
- **Language**: Java
- **Spring Boot**: 3.2.x or higher
- **Dependencies**: Spring Web, Spring Data JPA, Lombok, H2 Database

Or use the command line:

```bash
curl https://start.spring.io/starter.zip \
  -d dependencies=web,data-jpa,lombok,h2 \
  -d type=maven-project \
  -d language=java \
  -d bootVersion=3.2.0 \
  -d baseDir=my-app \
  -d groupId=com.example \
  -d artifactId=my-app \
  -o my-app.zip

unzip my-app.zip
cd my-app
```

## Step 2: Initialize Spring-Hex

Initialize the project configuration:

```bash
spring-hex init
```

Output:

```
Scanning project structure...
Auto-detected base package: com.example.myapp
Created: .hex/config.yml

Configuration:
  base-package: com.example.myapp

Next steps:
  spring-hex make:module <AggregateName>   Generate a full bounded context
  spring-hex make:crud <EntityName>        Generate a CRUD resource
  spring-hex --help                        See all available commands
```

## Step 3: Generate Mediator Infrastructure

Create the CQRS mediator infrastructure (CommandBus, QueryBus, handlers):

```bash
spring-hex make:mediator
```

This generates 9 files:

- `AggregateRoot.java` - Base class for domain entities
- `CommandHandler.java` - Interface for command handlers
- `QueryHandler.java` - Interface for query handlers
- `CommandBus.java` - Command dispatcher interface
- `QueryBus.java` - Query dispatcher interface
- `SimpleCommandBus.java` - Default CommandBus implementation
- `SimpleQueryBus.java` - Default QueryBus implementation
- `MediatorConfig.java` - Spring configuration
- `DomainConfig.java` - Domain layer configuration

## Step 4: Generate Your First Module

Generate a complete bounded context for an `Order` aggregate:

```bash
spring-hex make:module Order
```

This creates:

- Domain models (Order, OrderId)
- Commands and handlers (CreateOrderCommand, CreateOrderCommandHandler)
- Queries and handlers (GetOrderByIdQuery, GetOrderByIdQueryHandler)
- Domain events (OrderCreatedEvent)
- Ports (in/out interfaces, OrderRepository)
- Infrastructure adapters (JPA entities, repositories, mappers)
- REST controller (OrderController)
- Event listeners (OrderCreatedEventListener)

## Step 5: Verify Generated Structure

Your project now has a complete hexagonal architecture:

```
src/main/java/com/example/myapp/
├── domain/
│   ├── AggregateRoot.java
│   ├── cqrs/
│   │   ├── CommandHandler.java
│   │   └── QueryHandler.java
│   └── order/
│       ├── command/
│       │   ├── CreateOrderCommand.java
│       │   └── CreateOrderCommandHandler.java
│       ├── event/
│       │   └── OrderCreatedEvent.java
│       ├── model/
│       │   ├── Order.java
│       │   └── OrderId.java
│       ├── port/
│       │   ├── in/
│       │   │   ├── CreateOrderUseCase.java
│       │   │   └── GetOrderUseCase.java
│       │   └── out/
│       │       └── OrderRepository.java
│       └── query/
│           ├── GetOrderByIdQuery.java
│           └── GetOrderByIdQueryHandler.java
└── infrastructure/
    ├── config/
    │   ├── DomainConfig.java
    │   └── MediatorConfig.java
    ├── event/
    │   └── order/
    │       └── OrderCreatedEventListener.java
    ├── mediator/
    │   ├── CommandBus.java
    │   ├── QueryBus.java
    │   ├── SimpleCommandBus.java
    │   └── SimpleQueryBus.java
    ├── persistence/
    │   └── order/
    │       ├── OrderJpaEntity.java
    │       ├── OrderJpaRepository.java
    │       ├── OrderMapper.java
    │       └── OrderRepositoryAdapter.java
    └── web/
        └── order/
            └── OrderController.java
```

## Step 6: Build and Run

Compile and run your application:

```bash
./mvnw clean install
./mvnw spring-boot:run
```

The REST API is now available at `http://localhost:8080/api/orders`.

## Test the API

Create an order:

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-001",
    "items": ["Item 1", "Item 2"],
    "totalAmount": 99.99
  }'
```

Get an order by ID:

```bash
curl http://localhost:8080/api/orders/{orderId}
```

## What's Next?

### Learn More Commands

- [Command Reference]({% link reference/commands.md %}) - Full list of available commands
- [CRUD Module Tutorial]({% link tutorials/crud-module.md %}) - Generate simple CRUD resources

### Customize Your Architecture

- [Configuration Guide]({% link guide/configuration.md %}) - Customize path patterns and templates
- [Path Defaults]({% link reference/path-defaults.md %}) - Default and custom package structures

### Best Practices

- [Hexagonal Architecture]({% link guide/architecture.md %}) - Understanding the pattern
- [CQRS Pattern]({% link guide/cqrs-pattern.md %}) - Command Query Responsibility Segregation
- [Database Migrations]({% link guide/database-migrations.md %}) - Version-controlled schema changes

## Summary

In this quick start, you:

1. Created a Spring Boot project
2. Initialized Spring-Hex CLI configuration
3. Generated CQRS mediator infrastructure
4. Created a complete Order bounded context
5. Tested the generated REST API

You now have a production-ready hexagonal architecture foundation ready for business logic implementation.

---
title: Architecture
parent: Guide
nav_order: 1
---

# Hexagonal Architecture

Spring-Hex generates project scaffolding based on hexagonal architecture principles, also known as Ports and Adapters architecture. This guide explains the core concepts and how Spring-Hex implements them.

## What is Hexagonal Architecture?

Hexagonal architecture is a software design pattern that emphasizes separation of concerns by organizing code into distinct layers. The fundamental principle is:

**The domain core should not depend on infrastructure details.**

This means your business logic remains independent of frameworks, databases, web technologies, and external services. Infrastructure depends on the domain, never the reverse.

### The Three Layers

```
┌─────────────────────────────────────────────┐
│                Infrastructure                │
│  ┌─────────┐  ┌──────────┐  ┌───────────┐  │
│  │Controller│  │JPA Adapter│  │Event Lstnr│  │
│  └────┬────┘  └─────┬────┘  └─────┬─────┘  │
│       │             │              │         │
│  ┌────▼────┐  ┌─────▼────┐  ┌─────▼─────┐  │
│  │Input Port│  │Output Port│  │  Events   │  │
│  └────┬────┘  └─────┬────┘  └─────┬─────┘  │
│       │             │              │         │
│  ┌────▼─────────────▼──────────────▼─────┐  │
│  │              Domain Core               │  │
│  │  Aggregates, Models, Value Objects     │  │
│  │  Commands, Queries, Domain Events      │  │
│  └───────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
```

1. **Domain Core** — Contains business logic, entities, value objects, and domain events. Has no dependencies on infrastructure.

2. **Ports** — Interfaces that define contracts for communication:
   - **Input Ports** (driving/primary) — Define use cases the application offers (called by infrastructure)
   - **Output Ports** (driven/secondary) — Define dependencies the domain needs (implemented by infrastructure)

3. **Infrastructure** — Contains all framework-specific and external system code:
   - **Adapters** — Implement ports to connect the domain to the outside world
   - **Controllers** — HTTP/REST endpoints that call input ports
   - **Persistence** — JPA repositories implementing output ports
   - **Event Listeners** — React to domain events

## Spring-Hex Package Structure

Spring-Hex maps hexagonal architecture to a specific package structure. For a base package of `com.example.app` and an aggregate called `order`:

### Domain Layer

```
com.example.app.domain.order
├── model/           # Entities and value objects
│   ├── Order.java
│   └── OrderId.java
├── command/         # CQRS commands
│   ├── CreateOrderCommand.java
│   └── CreateOrderCommandHandler.java
├── query/           # CQRS queries
│   ├── GetOrderByIdQuery.java
│   └── GetOrderByIdQueryHandler.java
├── event/           # Domain events
│   └── OrderCreatedEvent.java
├── port/
│   ├── in/          # Input ports (use cases)
│   │   └── CreateOrderUseCase.java
│   └── out/         # Output ports (dependencies)
│       └── OrderRepository.java
└── service/         # Domain services
    └── OrderService.java
```

### Infrastructure Layer

```
com.example.app.infrastructure
├── web.order/              # REST controllers
│   └── OrderController.java
├── persistence.order/       # JPA implementations
│   ├── OrderJpaRepository.java
│   └── OrderRepositoryAdapter.java
├── event.order/            # Event listeners
│   └── OrderEventListener.java
└── config/                 # Spring configuration
    └── DomainConfig.java
```

### CQRS Infrastructure

```
com.example.app.domain.cqrs/     # CQRS interfaces
com.example.app.infrastructure.mediator/  # Command/Query buses
```

## Domain-Driven Design Concepts

Spring-Hex incorporates DDD tactical patterns:

### Aggregates

An aggregate is a cluster of domain objects that can be treated as a single unit. Spring-Hex provides an `AggregateRoot` base class:

```java
@Entity
public class Order extends AggregateRoot {
    @EmbeddedId
    private OrderId id;

    private OrderStatus status;
    private List<OrderLine> lines;

    public void confirm() {
        this.status = OrderStatus.CONFIRMED;
        registerEvent(new OrderConfirmedEvent(this.id));
    }
}
```

The `AggregateRoot` class provides domain event registration capabilities.

### Value Objects

Immutable objects defined by their attributes rather than identity. Spring-Hex generates these as Java records:

```java
public record OrderId(UUID value) {
    public OrderId {
        if (value == null) {
            throw new IllegalArgumentException("OrderId cannot be null");
        }
    }
}
```

### Domain Events

Events that represent something meaningful that happened in the domain:

```java
public record OrderCreatedEvent(
    OrderId orderId,
    CustomerId customerId,
    Instant occurredAt
) {}
```

Domain events are registered on aggregates and published by the infrastructure layer after persistence.

## CQRS Pattern

Spring-Hex implements Command Query Responsibility Segregation:

### Commands

Represent intentions to change state:

```java
public record CreateOrderCommand(
    CustomerId customerId,
    List<OrderLineDto> lines
) {}
```

Commands are handled by command handlers that interact with the domain:

```java
@Component
public class CreateOrderCommandHandler
    implements CommandHandler<CreateOrderCommand, OrderId> {

    private final OrderRepository orderRepository;

    @Override
    public OrderId handle(CreateOrderCommand command) {
        Order order = Order.create(
            command.customerId(),
            command.lines()
        );
        return orderRepository.save(order).getId();
    }
}
```

### Queries

Represent requests for data without side effects:

```java
public record GetOrderByIdQuery(OrderId orderId) {}
```

Query handlers retrieve data, often bypassing the domain model for efficiency:

```java
@Component
public class GetOrderByIdQueryHandler
    implements QueryHandler<GetOrderByIdQuery, OrderResponse> {

    private final OrderQueryRepository queryRepository;

    @Override
    public OrderResponse handle(GetOrderByIdQuery query) {
        return queryRepository.findById(query.orderId())
            .orElseThrow(() -> new OrderNotFoundException(query.orderId()));
    }
}
```

## Benefits of This Architecture

1. **Testability** — Domain logic can be tested without Spring, databases, or HTTP
2. **Technology Independence** — Swap persistence, messaging, or web frameworks without changing domain code
3. **Maintainability** — Clear boundaries make code easier to understand and modify
4. **Parallel Development** — Teams can work on infrastructure and domain independently
5. **Domain Focus** — Business logic is explicit and not obscured by framework code

## Generated Code Organization

When you run `spring-hex make:aggregate Order`, Spring-Hex generates:

- **Model package** — `Order` entity extending `AggregateRoot`, `OrderId` value object
- **Port packages** — Input and output port interfaces in `port.in` and `port.out`
- **Infrastructure** — Controller, JPA repository adapter, and configuration

All generated code follows hexagonal architecture principles, with clear dependency direction from infrastructure toward the domain.

For detailed information on customizing where code is generated, see the [Configuration]({% link guide/configuration.md %}) guide.

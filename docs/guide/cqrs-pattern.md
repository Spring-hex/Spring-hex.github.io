---
title: CQRS Pattern
parent: Guide
nav_order: 3
---

# CQRS Pattern

Spring-Hex implements the Command Query Responsibility Segregation (CQRS) pattern using a mediator-based approach. This guide explains how to generate and use commands, queries, and the mediator infrastructure.

## What is CQRS?

CQRS separates read and write operations into distinct models:

- **Commands** — Represent intentions to change state (writes)
- **Queries** — Represent requests for data (reads)

This separation allows you to optimize each side independently and maintain clear boundaries between state-changing and read-only operations.

## Generating the Mediator Infrastructure

Before creating commands and queries, generate the mediator infrastructure:

```bash
spring-hex make:mediator
```

This creates four key components in your project.

### Command Bus

Interface for dispatching commands:

```java
package com.example.app.domain.cqrs;

public interface CommandBus {
    <C, R> R execute(C command);
}
```

Implementation with auto-discovery:

```java
package com.example.app.infrastructure.mediator;

@Component
public class SimpleCommandBus implements CommandBus {
    private final ApplicationContext applicationContext;
    private final Map<Class<?>, CommandHandler<?, ?>> handlers = new ConcurrentHashMap<>();

    public SimpleCommandBus(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <C, R> R execute(C command) {
        CommandHandler<C, R> handler = (CommandHandler<C, R>) getHandler(command.getClass());
        return handler.handle(command);
    }

    private CommandHandler<?, ?> getHandler(Class<?> commandClass) {
        return handlers.computeIfAbsent(commandClass, this::findHandler);
    }

    private CommandHandler<?, ?> findHandler(Class<?> commandClass) {
        Map<String, CommandHandler> beans = applicationContext.getBeansOfType(CommandHandler.class);

        for (CommandHandler<?, ?> handler : beans.values()) {
            Type[] interfaces = handler.getClass().getGenericInterfaces();
            for (Type type : interfaces) {
                if (type instanceof ParameterizedType) {
                    Type[] typeArgs = ((ParameterizedType) type).getActualTypeArguments();
                    if (typeArgs.length > 0 && typeArgs[0].equals(commandClass)) {
                        return handler;
                    }
                }
            }
        }

        throw new IllegalArgumentException("No handler found for command: " + commandClass.getName());
    }
}
```

### Query Bus

Interface for dispatching queries:

```java
package com.example.app.domain.cqrs;

public interface QueryBus {
    <Q, R> R execute(Q query);
}
```

Implementation mirrors the command bus pattern with query-specific logic.

### Handler Interfaces

**CommandHandler:**

```java
package com.example.app.domain.cqrs;

public interface CommandHandler<C, R> {
    R handle(C command);
}
```

**QueryHandler:**

```java
package com.example.app.domain.cqrs;

public interface QueryHandler<Q, R> {
    R handle(Q query);
}
```

## Creating Commands

Generate a command with its handler:

```bash
spring-hex make:command CreateOrder -a order
```

This creates two files in the `domain.order.command` package.

### Generated Command

```java
package com.example.app.domain.order.command;

import lombok.Value;

@Value
public class CreateOrderCommand {
    String customerId;
    List<OrderLineDto> orderLines;
    String shippingAddress;
}
```

Commands are immutable value objects. The `@Value` annotation generates:
- Constructor with all fields
- Getters for all fields
- `equals()`, `hashCode()`, and `toString()`
- Makes all fields `private final`

### Generated Command Handler

```java
package com.example.app.domain.order.command;

import com.example.app.domain.cqrs.CommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateOrderCommandHandler implements CommandHandler<CreateOrderCommand, Void> {

    @Override
    public Void handle(CreateOrderCommand command) {
        // TODO: Implement command handling logic
        return null;
    }
}
```

### Implementing Command Logic

Fill in the handler with domain logic:

```java
@Component
@RequiredArgsConstructor
public class CreateOrderCommandHandler implements CommandHandler<CreateOrderCommand, OrderId> {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;

    @Override
    public OrderId handle(CreateOrderCommand command) {
        // Validate customer exists
        Customer customer = customerRepository.findById(command.getCustomerId())
            .orElseThrow(() -> new CustomerNotFoundException(command.getCustomerId()));

        // Create order aggregate
        Order order = Order.create(
            customer.getId(),
            command.getOrderLines(),
            command.getShippingAddress()
        );

        // Persist
        Order savedOrder = orderRepository.save(order);

        return savedOrder.getId();
    }
}
```

## Creating Queries

Generate a query with its handler:

```bash
spring-hex make:query GetOrderById -a order -r OrderResponse
```

The `-r` flag specifies the return type. This creates two files.

### Generated Query

```java
package com.example.app.domain.order.query;

import lombok.Value;

@Value
public class GetOrderByIdQuery {
    String orderId;
}
```

### Generated Query Handler

```java
package com.example.app.domain.order.query;

import com.example.app.domain.cqrs.QueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetOrderByIdQueryHandler implements QueryHandler<GetOrderByIdQuery, OrderResponse> {

    @Override
    public OrderResponse handle(GetOrderByIdQuery query) {
        // TODO: Implement query handling logic
        return null;
    }
}
```

### Implementing Query Logic

```java
@Component
@RequiredArgsConstructor
public class GetOrderByIdQueryHandler implements QueryHandler<GetOrderByIdQuery, OrderResponse> {

    private final OrderJpaRepository orderJpaRepository;

    @Override
    public OrderResponse handle(GetOrderByIdQuery query) {
        OrderEntity entity = orderJpaRepository.findById(query.getOrderId())
            .orElseThrow(() -> new OrderNotFoundException(query.getOrderId()));

        return OrderResponse.builder()
            .orderId(entity.getId())
            .customerId(entity.getCustomerId())
            .status(entity.getStatus())
            .orderLines(entity.getOrderLines().stream()
                .map(this::toOrderLineDto)
                .collect(Collectors.toList()))
            .createdAt(entity.getCreatedAt())
            .build();
    }

    private OrderLineDto toOrderLineDto(OrderLineEntity entity) {
        return new OrderLineDto(
            entity.getProductId(),
            entity.getQuantity(),
            entity.getUnitPrice()
        );
    }
}
```

## Using the Command Bus

Inject and use the command bus in your controllers:

```java
package com.example.app.infrastructure.web.order;

import com.example.app.domain.cqrs.CommandBus;
import com.example.app.domain.order.command.CreateOrderCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CommandBus commandBus;

    @PostMapping
    public ResponseEntity<OrderCreatedResponse> createOrder(
        @RequestBody CreateOrderRequest request
    ) {
        CreateOrderCommand command = new CreateOrderCommand(
            request.getCustomerId(),
            request.getOrderLines(),
            request.getShippingAddress()
        );

        OrderId orderId = commandBus.execute(command);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(new OrderCreatedResponse(orderId.getValue()));
    }
}
```

## Using the Query Bus

Similarly, inject and use the query bus:

```java
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final QueryBus queryBus;

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String id) {
        GetOrderByIdQuery query = new GetOrderByIdQuery(id);
        OrderResponse response = queryBus.execute(query);
        return ResponseEntity.ok(response);
    }
}
```

## Handler Auto-Discovery

Handlers are automatically discovered via Spring's `ApplicationContext`. The mediator infrastructure:

1. Scans for all beans implementing `CommandHandler` or `QueryHandler`
2. Inspects generic type parameters to determine which command/query each handler handles
3. Caches the handler mapping for performance
4. Throws `IllegalArgumentException` if no handler is found for a command/query

This means you only need to:
- Implement the handler interface
- Annotate the handler with `@Component`
- Spring will automatically register it

## Registering Handlers Manually

While `@Component` is convenient, you can also register handlers manually in a configuration class:

```java
package com.example.app.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {

    @Bean
    public CreateOrderCommandHandler createOrderCommandHandler(
        OrderRepository orderRepository,
        CustomerRepository customerRepository
    ) {
        return new CreateOrderCommandHandler(orderRepository, customerRepository);
    }

    @Bean
    public GetOrderByIdQueryHandler getOrderByIdQueryHandler(
        OrderJpaRepository orderJpaRepository
    ) {
        return new GetOrderByIdQueryHandler(orderJpaRepository);
    }
}
```

This approach gives you more control over handler instantiation and dependencies.

## Best Practices

1. **Keep commands immutable** — Use `@Value` or records
2. **One handler per command/query** — Don't share handlers
3. **Validate in handlers** — Business validation belongs in the handler
4. **Use domain repositories in commands** — Commands work with aggregates
5. **Use query repositories in queries** — Queries can bypass the domain model for performance
6. **Return specific types** — Avoid returning `Object` or generic maps
7. **Handle errors** — Throw domain-specific exceptions from handlers

## Command and Query Naming Conventions

**Commands** should express intent with action verbs:
- `CreateOrderCommand`
- `UpdateOrderStatusCommand`
- `CancelOrderCommand`
- `ConfirmOrderPaymentCommand`

**Queries** should express data requests:
- `GetOrderByIdQuery`
- `ListOrdersByCustomerQuery`
- `FindPendingOrdersQuery`
- `SearchOrdersQuery`

## Related Commands

- `spring-hex make:mediator` — Generate mediator infrastructure
- `spring-hex make:command <name> -a <aggregate>` — Generate command and handler
- `spring-hex make:query <name> -a <aggregate> -r <return-type>` — Generate query and handler

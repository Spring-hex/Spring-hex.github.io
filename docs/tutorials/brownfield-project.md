---
title: Brownfield Project
parent: Tutorials
nav_order: 2
---

# Brownfield Project Tutorial

This tutorial demonstrates how to integrate Spring-Hex CLI into an existing Spring Boot application without disrupting your current codebase.

## The Challenge

Existing projects often have established package structures and architectural patterns. Introducing hexagonal architecture should be gradual and non-disruptive. Spring-Hex CLI provides flexible configuration to adapt to your existing conventions.

## Scenario

You have an existing ERP system with this package structure:

```
com.mycompany.erp/
├── customer/
│   ├── domain/
│   ├── service/
│   └── controller/
├── invoice/
│   ├── domain/
│   ├── service/
│   └── controller/
└── config/
```

You want to add a new `Payment` module using hexagonal architecture while maintaining consistency with your existing structure.

## Step 1: Initialize with Custom Base Package

Initialize Spring-Hex with your existing base package:

```bash
cd /path/to/your/erp-project
spring-hex init -p com.mycompany.erp
```

**Output:**
```
Spring-Hex initialized successfully!
Configuration file created: .hex/config.yml
Base package: com.mycompany.erp

Configuration customized for existing project structure.
Review .hex/config.yml to adjust path mappings.
```

This creates a configuration file that respects your existing package structure.

## Step 2: Customize Configuration for Existing Conventions

Open `.hex/config.yml` and customize the paths to match your established patterns:

```yaml
base-package: com.mycompany.erp
paths:
  model: modules.{aggregate}.domain
  command: modules.{aggregate}.application.command
  query: modules.{aggregate}.application.query
  handler: modules.{aggregate}.application.handler
  port: modules.{aggregate}.application.port
  adapter: modules.{aggregate}.infrastructure.adapter
  persistence: modules.{aggregate}.infrastructure.persistence
  controller: modules.{aggregate}.api
  event: modules.{aggregate}.domain.event
  listener: modules.{aggregate}.application.listener
  mapper: modules.{aggregate}.infrastructure.mapper
  config: config

mediator:
  enabled: true
  package: shared.mediator

shared:
  package: shared.domain
```

### Path Customization Explained

Each path token supports the `{aggregate}` placeholder:

- **model**: Domain aggregates and value objects
- **command/query**: CQRS command and query objects
- **handler**: Command and query handlers
- **port**: Repository interfaces (ports)
- **persistence**: JPA entities and repository implementations
- **controller**: REST controllers
- **event/listener**: Domain events and listeners

The structure `modules.{aggregate}.domain` will generate:
```
com.mycompany.erp.modules.payment.domain
```

For a legacy structure like `{aggregate}.domain.model`, it generates:
```
com.mycompany.erp.payment.domain.model
```

## Step 3: Generate a Module with Custom Structure

Generate the Payment module using your custom configuration:

```bash
spring-hex make:module Payment
```

**Output:**
```
Generated: src/main/java/com/mycompany/erp/modules/payment/domain/Payment.java
Generated: src/main/java/com/mycompany/erp/modules/payment/domain/PaymentId.java
Generated: src/main/java/com/mycompany/erp/modules/payment/application/command/CreatePaymentCommand.java
Generated: src/main/java/com/mycompany/erp/modules/payment/application/handler/CreatePaymentCommandHandler.java
Generated: src/main/java/com/mycompany/erp/modules/payment/application/query/GetPaymentQuery.java
Generated: src/main/java/com/mycompany/erp/modules/payment/application/handler/GetPaymentQueryHandler.java
Generated: src/main/java/com/mycompany/erp/modules/payment/application/port/PaymentRepository.java
Generated: src/main/java/com/mycompany/erp/modules/payment/infrastructure/persistence/JpaPaymentRepository.java
Generated: src/main/java/com/mycompany/erp/modules/payment/infrastructure/persistence/PaymentEntity.java
Generated: src/main/java/com/mycompany/erp/modules/payment/infrastructure/mapper/PaymentMapper.java
Generated: src/main/java/com/mycompany/erp/modules/payment/api/PaymentController.java
Generated: src/main/java/com/mycompany/erp/modules/payment/domain/event/PaymentCreatedEvent.java
Generated: src/main/java/com/mycompany/erp/modules/payment/application/listener/PaymentCreatedEventListener.java

Payment module created successfully!
```

### Resulting Structure

```
com.mycompany.erp/
├── customer/                          # Existing module (old style)
│   ├── domain/
│   ├── service/
│   └── controller/
├── invoice/                           # Existing module (old style)
│   ├── domain/
│   ├── service/
│   └── controller/
├── modules/                           # New hexagonal modules
│   └── payment/
│       ├── domain/
│       │   ├── Payment.java
│       │   ├── PaymentId.java
│       │   └── event/
│       │       └── PaymentCreatedEvent.java
│       ├── application/
│       │   ├── command/
│       │   │   └── CreatePaymentCommand.java
│       │   ├── query/
│       │   │   └── GetPaymentQuery.java
│       │   ├── handler/
│       │   │   ├── CreatePaymentCommandHandler.java
│       │   │   └── GetPaymentQueryHandler.java
│       │   ├── port/
│       │   │   └── PaymentRepository.java
│       │   └── listener/
│       │       └── PaymentCreatedEventListener.java
│       ├── infrastructure/
│       │   ├── persistence/
│       │   │   ├── JpaPaymentRepository.java
│       │   │   └── PaymentEntity.java
│       │   └── mapper/
│       │       └── PaymentMapper.java
│       └── api/
│           └── PaymentController.java
├── shared/
│   ├── domain/
│   │   └── AggregateRoot.java
│   └── mediator/
│       ├── CommandBus.java
│       └── QueryBus.java
└── config/
    ├── DomainConfig.java
    └── MediatorConfig.java
```

## Step 4: Per-Command Package Override

For one-off cases where you need a different package structure, use the `-p` flag:

```bash
spring-hex make:command ProcessRefund -a payment -p com.mycompany.erp.legacy.payment.commands
```

**Output:**
```
Generated: src/main/java/com/mycompany/erp/legacy/payment/commands/ProcessRefundCommand.java
Generated: src/main/java/com/mycompany/erp/legacy/payment/commands/ProcessRefundCommandHandler.java

ProcessRefund command created successfully!
```

This generates files in the specified package instead of using the config defaults.

## Step 5: Integration with Existing Code

The new Payment module can interact with existing services:

```java
// New hexagonal module calling existing service
@Component
@RequiredArgsConstructor
public class CreatePaymentCommandHandler implements CommandHandler<CreatePaymentCommand> {

    private final PaymentRepository paymentRepository;
    private final InvoiceService invoiceService; // Existing service

    @Override
    public void handle(CreatePaymentCommand command) {
        // Validate invoice exists using existing service
        var invoice = invoiceService.findById(command.invoiceId());

        Payment payment = Payment.builder()
                .id(new PaymentId(UUID.randomUUID().toString()))
                .invoiceId(command.invoiceId())
                .amount(command.amount())
                .status("PENDING")
                .build();

        paymentRepository.save(payment);
    }
}
```

Existing services can also dispatch commands to the new module:

```java
// Existing InvoiceService using new hexagonal module
@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final CommandBus commandBus; // Injected from new mediator infrastructure

    public void markAsPaid(Long invoiceId, BigDecimal amount) {
        // Update invoice status
        invoice.setStatus("PAID");
        invoiceRepository.save(invoice);

        // Create payment record via command bus
        commandBus.dispatch(new CreatePaymentCommand(
            invoiceId.toString(),
            amount,
            "CREDIT_CARD"
        ));
    }
}
```

## Step 6: Gradual Migration Strategy

### Start Small

1. **Choose one new feature**: Implement it with hexagonal architecture
2. **Prove the pattern**: Let the team experience the benefits
3. **Evaluate**: Assess maintainability, testability, and team feedback

### Expand Strategically

4. **Identify candidates**: Find modules with complex domain logic
5. **Refactor incrementally**: Move one module at a time
6. **Maintain consistency**: Keep existing modules working during transition

### When to Use Hexagonal vs. Traditional

- **Use hexagonal architecture** for:
  - Complex domain logic with business rules
  - Modules requiring extensive testing
  - Features with multiple adapters (REST, messaging, batch)
  - Long-term strategic modules

- **Keep traditional MVC** for:
  - Simple CRUD operations
  - Admin panels and utilities
  - Rapid prototypes
  - Low-complexity features

## Step 7: Using CRUD for Simpler Resources

For straightforward resources in your existing project, use the CRUD generator:

```bash
spring-hex make:crud Notification
```

This generates a simple MVC structure that complements existing patterns without the full hexagonal overhead.

## Configuration Best Practices

### 1. Match Existing Conventions

Analyze your current structure:

```bash
# Review existing packages
find src/main/java -type d -name "domain" -o -name "service" -o -name "controller"
```

Configure paths to match what you already have.

### 2. Use Modules Directory for New Code

Create a `modules` subdirectory for new hexagonal code:

```yaml
paths:
  model: modules.{aggregate}.domain
```

This clearly separates new architecture from legacy code.

### 3. Document the Transition

Add architectural decision records (ADRs) explaining:

- Why hexagonal architecture was chosen
- Which modules use which pattern
- Migration roadmap

### 4. Team Training

- Run workshops on hexagonal architecture concepts
- Pair existing developers with new module generation
- Code review sessions to reinforce patterns

## Troubleshooting

### Package Conflicts

If generated code conflicts with existing classes:

```bash
# Use -p flag to specify alternative package
spring-hex make:command UpdatePayment -a payment -p com.mycompany.erp.modules.payment.commands.v2
```

### Import Errors

Ensure your IDE indexes new packages:

- IntelliJ IDEA: File → Invalidate Caches → Restart
- Eclipse: Project → Clean
- VS Code: Reload window

### Spring Bean Conflicts

If you have existing configuration classes, rename generated configs:

```bash
# Manually rename DomainConfig.java to HexDomainConfig.java
```

## Next Steps

1. Generate mediator infrastructure: `spring-hex make:mediator`
2. Create your first hexagonal module
3. Write tests for new commands and queries
4. Gradually refactor existing modules as they require significant changes
5. Update team documentation with new patterns

## Key Takeaways

- Spring-Hex adapts to existing project structures via configuration
- Use the `-p` flag for one-off package overrides
- Introduce hexagonal architecture gradually, not all at once
- Choose the right pattern (hexagonal vs. MVC) based on domain complexity
- New and old code can coexist and interact seamlessly

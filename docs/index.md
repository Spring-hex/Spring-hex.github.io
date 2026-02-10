---
title: Home
layout: home
nav_order: 1
---

# Spring Hex CLI
{: .fs-9 }

Generate hexagonal architecture scaffolding for Spring Boot projects in seconds.
{: .fs-6 .fw-300 }

[Get Started](/spring-hex/getting-started/installation){: .btn .btn-primary .fs-5 .mb-4 .mb-md-0 .mr-2 }
[Command Reference](/spring-hex/reference/commands){: .btn .fs-5 .mb-4 .mb-md-0 }

---

Spring Hex CLI is a command-line tool that generates **hexagonal architecture** (ports and adapters), **CQRS**, and **DDD** scaffolding code for Spring Boot projects. It eliminates the boilerplate of setting up clean architecture, letting you focus on domain logic.

## Quick Example

```bash
# Initialize configuration
spring-hex init

# Set up CQRS infrastructure (CommandBus, QueryBus, etc.)
spring-hex make:mediator

# Generate a full bounded context for "Order"
spring-hex make:module Order
```

This generates a complete module with:
- Domain model, aggregate root, value objects
- CQRS commands, queries, and handlers
- Repository port and JPA adapter
- REST controller
- Event infrastructure
- Mapper between domain and persistence layers

## Features

**Hexagonal Architecture** --- Generates domain, port, and adapter layers with proper dependency inversion.

**CQRS Support** --- Built-in CommandBus/QueryBus pattern with auto-wired handlers.

**Multiple Data Stores** --- JPA, MongoDB, and Redis adapter generation out of the box.

**Database Migrations** --- Flyway and Liquibase support with auto-detection.

**Customizable Paths** --- Configure package structure via `.hex/config.yml` to match your conventions.

**Simple CRUD Too** --- `make:crud` generates a full MVC stack when you don't need hexagonal complexity.

## What It Generates

| Layer | Components |
|:------|:-----------|
| **Domain** | Models, Aggregates, Value Objects, Commands, Queries, Events, Ports |
| **Infrastructure** | Controllers, Repository Adapters, Event Listeners, Mappers, JPA Entities |
| **Mediator** | CommandBus, QueryBus, Handler interfaces, Configuration |
| **Migration** | Flyway SQL, Liquibase XML/YAML/SQL changesets |
| **Tests** | Feature tests (SpringBootTest), Unit tests (Mockito) |

---

Ready to get started? Head to the [Installation Guide](/spring-hex/getting-started/installation).

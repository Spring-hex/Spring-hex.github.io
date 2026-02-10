---
title: Configuration
parent: Guide
nav_order: 2
---

# Configuration

Spring-Hex uses a configuration file located at `.hex/config.yml` in your project root to control code generation behavior. This guide provides a complete reference for all configuration options.

## Configuration File Structure

```yaml
base-package: com.example.app

paths:
  model: domain.{aggregate}.model
  command: domain.{aggregate}.command
  query: domain.{aggregate}.query
  event: domain.{aggregate}.event
  # ... additional path configurations

crud:
  model: domain.{aggregate}.crud.model
  repository: domain.{aggregate}.crud.repository
  # ... additional CRUD path configurations
```

## Base Package

The `base-package` key defines the root Java package for your application:

```yaml
base-package: com.example.myapp
```

All generated code will be placed under this package according to the path patterns defined in the `paths` section.

## Path Configuration

The `paths` section contains 16 configurable path patterns that control where Spring-Hex generates different types of code.

### Path Variables

Path patterns support the following variables:

- `{aggregate}` — The aggregate name (e.g., `order`, `customer`)
- `{name}` — The component name (e.g., `CreateOrder`, `OrderService`)
- `{category}` — The adapter category (e.g., `notification`, `payment`)

### Default Path Patterns

| Key | Default Pattern | Example Output |
|-----|----------------|----------------|
| `model` | `domain.{aggregate}.model` | `com.app.domain.order.model` |
| `command` | `domain.{aggregate}.command` | `com.app.domain.order.command` |
| `query` | `domain.{aggregate}.query` | `com.app.domain.order.query` |
| `event` | `domain.{aggregate}.event` | `com.app.domain.order.event` |
| `event-listener` | `infrastructure.event.{aggregate}` | `com.app.infrastructure.event.order` |
| `dto` | `domain.{aggregate}.dto` | `com.app.domain.order.dto` |
| `port-in` | `domain.{aggregate}.port.in` | `com.app.domain.order.port.in` |
| `port-out` | `domain.{aggregate}.port.out` | `com.app.domain.order.port.out` |
| `persistence` | `infrastructure.persistence.{aggregate}` | `com.app.infrastructure.persistence.order` |
| `controller` | `infrastructure.web.{aggregate}` | `com.app.infrastructure.web.order` |
| `adapter` | `infrastructure.{category}.{aggregate}` | `com.app.infrastructure.notification.order` |
| `config` | `infrastructure.config` | `com.app.infrastructure.config` |
| `mediator` | `infrastructure.mediator` | `com.app.infrastructure.mediator` |
| `cqrs` | `domain.cqrs` | `com.app.domain.cqrs` |
| `domain-root` | `domain` | `com.app.domain` |
| `service` | `domain.{aggregate}.service` | `com.app.domain.order.service` |

### Path Configuration Examples

#### Standard Hexagonal Layout (Default)

```yaml
paths:
  model: domain.{aggregate}.model
  port-in: domain.{aggregate}.port.in
  port-out: domain.{aggregate}.port.out
  controller: infrastructure.web.{aggregate}
  persistence: infrastructure.persistence.{aggregate}
```

#### Feature-Based Organization

```yaml
paths:
  model: feature.{aggregate}.domain.model
  port-in: feature.{aggregate}.domain.port.in
  port-out: feature.{aggregate}.domain.port.out
  controller: feature.{aggregate}.api
  persistence: feature.{aggregate}.persistence
```

#### Flat CQRS Structure

```yaml
paths:
  command: domain.command.{aggregate}
  query: domain.query.{aggregate}
  model: domain.model.{aggregate}
```

## CRUD Path Configuration

The `crud` section defines paths for CRUD-specific scaffolding generated with the `--crud` flag.

### Default CRUD Paths

| Key | Default Pattern | Example Output |
|-----|----------------|----------------|
| `model` | `domain.{aggregate}.crud.model` | `com.app.domain.order.crud.model` |
| `repository` | `domain.{aggregate}.crud.repository` | `com.app.domain.order.crud.repository` |
| `service` | `domain.{aggregate}.crud.service` | `com.app.domain.order.crud.service` |
| `controller` | `infrastructure.web.{aggregate}.crud` | `com.app.infrastructure.web.order.crud` |
| `dto` | `domain.{aggregate}.crud.dto` | `com.app.domain.order.crud.dto` |
| `mapper` | `domain.{aggregate}.crud.mapper` | `com.app.domain.order.crud.mapper` |

### CRUD Configuration Example

```yaml
crud:
  model: domain.{aggregate}.model
  repository: infrastructure.persistence.{aggregate}
  service: application.{aggregate}.service
  controller: presentation.{aggregate}.controller
  dto: application.{aggregate}.dto
  mapper: application.{aggregate}.mapper
```

## Package Resolution Priority

Spring-Hex resolves the base package using the following priority order:

1. **Command-line flag** — The `-p` or `--package` flag
2. **Configuration file** — The `base-package` value in `.hex/config.yml`
3. **Auto-detection** — Automatic detection from project structure

### Command-Line Override

```bash
spring-hex make:aggregate Order -p com.custom.package
```

This will use `com.custom.package` regardless of configuration file settings.

### Configuration File

```yaml
base-package: com.example.myapp
```

If no `-p` flag is provided, this value is used.

### Auto-Detection Strategies

When no package is specified via flag or configuration, Spring-Hex attempts auto-detection in this order:

#### 1. @SpringBootApplication Scan

Searches for the `@SpringBootApplication` annotation:

```java
package com.example.myapp;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

Detected package: `com.example.myapp`

#### 2. Maven pom.xml GroupId

Parses `pom.xml` for the `<groupId>` element:

```xml
<groupId>com.example.myapp</groupId>
<artifactId>order-service</artifactId>
```

Detected package: `com.example.myapp`

#### 3. Gradle build.gradle Group

Parses `build.gradle` or `build.gradle.kts`:

```groovy
group = 'com.example.myapp'
```

Detected package: `com.example.myapp`

#### 4. Source Directory Scan

Scans `src/main/java` for the deepest common package containing Java files.

If auto-detection fails, Spring-Hex will prompt for the base package interactively.

## Complete Configuration Example

```yaml
# Base package for all generated code
base-package: com.example.ecommerce

# Hexagonal architecture paths
paths:
  # Domain layer
  model: domain.{aggregate}.model
  command: domain.{aggregate}.command
  query: domain.{aggregate}.query
  event: domain.{aggregate}.event
  dto: domain.{aggregate}.dto
  service: domain.{aggregate}.service

  # Ports
  port-in: domain.{aggregate}.port.in
  port-out: domain.{aggregate}.port.out

  # Infrastructure layer
  controller: infrastructure.web.{aggregate}
  persistence: infrastructure.persistence.{aggregate}
  event-listener: infrastructure.event.{aggregate}
  adapter: infrastructure.{category}.{aggregate}

  # Cross-cutting
  config: infrastructure.config
  mediator: infrastructure.mediator
  cqrs: domain.cqrs
  domain-root: domain

# CRUD scaffolding paths
crud:
  model: domain.{aggregate}.model
  repository: domain.{aggregate}.repository
  service: application.{aggregate}
  controller: infrastructure.web.{aggregate}
  dto: application.{aggregate}.dto
  mapper: application.{aggregate}.mapper
```

## Initializing Configuration

To create a default `.hex/config.yml` file:

```bash
spring-hex init
```

This generates a configuration file with all default values, which you can then customize.

## Validating Configuration

To validate your configuration file:

```bash
spring-hex config:validate
```

This checks for:
- Valid YAML syntax
- Required keys present
- Valid path patterns
- Proper variable usage

## Best Practices

1. **Commit the configuration** — Include `.hex/config.yml` in version control so all team members use consistent paths
2. **Document customizations** — If you deviate from defaults, add comments explaining why
3. **Use consistent patterns** — Keep `{aggregate}` placement consistent across paths
4. **Validate after changes** — Run `config:validate` after modifying the file
5. **Consider migration** — If changing paths in an existing project, plan how to migrate existing code

## Related Commands

- `spring-hex init` — Initialize a new configuration file
- `spring-hex config:show` — Display current configuration
- `spring-hex config:validate` — Validate configuration file
- `spring-hex config:reset` — Reset to default configuration

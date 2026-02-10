---
title: Initialization
parent: Getting Started
nav_order: 2
---

# Initialization

## Overview

Before generating code, Spring-Hex CLI needs to know your project's base package. The `init` command creates a `.hex/config.yml` file that stores your project configuration, including the base package and optional custom path patterns.

## Running Init

Navigate to your Spring Boot project root directory and run:

```bash
spring-hex init
```

### Auto-Detection

Spring-Hex CLI automatically detects your base package by scanning for:

1. `@SpringBootApplication` annotation in Java source files
2. `groupId` in `pom.xml` (Maven projects)
3. Package structure in `src/main/java`

Example output:

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

## Command Options

### Specify Package Manually

Override auto-detection with the `-p` flag:

```bash
spring-hex init -p com.mycompany.myproject
```

### Custom Output Directory

Specify a different directory for the config file:

```bash
spring-hex init -o /path/to/project
```

### Force Overwrite

Overwrite an existing `.hex/config.yml` file:

```bash
spring-hex init --force
```

## Generated Configuration

The `init` command creates `.hex/config.yml` with the following structure:

```yaml
base-package: com.example.myapp

# Uncomment and customize to override default path patterns
# paths:
#   model: domain.{aggregate}.model
#   command: domain.{aggregate}.command
#   command-handler: domain.{aggregate}.command
#   query: domain.{aggregate}.query
#   query-handler: domain.{aggregate}.query
#   event: domain.{aggregate}.event
#   port-in: domain.{aggregate}.port.in
#   port-out: domain.{aggregate}.port.out
#   controller: infrastructure.web.{aggregate}
#   persistence: infrastructure.persistence.{aggregate}
#   event-listener: infrastructure.event.{aggregate}

# Uncomment and customize CRUD templates
# crud:
#   model: "{name}.model"
#   repository: "{name}.repository"
#   service: "{name}.service"
#   controller: "{name}.controller"
```

### Path Patterns

Path patterns use `{aggregate}` as a placeholder for the module name. For example, if you generate a module named `Order`:

- `domain.{aggregate}.model` resolves to `domain.order.model`
- `infrastructure.web.{aggregate}` resolves to `infrastructure.web.order`

### CRUD Patterns

CRUD patterns use `{name}` as a placeholder for the entity name in lowercase.

## Package Resolution Priority

When Spring-Hex CLI determines the base package, it follows this priority:

1. **Command-line flag**: `-p` option on any command
2. **Configuration file**: `base-package` in `.hex/config.yml`
3. **Auto-detection**: Scan project structure

This allows you to override the configured package on a per-command basis if needed.

## Example Workflow

```bash
# Navigate to project root
cd my-spring-boot-app

# Initialize configuration
spring-hex init

# Verify configuration was created
cat .hex/config.yml

# Generate your first module
spring-hex make:module Order
```

## Next Steps

Continue to [Quick Start]({% link getting-started/quick-start.md %}) for a complete example of building your first bounded context.

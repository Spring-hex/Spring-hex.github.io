---
title: Reference
nav_order: 5
has_children: true
---

# Reference

This section provides comprehensive API and configuration reference documentation for Spring-Hex CLI. Here you'll find detailed specifications for all commands, configuration options, and customization capabilities.

## Contents

- **[Commands](commands.md)** - Complete reference for all 33 CLI commands, including setup, hexagonal architecture generation, data seeding, CRUD scaffolding, testing, and database migrations
- **[Path Defaults](path-defaults.md)** - Default package path patterns for hexagonal and CRUD architectures, with customization examples
- **[Stubs](stubs.md)** - Template reference for all stub files used in code generation, including available placeholders and customization

## Quick Reference

Spring-Hex CLI provides four main categories of functionality:

1. **Setup Commands** - Initialize configuration files for your project
2. **Hexagonal Architecture Generators** - Create domain models, commands, queries, ports, adapters, and infrastructure components following hexagonal architecture principles
3. **Data Seeding** - Generate factories (Datafaker) and seeders for populating dev/test data
4. **CRUD Generators** - Quickly scaffold traditional MVC-style CRUD operations
5. **Database Migrations** - Manage schema migrations with Flyway or Liquibase

All commands support the `-p/--package` and `-o/--output` options for controlling where code is generated. The tool automatically detects your base package from existing source files when these options are omitted.

## Configuration

Most commands can be customized through the `.hex/config.yml` file created by the `init` command. This configuration file allows you to:

- Override default package paths for each component type
- Customize stub templates
- Set project-wide defaults

Refer to the individual command documentation for specific configuration options.

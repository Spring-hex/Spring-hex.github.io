---
title: Database Migrations
parent: Guide
nav_order: 4
---

# Database Migrations

Spring-Hex provides commands for managing database migrations using Flyway or Liquibase. The tool auto-detects which migration framework your project uses and generates migrations accordingly.

## Supported Migration Tools

Spring-Hex supports two industry-standard migration tools:

- **Flyway** — SQL-based migrations with versioned files
- **Liquibase** — XML, YAML, SQL, or JSON changesets with advanced rollback support

## Auto-Detection

Spring-Hex automatically detects which migration tool you're using by checking in this order:

1. **Directory structure** — Presence of `db/migration/` (Flyway) or `db/changelog/` (Liquibase)
2. **application.properties** — Properties like `spring.flyway.*` or `spring.liquibase.*`
3. **application.yml** — YAML configuration for `spring.flyway` or `spring.liquibase`
4. **pom.xml** — Maven dependencies for `flyway-core` or `liquibase-core`
5. **build.gradle** — Gradle dependencies

If no migration tool is detected, Spring-Hex prompts you to choose one.

## Creating Migrations

### Basic Migration Creation

```bash
spring-hex make:migration create_users_table
```

This generates a new migration file with a timestamp prefix.

### Flyway Migration Output

**File:** `src/main/resources/db/migration/V20260210143522__create_users_table.sql`

```sql
-- Migration: create_users_table
-- Created: 2026-02-10 14:35:22

-- TODO: Write your migration SQL here
```

The filename format is `V{timestamp}__{description}.sql`:
- `V` prefix indicates a versioned migration
- `{timestamp}` ensures chronological ordering (YYYYMMDDHHmmss)
- `__` (double underscore) separates version from description
- `.sql` extension

### Liquibase Migration Output

**File:** `src/main/resources/db/changelog/changes/20260210143522_create_users_table.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.9.xsd">

    <changeSet id="20260210143522_create_users_table" author="spring-hex">
        <!-- TODO: Add your changes here -->

        <rollback>
            <!-- TODO: Add rollback logic here -->
        </rollback>
    </changeSet>
</databaseChangeLog>
```

**Master changelog updated:** `src/main/resources/db/changelog/db.changelog-master.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.9.xsd">

    <include file="db/changelog/changes/20260210143522_create_users_table.xml"/>
</databaseChangeLog>
```

### Liquibase Format Options

Liquibase supports multiple formats specified with the `--format` flag:

**XML format (default):**

```bash
spring-hex make:migration create_users_table --format xml
```

**YAML format:**

```bash
spring-hex make:migration create_users_table --format yaml
```

Generates `20260210143522_create_users_table.yaml`:

```yaml
databaseChangeLog:
  - changeSet:
      id: 20260210143522_create_users_table
      author: spring-hex
      changes:
        # TODO: Add your changes here
      rollback:
        # TODO: Add rollback logic here
```

**SQL format:**

```bash
spring-hex make:migration create_users_table --format sql
```

Generates `20260210143522_create_users_table.sql`:

```sql
--liquibase formatted sql

--changeset spring-hex:20260210143522_create_users_table
-- TODO: Add your changes here

--rollback TODO: Add rollback SQL here
```

## Example Migration Content

### Flyway: Creating a Users Table

```sql
-- Migration: create_users_table
-- Created: 2026-02-10 14:35:22

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
```

### Liquibase XML: Creating a Users Table

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.9.xsd">

    <changeSet id="20260210143522_create_users_table" author="spring-hex">
        <createTable tableName="users">
            <column name="id" type="BIGINT" autoIncrement="true">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="username" type="VARCHAR(50)">
                <constraints nullable="false" unique="true"/>
            </column>
            <column name="email" type="VARCHAR(255)">
                <constraints nullable="false" unique="true"/>
            </column>
            <column name="password_hash" type="VARCHAR(255)">
                <constraints nullable="false"/>
            </column>
            <column name="first_name" type="VARCHAR(100)"/>
            <column name="last_name" type="VARCHAR(100)"/>
            <column name="created_at" type="TIMESTAMP" defaultValueComputed="CURRENT_TIMESTAMP">
                <constraints nullable="false"/>
            </column>
            <column name="updated_at" type="TIMESTAMP" defaultValueComputed="CURRENT_TIMESTAMP">
                <constraints nullable="false"/>
            </column>
        </createTable>

        <createIndex indexName="idx_users_email" tableName="users">
            <column name="email"/>
        </createIndex>

        <createIndex indexName="idx_users_username" tableName="users">
            <column name="username"/>
        </createIndex>

        <rollback>
            <dropTable tableName="users"/>
        </rollback>
    </changeSet>
</databaseChangeLog>
```

## Running Migrations

Execute all pending migrations:

```bash
spring-hex migrate
```

This runs the appropriate migration command based on your detected tool:
- **Flyway:** `mvn flyway:migrate` or `./mvnw flyway:migrate`
- **Liquibase:** `mvn liquibase:update` or `./mvnw liquibase:update`

### Build Tool Detection

Spring-Hex auto-detects Maven or Gradle:
- Maven: Uses `mvn` or `./mvnw` (Maven Wrapper)
- Gradle: Uses `gradle` or `./gradlew` (Gradle Wrapper)

Wrapper scripts are preferred when available.

## Migration Status

Check which migrations have been applied:

```bash
spring-hex migrate:status
```

**Flyway output:**

```
+-----------+---------+---------------------+----------+
| Version   | Status  | Description         | Installed|
+-----------+---------+---------------------+----------+
| 1         | Success | create users table  | 2026-02-10 14:35:22 |
| 2         | Pending | add orders table    |          |
+-----------+---------+---------------------+----------+
```

**Liquibase output:**

```
2 change sets have not been applied to database
     db/changelog/changes/20260210143522_create_users_table.xml::20260210143522_create_users_table::spring-hex
     db/changelog/changes/20260210144015_add_orders_table.xml::20260210144015_add_orders_table::spring-hex
```

## Rolling Back Migrations

### Flyway Rollback

Flyway requires Flyway Teams edition for automatic rollback. For community edition, create a separate undo migration:

```bash
spring-hex make:migration undo_create_users_table
```

```sql
-- Migration: undo_create_users_table
-- Created: 2026-02-10 15:00:00

DROP TABLE IF EXISTS users;
```

### Liquibase Rollback

Liquibase supports rollback natively:

```bash
spring-hex migrate:rollback
```

By default, this rolls back the last changeset. To rollback multiple steps:

```bash
spring-hex migrate:rollback --step=3
```

This rolls back the last 3 changesets.

Rollback to a specific tag:

```bash
spring-hex migrate:rollback --tag=version-1.0
```

## Validating Migrations

Verify that migrations have been applied correctly:

```bash
spring-hex migrate:validate
```

**Flyway:** Validates applied migrations against available migration files
**Liquibase:** Validates the changelog against the database

## Repairing Migration Metadata

If migration metadata becomes corrupted:

```bash
spring-hex migrate:repair
```

**Flyway:** Repairs the schema history table
**Liquibase:** Clears checksums and re-syncs

## Fresh Database

Drop all tables and re-run all migrations (destructive operation):

```bash
spring-hex migrate:fresh --force
```

The `--force` flag is required to confirm this destructive action. This command:
1. Drops all database objects
2. Recreates the schema
3. Runs all migrations from scratch

**Warning:** This permanently deletes all data. Only use in development environments.

## Best Practices

1. **Never modify applied migrations** — Create new migrations to fix issues
2. **Test migrations** — Run against a copy of production data before deploying
3. **Include rollback logic** — Especially important for Liquibase
4. **Use descriptive names** — `create_users_table` is better than `migration1`
5. **Keep migrations atomic** — One logical change per migration
6. **Version control** — Commit migration files to Git
7. **Review generated SQL** — Always verify the SQL before running in production
8. **Coordinate with team** — Avoid concurrent migration creation

## Migration File Locations

### Flyway

```
src/main/resources/
└── db/
    └── migration/
        ├── V20260210143522__create_users_table.sql
        ├── V20260210144015__add_orders_table.sql
        └── V20260210145230__add_user_indexes.sql
```

### Liquibase

```
src/main/resources/
└── db/
    └── changelog/
        ├── db.changelog-master.xml
        └── changes/
            ├── 20260210143522_create_users_table.xml
            ├── 20260210144015_add_orders_table.xml
            └── 20260210145230_add_user_indexes.yaml
```

## Configuration

### Flyway Configuration (application.yml)

```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
    out-of-order: false
```

### Liquibase Configuration (application.yml)

```yaml
spring:
  liquibase:
    enabled: true
    change-log: classpath:db/changelog/db.changelog-master.xml
    drop-first: false
```

## Related Commands

- `spring-hex make:migration <name>` — Create a new migration
- `spring-hex make:migration <name> --format <xml|yaml|sql>` — Create Liquibase migration with specific format
- `spring-hex migrate` — Run pending migrations
- `spring-hex migrate:status` — Show migration status
- `spring-hex migrate:rollback [--step=N]` — Rollback migrations (Liquibase)
- `spring-hex migrate:validate` — Validate migrations
- `spring-hex migrate:repair` — Repair migration metadata
- `spring-hex migrate:fresh --force` — Drop and recreate database

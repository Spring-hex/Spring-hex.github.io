---
title: CRUD Module
parent: Tutorials
nav_order: 3
---

# CRUD Module Tutorial

This tutorial demonstrates building a simple User management system using the CRUD generator for straightforward resources that don't require full hexagonal architecture.

## When to Use CRUD vs. Hexagonal Architecture

### Use `make:crud` for:

- **Simple CRUD operations**: Create, Read, Update, Delete without complex business logic
- **Master data**: Countries, categories, tags, settings
- **Admin resources**: User management, configuration panels
- **Straightforward entities**: Resources with minimal invariants
- **Rapid development**: Prototypes and MVPs

### Use `make:module` for:

- **Complex domain logic**: Business rules, aggregates, invariants
- **Event-driven features**: Publishing and consuming domain events
- **Multiple adapters**: REST + messaging + batch processing
- **CQRS patterns**: Separate read and write models
- **Strategic modules**: Core business capabilities

## The CRUD Pattern

The CRUD generator creates a traditional MVC structure:

```
User
├── Model (domain object)
├── Entity (JPA entity)
├── Repository (Spring Data JPA)
├── Mapper (domain ↔ entity conversion)
├── Service (business logic)
└── Controller (REST API)
```

This is simpler than hexagonal architecture but sufficient for many use cases.

## Step 1: Initialize Spring-Hex

If you haven't already initialized your project:

```bash
spring-hex init
```

**Output:**
```
Spring-Hex initialized successfully!
Configuration file created: .hex/config.yml
Base package: com.example.demo

You can now start generating components.
```

## Step 2: Generate a CRUD Module

Generate a complete CRUD module for User management:

```bash
spring-hex make:crud User
```

**Output:**
```
Generated: src/main/java/com/example/demo/user/model/User.java
Generated: src/main/java/com/example/demo/user/entity/UserEntity.java
Generated: src/main/java/com/example/demo/user/repository/UserRepository.java
Generated: src/main/java/com/example/demo/user/mapper/UserMapper.java
Generated: src/main/java/com/example/demo/user/service/UserService.java
Generated: src/main/java/com/example/demo/user/web/UserController.java

User CRUD module created successfully!
6 files generated.
```

## Step 3: Examine Generated Files

### User Model (Domain Object)

```java
// src/main/java/com/example/demo/user/model/User.java
package com.example.demo.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

The model represents the domain object without persistence concerns.

### UserEntity (JPA Entity)

```java
// src/main/java/com/example/demo/user/entity/UserEntity.java
package com.example.demo.user.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

The entity handles persistence with JPA annotations.

### UserRepository (Spring Data JPA)

```java
// src/main/java/com/example/demo/user/repository/UserRepository.java
package com.example.demo.user.repository;

import com.example.demo.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
```

The repository provides data access methods using Spring Data JPA.

### UserMapper (Domain ↔ Entity Conversion)

```java
// src/main/java/com/example/demo/user/mapper/UserMapper.java
package com.example.demo.user.mapper;

import com.example.demo.user.entity.UserEntity;
import com.example.demo.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        return User.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public UserEntity toEntity(User user) {
        if (user == null) {
            return null;
        }

        UserEntity entity = new UserEntity();
        entity.setId(user.getId());
        entity.setUsername(user.getUsername());
        entity.setEmail(user.getEmail());
        entity.setFirstName(user.getFirstName());
        entity.setLastName(user.getLastName());
        entity.setActive(user.isActive());

        return entity;
    }

    public void updateEntity(User user, UserEntity entity) {
        if (user == null || entity == null) {
            return;
        }

        entity.setUsername(user.getUsername());
        entity.setEmail(user.getEmail());
        entity.setFirstName(user.getFirstName());
        entity.setLastName(user.getLastName());
        entity.setActive(user.isActive());
    }
}
```

The mapper converts between domain and persistence models.

### UserService (Business Logic)

```java
// src/main/java/com/example/demo/user/service/UserService.java
package com.example.demo.user.service;

import com.example.demo.user.entity.UserEntity;
import com.example.demo.user.mapper.UserMapper;
import com.example.demo.user.model.User;
import com.example.demo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public User create(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }

        UserEntity entity = userMapper.toEntity(user);
        UserEntity savedEntity = userRepository.save(entity);
        return userMapper.toDomain(savedEntity);
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDomain);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toDomain);
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toDomain)
                .collect(Collectors.toList());
    }

    public User update(Long id, User user) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        userMapper.updateEntity(user, entity);
        UserEntity updatedEntity = userRepository.save(entity);
        return userMapper.toDomain(updatedEntity);
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found: " + id);
        }
        userRepository.deleteById(id);
    }
}
```

The service layer handles business logic and transaction management.

### UserController (REST API)

```java
// src/main/java/com/example/demo/user/web/UserController.java
package com.example.demo.user.web;

import com.example.demo.user.model.User;
import com.example.demo.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAll();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @RequestBody User user) {
        User updatedUser = userService.update(id, user);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

The controller exposes REST endpoints for the User resource.

## Step 4: Default CRUD Path Structure

The CRUD generator uses these default paths (configurable in `.hex/config.yml`):

```yaml
crud:
  model: "{name}.model"
  entity: "{name}.entity"
  repository: "{name}.repository"
  mapper: "{name}.mapper"
  service: "{name}.service"
  controller: "{name}.web"
```

The `{name}` placeholder is replaced with the lowercased entity name.

For `spring-hex make:crud User`, this generates:

```
com.example.demo/
└── user/
    ├── model/
    │   └── User.java
    ├── entity/
    │   └── UserEntity.java
    ├── repository/
    │   └── UserRepository.java
    ├── mapper/
    │   └── UserMapper.java
    ├── service/
    │   └── UserService.java
    └── web/
        └── UserController.java
```

## Step 5: Customizing CRUD Paths

You can customize the CRUD structure to match your conventions. Edit `.hex/config.yml`:

```yaml
crud:
  model: "modules.{name}.domain"
  entity: "modules.{name}.persistence"
  repository: "modules.{name}.persistence"
  mapper: "modules.{name}.infrastructure"
  service: "modules.{name}.application"
  controller: "modules.{name}.api"
```

After this change, `spring-hex make:crud Product` generates:

```
com.example.demo/
└── modules/
    └── product/
        ├── domain/
        │   └── Product.java
        ├── persistence/
        │   ├── ProductEntity.java
        │   └── ProductRepository.java
        ├── infrastructure/
        │   └── ProductMapper.java
        ├── application/
        │   └── ProductService.java
        └── api/
            └── ProductController.java
```

## Step 6: Partial Generation Options

Skip specific components using flags:

### Skip Model Generation

```bash
spring-hex make:crud Category --no-model
```

Generates entity, repository, service, and controller but no separate domain model. The entity serves as both persistence and domain model.

### Skip Service Layer

```bash
spring-hex make:crud Tag --no-service
```

Generates model, entity, repository, and controller. The controller calls the repository directly.

### Combined Options

```bash
spring-hex make:crud Setting --no-model --no-service
```

Generates only entity, repository, and controller for the simplest CRUD setup.

## Step 7: Testing the Generated Code

Create a test for the UserService:

```java
// src/test/java/com/example/demo/user/service/UserServiceTest.java
package com.example.demo.user.service;

import com.example.demo.user.entity.UserEntity;
import com.example.demo.user.mapper.UserMapper;
import com.example.demo.user.model.User;
import com.example.demo.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void create_shouldSaveUser_whenValidUser() {
        // Given
        User user = User.builder()
                .username("johndoe")
                .email("john@example.com")
                .build();

        UserEntity entity = new UserEntity();
        UserEntity savedEntity = new UserEntity();
        savedEntity.setId(1L);

        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userMapper.toEntity(user)).thenReturn(entity);
        when(userRepository.save(entity)).thenReturn(savedEntity);
        when(userMapper.toDomain(savedEntity)).thenReturn(user);

        // When
        User result = userService.create(user);

        // Then
        assertThat(result).isEqualTo(user);
        verify(userRepository).save(entity);
    }

    @Test
    void create_shouldThrowException_whenUsernameExists() {
        // Given
        User user = User.builder()
                .username("existing")
                .email("new@example.com")
                .build();

        when(userRepository.existsByUsername("existing")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> userService.create(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username already exists");

        verify(userRepository, never()).save(any());
    }
}
```

## Step 8: API Testing

Test the REST endpoints:

```bash
# Create user
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "active": true
  }'

# Get user by ID
curl http://localhost:8080/api/users/1

# Get all users
curl http://localhost:8080/api/users

# Update user
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john.doe@example.com",
    "firstName": "Jonathan",
    "lastName": "Doe",
    "active": true
  }'

# Delete user
curl -X DELETE http://localhost:8080/api/users/1
```

## Step 9: Adding Validation

Enhance the model with Bean Validation:

```java
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    private String firstName;
    private String lastName;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

Enable validation in the controller:

```java
import jakarta.validation.Valid;

@PostMapping
public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
    User createdUser = userService.create(user);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
}
```

## When to Graduate to Hexagonal Architecture

Consider migrating from CRUD to hexagonal when:

1. **Business logic complexity increases**: Validation rules, state transitions, invariants
2. **Multiple adapters needed**: REST + GraphQL + messaging + scheduled jobs
3. **Domain events required**: Publish events for other bounded contexts
4. **Testing becomes difficult**: Need to mock infrastructure in domain tests
5. **Team grows**: Clear boundaries help with parallel development

Migration path:

```bash
# 1. Extract domain logic to a proper aggregate
# 2. Generate hexagonal module
spring-hex make:module User

# 3. Migrate business logic from UserService to command handlers
# 4. Keep existing controller temporarily, redirect to command bus
# 5. Replace controller with new hexagonal controller
# 6. Remove old CRUD files
```

## Key Takeaways

- Use CRUD for simple resources without complex domain logic
- The CRUD generator creates a complete MVC stack in seconds
- Customize paths in `.hex/config.yml` to match your conventions
- Use `--no-model` and `--no-service` flags for simpler structures
- CRUD and hexagonal modules can coexist in the same project
- Graduate to hexagonal architecture when domain complexity warrants it

## Next Steps

1. Add validation constraints to your models
2. Implement pagination for list endpoints
3. Add filtering and sorting capabilities
4. Create integration tests with TestContainers
5. Add API documentation with SpringDoc OpenAPI
6. Implement soft deletes instead of hard deletes

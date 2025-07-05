# User Service

## Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Technologies Used](#technologies-used)
- [Setup Instructions](#setup-instructions)
  - [Prerequisites](#prerequisites)
  - [Steps](#steps)
- [API Endpoints](#api-endpoints)
  - [Authentication](#authentication)
  - [Role Management](#role-management)
- [Database Migrations](#database-migrations)
- [Testing](#testing)
- [License](#license)

## Overview
User Service is a Spring Boot application designed to manage user authentication, authorization, and role-based access control.
It provides APIs for user signup, login, logout, and role management. The service uses JWT for token-based authentication and supports admin functionalities for managing user roles.
It uses the jjwt library for token generation and validation (GitHub repository: [jjwt](https://github.com/jwtk/jjwt)), and Flyway for database migrations.

## Features
- User registration and login
- User authentication and authorization
- Token validation
- User logout
- Role-based access control
- Admin functionalities for role management

## Technologies Used
- Java
- Spring Boot
- Spring Security
- Hibernate
- Flyway for database migrations
- MySQL

## Setup Instructions

### Prerequisites
- Java 17 or higher
- Maven
- MySQL database

### Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/spa-raj/userservice.git
   ```
2. Navigate to the project directory:
   ```bash
   cd userservice
   ```
3. Configure the database and environment variables:
   - Update `application.properties` with your database credentials and other environment variables.
   - Example:
     ```properties
     spring.datasource.url=jdbc:mysql://localhost:3306/userservice
     spring.datasource.username=root
     spring.datasource.password=yourpassword
     spring.application.name=userservice
     server.port=8081
     spring.jpa.hibernate.ddl-auto=validate
     spring.jpa.show-sql=true
     spring.flyway.enabled=true
     spring.flyway.locations=classpath:db/migration
     admin.email=admin@example.com
     admin.password=adminpassword
     ```
4. Run Flyway migrations:
   ```bash
   mvn flyway:migrate
   ```
5. Build and run the application:
   ```bash
   mvn spring-boot:run
   ```

## API Endpoints

### Authentication
- **POST /auth/signup**
  - Description: Register a new user.
  - Request Body:
    ```json
    {
      "email":"rohit2@gmail.com",
      "name": "Rohit",
      "password":"abc@1234",
      "phone":"8585445435",
      "role":"seller"
    }
    ```
  - Response:
    ```json
    {
      "userEmail": "rohit2@gmail.com",
      "name": "Rohit ",
      "phone": "8585445435",
      "role": "Role{name='SELLER', description='The seller/merchant of our products.'}"
    }
    ```

- **POST /auth/login**
  - Description: Authenticate a user and return a token.
  - Request Body:
    ```json
    {
      "email":"rohit2@gmail.com",
      "password":"abc@1234"
    }
    ```
  - Response:
    ```json
    {
      "status": 200,
      "token": "eyJraWQiOiIyMmNiZjZlZS1ij......",
      "sessionId": "fc4f9769-6683-412a-9865-a094a7a2151e"
    }
    ```

- **POST /auth/logout**
  - Description: Logout a user and invalidate the token.
  - Request Body:
    ```json
    {
      "userEmail": "rohit2@gmail.com",
      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    }
    ```
  - Response:
    ```json
    {
      "status": 204,
      "message": "No Content"
    }
    ```

- **POST /auth/validate**
  - Description: Validate a user's token.
  - Request Headers:
    ```json
    {
      "Authorization": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    }
    ```
  - Response:
    ```json
    {
      "status": 200,
      "email": "rohit2@gmail.com",
      "name": "Rohit ",
      "phone": "8585445435",
      "roles": [
        "SELLER"
      ]
    }
    ```

### Role Management
- **POST /roles/create**
  - Description: Create a new role (Only Admins can create a new role).
  - Request Body:
    ```json
    {
      "roleName": "BUYER",
      "description": "This role is for buyers who purchase products."
    }
    ```
  - Response:
    ```json
    {
      "status": 201,
      "roleName": "BUYER",
      "description": "This role is for buyers who purchase products.",
      "message": "Role created successfully"
    }
    ```
- **POST /roles/update**
  - Description: Update an existing role (Only Admins can update a role).
  - Request Body:
    ```json
    {
      "roleName": "BUYER",
      "description": "Updated description for the buyer role."
    }
    ```
  - Response:
    ```json
    {
      "status": 200,
      "roleName": "BUYER",
      "description": "Updated description for the buyer role.",
      "message": "Role updated successfully"
    }
    ```
    
- **GET /roles**
  - Description: Get all roles. (Only Admins can access this endpoint).
    - Request Headers:
      ```json
      {
        "Authorization": "eydsfbakfjjdsf...."
      }
      ```
    - Response:
      ```json
        {
            "status": 200,
            "roles": [
            {
                "roleId": "fsad5sfsas....",
                "name": "SELLER",
                "description": "The seller/merchant of our products."
            },
            {
                "roleId": "ghwafe412sa3w....",
                "name": "BUYER",
                "description": "This role is for buyers who purchase products."
            }
            ]
        }
      ```
- **GET /roles/{roleId}**
  - Description: Get details of a specific role by its ID (Only Admins can access this endpoint).
  - Response:
    ```json
    {
      "status": 200,
      "role": {
        "roleId": "fsad5sfsas....",
        "name": "SELLER",
        "description": "The seller/merchant of our products."
      }
    }
    ```
## Testing

The application includes comprehensive unit and integration tests to ensure functionality and reliability. Below are the key testing points:

### Controller Tests
- **AuthControllerTest**: Validates authentication endpoints such as signup, login, logout, and token validation.
- **AuthControllerMVCTest**: Performs mock MVC tests for authentication endpoints.
- **RoleControllerTest**: Tests role management endpoints including role creation and updates.
- **RoleControllerMVCTest**: Performs mock MVC tests for role management endpoints.

### Service Tests
- **AuthServiceTest**: Tests the authentication service logic, including token generation and validation.
- **KeyLocatorImplTest**: Validates the key locator implementation used for JWT operations.
- **RoleServiceTest**: Tests the role service logic, including role creation and updates.

## Database Migrations
Flyway is used for managing database schema migrations. Migration scripts are located in `src/main/resources/db/migration`.

## License
This project is licensed under the Apache-2.0 License. See the LICENSE file for details.
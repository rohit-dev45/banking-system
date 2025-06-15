
# Banking System Backend

## Timeline
**Jan 2025 – Mar 2025**

## Description
- Developed a Spring Boot application with RESTful APIs using Spring MVC to manage 500+ daily banking transactions with 99% uptime.
- Optimized Hibernate-MySQL queries using collections (hash maps) for transaction lookups, reducing latency by 30%.
- Implemented Spring Security for authentication and JUnit for unit testing, achieving 90% code coverage, ensuring compliance with banking standards in a team of 4.

## Technologies Used
- Java
- Spring Boot (MVC, Security, JPA)
- MySQL
- Maven
- JUnit

## Features
- RESTful APIs for transactions
- User authentication using Spring Security
- Hibernate ORM with optimized query lookup
- Unit tested with 90% code coverage

## Run Instructions
1. Ensure MySQL is running and database `bankdb` is created.
2. Run with:
   ```bash
   mvn spring-boot:run
   ```

3. Use Basic Auth for `/api/transactions` endpoint access.

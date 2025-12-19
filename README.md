# TimeLapse Store API

Spring Boot REST API for a simple e-commerce catalog and checkout flow. Built with concepts of REST, JDBC, MySQL, and JWT-based authentication.

---

## Features
- JWT login/registration endpoints with Spring Security.
- Product catalog search with category, price range, and subcategory filters.
- Shopping cart stored in MySQL; checkout creates orders and line items.
- Profile read/update for the authenticated user.
- Role-based admin endpoints for product and category management.
- Starter unit/integration tests using JUnit 5 and Spring Test.

## Tech Stack
- Java 17, Spring Boot 2.7.x
- Spring Security + JWT
- JDBC via Apache DBCP2 `BasicDataSource`
- MySQL 8+
- Maven

## Project Layout
```
capstone-api-starter/
|-- database/                          # SQL bootstrap scripts
|   |-- create_database_recordshop.sql # default schema + seed data used by the app
|   |-- create_database_easyshop.sql   # alternative sample datasets
|   |-- create_database_groceryapp.sql
|   |-- create_database_clothingstore.sql
|   `-- create_database_videogamestore.sql
|-- src/
|   |-- main/
|   |   |-- java/org/yearup/
|   |   |   |-- configurations/          # DataSource configuration
|   |   |   |-- controllers/             # REST endpoints
|   |   |   |-- data/                    # DAO interfaces and MySQL implementations
|   |   |   |-- models/                  # Domain models
|   |   |   |-- security/                # Security + JWT utilities
|   |   |   `-- TimeLapseStoreApplication.java
|   |   `-- resources/
|   |       |-- application.properties   # Database + JWT configuration
|   |       `-- banner.txt
|   `-- test/
|       |-- java/                        # Unit/integration tests
|       `-- resources/                   # Test DB config and seed data
`-- pom.xml
```

## Setup
1) **Create and seed the database**  
   - Default script: `database/create_database_recordshop.sql` (includes users, products, categories, cart items).  
   - Run with MySQL client, e.g.:
   ```bash
   mysql -u root -p < database/create_database_recordshop.sql
   ```
   - Seeded users include `user`, `admin`, and `george` with bcrypt-hashed passwords; update the script if you need known plaintext credentials.

2) **Configure the application** in `src/main/resources/application.properties`:
   ```properties
   datasource.url=jdbc:mysql://localhost:3306/recordshop
   datasource.username=your_mysql_user
   datasource.password=your_mysql_password

   jwt.secret=change_me_to_a_long_random_value
   jwt.token-timeout-seconds=108000
   # server.port=8080  # uncomment to override the default
   ```
   The app uses `datasource.*` keys (not Spring Boot's default `spring.datasource.*`), wired via `DatabaseConfig`.

3) **Run locally**
   ```bash
   ./mvnw spring-boot:run
   # or
   ./mvnw clean package && java -jar target/*.jar
   ```
   The API listens on port `8080` by default.

## API Surface (high level)
- **Auth**  
  - `POST /login` - returns `Authorization: Bearer <jwt>` and user info.  
  - `POST /register` - create user + empty profile.
- **Catalog** (public)  
  - `GET /products?cat=1&minPrice=10&maxPrice=50&subCategory=Rock`  
  - `GET /products/{id}`  
  - `GET /categories`  
  - `GET /categories/{id}`  
  - `GET /categories/{id}/products`
- **Admin** (requires `ROLE_ADMIN` bearer token)  
  - `POST /products`, `PUT /products/{id}`, `DELETE /products/{id}`  
  - `POST /categories`, `DELETE /categories/{id}` (update endpoint is not implemented yet)
- **Profile & Cart** (requires bearer token)  
  - `GET /profile`, `PUT /profile`  
  - `GET /cart`, `POST /cart/products/{productId}` (adds or increments), `DELETE /cart`  
  - `POST /orders` - checkout current cart and create order + line items



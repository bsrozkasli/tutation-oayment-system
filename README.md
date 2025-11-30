# Tuition Payment System API

University Tuition Payment System - REST API Project

## 🚀 Project Status

✅ **Project is operational and production-ready!**

## 📋 Features

### API Endpoints

#### Mobile App
- `GET /api/v1/tuition/{studentNo}` - Query Tuition Status (No Auth, Rate Limited: 3 per student per day)

#### Banking App
- `GET /api/v1/banking/tuition/{studentNo}` - Query Tuition Status (Auth Required)
- `POST /api/v1/payment` - Pay Tuition (No Auth)

#### Admin
- `POST /api/v1/admin/add-tuition` - Add Single Tuition (Auth Required)
- `POST /api/v1/admin/batch-upload` - Batch Upload CSV (Auth Required)
- `GET /api/v1/admin/unpaid-status` - Unpaid Tuition Status (Auth Required, Paging)

#### Authentication
- `POST /api/v1/auth/login` - Login (Get JWT Token)

## 🏗️ Architecture & Design

### System Architecture

The application follows a **layered architecture** pattern with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────┐
│                    Client Layer                          │
│  (Mobile App / Banking App / Admin Web)                 │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│                 API Gateway Layer                        │
│  (Spring Security / CORS / Rate Limiting)               │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│                Controller Layer                          │
│  (TuitionController / AdminController / AuthController) │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│                 Service Layer                            │
│  (TuitionService / RateLimitingService)                 │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│              Repository Layer                            │
│  (StudentRepository / TuitionRepository /               │
│   PaymentRepository)                                     │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│              Database Layer                              │
│  (PostgreSQL - Azure Database)                          │
└─────────────────────────────────────────────────────────┘
```

### Component Layers

1. **Controller Layer** (`controller/`)
   - Handles HTTP requests and responses
   - Validates input data
   - Delegates business logic to service layer

2. **Service Layer** (`service/`)
   - Contains business logic
   - Handles transactions
   - Processes payments and tuition calculations

3. **Repository Layer** (`repository/`)
   - Data access abstraction
   - Custom queries for complex operations
   - Spring Data JPA repositories

4. **Entity Layer** (`entity/`)
   - Domain models
   - JPA entities mapping to database tables
   - Relationships between entities

5. **Configuration Layer** (`config/`)
   - Security configuration
   - JWT authentication setup
   - CORS configuration
   - Logging filters

6. **Utility Layer** (`util/`)
   - JWT token utilities
   - Helper classes

### Security Architecture

```
Request Flow:
1. Client Request
   ↓
2. LoggingFilter (Logs request/response)
   ↓
3. JwtAuthFilter (Validates JWT token if present)
   ↓
4. SecurityConfig (Authorization checks)
   ↓
5. Controller (Processes request)
```

### Database Schema (ER Diagram)

```
┌─────────────────────┐
│      STUDENTS       │
├─────────────────────┤
│ PK student_no (VARCHAR) │
│     name (VARCHAR)      │
└──────────┬──────────┘
           │
           │ 1:N
           │
┌──────────▼──────────┐      ┌─────────────────┐
│      TUITIONS       │      │     PAYMENTS    │
├─────────────────────┤      ├─────────────────┤
│ PK id (BIGINT)      │      │ PK id (BIGINT)  │
│ FK student_no       │      │ FK student_no   │
│     term (VARCHAR)  │      │     term (VARCHAR)│
│     amount (DOUBLE) │      │     amount (DOUBLE)│
│     balance (DOUBLE)│      │     payment_date │
└─────────────────────┘      │      (TIMESTAMP) │
                              └─────────────────┘
                                     │
                                     │ N:1
                                     │
                              ┌──────▼──────────┐
                              │    STUDENTS     │
                              └─────────────────┘
```

### Entity Relationships

- **Student** (1) ──< (N) **Tuition**: One student can have multiple tuition records
- **Student** (1) ──< (N) **Payment**: One student can have multiple payment records
- **Tuition** and **Payment** are linked through **Student** (student_no)

### Key Design Patterns

1. **Repository Pattern**: Data access abstraction
2. **Service Layer Pattern**: Business logic encapsulation
3. **DTO Pattern**: Data transfer objects for API requests/responses
4. **Filter Pattern**: Request/response logging and authentication
5. **Singleton Pattern**: Configuration beans

## 🛠️ Technologies

- **Java 21**
- **Spring Boot 3.4.12**
- **Spring Security** (JWT Authentication)
- **PostgreSQL** (Azure Database)
- **Spring Data JPA**
- **Swagger/OpenAPI 3**
- **Maven**
- **Lombok** (Code generation)

## 📦 Installation

### Requirements
- Java 21
- Maven 3.6+
- PostgreSQL (or Azure PostgreSQL)

### Local Development

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd tuition-payment-system
   ```

2. **Configure database settings**
   - Edit `src/main/resources/application.properties` file
   - Or use environment variables:
     ```bash
     export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/tuition_db
     export SPRING_DATASOURCE_USERNAME=your_username
     export SPRING_DATASOURCE_PASSWORD=your_password
     ```

3. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   # or
   mvn spring-boot:run
   ```

4. **Access Swagger UI**
   - http://localhost:8080/swagger-ui.html

## 🧪 Testing

### Method 1: Postman Collection
1. Open Postman
2. Import `postman_collection.json` file
3. Set `baseUrl` variable (default: http://localhost:8080)
4. Run "Login" request (JWT token will be automatically saved)
5. Test other endpoints

### Method 2: VS Code REST Client
1. Install REST Client extension in VS Code
2. Open `test-api.http` file
3. Set `@baseUrl` variable
4. Click "Send Request" button above each request

### Method 3: Swagger UI
1. Start the application
2. Go to http://localhost:8080/swagger-ui.html
3. Click "Authorize" button and enter JWT token
4. Test endpoints

### Method 4: cURL
```bash
# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Query Tuition (Mobile App)
curl http://localhost:8080/api/v1/tuition/2023001

# Query Tuition (Banking App - Auth Required)
curl http://localhost:8080/api/v1/banking/tuition/2023001 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## ☁️ Azure Deployment

See `DEPLOYMENT_GUIDE.md` for detailed deployment guide.

### Quick Deployment

1. **Build JAR file**
   ```bash
   mvn clean package
   ```

2. **Deploy to Azure App Service**
   - Via Azure Portal or
   - Via Azure CLI (see DEPLOYMENT_GUIDE.md for details)

3. **Set Environment Variables**
   - `SPRING_DATASOURCE_URL`
   - `SPRING_DATASOURCE_USERNAME`
   - `SPRING_DATASOURCE_PASSWORD`

## 📊 API Documentation

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## 🔐 Authentication

### Default Admin Credentials
- Username: `admin`
- Password: `admin123`

**Note:** Must be changed in production!

### JWT Token Usage
1. Make POST request to `/api/v1/auth/login` endpoint
2. Get `token` value from response
3. Add to header in subsequent requests:
   ```
   Authorization: Bearer <token>
   ```

## 📝 Rate Limiting

- **Mobile App Query Tuition**: 3 requests per student per day
- When rate limit exceeded: `429 Too Many Requests`

## 📁 CSV Batch Upload Format

CSV file format:
```csv
studentNo,term,amount
2023001,2025-SUMMER,4500
2023002,2025-SUMMER,5500
```

## 📊 Logging

All API requests are logged:
- Request: Method, Path, IP, Headers, Timestamp, Auth Status
- Response: Status Code, Latency, Response Size

Logs are displayed in console.

## 🗄️ Database Schema

### Tables

- **students**: Student information
  - `student_no` (PK): Student number
  - `name`: Student name

- **tuitions**: Tuition fee records
  - `id` (PK): Auto-generated ID
  - `student_no` (FK): Reference to students table
  - `term`: Academic term (e.g., "2024-FALL")
  - `amount`: Total tuition amount
  - `balance`: Remaining balance

- **payments**: Payment records
  - `id` (PK): Auto-generated ID
  - `student_no` (FK): Reference to students table
  - `term`: Academic term
  - `amount`: Payment amount
  - `payment_date`: Payment timestamp



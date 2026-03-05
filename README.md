# Tresvita Todo Backend

**Managed by Wissen Team**

A Java Spring Boot REST API for the Tresvita Todo Application. This backend service provides CRUD operations for managing todo items with H2/PostgreSQL database support.

## 🚀 Features

- ✅ RESTful API for Todo management
- ✅ CRUD operations (Create, Read, Update, Delete)
- ✅ Search and filter todos
- ✅ Toggle completion status
- ✅ H2 Database for development
- ✅ PostgreSQL support for production
- ✅ Spring Boot Actuator for health checks and metrics
- ✅ Docker containerization
- ✅ Kubernetes ready

## 🛠️ Technology Stack

- **Java**: 17 (Eclipse Temurin)
- **Spring Boot**: 3.2.x
- **Spring Data JPA**: Data persistence
- **H2 Database**: In-memory database for development
- **PostgreSQL**: Production database
- **Maven**: Build tool
- **Docker**: Containerization
- **Lombok**: Boilerplate code reduction

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.9+
- Docker (for containerization)

## 🚀 Getting Started

### Local Development

1. **Clone the repository:**
   ```bash
   git clone https://github.com/chilukavinayak/todo-backend-eks.git
   cd todo-backend-eks
   ```

2. **Build the project:**
   ```bash
   ./mvnw clean package
   ```

3. **Run the application:**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Access the application:**
   - API Base URL: http://localhost:8080/api
   - H2 Console: http://localhost:8080/api/h2-console
   - Health Check: http://localhost:8080/api/actuator/health

### Docker Build

1. **Build Docker image:**
   ```bash
   docker build -t tresvita-todo-backend:latest .
   ```

2. **Run Docker container:**
   ```bash
   docker run -p 8080:8080 tresvita-todo-backend:latest
   ```

## 📡 API Endpoints

### Todo Operations

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/todos` | Get all todos |
| GET | `/api/todos/{id}` | Get todo by ID |
| POST | `/api/todos` | Create a new todo |
| PUT | `/api/todos/{id}` | Update a todo |
| DELETE | `/api/todos/{id}` | Delete a todo |
| GET | `/api/todos/completed/{completed}` | Get todos by completion status |
| GET | `/api/todos/search?title={title}` | Search todos by title |
| PATCH | `/api/todos/{id}/toggle` | Toggle todo completion status |

### Health & Monitoring

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/actuator/health` | Health check endpoint |
| GET | `/api/actuator/info` | Application info |
| GET | `/api/actuator/metrics` | Application metrics |
| GET | `/api/actuator/prometheus` | Prometheus metrics |

### Example Requests

**Create a Todo:**
```bash
curl -X POST http://localhost:8080/api/todos \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Complete EKS setup",
    "description": "Setup EKS cluster for Tresvita",
    "completed": false
  }'
```

**Get All Todos:**
```bash
curl http://localhost:8080/api/todos
```

**Update a Todo:**
```bash
curl -X PUT http://localhost:8080/api/todos/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Complete EKS setup",
    "description": "Setup EKS cluster for Tresvita - Updated",
    "completed": true
  }'
```

**Delete a Todo:**
```bash
curl -X DELETE http://localhost:8080/api/todos/1
```

**Toggle Completion:**
```bash
curl -X PATCH http://localhost:8080/api/todos/1/toggle
```

## 🧪 Testing

### Run Unit Tests

```bash
./mvnw test
```

### Run Tests with Coverage

```bash
./mvnw clean test jacoco:report
```

Coverage report will be generated at `target/site/jacoco/index.html`

## 🔧 Configuration

### Application Profiles

- **dev** (default): H2 in-memory database, debug logging
- **prod**: PostgreSQL database, INFO logging

### Environment Variables (Production)

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_HOST` | Database host | localhost |
| `DB_PORT` | Database port | 5432 |
| `DB_NAME` | Database name | todo_db |
| `DB_USER` | Database username | postgres |
| `DB_PASSWORD` | Database password | password |
| `SERVER_PORT` | Application port | 8080 |
| `JAVA_OPTS` | JVM options | -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 |

## 🐳 Docker

### Build Image

```bash
docker build -t tresvita-todo-backend:latest .
```

### Run with Environment Variables

```bash
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=postgres \
  -e DB_PORT=5432 \
  -e DB_NAME=todo_db \
  -e DB_USER=postgres \
  -e DB_PASSWORD=secret \
  tresvita-todo-backend:latest
```

## ☸️ Kubernetes Deployment

The application is designed to run on AWS EKS. Use the Helm charts in the infrastructure repository:

```bash
# Deploy to EKS
helm upgrade --install todo-backend ./helm_charts/todo-backend \
  --namespace backend \
  --set image.repository=<ecr-repo>/tresvita-todo-backend \
  --set image.tag=v1.0.0
```

## 📁 Project Structure

```
todo-backend-eks/
├── src/
│   ├── main/
│   │   ├── java/com/tresvita/todo/
│   │   │   ├── TodoApplication.java
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   │   └── TodoController.java
│   │   │   ├── model/
│   │   │   │   └── Todo.java
│   │   │   ├── repository/
│   │   │   │   └── TodoRepository.java
│   │   │   ├── service/
│   │   │   │   ├── TodoService.java
│   │   │   │   └── TodoNotFoundException.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       └── java/com/tresvita/todo/
├── Dockerfile
├── pom.xml
└── README.md
```

## 🔒 Security

- Non-root user in Docker container
- Read-only root filesystem
- Security headers enabled
- CORS configured for cross-origin requests

## 📞 Support

For support, contact the **Wissen Team**.

---

**Client**: Tresvita  
**Managed by**: Wissen Team  
**Version**: 1.0.0  
**Last Updated**: 2024

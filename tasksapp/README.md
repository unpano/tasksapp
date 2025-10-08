# Tasks App

Spring Boot REST API for task management with pagination, filtering, and sorting capabilities.

## Features

- CRUD operations for tasks
- Pagination support
- Filter by status and priority
- Search by title/description
- Sort by multiple fields
- H2 in-memory database with sample data
- CORS enabled for frontend integration

## Tech Stack

- Java 21
- Spring Boot 3.5.6
- Spring Data JPA
- H2 Database
- MapStruct
- Lombok
- Maven

## API Endpoints

### Get Tasks (with pagination)
```
GET /api/tasks?page=0&size=5
GET /api/tasks?status=OPEN&priority=HIGH
GET /api/tasks?q=search&sort=dueDate,ASC
```

### Create Task
```
POST /api/tasks
{
  "title": "Task title",
  "description": "Description",
  "priority": "HIGH",
  "dueDate": "2025-10-15"
}
```

### Update Task
```
PATCH /api/tasks/{id}
{
  "status": "IN_PROGRESS"
}
```

### Delete Task
```
DELETE /api/tasks/{id}
```

## Running Locally

```bash
./mvnw spring-boot:run
```

App runs on `http://localhost:8080`

## Docker

Build image:
```bash
docker build -t tasksapp .
```

Run container:
```bash
docker run -d -p 8080:8080 --name tasksapp tasksapp
```

## H2 Console

Access at `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:taskdb`
- Username: `username`
- Password: `pass`

## Sample Data

25 tasks are automatically loaded on startup for pagination testing.

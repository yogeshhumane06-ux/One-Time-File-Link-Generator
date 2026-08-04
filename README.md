# One-Time-File-Link-Generator
## Project Description
This is a Spring Boot application that allows users to upload a file and generate a one-time download link. The file can be downloaded only once, making it secure for file sharing.

## Features
- Upload files
- Generate one-time download link
- Download file only once
- File expiry support
- REST API
- Simple HTML frontend

## Technologies Used
- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA
- Maven
- HTML
- CSS
- JavaScript

## Project Structure
```
src
 ├── main
 │   ├── java
 │   ├── resources
 │   │   ├── static
 │   │   └── application.properties
 └── test
```

## How to Run
1. Clone the repository.
2. Open the project in Eclipse or IntelliJ.
3. Run `OneTimeFileLinkApplication`.
4. Open the browser.
5. Visit:
```
http://localhost:8080/
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/files/upload | Upload File |
| GET | /api/files/view/{token} | View File |
| GET | /api/files/download/{token} | Download File |

# Institution Project Hub

A robust, secure, and professional academic web application designed for higher education institutions. **Institution Project Hub** enables students to submit and manage academic projects, allows faculty members to evaluate and govern student submissions, and features an integrated AI Assistant powered by **Groq API** and a grounded **Retrieval-Augmented Generation (RAG)** knowledge base of senior institutional projects.

---

## Table of Contents
1. [Project Overview](#project-overview)
2. [Key Features](#key-features)
3. [Technology Stack](#technology-stack)
4. [Architecture](#architecture)
5. [SQLite & Persistence](#sqlite--persistence)
6. [Groq API Configuration](#groq-api-configuration)
7. [RAG Architecture & Knowledge Base](#rag-architecture--knowledge-base)
8. [File Format: senior_projects.txt](#file-format-senior_projectstxt)
9. [RAG Reindexing Process](#rag-reindexing-process)
10. [Local Development](#local-development)
11. [AWS EC2 Deployment](#aws-ec2-deployment)
12. [REST API Endpoints](#rest-api-endpoints)
13. [Security & Permissions](#security--permissions)
14. [Project Directory Structure](#project-directory-structure)
15. [Future Cloud Migration (AWS S3)](#future-cloud-migration-aws-s3)

---

## 1. Project Overview

Higher education institutions need a centralized, organized repository for academic capstone, thesis, and coursework projects. At the same time, students contemplating project topics frequently struggle to know whether a senior has already built something similar.

**Institution Project Hub** solves this by:
- Providing role-based portals for students and faculty.
- Enforcing granular permissions on source code archives and deployed live links.
- Offering an intelligent, grounded AI assistant that searches previous senior projects using RAG while strictly querying the SQLite database for live project counts and statistics.

---

## 2. Key Features

### For Students:
- **Authentication**: Secure registration and login with JWT and BCrypt.
- **Dashboard**: Track personal submission metrics (total, approved, pending review).
- **Project Submission**: Upload project title, description, source code (.ZIP), and deployed project link.
- **Granular Permissions**:
  - Source Code Visibility: `ALL_STUDENTS` or `FACULTY_ONLY`.
  - Deployed Link Visibility: `ALL_STUDENTS` or `FACULTY_ONLY`.
  - Permissions are strictly enforced on the Spring Boot backend, preventing unauthorized downloads.
- **Project Management**: Edit existing project details or replace source code archives.
- **AI Assistant**: Check if a similar project was already built by seniors or query project statistics.

### For Faculty:
- **Governance Dashboard**: Real-time metrics on Total Projects, Registered Students, Submitted/Pending Submissions, Approved Projects, and Rejected Projects.
- **Audit & Search**: Filter projects by status (`SUBMITTED`, `APPROVED`, `REJECTED`) and search across student names, emails, and project descriptions.
- **Code Inspection**: Download student source code archives with unrestricted faculty authorization.
- **Review & Decision**: Approve or reject student submissions with status updates.
- **RAG Knowledge Base Management**: Trigger reindexing of `senior_projects.txt` with a single click.

### Grounded AI Assistant:
- Small, unobtrusive `[ AI ]` floating launcher at the bottom-right corner.
- **RAG Intent Routing**: Semantic search across indexed senior projects using vector embeddings and cosine similarity.
- **Database Intent Routing**: Exact SQLite counts for queries like *"How many projects are uploaded?"* or *"How many approved projects?"*.
- **Anti-Hallucination & Grounding**:
  - The AI assistant **never invents** senior projects or fake project numbers.
  - If a similar project is found, it explains the title, batch, authors, why it is similar, and recommendations to differentiate.
  - If no similar project is found, it clearly states: *"I couldn't find a sufficiently similar project in the current senior-project database."*
  - It **never claims** *"No one in the college has ever done this"*.

---

## 3. Technology Stack

- **Backend**:
  - Java 21 LTS
  - Spring Boot 3.3.4 (Spring Web, Spring Security, Spring Data JPA, Validation)
  - SQLite JDBC Driver (`org.xerial:sqlite-jdbc`)
  - Hibernate Community Dialects (`org.hibernate.orm:hibernate-community-dialects`)
  - JSON Web Tokens (JJWT 0.12.6)
  - Apache Maven
- **Frontend**:
  - Semantic HTML5
  - Vanilla CSS3 (Clean, standard institutional theme; no Tailwind, no 3D/glassmorphism)
  - Vanilla JavaScript (ES6+ modular design, fetch interceptors; no React or complex frameworks)
- **AI & RAG Engine**:
  - Groq Cloud API (`llama-3.3-70b-versatile` / `llama-3.1-8b-instant`)
  - Modular `EmbeddingService` with high-dimensional normalized n-gram semantic vectorizer
  - SQLite-backed vector storage with cosine similarity ranking

---

## 4. Architecture

```
                      +-----------------------------+
                      |   Vanilla HTML / CSS / JS   |
                      | (Student & Faculty Portals) |
                      +--------------+--------------+
                                     |
                         HTTPS / REST API (JWT)
                                     |
                      +--------------v--------------+
                      |     Spring Security / JWT   |
                      +--------------+--------------+
                                     |
             +-----------------------+-----------------------+
             |                                               |
+------------v------------+                     +------------v------------+
|    Project Controller   |                     |      AI Controller      |
|  (File upload & CRUD)   |                     |     (POST /api/ai/chat) |
+------------+------------+                     +------------+------------+
             |                                               |
+------------v------------+                     +------------v------------+
|     StorageService      |                     |      AIOrchestrator     |
| (Local uploads/projects)|                     +----+---------------+----+
+------------+------------+                          |               |
             |                         [Statistics]  |               | [Semantic Similarity]
+------------v------------+            +-------------v-----+   +-----v-------------+
|    SQLite Database      |<-----------| ProjectStatistics |   |     RAGService    |
| (project_hub.db)        |            |      Service      |   | (Vector Similarity|
+-------------------------+            +-------------------+   |   from SQLite)    |
                                                               +---------+---------+
                                                                         |
                                                                         | Grounded Context
                                                                         v
                                                               +-------------------+
                                                               |    GroqService    |
                                                               | (api.groq.com LLM)|
                                                               +-------------------+
```

---

## 5. SQLite & Persistence

The application uses SQLite as its primary relational and vector storage database:
- **File**: `project_hub.db` (auto-created in root directory upon launch).
- **Driver**: `org.sqlite.JDBC`.
- **Dialect**: `org.hibernate.community.dialect.SQLiteDialect`.
- **Tables**:
  - `users`: Stores institutional user profiles, BCrypt password hashes, and roles (`STUDENT`, `FACULTY`).
  - `projects`: Stores project metadata, student foreign key, file path, size, visibility settings, and status.
  - `knowledge_documents`: Stores normalized content, metadata, and JSON-serialized vector embeddings from `senior_projects.txt`.

---

## 6. Groq API Configuration

The application integrates with Groq's high-speed LPU inference API.
In accordance with strict security standards:
- The Groq API key is **never** placed inside frontend code, HTML, CSS, JavaScript, or Git-tracked files.
- The backend reads the key from the `GROQ_API_KEY` environment variable.

### Setting your Groq API Key:

#### Windows (PowerShell):
```powershell
$env:GROQ_API_KEY="gsk_your_actual_groq_api_key_here"
```

#### Windows (CMD):
```cmd
set GROQ_API_KEY=gsk_your_actual_groq_api_key_here
```

#### Linux / macOS:
```bash
export GROQ_API_KEY=gsk_your_actual_groq_api_key_here
```

> **Note**: If `GROQ_API_KEY` is omitted or empty, the application will not crash. Instead, the AI Assistant will operate in local mode, returning exact SQL statistics and retrieved RAG senior project matches with a clear note explaining that live LLM synthesis is offline until the key is set.

---

## 7. RAG Architecture & Knowledge Base

To prevent model hallucination and ensure students receive authentic information about previously completed senior capstone projects:

1. **Knowledge Ingestion**:
   - `senior_projects.txt` contains human-readable senior project records.
   - `SeniorProjectsParser` parses records delimited by `---`.
2. **Embedding Generation**:
   - `EmbeddingService` generates high-dimensional (256-d) normalized vector representations covering unigrams, bigrams, and character trigrams for robust typo and phrase matching.
3. **Vector Storage**:
   - Vectors are stored directly inside SQLite in `knowledge_documents.embedding_json`.
4. **Retrieval**:
   - When an inquiry arrives, the query is embedded into a vector.
   - Cosine similarity is computed against all documents.
   - Top-K results above the threshold (score >= 0.22) are passed into the context window.
5. **Generation**:
   - Groq synthesizes a grounded response explaining similarities, differences, and recommendations.

---

## 8. File Format: senior_projects.txt

The file `senior_projects.txt` is located in the project root. It is human-readable and formatted as follows:

```
PROJECT
Title: Campus Mate
Batch: A06
Authors: Priya Sharma, Rahul Verma
Description: AI based college assistance chatbot system designed to answer campus queries, library timings, and course registrations.
Technologies: Java, Spring Boot, MySQL, NLP
---
PROJECT
Title: AI Interview Simulation
Batch: A07
Authors: Rohan Mehta, Ananya Iyer
Description: Generative AI interview simulator providing real-time question generation, candidate speech evaluation, and feedback scoring.
Technologies: Java, Python, OpenAI, Groq
---
```

A sample reference file with populated senior projects is provided in `senior_projects_sample.txt`.

---

## 9. RAG Reindexing Process

When new senior projects are added to `senior_projects.txt`:
1. **Via UI**: Log in as Faculty, open the **Faculty Dashboard**, and click **"Reindex Senior Projects (RAG)"**.
2. **Via REST API**: Send an authenticated POST request:
   ```http
   POST /api/rag/reindex
   Authorization: Bearer <FACULTY_JWT_TOKEN>
   ```
   **Response**:
   ```json
   {
     "documentsProcessed": 5,
     "status": "SUCCESS",
     "message": "Successfully processed and indexed 5 senior project documents."
   }
   ```
The reindexer clears old knowledge documents to prevent duplicates and recomputes all embeddings atomically.

---

## 10. Local Development

### Prerequisites:
- **Java 21 LTS** installed (`java -version`)
- **Port 8080** available (or configure `SERVER_PORT`)
- Standard terminal (PowerShell, Command Prompt, or Bash)

### Step 1: Configure Environment Variables
Copy the template `.env.example` to create your local `.env`:
```powershell
# On Windows PowerShell:
Copy-Item .env.example .env

# On Linux / macOS / Git Bash:
cp .env.example .env
```
Edit `.env` and supply your Groq API key:
```ini
GROQ_API_KEY=your_groq_api_key_here
GROQ_MODEL=qwen/qwen3.8-27b
SERVER_PORT=8080
PROJECT_HUB_DB_PATH=./project_hub.db
PROJECT_HUB_UPLOAD_DIR=./uploads/projects/
```
*(Note: If `GROQ_API_KEY` is omitted or empty, the application runs gracefully in local fallback mode, providing exact database statistics and RAG search.)*

### Step 2: Build the Project
```bash
# Windows:
.\mvnw.cmd clean package -DskipTests

# Linux / macOS:
./mvnw clean package -DskipTests
```

### Step 3: Run the Application
You can run directly using Maven:
```bash
# Windows:
.\mvnw.cmd spring-boot:run

# Linux / macOS:
./mvnw spring-boot:run
```
Or execute the packaged executable JAR:
```bash
java -jar target/institution-project-hub-1.0.0.jar
```

### Step 4: Access the Web Application
Open your browser and navigate to:
```
http://localhost:8080/
```

### Step 5: Verify Health Endpoint
```bash
curl http://localhost:8080/actuator/health
# Response: {"status":"UP"}
```

### Default Institutional Test Accounts:
The system automatically seeds the database on first run:
| Role | Email | Password | Name |
|---|---|---|---|
| **Faculty** | `faculty@institution.edu` | `faculty123` | Dr. Robert Vance |
| **Student** | `student@institution.edu` | `student123` | Alex Mercer |
| **Student** | `student2@institution.edu` | `student123` | Sophia Lin |

---

## 11. AWS EC2 Deployment

Follow these application-specific steps after launching an Ubuntu EC2 instance (e.g., Ubuntu 24.04 or 22.04 LTS on `t2.micro` or `t3.micro`).

### 1. Prerequisites on EC2
Install OpenJDK 21 and Maven on Ubuntu:
```bash
sudo apt update && sudo apt upgrade -y
sudo apt install -y openjdk-21-jdk maven git
java -version
```

*(Recommended for `t2.micro` 1GB RAM instances)*: Set up a 2GB swap space to ensure Maven compiles and Spring Boot runs without Out-Of-Memory errors:
```bash
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

### 2. Persistent Storage Directories
SQLite database and user project uploads must be kept in persistent directories outside the application JAR:
```bash
sudo mkdir -p /var/lib/project-hub/uploads/projects
sudo chown -R ubuntu:ubuntu /var/lib/project-hub
sudo chmod -R 755 /var/lib/project-hub
```

### 3. Required Environment Variables
Configure the application environment variables in your deployment environment or systemd service:
| Variable | Recommended EC2 Value | Description |
|---|---|---|
| `PROJECT_HUB_DB_PATH` | `/var/lib/project-hub/project_hub.db` | Persistent SQLite database file |
| `PROJECT_HUB_UPLOAD_DIR` | `/var/lib/project-hub/uploads/projects/` | Persistent upload directory for project archives |
| `PROJECT_HUB_KNOWLEDGE_FILE` | `senior_projects.txt` | Path to RAG knowledge base text file |
| `GROQ_API_KEY` | `gsk_...` | Your secret Groq API key |
| `GROQ_MODEL` | `qwen/qwen3.8-27b` | Groq LLM model name |
| `SERVER_PORT` | `8080` | Application listening port |
| `JWT_SECRET` | *(64-char hex string)* | Production JWT signing key |

### 4. Build the Application
Clone or copy the project to `/home/ubuntu/Project_Hub`:
```bash
cd /home/ubuntu/Project_Hub
chmod +x ./mvnw
./mvnw clean package -DskipTests
```
This produces `target/institution-project-hub-1.0.0.jar`.

### 5. Running as a Systemd Service (Production Mode)
Create a systemd unit file so the application starts on boot and restarts automatically on failure:
```bash
sudo nano /etc/systemd/system/project-hub.service
```

Add the following configuration:
```ini
[Unit]
Description=Project Hub Spring Boot Application
After=network.target

[Service]
User=ubuntu
WorkingDirectory=/home/ubuntu/Project_Hub
ExecStart=/usr/bin/java -Xmx512m -jar /home/ubuntu/Project_Hub/target/institution-project-hub-1.0.0.jar

# Environment Variables
Environment="SERVER_PORT=8080"
Environment="PROJECT_HUB_DB_PATH=/var/lib/project-hub/project_hub.db"
Environment="PROJECT_HUB_UPLOAD_DIR=/var/lib/project-hub/uploads/projects/"
Environment="PROJECT_HUB_KNOWLEDGE_FILE=/home/ubuntu/Project_Hub/senior_projects.txt"
Environment="GROQ_API_KEY=your_actual_groq_api_key_here"
Environment="GROQ_MODEL=qwen/qwen3.8-27b"
Environment="JWT_SECRET=9a3f4b8c2d1e0f9a8b7c6d5e4f3a2b1c0d9e8f7a6b5c4d3e2f1a0b9c8d7e6f5a"

SuccessExitStatus=143
TimeoutStopSec=10
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```

Enable and start the service:
```bash
sudo systemctl daemon-reload
sudo systemctl enable project-hub
sudo systemctl start project-hub
sudo systemctl status project-hub
```

### 6. Health Check Verification
Verify the application is running:
```bash
curl http://localhost:8080/actuator/health
# Expected output: {"status":"UP"}
```

---

## 12. REST API Endpoints

### Health & Monitoring:
- `GET /actuator/health` - Public health check endpoint (returns `{"status":"UP"}`).

### Authentication:
- `POST /api/auth/login` - Authenticate user, returns JWT token and role.
- `POST /api/auth/register` - Register a new student or faculty member.
- `GET /api/auth/me` - Retrieve current logged-in user profile.

### Student & Project Management:
- `POST /api/projects` - Submit project (`multipart/form-data`: title, description, deployedUrl, visibility, zip file).
- `PUT /api/projects/{id}` - Update project (student can only edit own project).
- `GET /api/projects/my` - List own projects.
- `GET /api/projects` - List campus-accessible projects.
- `GET /api/projects/{id}` - View project details.
- `GET /api/projects/{id}/download` - Download project source code (enforces permission rules).

### Faculty Operations:
- `GET /api/faculty/statistics` - Institutional project and student counts.
- `GET /api/faculty/projects` - Filter and search all submissions.
- `GET /api/faculty/projects/{id}` - Complete project audit view.
- `PUT /api/faculty/projects/{id}/status` - Update submission status (`SUBMITTED`, `APPROVED`, `REJECTED`).

### AI & RAG:
- `POST /api/ai/chat` - AI assistant query endpoint.
- `POST /api/rag/search` - Semantic similarity search against senior projects.
- `POST /api/rag/reindex` - Faculty-only trigger to re-parse `senior_projects.txt`.

---

## 13. Security & Permissions

1. **Authentication**: Stateless JWT Bearer tokens passed via the `Authorization: Bearer <token>` header.
2. **Password Security**: Passwords hashed with BCrypt.
3. **Role-Based Access Control (RBAC)**:
   - Students cannot access `/api/faculty/**` or `/api/rag/reindex`.
   - Students can only modify their own submissions.
   - Students cannot download source code marked `FACULTY_ONLY` belonging to other students.
   - Faculty members have permission to inspect and download all submitted source archives.
4. **Input Sanitization & Validation**:
   - File uploads restricted to archive formats (`.zip`, `.tar.gz`, `.7z`).
   - Strict path traversal prevention (`..` sanitization and base storage containment).
   - Server-side validation on titles, descriptions, and URLs.

---

## 14. Project Directory Structure

```
Project_Hub/
├── pom.xml
├── mvnw                         # Linux / macOS Maven wrapper
├── mvnw.cmd                     # Windows Command Prompt Maven wrapper
├── mvnw.ps1                     # Windows PowerShell Maven wrapper
├── application.properties
├── senior_projects.txt
├── senior_projects_sample.txt
├── .env.example                 # Environment variable template
├── .gitignore                   # Ignores .env, *.db, target/, uploads/*
├── uploads/                     # Configurable storage directory
│   ├── .gitkeep
│   └── projects/
│       └── .gitkeep
├── src/
│   ├── main/
│   │   ├── java/com/projecthub/
│   │   │   ├── config/          # Security, DirectoryInitializer, WebMVC config
│   │   │   ├── controller/      # Auth, Project, Faculty, AI, RAG controllers
│   │   │   ├── dto/             # Request & response data transfer objects
│   │   │   ├── entity/          # User, Project, KnowledgeDocument JPA entities
│   │   │   ├── repository/      # Spring Data JPA repositories
│   │   │   ├── service/         # Project, User, Faculty, Stats, Seed services
│   │   │   ├── security/        # JWT provider, filter, UserDetails service
│   │   │   ├── storage/         # StorageService & LocalStorageService
│   │   │   ├── exception/       # GlobalExceptionHandler & custom exceptions
│   │   │   └── ai/
│   │   │       ├── groq/        # GroqService & REST integration
│   │   │       ├── embedding/   # EmbeddingService & LocalSemanticEmbedding
│   │   │       ├── rag/         # RagService, SeniorProjectsParser
│   │   │       └── orchestration/# AIOrchestrator
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/          # HTML/CSS/JS served directly by Spring Boot
│   └── test/java/com/projecthub/
│       ├── AuthTests.java
│       ├── ProjectSecurityTests.java
│       ├── RagEngineTests.java
│       └── StatisticsAndAiTests.java
└── frontend/                    # Standalone mirror of frontend assets
```

---

## 15. Future Cloud Migration (AWS S3)

The file storage architecture is designed around the `StorageService` interface:
```java
public interface StorageService {
    String storeFile(MultipartFile file, String subDirectory);
    Resource loadFileAsResource(String filePath);
    void deleteFile(String filePath);
}
```
Currently, `LocalStorageService` stores files in `uploads/projects/` on the local filesystem. To transition to **AWS S3**:
1. Add `software.amazon.awssdk:s3` to `pom.xml`.
2. Implement `S3StorageService implements StorageService` using `S3Client.putObject` and `GetObjectPresignedUrl`.
3. Set `@ConditionalOnProperty(name = "app.storage.type", havingValue = "s3")` on the S3 implementation.
4. No controller, entity, or frontend code requires alteration.

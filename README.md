Djinni Job Scraper & API
A Spring Boot application that scrapes job listings from Djinni.co, stores them in a PostgreSQL database, and provides a REST API for accessing and filtering job data.

Features
Djinni Job Scraping: Automatically scrapes job listings from Djinni.co including detailed information such as salary, requirements, benefits, and more
Data Storage: Persists job data in a PostgreSQL database
REST API: Provides endpoints to:
Retrieve all jobs with pagination and sorting
Filter jobs by multiple criteria (location, job type, experience level, etc.)
Get detailed information about specific jobs
Docker Support: Easy deployment with Docker Compose
Tech Stack
Java with Spring Boot
Spring Data JPA for database operations
Jsoup for web scraping
PostgreSQL for data storage
Docker and Docker Compose for containerization
Lombok for reducing boilerplate code
Getting Started
Prerequisites
Java 17 or higher
Docker and Docker Compose
Maven
Setup and Running
Clone the repository:
bash
git clone <your-repository-url>
cd <project-folder>
Start the PostgreSQL database with Docker Compose:
bash
docker-compose up -d
Build and run the application:
bash
./mvnw spring-boot:run
The application will be available at http://localhost:8080
API Endpoints
Get All Jobs
GET /api/jobs?page=0&size=10&sortBy=postedDate&sortDirection=desc
Filter Jobs
POST /api/jobs/filter
Example request body:

json
{
  "location": "Remote",
  "jobType": "Full-time",
  "experienceLevel": "Senior",
  "industry": "Tech",
  "tags": "Java,Spring",
  "minSalary": 3000,
  "maxSalary": 6000,
  "page": 0,
  "size": 10,
  "sortBy": "postedDate",
  "sortDirection": "desc"
}
Get Job by ID
GET /api/jobs/{id}
Trigger Job Scraping
POST /api/jobs/scrape
Database Structure
The application uses a Job entity with the following fields:

id
jobTitle
companyName
location
jobType
salaryMin
salaryMax
jobDescription
requirements
experienceLevel
educationLevel
industry
postedDate
applicationDeadline
howToApply
companyLogo
benefits
tags
source
Configuration
Database configuration can be modified in the docker-compose.yml file:

yaml
version: '3.8'
services:
  postgres:
    image: postgres:latest
    container_name: postgres_db
    environment:
      POSTGRES_USER: youruser
      POSTGRES_PASSWORD: yourpassword
      POSTGRES_DB: yourdb
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U youruser -d yourdb"]
      interval: 5s
      timeout: 5s
      retries: 5

volumes:
  postgres_data:
Spring Boot application properties can be adjusted in the application.properties or application.yml file.

Development
Project Structure
com.example.demo.model.entity - Contains the Job entity
com.example.demo.model.dto - Data Transfer Objects
com.example.demo.repository - Spring Data JPA repositories
com.example.demo.service - Business logic layer
com.example.demo.scraper - Contains the DjinniJobScraper
com.example.demo.mapper - DTO to Entity mapping
com.example.demo.controller - REST endpoints
About Djinni.co
Djinni.co is a popular job board for tech professionals, primarily focused on the Ukrainian and Eastern European market. The platform specializes in IT and development positions across various technologies and experience levels.

Extending for Other Job Sources
While this application currently focuses on Djinni.co, the architecture allows for extending to other job sources:

Create a new scraper class implementing the scraping logic for the new source
Inject the new scraper into the JobService
Update the API to support the new source
License
[Include your license information here]

Contributing
[Include contribution guidelines here]


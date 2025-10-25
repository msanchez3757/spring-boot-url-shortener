Spring Boot URL Shortener

This project is a full-stack web application built with Spring Boot and Thymeleaf. It provides a simple interface for shortening URLs while supporting user authentication, role-based access control, and administrative moderation.

Overview

The application allows both guest and registered users to create and manage short links. It integrates a secure login system using Spring Security, stores data in PostgreSQL via Spring Data JPA, and manages schema changes with Flyway. The frontend is rendered with Thymeleaf, incorporating HTML, CSS, and JavaScript.

Features
Guest Users

Can create short links without an account

Can visit and use shortened URLs freely

Registered Users

Can register and log in through a dedicated user page

Can create short links with additional options such as:

Setting custom expiration dates

Marking links as private

Can view and manage all their created links in a personal dashboard

Administrators

Have access to view all links in the system

Can remove expired or inappropriate links

Technologies Used

Backend

Spring Boot (application framework)

Spring Security (authentication and authorization)

Spring Data JPA (ORM for database access)

Flyway (database migration management)

PostgreSQL (database)

Frontend

Thymeleaf (template rendering)

HTML, CSS, JavaScript

Deployment

Spring Boot Buildpacks for container image generation

Docker for deployment and container management

Setup Instructions
1. Clone the repository
git clone https://github.com/msanchez3757/spring-boot-url-shortener.git
cd spring-boot-url-shortener

2. Configure the database

Edit src/main/resources/application.properties to match your PostgreSQL setup:

spring.datasource.url=jdbc:postgresql://localhost:5432/url_shortener
spring.datasource.username=postgres
spring.datasource.password=postgres

3. Run PostgreSQL (via Docker)
docker run -d --name postgres-db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=url_shortener \
  -p 5432:5432 postgres:15

4. Build and run the application
./mvnw spring-boot:run


Access the application at:

http://localhost:8080

Building a Docker Image

To package the application as a container image:

./mvnw spring-boot:build-image "-Dspring-boot.build-image.imageName=msanchez/spring-boot-url-shortener" -DskipTests


To run the container:

docker run -p 8080:8080 --name url-shortener-app msanchez/spring-boot-url-shortener

User Roles
Role	Description
Guest	Can create and visit short links
User	Can customize link properties and manage their own links
Admin	Can view and delete all links
Purpose and Learning Goals

This project was developed to gain experience with:

Designing and implementing a full-stack web application using Spring Boot

Building secure authentication and authorization systems

Managing relational data with JPA and Flyway

Rendering dynamic HTML with Thymeleaf

Containerizing and deploying applications using Docker

# Blog-project — Live Demo Readiness

A full-stack blog application built with Java 17, Spring Boot, Thymeleaf, Spring Data JPA, and MySQL.

## Demo goals
- Provide a reliable public URL for recruiters and portfolio visitors.
- Use a persistent hosted database and environment-based configuration.
- Protect write operations from anonymous visitors.
- Keep the existing Spring MVC + Thymeleaf architecture.

## Prerequisites
- JDK 17
- Maven 3.8+
- MySQL for local development (or a configured hosted database for deployment)

## Local run
From the application directory:

```bash
cd blog-app
mvn clean test
mvn spring-boot:run
```

Open http://localhost:8080.

Configure database settings via environment variables (see `application.properties`). Never commit production secrets.

## Deployment checklist
- [ ] Build and tests pass from a clean checkout.
- [ ] Configure JDBC URL, DB user, and DB password using host environment variables.
- [ ] Provision a persistent database and verify schema initialization.
- [ ] Verify app binds to the host-provided PORT.
- [ ] Restrict create/edit/delete/comment mutation routes to authorized users, or disable write actions for a public read-only demo.
- [ ] Validate request parameters for pagination and sorting.
- [ ] Smoke-test home, search, tag/author filtering, sorting, pagination, post details, comments, and error pages.
- [ ] Confirm data persists after service restart/redeployment.
- [ ] Add the live demo URL here after it has been verified.

## Feature overview
- Blog post listing and detail pages
- Search by title, content, author, excerpt, and tag
- Author/tag filters and sorting
- Pagination
- Comments and reusable tags
- Sample data seeding for demo environments

## Security note
Never publish database passwords, credentials, or other secrets. A public demo should not expose unrestricted content modification or deletion.

# Blog Project

A server-rendered blog application built with Java 17, Spring Boot, Spring MVC, Thymeleaf, Spring Data JPA, and MySQL. It supports published post browsing, search, author/tag filters, sorting, pagination, and comments.

## Features

- Browse and read published posts
- Search titles, content, excerpts, authors, and tags
- Filter by author and tag; sort and paginate results
- Create and edit posts, manage tags, and add/edit/delete comments in local development
- Optional read-only mode for a public demo
- Sample content seeded when the database is empty

The project does not implement Spring Security or user accounts. Set `APP_DEMO_READ_ONLY=true` for a public demo: write routes are rejected on the server and editing controls are hidden. Keep the demo read-only until proper authentication and authorization are added.

## Technology

| Area | Technology |
|---|---|
| Language | Java 17 |
| Web | Spring Boot, Spring MVC, Thymeleaf |
| Persistence | Spring Data JPA / Hibernate |
| Local database | MySQL 8 |
| Hosted demo database | PostgreSQL |
| Build and tests | Maven, JUnit, MockMvc, H2 |
| Packaging | Docker and Docker Compose |

## Run locally with Maven

Requirements: JDK 17, Maven 3.8+, and a local MySQL server.

The default database is `blogdb` on localhost. Set credentials in your shell rather than editing the tracked configuration:

```bash
export DATABASE_USERNAME=root
export DATABASE_PASSWORD=your-local-mysql-password
cd blog-app
mvn test
mvn spring-boot:run
```

Open <http://localhost:8080>. To run in read-only mode, set `APP_DEMO_READ_ONLY=true` before starting the app.

## Run with Docker Compose

From the repository root:

```bash
cp .env.example .env
# Change the two local-only passwords in .env.
docker compose up --build
```

Open <http://localhost:8080>. Compose keeps MySQL data in the named `blog-db-data` volume across container restarts. `docker compose down` stops the services; it does not remove that volume. Local passwords in `.env.example` are examples only and must never be reused in a hosted environment.

## Configuration

| Variable | Purpose | Local fallback |
|---|---|---|
| `DATABASE_URL` | JDBC connection string | Local MySQL URL |
| `DATABASE_USERNAME` | Database user | `root` |
| `DATABASE_PASSWORD` | Database password | Empty |
| `PORT` | HTTP port | `8080` |
| `APP_DEMO_READ_ONLY` | Blocks all write routes and hides edit controls | `false` |
| `DDL_AUTO` | Hibernate schema handling | `update` |
| `SHOW_SQL` | SQL logging | `false` |

Hosted credentials belong in the hosting provider's secret settings. Never commit a real database password or connection string containing credentials.

## Tests

The integration tests use an in-memory H2 database and cover request validation, missing records, sample-data seeding, published/draft visibility, comment/post ownership, and read-only demo behavior.

```bash
cd blog-app
mvn test
```

## Deployment

The repository includes a Dockerfile, a Render Blueprint, and deployment notes. The proposed public demo uses Render's free web service with Neon PostgreSQL; the free web service can sleep when idle, so the first visit may be slow. Follow [DEPLOYMENT.md](DEPLOYMENT.md) and set `APP_DEMO_READ_ONLY=true` in the hosted environment.

The free-tier limits and current deployment status are documented separately. No live URL is claimed until it has been deployed and smoke-tested.

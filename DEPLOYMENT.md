# Deployment guide

## Proposed demo setup

- **Application:** Render Docker web service using the free plan.
- **Database:** Neon PostgreSQL free project.
- **Demo access:** Read-only (`APP_DEMO_READ_ONLY=true`). The project has no user authentication, so the public instance must not allow anonymous writes.
- **Branch:** `codex/phase2-request-validation-404`.

The repository's `render.yaml` defines the Render service. Connect the repository and this branch as a Blueprint, then provide the database values in Render's environment settings. The Blueprint does not include database credentials.

## Free-tier limitations

Render's free web service sleeps after 15 minutes without traffic and may take about a minute to start on the next request. It has ephemeral local storage, so the database must remain external. Neon is used for the database; its free plan currently lists 1 GB storage and 100 compute-hours per project each month. These free plans are suitable for a portfolio demo, not a production service. Provider limits can change.

If the demo needs to stay warm and respond immediately, a paid web-service plan is required. No paid resource is configured by this repository.

## Hosted environment variables

Set these values in Render's service environment. Do not commit them to GitHub.

| Variable | Value |
|---|---|
| `DATABASE_URL` | `jdbc:postgresql://<neon-host>:5432/<database>?sslmode=require` |
| `DATABASE_USERNAME` | Database role from Neon |
| `DATABASE_PASSWORD` | Database password from Neon |
| `APP_DEMO_READ_ONLY` | `true` |

Render supplies the HTTP `PORT`; Spring Boot binds to it. Hibernate uses `update` by default so a fresh demo database can receive the current tables. The seeder adds its sample posts only when the database has no posts. Back up any data before changing schema behavior; do not point the demo at a database containing user data.

The application has MySQL and PostgreSQL JDBC drivers. Local configuration defaults to MySQL; hosted configuration selects PostgreSQL from the JDBC URL. The JPA entities use standard relationships and text fields, but no automated migration of an existing MySQL database is included.

## Local Docker setup

```bash
cp .env.example .env
# Change the example local passwords.
docker compose up --build
```

Compose starts MySQL and the application. MySQL data is stored in a named Docker volume. The checked-in values in `.env.example` are examples for local use only.

## Verify deployment

After Render reports a successful deploy:

1. Open the public URL and wait for the service to wake if it has been idle.
2. Verify the home page, post details, search, sorting, author/tag filters, and pagination.
3. Confirm the New Post, Edit, Delete, and comment mutation controls are hidden.
4. Try write routes directly and confirm they return HTTP 403.
5. Restart/redeploy the service and confirm the seeded posts remain in PostgreSQL.
6. Record the URL here only after those checks pass.

**Live demo URL:** Not yet verified.

## Build and test

```bash
cd blog-app
mvn test
```

The tests use H2 and do not require the hosted database. The current development machine has Java 21 installed; run the final verification with Java 17, matching the project's configured version, before calling the build verified for the target runtime.

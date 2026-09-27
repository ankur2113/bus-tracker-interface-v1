# Build and run

## Backend

Open this folder in IntelliJ as a Maven project, or run from PowerShell:

```powershell
cd <extracted-backend-folder>
.\mvnw clean package -DskipTests
.\mvnw spring-boot:run "-Dspring-boot.run.profiles=local"
```

## Important

This ZIP is source-only. It excludes generated folders like `target`, `.git`, and IDE cache files.

Flyway will apply the new migration:

```text
src/main/resources/db/migration/V3__emergency_reports.sql
```

Make sure PostgreSQL is running before starting the app.

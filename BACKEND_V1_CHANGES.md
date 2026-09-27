# Backend V1 changes

Implemented V1-ready features and contract improvements:

- Emergency/SOS reports
  - Coordinator: `POST /api/v1/coordinator/trips/{tripId}/emergencies`
  - Coordinator: `GET /api/v1/coordinator/trips/{tripId}/emergencies`
  - Admin: `GET /api/v1/admin/emergencies`
  - Admin: `POST /api/v1/admin/emergencies/{emergencyId}/resolve`
  - Flyway migration: `V3__emergency_reports.sql`

- Commuter trip progress / smart arrival status
  - Commuter: `GET /api/v1/commuter/trips/{tripId}/progress`
  - Returns stops-away, estimated minutes, alert level, and message.
  - Alert levels include `FIVE_STOPS_AWAY`, `TWO_STOPS_AWAY`, `TEN_MINUTES_AWAY`, `ARRIVING`, and `ON_THE_WAY`.

- Admin commuter-route assignment utility
  - Admin: `GET /api/v1/admin/commuter-route-assignments`
  - Existing commuter-specific endpoint is still available.

Existing backend role protection remains unchanged:

- `/api/v1/admin/**` requires ADMIN.
- `/api/v1/coordinator/**` requires COORDINATOR or ADMIN.
- `/api/v1/commuter/**` requires COMMUTER or ADMIN.

Build locally with:

```powershell
.\mvnw clean package -DskipTests
```

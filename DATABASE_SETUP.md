# Database Setup — PostgreSQL with Docker (local dev)

The cleanest local dev setup: Docker runs Postgres in a container, no system
installation needed, zero pollution. Production (Render) will use a Render-
managed Postgres instance — same setup, just in the cloud.

## Option A: Docker Compose (recommended — one command)

**Prerequisites:** Docker Desktop installed (https://docker.com).

1. **Start the database:**
   ```bash
   cd "Pet Adoption Service"
   docker-compose up -d
   ```
   That's it. The container starts in ~3s. You'll see:
   ```
   Creating pet-adoption-db ... done
   ```

2. **Verify it's running:**
   ```bash
   docker ps
   # Should show "pet-adoption-db" with status "Up"
   ```

3. **Run the app (no env vars needed):**
   ```bash
   mvn spring-boot:run
   # Connects to localhost:5432/pet_adoption with user postgres / password postgres
   # These defaults in application.properties match the docker-compose.yml config
   ```

4. **Logs confirm success:**
   Watch for `HHH000204: Processing PersistenceUnitInfo` → table creation
   → `DataLoader` seeding admin/test users and pets. If you see it, the
   connection worked.

5. **Test the app:**
   ```
   http://localhost:9090
   Login: admin@petcenter.com / admin123
   ```

6. **Stop the database (keep data):**
   ```bash
   docker-compose down
   # Data persists in a Docker volume named "pet-adoption-db_postgres_data"
   # Next time you run "docker-compose up -d", everything is still there
   ```

7. **Wipe the database (fresh start):**
   ```bash
   docker-compose down -v
   # The -v flag destroys the volume too. Next run will be a blank database.
   ```

## Option B: If you prefer your own Postgres install

If Docker feels overkill or you already have Postgres running locally:

```bash
# macOS (Homebrew):
brew install postgresql@16 && brew services start postgresql@16

# Windows: download installer from postgresql.org
# Ubuntu/Debian:
sudo apt install postgresql

# Create the pet_adoption database:
psql -U postgres -c "CREATE DATABASE pet_adoption;"

# Then run the app with no env vars (uses localhost:5432 defaults):
mvn spring-boot:run
```

## Production (Render)

When you deploy to Render, you'll:
1. Provision a free-tier Postgres instance within Render's dashboard (one-click)
2. Set three env vars in your Web Service pointing at the Render DB:
   ```
   DB_URL=jdbc:postgresql://<render-host>:5432/<db-name>
   DB_USER=<render-user>
   DB_PASS=<render-password>
   ```

Same `docker-compose.yml` approach for local; same `application.properties`
fallbacks. Nothing to change except the env vars at deployment time.

## How tables are created

`spring.jpa.hibernate.ddl-auto=update` in `application.properties` tells
Hibernate to auto-create schema on startup:
- First boot against empty DB → all tables created (users, pets, etc.)
- Subsequent boots → schema stays, only new columns added if the entity
  definitions changed (no destructive drops)
- `DataLoader` runs after schema setup and seeds mock data (if tables empty)

You never write DDL manually — Hibernate handles it.

## Troubleshooting

**Connection refused on 5432?**
- Confirm Docker container is running: `docker ps`
- Check logs: `docker-compose logs postgres`
- Restart: `docker-compose down && docker-compose up -d`

**Tables didn't get created?**
- Check app logs for `HHH000204` — that's Hibernate's schema-generation marker
- If missing, the app crashed during startup (scroll up in logs to see why)

**"peer authentication failed" when using local Postgres (not Docker)?**
- Linux/Mac: Make sure you ran `psql -U postgres` (as the postgres user)
- Windows: Installer prompts for a password; use that

**Want to inspect the database?**
```bash
# Using psql inside the container:
docker-compose exec postgres psql -U postgres -d pet_adoption

# Or use a GUI like DBeaver (free): File → New Database Connection → PostgreSQL
# Hostname: localhost, Port: 5432, Database: pet_adoption, User: postgres, Password: postgres
```

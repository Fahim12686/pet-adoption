# Pet Adoption Service — Modern, Playful Pet Adoption Platform

A full-stack Spring Boot web application for browsing adoptable pets, managing adoptions, and shopping for pet essentials. Features pet browsing, adoption applications, mock payment processing, and an admin dashboard.

**Status:** Pre-production refactor in progress (PostgreSQL, modern UI/UX, cart checkout)  
**Tech:** Spring Boot 4.1 | Java 17 | Thymeleaf | Bootstrap 5.3 | PostgreSQL | Docker

---

## Quick Start (3 minutes)

### 1. Start the database (Docker)

```bash
docker-compose up -d
```

Postgres container spins up in ~3 seconds. Data persists in a Docker volume.

### 2. Run the app

```bash
mvn clean install
mvn spring-boot:run
```

Watch logs for `Started PetAdoptionServiceApplication in X seconds`. No errors = success.

### 3. Open in browser

```
http://localhost:9090
```

**Test login:**
- Admin: `admin@petcenter.com` / `admin123`
- User: `user@test.com` / `user123`

---

## Database Setup

See [`DATABASE_SETUP.md`](./DATABASE_SETUP.md) for:
- Docker Compose (recommended)
- Local Postgres install
- Troubleshooting connection issues

**TL;DR:** `docker-compose up -d` starts a Postgres container with zero config. Hibernate auto-creates all tables on first app boot.

---

## Project Structure

```
src/main/java/com/seu/petadoptionservice/
├── controller/          # 8 REST/MVC endpoints (Pet, Store, Auth, etc.)
├── entity/              # 5 JPA entities (User, Pet, StoreItem, etc.)
├── repository/          # 5 Spring Data repos
├── service/             # Business logic (CRUD, auth, image upload)
├── config/              # Spring Security, DataLoader, WebMvc
└── security/            # Custom user details service

src/main/resources/
├── templates/           # 19 Thymeleaf HTML pages
├── static/
│   ├── css/style.css    # Modern playful color palette
│   └── images/          # Uploaded pet & store photos
└── application.properties
```

---

## Features

### ✅ Complete
- **Authentication:** Signup/signin with BCrypt password hashing, role-based access (USER/ADMIN)
- **Pet browsing:** Public pet grid, individual pet detail pages with adoption CTA
- **Adoption workflow:** Application form → admin approval → mock payment → completion
- **Admin dashboard:** Manage pets, users, adoption applications
- **Store:** Browse ~10 mock pet products, admin CRUD for inventory
- **Image upload:** Pet & store item photos stored locally, served at `/images/**`
- **Responsive design:** Bootstrap 5.3 with custom CSS theme

### 🚧 In Progress (Increment 2+)
- **Modern UI redesign:** Fresh playful palette replacing current coral/amber Bootstrap defaults
- **Shopping cart:** Session-based cart, add/remove items, checkout flow (Increment 3)
- **Order management:** Track orders, order history, confirmation emails
- **Reviews:** Sliding review carousel on homepage, user-submitted pet reviews

---

## Environment Variables

### Local dev (Docker, no env vars needed)
App defaults to `localhost:5432/pet_adoption` with user `postgres` / password `postgres`.

### Production (Render)
Set these in Render's Web Service → Environment:
```
DB_URL=jdbc:postgresql://<render-host>:5432/<db-name>
DB_USER=<render-user>
DB_PASS=<render-password>
PORT=<render-assigns>
```

---

## Build & Deploy

### Local build
```bash
mvn clean package
java -jar target/pet-adoption-service-0.0.1-SNAPSHOT.jar
```

### Docker build (for production or testing)
```bash
docker build -t pet-adoption-service:latest .
docker run -p 9090:9090 -e DB_URL=... -e DB_USER=... -e DB_PASS=... pet-adoption-service:latest
```

### Deploy to Render (free tier)
1. Push to GitHub
2. Create Render Web Service from repo
3. Set DB env vars (see above)
4. Render auto-builds from `Dockerfile` and deploys

See [`DEPLOY.md`](./DEPLOY.md) for step-by-step (coming in final increment).

---

## Development Notes

### Why PostgreSQL?
- Free cloud hosting (Render, Supabase, Railway, Neon)
- Better than MySQL for Render's free tier (connection pooling, SSL)
- Same code runs locally (Docker) and in production (Render-managed DB)

### Why local disk for images?
- Simple during development (no cloud account setup, instant uploads)
- Will be replaced with Cloudinary (free tier) before production deploy

### Why session-based cart (not database)?
- Stateless, simpler architecture
- User cart lost on logout (acceptable for MVP)
- Upgradeable to persistent cart later

---

## Troubleshooting

**"Connection refused" on app startup?**
- Confirm Docker container running: `docker ps`
- Check: `docker-compose logs postgres`
- Restart: `docker-compose down && docker-compose up -d`

**Blank page at localhost:9090?**
- Check logs for `Started PetAdoptionServiceApplication`
- Database may have failed to connect (see above)

**Can't upload pet photo?**
- Confirm `src/main/resources/static/images/` exists
- Directory is created auto on first upload if missing

**"Permission denied" on macOS/Linux?**
- Images saved with user's permissions; make sure user owns the project directory

---

## Next Steps

1. ✅ **Database setup** — Run Docker + confirm login works
2. 🎨 **UI redesign** — Fresh modern-playful palette + new components
3. 🛒 **Cart & checkout** — Session cart, order flow, confirmation
4. ⭐ **Reviews & admin stats** — Sliding carousel, dashboard charts
5. 🚀 **Production deploy** — Cloudinary + Render hosting

---

## License

MIT (or your choice)

## Contact

Built with ❤️ for pet lovers everywhere.

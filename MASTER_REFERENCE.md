# Pet Adoption Service — Master Build Reference
**Purpose of this document:** This is the single source of truth for an AI assistant (Claude or otherwise) picking up work on this project at any point. If you are an AI reading this for the first time, read this entire file before writing or changing any code. It tells you what the product is, what has been decided and why, what's been built already, what's left, and the exact conventions to follow so new work looks like it was written by the same hand as the old work.

If a person pastes this file into a new chat, the correct first move is: read it fully, ask them to upload the current project zip, inspect the actual code to confirm it matches what this document claims (documents can drift out of date — the code is ground truth), then resume at the increment marked `IN PROGRESS` or `NOT STARTED` below.

---

## 1. Product vision

A **Pet Adoption Center** web platform: browse adoptable pets, apply to adopt, and shop for pet essentials — all in one site. The tone is **modern but playful and professional** — this is not a hobby-project look; it should feel like a real, fundable startup product, while staying warm and approachable (it's about pets, not enterprise software).

**End-to-end shape of the finished product:**
- Public visitor can browse pets and the store without an account
- Visitor signs up, applies to adopt a pet, and/or buys pet essentials through a full cart → checkout flow
- Logged-in user has a dashboard showing their adoption applications and order history
- Admin has a full back-office: manage pets, review applications, manage store inventory, view sales/adoption stats, moderate reviews
- Whole thing is hosted for free (Render for the app, a free Postgres instance for the database, Cloudinary for images)

---

## 2. Non-negotiable decisions (locked in — do not re-litigate)

| Area | Decision | Why |
|---|---|---|
| Database | **PostgreSQL** (not MySQL) | Free cloud hosting compatibility (Render/Supabase/Neon all offer free Postgres); MySQL was the original stack but had setup problems and worse free-tier cloud support |
| Local DB dev | **Docker Compose** running `postgres:16-alpine` | User doesn't have Postgres installed locally (only MySQL); Docker avoids installing anything on the host OS and mirrors a production-like setup. User does NOT want to use Supabase/cloud DB for local dev — local is fully Dockerized, cloud DB is only for the final deployed version |
| Hosting | **Render free tier** | User's explicit choice; budget constraint drives every later architectural call (ephemeral filesystem, cold starts, etc.) |
| Image storage (interim) | Local disk, saved into `src/main/resources/static/images/`, served at `/images/**` | Zero-config for local development; this is a REPO-relative path so images persist across local restarts. **This must change before deployment** — Render's filesystem is ephemeral, uploads will vanish on redeploy/restart. Cloudinary is the planned replacement, deferred to the deployment increment. |
| Image storage (production) | **Cloudinary** (planned, not yet implemented) | Single env var (`CLOUDINARY_URL`), minimal code changes, no bucket/IAM setup unlike S3 |
| Payments | **100% mock, no real gateway** | User explicitly wants no real payment processor. Client-side JS simulates "Processing → Success" (setTimeout), backend just validates card format and marks status PAID/COMPLETED. This applies to both adoption payments and store checkout. |
| Cart | **Session-based, no DB persistence** | Simpler for MVP; cart lost on logout is an accepted tradeoff. Do not build a `Cart` entity/table unless explicitly asked to change this later. |
| Styling | Single shared `style.css`, cool-modern-friendly palette (see §4), applied consistently across every template | User explicitly disliked the original generic Bootstrap coral/amber look and asked for a professional redesign |
| Framework/stack | Spring Boot 4.1, Java 17, Thymeleaf (server-rendered HTML, no SPA/JS framework), Spring Security, JPA/Hibernate, Maven | Inherited from original project; not up for debate unless user raises it |

---

## 3. Tech stack reference

- **Backend:** Spring Boot 4.1.0, Java 17, Maven
- **View layer:** Thymeleaf (server-rendered), no React/Vue/frontend build step
- **Security:** Spring Security, BCrypt password hashing, role-based (`USER`, `ADMIN`) via `hasAnyRole`/`sec:authorize`
- **ORM:** JPA/Hibernate, `spring.jpa.hibernate.ddl-auto=update` (auto-migrates schema on boot — no manual SQL/migration files in this project)
- **Database:** PostgreSQL. Local: Docker Compose (`postgres:16-alpine`, port 5432, db `pet_adoption`, user/pass `postgres`/`postgres`). Production: TBD free Postgres provider, connected via `DB_URL`/`DB_USER`/`DB_PASS` env vars (already wired into `application.properties` with local defaults as fallback).
- **Build/deploy:** Maven, multistage Dockerfile (`maven:3.9.6-eclipse-temurin-17` build stage → `eclipse-temurin:17-jre` run stage), target host Render
- **Fonts:** Fredoka (headings — rounded, friendly, warm without being cartoonish) + Plus Jakarta Sans (body/UI — clean, modern, cool), loaded via Google Fonts `@import` in `style.css`
- **CSS:** One file, `src/main/resources/static/css/style.css`, using CSS custom properties (`--pa-*` prefix) for the design system. No Sass/build step — hand-written CSS with variables.

---

## 4. Design system (Increment 2 — already built, treat as final unless user asks to change)

**Palette** (cool & modern base, one playful accent used sparingly, not everywhere):
- `--pa-teal-deep: #0e5c56` — primary brand color, headers, primary buttons
- `--pa-teal: #1c9c8e` — links, secondary accents, hover states
- `--pa-teal-light: #e0f2f1` — subtle backgrounds, badges, hover fills
- `--pa-coral: #ff6f59` — the ONE playful accent color, used only for high-priority CTAs (Adopt Me, Add to Cart) — must not be sprinkled everywhere or it stops reading as an accent
- Neutral ink/paper tones for text and backgrounds (see actual `style.css` for exact hex values — check the file directly rather than assuming, since it may have been refined during implementation)

**Typography:**
- Headings: **Fredoka** (rounded terminals, weight 500-700)
- Body/UI text: **Plus Jakarta Sans** (weights 400-700)
- Both loaded via a single Google Fonts `@import` at the top of `style.css`

**Design principles established in Increment 2 (follow these for all future template work):**
- Left-aligned hero content (not centered generic hero blocks)
- Pet cards, store cards, and admin panels each have their own distinct visual treatment — not one identical rounded-card-with-shadow template stamped everywhere
- Numbered steps/sequences only used where content is genuinely sequential (e.g., the adoption process) — not decoratively
- One deliberate motion moment on page load is acceptable; hover-lift only on genuinely clickable cards; no scattered fade-in-on-scroll effects on every section
- Every template links the shared `style.css`; no template should introduce its own one-off inline `<style>` block with duplicate design tokens (the old `admin.html` had done this before Increment 2 — this pattern should NOT be reintroduced)

**Before writing new UI in future increments:** open the actual current `src/main/resources/static/css/style.css` and reuse its existing variables/classes rather than re-deriving the palette from memory or from this document — this document describes intent, the CSS file is the implementation of record.

---

## 5. Data model — current state (as of end of Increment 2)

### Existing entities (do not rename without strong reason — templates and repos reference these names throughout)

**User**
- id, name, email, password (BCrypt), phone, roles (`Set<String>`, e.g. USER/ADMIN via `user_roles` join table), status (ACTIVE/SUSPENDED), createdAt
- 1:M with AdoptionApplication, Payment

**Pet**
- id, name, species, breed, age, gender, description, imageUrl, adoptionFee, status (AVAILABLE/PENDING/ADOPTED/FOSTERED)
- 1:M with AdoptionApplication

**AdoptionApplication**
- id, user (FK), pet (FK), status (PENDING/APPROVED/REJECTED/COMPLETED), createdAt

**Payment**
- id, user (FK), amount, status (PENDING/COMPLETED), transactionId, createdAt
- Currently used for adoption fee mock payments only

**StoreItem**
- id, name, description, price, imageUrl, stockQty, category, active (bool)
- No relationships yet — cart/order entities do not exist yet (this is Increment 3's job)

### Entities that DO NOT exist yet (to be created in future increments)

- `Order`, `OrderItem` — Increment 3 (cart & checkout)
- `Review` — Increment 4 (reviews)
- Nothing exists yet for admin stats aggregation beyond what can be computed live from existing repos — Increment 5 will likely add a `StatsService` doing aggregate queries rather than a new entity

---

## 6. Existing code structure (as of end of Increment 2)

```
src/main/java/com/seu/petadoptionservice/
├── PetAdoptionServiceApplication.java
├── config/
│   ├── DataLoader.java              # Seeds admin/test users, mock pets, mock store items on startup
│   ├── GlobalExceptionHandler.java  # @ControllerAdvice
│   ├── SecurityConfiguration.java   # Spring Security rules
│   └── WebMvcConfig.java            # Serves /images/** from static folder
├── controller/
│   ├── HomeController.java          # GET / → index.html
│   ├── AuthController.java          # /signin, /signup, /logout
│   ├── PetController.java           # GET /pets, GET /pets/{id}, admin pet CRUD
│   ├── AdoptionController.java      # POST /adopt, adoption application flow
│   ├── PaymentController.java       # POST /payment/process (mock)
│   ├── StoreController.java         # GET /store (public browse — NOT YET connected to a cart)
│   ├── AdminController.java         # /admin/dashboard, /admin/pets, /admin/applications, /admin/users
│   └── AdminStoreController.java    # /admin/store/* CRUD
├── entity/                          # 5 entities, see §5
├── repository/                      # 5 Spring Data repos, minimal custom queries
├── service/
│   ├── UserService.java
│   ├── PetService.java
│   ├── StoreService.java
│   └── LocalImageStorageService.java  # Saves to disk, returns /images/{filename} URL
├── dto/
│   └── PaymentRequest.java
└── security/
    └── CustomUserDetailsService.java

src/main/resources/
├── application.properties           # PostgreSQL config, env-var driven with local Docker defaults
├── templates/                       # 19 Thymeleaf templates, ALL redesigned in Increment 2
│   ├── index.html, pet-list.html, pet-detail.html, store.html   # public
│   ├── signin.html, signup.html                                 # auth
│   ├── dashboard.html, adopter-form.html, payment.html, payment-success.html  # user
│   ├── admin-dashboard.html, admin.html, admin-pets.html, admin-store.html,
│   │   admin-store-form.html, admin-applications.html, admin-users.html      # admin
│   ├── pet-form.html
│   └── error.html
└── static/
    ├── css/style.css                # Single shared stylesheet, design system, see §4
    └── images/                      # Uploaded pet/store photos (local disk, interim solution)
```

**Login credentials seeded by DataLoader (for testing at any point):**
- Admin: `admin@petcenter.com` / `admin123`
- User: `user@test.com` / `user123`

**Local dev workflow:**
```bash
docker-compose up -d      # starts Postgres container
mvn spring-boot:run       # or ./mvnw / mvnw.cmd on Windows if mvn isn't on PATH
# → http://localhost:9090
```

---

## 7. Increment plan — status tracker

**Update the status markers below as work completes.** Use exactly these three states: `NOT STARTED`, `IN PROGRESS`, `DONE`. When you finish an increment, update this table before ending your turn — don't leave it stale for the next session to trip over.

| # | Increment | Status | Notes |
|---|---|---|---|
| 1 | PostgreSQL migration + Docker local dev | **DONE** | Confirmed running locally by user (admin login tested working) |
| 2 | Modern UI/UX redesign | **DONE** | User confirmed implementation; palette/fonts/PostgreSQL/entities verified present in uploaded code as of this document's writing |
| 3 | Cart & checkout | **DONE** | Implemented Order/OrderItem entities, CartService, OrderService, cart endpoints in StoreController, OrderController, cart.html, checkout.html, order-confirmation.html templates. Store page wired with Add to Cart buttons. Cart icon added to navbars (dashboard.html needs manual update due to edit tool issues). Session-based cart, mock checkout flow with stock validation. |
| 4 | Reviews | **NOT STARTED** | See §9 |
| 5 | Admin sales/adoption stats | **NOT STARTED** | See §10 |
| 6 | Production deployment (Cloudinary + Render) | **NOT STARTED** | See §11. Image storage MUST migrate off local disk before this ships. |
| 7 | Final QA / code review pass | **NOT STARTED** | Re-read all changed files for auth holes on new routes, N+1 queries, null handling, mock-payment abuse potential |

---

## 8. Increment 3 spec — Cart & Checkout (must-have before launch)

**Goal:** A visitor can add store items to a cart, view/edit the cart, and complete a mock checkout that marks an order as paid — mirroring the existing mock adoption-payment pattern.

**New entities:**
- `Order` — id, user (FK), totalAmount, status (PENDING/PAID), createdAt, transactionId
- `OrderItem` — id, order (FK), storeItem (FK), quantity, unitPrice (snapshot price at time of purchase, not a live join to StoreItem.price)

**New/changed components:**
- Session-based cart (likely stored in the HTTP session as a simple `Map<Long, Integer>` of storeItemId → quantity, or a small `CartService` wrapping session access) — **no new DB table for the in-progress cart itself**, only for completed Orders
- `StoreController` — needs `POST /cart/add/{itemId}`, `GET /cart`, `POST /cart/update`, `POST /cart/remove/{itemId}`
- New `OrderController` (or extend an existing one) — `GET /checkout`, `POST /orders/checkout` (marks order PAID, reuses the mock "Processing → Success" JS pattern already used in `payment.html`/`PaymentController`)
- New templates: `cart.html`, `checkout.html`, `order-confirmation.html` (names indicative — match existing naming conventions in the templates folder)
- `store.html`'s "Add to Cart" button currently exists but is non-functional (flagged honestly in the UI per the original build log) — this increment wires it up for real
- Navbar needs a cart icon/count indicator once cart has items (check `style.css` / nav partial for how nav items are currently structured, keep it visually consistent)

**Constraints to respect:**
- Reuse the mock payment UX pattern already established (client-side "Processing…" → "Success" simulation via `setTimeout`, then a POST to a backend endpoint that just validates format and flips status) — do not build a second, differently-styled mock payment flow
- Respect stock quantity — decrement `StoreItem.stockQty` on successful order, and don't allow checkout past available stock
- Keep admin's existing store CRUD (`AdminStoreController`) untouched except where it needs to react to new Order-related state (e.g., maybe showing stock changes)
- Follow the Increment 2 design system exactly — new templates must look like they belong with the rest of the site, not like a bolted-on feature

---

## 9. Increment 4 spec — Reviews

- New `Review` entity: id, user (FK), rating (1–5), comment, createdAt, approved (bool, default true for now — no moderation queue yet unless requested)
- Public POST endpoint for logged-in users to submit a review
- Auto-sliding review carousel injected into `index.html`, pure CSS/JS (no external library), consistent with the "one deliberate motion moment" principle from §4 — this carousel is the deliberate motion, so don't add competing animations elsewhere on the homepage
- New `ReviewRepository`, `ReviewService`, `ReviewController`

---

## 10. Increment 5 spec — Admin sales/adoption stats

- New `StatsService` aggregating: total store revenue, order count, top-selling items, pending adoption applications count, total pets by status, total users
- Rendered on `admin-dashboard.html` as stat cards (there's already a stat-card visual pattern established in Increment 2 — reuse it, don't invent a new one) plus a small chart (Chart.js via CDN, no build step — matches the project's "no frontend build tooling" constraint)
- No new entity needed — this is aggregate queries over existing tables (Order/OrderItem from Increment 3, AdoptionApplication, Pet, User)

---

## 11. Increment 6 spec — Production Deployment

**Blocking issue to resolve first:** Local disk image storage (`LocalImageStorageService`) will NOT survive a Render deploy — Render's filesystem is ephemeral and wipes on every restart/redeploy. This must be swapped to Cloudinary before deployment, not after.

**Steps:**
1. Swap `LocalImageStorageService` for a Cloudinary-backed implementation (single `CLOUDINARY_URL` env var, minimal SDK usage)
2. Update any templates/services referencing local `/images/**` URLs to use Cloudinary-returned URLs instead
3. Provision a free Postgres instance for production (Render Postgres, or another free provider — user has not locked in which one yet since they don't currently have a Supabase project; confirm with user before assuming)
4. Set production env vars on Render: `DB_URL`, `DB_USER`, `DB_PASS`, `CLOUDINARY_URL`, `PORT`
5. Verify `Dockerfile` builds cleanly for Render's build process (multistage build already exists — confirm `EXPOSE`/port config matches `server.port=${PORT:9090}` in `application.properties`)
6. `render.yaml` (optional but recommended) for one-click future redeploys
7. Full smoke test on the live Render URL — signup, adopt a pet, buy something through checkout, admin login, admin CRUD

---

## 12. Working conventions for whoever (human or AI) continues this project

- **Increments are additive and self-contained.** Each one should leave the app in a fully working, boot-able state — never leave a half-wired feature that breaks existing pages.
- **Match existing naming and package conventions exactly.** Package root is `com.seu.petadoptionservice`; controllers/services/repositories/entities follow simple, un-prefixed names (`PetController`, not `PetControllerImpl` or `PetMvcController`).
- **No new frontend frameworks or build steps.** This is server-rendered Thymeleaf with vanilla CSS/JS. Don't introduce React, Vue, Tailwind's CLI/build step, Sass compilation, or npm-based tooling — it doesn't fit this stack or the user's hosting constraints.
- **Don't touch locked-in decisions (§2) without the user explicitly reopening them.** If something in this document seems wrong or outdated relative to the actual uploaded code, trust the code, but flag the discrepancy to the user rather than silently deciding which is correct.
- **Update §7's status table** at the end of any session where an increment's status changes.
- **Before starting UI work in any increment**, actually open and read the current `style.css` and at least 2–3 existing templates in the relevant section (public/user/admin) to match established patterns — don't design from this document's description alone, since implementation details (exact hex codes, exact class names) live in the code, not here.
- **This document describes intent and decisions, not verified current code state.** At the start of any new session, if the user has uploaded a current project zip, inspect it to confirm this document's claims about "what's done" are still accurate before proceeding — code drift between sessions is possible (user may have made manual edits).

---

## 13. How to resume work using this document

If you are an AI picking this up fresh:
1. Read this entire document.
2. Ask the user for the current project zip if not already provided.
3. Inspect the actual code — confirm entities, controllers, templates, and `style.css` match what §5–§7 claim.
4. Check §7's status table for the next `NOT STARTED` or `IN PROGRESS` increment.
5. Read that increment's spec section (§8–§11) in full.
6. Confirm scope with the user in one short message before writing code, especially if the spec has any open question (e.g., §11's "which free Postgres provider" question).
7. Build the increment, keeping it self-contained and fully working end-to-end.
8. Update §7's status table.
9. Summarize what changed for the user in plain terms, same as prior increments' summary documents.

# Spring Boot Social Media Backend

A premium, production-grade Spring Boot 4.x RESTful API backend for a Social Media Platform. Built with Java 17, Spring Security, JWT authentication, and Hibernate/JPA, featuring Supabase PostgreSQL and Supabase Storage integrations.

---

## 🚀 Features

### 1. Security & Identity Management
- **Stateless JWT Authentication:** Fully secured routes using stateless JSON Web Tokens.
- **Secure Password Hashing:** Uses `BCryptPasswordEncoder` for cryptographic hashing and verification of passwords.
- **Double-Opt-In Verification:** Transaction-managed email verification mechanism sending HTML links via Gmail SMTP.
- **Insecure Direct Object Reference (IDOR) Protection:** Profile updates are bound strictly to the security context principal.

### 2. Core Interactions
- **Social Graph:** Follow/unfollow mechanism to connect users.
- **Post Sharing:** Create, edit, and delete posts. Supports uploading images/videos to **Supabase Storage**.
- **User Engagement:** High-performance toggled Likes and Comments on posts.

### 3. Discovery & Delivery
- **Custom Chronological Feed:** Custom paginated query combining public posts and posts from followed accounts.
- **Global search:** Endpoint for cross-entity searching of users and posts with pagination support.
- **Health Check:** Subpackaged ping routes for API diagnostics.

### 4. Interactive SPA Frontend
- **Single Page Application:** Fast, dynamic client interface served directly from the Spring Boot server (`http://localhost:8080/`).
- **Responsive Layout:** Beautiful fluid design (Flexbox/Grid/CSS Media Queries) supporting mobile, tablet, and desktop viewports.
- **Aesthetic Styling:** Modern design styled with soft pastels, glassmorphism overlays (`backdrop-filter`), smooth hover transitions, and user experience micro-animations.
- **Client Side State:** Stateful local session storage for JWT token authentication, enabling smooth profile updates, interactive searches, real-time comment expansion, and dynamic following.

### 5. Snapchat & Instagram Hybrid Features
- **Temporal Stories (24h expiry):** Post images/videos as stories that automatically disappear after 24 hours (supported by a background Spring scheduler checking hourly).
- **Interactive Story Viewer:** Watch stories in a full-screen player modal complete with segmented visual progress timelines.
- **Direct Snaps & Streaks:** Engage in real-time private messages. Support "View Once" snaps that disappear from the chat feed once clicked, and daily streaks 🔥 that increment when users message each other daily.
- **Explore Grid (Instagram style):** Browse trending public vibes sorted by likes and comments popularity in a 3-column responsive photo grid.

---

## 💻 Frontend Interface

The application features an interactive, responsive frontend. Once the Spring Boot application is running locally:
1. Open your browser and navigate to `http://localhost:8080/`.
2. **Register/Login:** Unauthenticated users are served the login/register screen. Passwords are encrypted on the backend using BCrypt.
3. **Workspace Navigation (Instagram / Snapchat Blend):**
   - **🏠 Home Feed:** View chronological posts, post comments, like vibes, and view **Active Stories** at the top. Click your avatar to post a new 24h story!
   - **🧭 Explore Grid:** Search public trending media arranged in a 3-column Instagram photo grid. Click any image to view details, like, and comment in a modal drawer.
   - **💬 Chat Messages:** Chat with friends. Toggle the **⚡ Snap** button to send disappearing, view-once photos/videos, and maintain your **Daily Streak 🔥** count.
   - **Profile Sidebar:** Track profile stats, toggle account visibility (Public/Private), and edit credentials.

---

## 🛠️ Technology Stack

- **Framework:** Spring Boot 4.0.2 & Java 17
- **Database:** PostgreSQL (Supabase Pooler)
- **Database ORM:** Spring Data JPA / Hibernate
- **Security:** Spring Security (stateless filters, role mapping)
- **External Storage Integration:** Spring WebFlux (`WebClient`) connecting to Supabase Object Storage
- **Email Delivery:** Spring Mail (`JavaMailSender`) via SMTP
- **Lombok:** Boilerplate reduction (annotations for DTOs/Entities)
- **Local/Test DB:** H2 Database (isolated memory profiles)

---

## ⚙️ Configuration & Setup

### Database & External Credentials
Create a file named `src/main/resources/application.properties` and configure the following parameters:

```properties
spring.application.name=CRUD
server.port=8080

# PostgreSQL (e.g., Supabase)
spring.datasource.url=jdbc:postgresql://<your-db-host>:<port>/postgres
spring.datasource.username=<username>
spring.datasource.password=<password>
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# SMTP Mail Server (Gmail Example)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=<email@gmail.com>
spring.mail.password=<app-password>
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Supabase Storage Integration
supabase.url=https://<ref>.supabase.co
supabase.key=<service-role-key-or-anon-key>
supabase.bucket=<bucket-name>

# JWT Security
jwt.secret=<your-custom-base64-or-text-secret-key>
```

---

## 🧪 Testing and Isolation

To run tests without depending on the live database or SMTP servers, the project features an isolated test configuration (`src/test/resources/application.properties`) backed by an in-memory **H2 Database**.

To run unit tests:
```bash
./mvnw test
```

To compile the application:
```bash
./mvnw clean compile
```

To run the application:
```bash
./mvnw spring-boot:run
```

---

## 🔮 Future Roadmap (Proposed Features)

Below is a module-wise roadmap of additional features that can be built on top of this backend codebase:

### 📦 1. Authentication & Profile Module
- **OAuth2 Integration:** Support logging in with third-party identity providers like Google, GitHub, or Facebook.
- **Two-Factor Authentication (2FA):** Integrate TOTP (Time-Based One-Time Password) verification using Google Authenticator or SMS.
- **Password Reset Flow:** Request short-lived email tokens to safely reset forgotten passwords.
- **Profile Customization:** Support bio sections, custom headers, external links, and verification badges.

### 📦 2. Engagement & Social Graph Module
- **Private Accounts & Follow Requests:** Allow users to set their profile to private. When private, follow attempts generate "Pending" requests that must be explicitly approved.
- **Block & Mute Lists:** Allow users to block or mute accounts to restrict interactions and visibility.
- **Bookmarks/Saved Posts:** Let users save posts to private folders/collections.

### 📦 3. Interactive Posting Module
- **Drafts & Scheduled Posts:** Allow users to draft posts or schedule them to be published at a later date/time using Spring's `@Scheduled` tasks.
- **Multi-Media Support:** Expand database structures to support multiple images/videos per post.
- **Mentions & Hashtags:** Parse `@username` and `#tag` within posts and trigger system notifications/indexing automatically.

### 📦 4. Interactions (Likes/Comments) Module
- **Threaded / Nested Comments:** Support multi-level comment replies (replies to comments) for structured conversations.
- **Comment Likes:** Allow users to express appreciation for specific comments.
- **Reactions System:** Support multiple emojis (e.g. Heart, Haha, Wow, Sad, Angry) instead of a simple binary Like.

### 📦 5. Real-Time Notification Module
- **WebSocket Push Notifications:** Notify users instantly when they receive likes, comments, replies, or new follow requests using Spring WebSockets.
- **Notification Center:** API endpoint to fetch read/unread notifications.
- **Weekly Digests:** Send recurring email digests containing feed highlights.

### 📦 6. Feed & Search Optimization Module
- **Redis Feed Caching:** Cache timelines to prevent intensive database querying during high traffic.
- **Elasticsearch Integration:** Replace basic JPQL queries with full-text search engines to enable fuzzy searches, username suggestions, and trending topic rankings.
- **Algorithmic Feed Routing:** Integrate relevance scores based on user interests, likes, and comment velocity rather than strictly chronological timelines.

### 📦 7. Administration & System Governance Module
- **API Rate Limiting:** Implement rate limiting (e.g., using Redis or Bucket4j) on critical endpoints to prevent brute-force attacks and DDoS.
- **Content Moderation Admin Console:** Build endpoints for moderators to review flagged content, delete abusive comments, or suspend user accounts.
- **JWT Refresh Tokens:** Introduce refresh token rotation (RTR) to keep users logged in securely without exposing long-lived access tokens.

# Blog-project
Full Stack Blog Application built with Spring Boot, Thymeleaf, and MySQL featuring CRUD operations, pagination, search, filtering, sorting, and role-based authentication using Spring Security.

---

##  Features

- 1. Full CRUD for Posts and Comments
- 2. Pagination (`?start=1&limit=10`)
- 3. Full-text Search across title, content, author, tags
- 4. Filter by author and/or tag
- 5. Sort by date, title, or author
- 6. Tag system with reusable tags (Many-to-Many)
- 7. Auto-seeded sample data on startup

---

## 🛠️ Tech Stack

| Layer       | Technology              |
|-------------|-------------------------|
| Framework   | Spring Boot 3.2         |
| MVC         | Spring MVC              |
| Database    | Spring Data JPA + MySQL |
| Templates   | Thymeleaf               |
| Build Tool  | Maven                   |
| Java        | Java 17                 |

---

##  Setup Instructions

### 1. Prerequisites
- Java 17+
- Maven
- MySQL running locally

### 2. Create the Database
```sql
CREATE DATABASE blogdb;
```

### 3. Configure application.properties
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/blogdb
spring.datasource.username=root
spring.datasource.password=yourpassword
```

### 4. Run the Application
```bash
mvn spring-boot:run
```

### 5. Open in Browser
```
http://localhost:8080
```

---

##  Project Structure

```
src/main/java/com/blog/
├── BlogApplication.java
├── config/
│   └── DataSeeder.java         ← Seeds sample data on startup
├── entity/
│   ├── Post.java
│   ├── Tag.java
│   └── Comment.java
├── repository/
│   ├── PostRepository.java
│   ├── TagRepository.java
│   └── CommentRepository.java
├── service/
│   ├── PostService.java
│   └── CommentService.java
└── controller/
    ├── PostController.java
    └── CommentController.java

src/main/resources/
├── templates/
│   ├── posts/
│   │   ├── index.html          ← Blog list with search/filter/sort/pagination
│   │   ├── view.html           ← Single post + comments
│   │   └── form.html           ← Create / Edit post
│   └── comments/
│       └── edit.html           ← Edit comment
├── static/css/
│   └── style.css
└── application.properties
```

---

##  URL Reference

| URL | Method | Description |
|-----|--------|-------------|
| `/` | GET | List all posts |
| `/?search=java` | GET | Search posts |
| `/?author=Alice&tagId=1` | GET | Filter posts |
| `/?sortField=title&order=asc` | GET | Sort posts |
| `/?start=11&limit=10` | GET | Page 2 |
| `/posts/{id}` | GET | View single post |
| `/posts/new` | GET | New post form |
| `/posts` | POST | Create post |
| `/posts/{id}/edit` | GET | Edit post form |
| `/posts/{id}/update` | POST | Update post |
| `/posts/{id}/delete` | POST | Delete post |
| `/posts/{id}/comments` | POST | Add comment |
| `/posts/{postId}/comments/{id}/edit` | GET | Edit comment form |
| `/posts/{postId}/comments/{id}/update` | POST | Update comment |
| `/posts/{postId}/comments/{id}/delete` | POST | Delete comment |

---

##  Database Schema

```
posts       ←→ post_tags ←→ tags
posts        →  comments
```

---

##  Roadmap

- [ ] Part 2: Spring Security (Authentication + Author/Admin roles)
- [ ] Part 3: Deploy on AWS EC2 / Heroku
- [ ] Part 4: REST APIs with JSON responses

package com.blog.config;

import com.blog.entity.Post;
import com.blog.entity.Tag;
import com.blog.repository.PostRepository;
import com.blog.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final PostRepository postRepository;
    private final TagRepository  tagRepository;

    @Override
    public void run(String... args) {
        if (postRepository.count() > 0) return; // already seeded

        Tag springTag  = tagRepository.save(Tag.builder().name("Spring Boot").build());
        Tag javaTag    = tagRepository.save(Tag.builder().name("Java").build());
        Tag webTag     = tagRepository.save(Tag.builder().name("Web Dev").build());
        Tag dbTag      = tagRepository.save(Tag.builder().name("Database").build());
        Tag devopsTag  = tagRepository.save(Tag.builder().name("DevOps").build());

        postRepository.saveAll(List.of(
            Post.builder()
                .title("Getting Started with Spring Boot")
                .excerpt("A beginner-friendly introduction to building web apps with Spring Boot.")
                .content("Spring Boot makes it easy to create stand-alone, production-grade Spring-based applications. " +
                         "In this post, we'll walk through setting up your first Spring Boot project from scratch. " +
                         "We'll cover auto-configuration, embedded servers, and starter dependencies that make Spring Boot so powerful.")
                .author("Alice Johnson")
                .publishedAt(LocalDateTime.now().minusDays(10))
                .isPublished(true)
                .tags(List.of(springTag, javaTag))
                .build(),

            Post.builder()
                .title("Spring Data JPA Deep Dive")
                .excerpt("Mastering repositories, custom queries, and relationships with Spring Data JPA.")
                .content("Spring Data JPA dramatically simplifies database access in Java applications. " +
                         "This post explores derived queries, @Query annotations, pagination with Pageable, " +
                         "and mapping complex entity relationships like @OneToMany and @ManyToMany.")
                .author("Bob Smith")
                .publishedAt(LocalDateTime.now().minusDays(7))
                .isPublished(true)
                .tags(List.of(springTag, dbTag))
                .build(),

            Post.builder()
                .title("Thymeleaf Templates in Spring MVC")
                .excerpt("Building dynamic HTML pages using Thymeleaf template engine.")
                .content("Thymeleaf is a modern server-side Java template engine that integrates seamlessly with Spring MVC. " +
                         "In this guide, we explore th:each, th:if, th:href, fragments, and layouts to build " +
                         "reusable, dynamic HTML pages without any JavaScript framework.")
                .author("Alice Johnson")
                .publishedAt(LocalDateTime.now().minusDays(5))
                .isPublished(true)
                .tags(List.of(springTag, webTag))
                .build(),

            Post.builder()
                .title("REST APIs with Spring Boot")
                .excerpt("Design and build production-ready REST APIs using Spring Boot and Jackson.")
                .content("Building REST APIs with Spring Boot is straightforward thanks to @RestController and @RequestMapping. " +
                         "This article covers request mapping, DTOs, validation, error handling with @ControllerAdvice, " +
                         "and testing your APIs with MockMvc.")
                .author("Charlie Brown")
                .publishedAt(LocalDateTime.now().minusDays(3))
                .isPublished(true)
                .tags(List.of(springTag, webTag, javaTag))
                .build(),

            Post.builder()
                .title("Docker & Spring Boot: A Perfect Pair")
                .excerpt("Containerize your Spring Boot application with Docker for consistent deployments.")
                .content("Docker allows you to package your Spring Boot application and all its dependencies into a portable container. " +
                         "This post walks you through writing a Dockerfile, building an image, running containers, " +
                         "and using Docker Compose to spin up your app with a MySQL database in seconds.")
                .author("Bob Smith")
                .publishedAt(LocalDateTime.now().minusDays(1))
                .isPublished(true)
                .tags(List.of(springTag, devopsTag))
                .build()
        ));

        System.out.println("✅ Sample data seeded successfully!");
    }
}

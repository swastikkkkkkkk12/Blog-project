package com.blog.repository;

import com.blog.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // Full-text search across title, content, author, excerpt, and tags
    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN p.tags t WHERE " +
           "LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.content) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.author) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.excerpt) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Post> searchPosts(@Param("search") String search, Pageable pageable);

    // Filter by author name (partial match)
    Page<Post> findByAuthorContainingIgnoreCase(String author, Pageable pageable);

    // Filter by tag id
    @Query("SELECT DISTINCT p FROM Post p JOIN p.tags t WHERE t.id = :tagId")
    Page<Post> findByTagId(@Param("tagId") Long tagId, Pageable pageable);

    // Filter by author AND tag
    @Query("SELECT DISTINCT p FROM Post p JOIN p.tags t WHERE " +
           "LOWER(p.author) LIKE LOWER(CONCAT('%', :author, '%')) AND t.id = :tagId")
    Page<Post> findByAuthorAndTagId(@Param("author") String author,
                                    @Param("tagId") Long tagId,
                                    Pageable pageable);
}

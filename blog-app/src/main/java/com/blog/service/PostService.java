package com.blog.service;

import com.blog.entity.Post;
import com.blog.entity.Tag;
import com.blog.repository.PostRepository;
import com.blog.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;

    // ─── READ ────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<Post> getAllPosts(int start, int limit, String sortField, String order) {
        int page = (start - 1) / limit;
        Sort sort = order.equalsIgnoreCase("asc")
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page, limit, sort);
        return postRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
    }

    // ─── SEARCH ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<Post> searchPosts(String search, int start, int limit) {
        int page = (start - 1) / limit;
        Pageable pageable = PageRequest.of(page, limit, Sort.by("publishedAt").descending());
        return postRepository.searchPosts(search, pageable);
    }

    // ─── FILTER ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<Post> filterPosts(String author, Long tagId, int start, int limit,
                                   String sortField, String order) {
        int page = (start - 1) / limit;
        Sort sort = order.equalsIgnoreCase("asc")
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page, limit, sort);

        boolean hasAuthor = author != null && !author.isBlank();
        boolean hasTag = tagId != null;

        if (hasAuthor && hasTag) {
            return postRepository.findByAuthorAndTagId(author, tagId, pageable);
        } else if (hasAuthor) {
            return postRepository.findByAuthorContainingIgnoreCase(author, pageable);
        } else if (hasTag) {
            return postRepository.findByTagId(tagId, pageable);
        } else {
            return postRepository.findAll(pageable);
        }
    }

    // ─── CREATE ──────────────────────────────────────────────────────────────

    public Post createPost(Post post, String tagsCsv) {
        List<Tag> tags = parseTags(tagsCsv);
        post.setTags(tags);
        post.setPublished(true);
        return postRepository.save(post);
    }

    // ─── UPDATE ──────────────────────────────────────────────────────────────

    public Post updatePost(Long id, Post updatedPost, String tagsCsv) {
        Post existing = getPostById(id);
        existing.setTitle(updatedPost.getTitle());
        existing.setContent(updatedPost.getContent());
        existing.setExcerpt(updatedPost.getExcerpt());
        existing.setAuthor(updatedPost.getAuthor());
        existing.setPublishedAt(updatedPost.getPublishedAt());
        existing.setPublished(updatedPost.isPublished());

        List<Tag> tags = parseTags(tagsCsv);
        existing.getTags().clear();
        existing.getTags().addAll(tags);

        return postRepository.save(existing);
    }

    // ─── DELETE ──────────────────────────────────────────────────────────────

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────

    private List<Tag> parseTags(String tagsCsv) {
        if (tagsCsv == null || tagsCsv.isBlank()) return List.of();
        return List.of(tagsCsv.split(",")).stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(name -> tagRepository.findByNameIgnoreCase(name)
                        .orElseGet(() -> tagRepository.save(
                                Tag.builder().name(name).build())))
                .collect(Collectors.toList());
    }

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    public String tagsToString(Post post) {
        return post.getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.joining(", "));
    }
}

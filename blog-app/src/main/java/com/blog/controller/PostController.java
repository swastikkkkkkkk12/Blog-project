package com.blog.controller;

import com.blog.config.DemoMode;
import com.blog.entity.Comment;
import com.blog.entity.Post;
import com.blog.service.CommentService;
import com.blog.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class PostController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("publishedAt", "title", "author");
    private static final Set<String> ALLOWED_SORT_ORDERS = Set.of("asc", "desc");

    private final PostService postService;
    private final CommentService commentService;
    private final DemoMode demoMode;

    // ─────────────────────────────────────────────────────────────────────────
    // LIST — GET /
    // Supports: ?start=1&limit=10&sortField=publishedAt&order=desc
    //           ?search=keyword
    //           ?author=name&tagId=1
   
    @GetMapping("/")
    public String listPosts(
            @RequestParam(defaultValue = "1")      int start,
            @RequestParam(defaultValue = "10")     int limit,
            @RequestParam(defaultValue = "publishedAt") String sortField,
            @RequestParam(defaultValue = "desc")   String order,
            @RequestParam(required = false)        String search,
            @RequestParam(required = false)        String author,
            @RequestParam(required = false)        Long tagId,
            Model model) {

        validateListingParameters(start, limit, sortField, order);

        Page<Post> posts;

        if (search != null && !search.isBlank()) {
            posts = postService.searchPosts(search, start, limit);
        } else if ((author != null && !author.isBlank()) || tagId != null) {
            posts = postService.filterPosts(author, tagId, start, limit, sortField, order);
        } else {
            posts = postService.getAllPosts(start, limit, sortField, order);
        }

        // Pagination helpers
        int prevStart = Math.max(1, start - limit);
        long nextStart = (long) start + limit;
        boolean hasPrev = start > 1;
        boolean hasNext = nextStart <= posts.getTotalElements();

        model.addAttribute("posts",      posts);
        model.addAttribute("tags",       postService.getAllTags());
        model.addAttribute("currentStart", start);
        model.addAttribute("limit",      limit);
        model.addAttribute("sortField",  sortField);
        model.addAttribute("order",      order);
        model.addAttribute("search",     search);
        model.addAttribute("author",     author);
        model.addAttribute("tagId",      tagId);
        model.addAttribute("prevStart",  prevStart);
        model.addAttribute("nextStart",  nextStart);
        model.addAttribute("hasPrev",    hasPrev);
        model.addAttribute("hasNext",    hasNext);
        model.addAttribute("readOnly",   demoMode.isReadOnly());
        return "posts/index";
    }

    private void validateListingParameters(int start, int limit, String sortField, String order) {
        if (start < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "start must be at least 1");
        }
        if (limit < 1 || limit > 50) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "limit must be between 1 and 50");
        }
        if (sortField == null || !ALLOWED_SORT_FIELDS.contains(sortField)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported sort field");
        }
        if (order == null || !ALLOWED_SORT_ORDERS.contains(order.toLowerCase(Locale.ROOT))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "order must be asc or desc");
        }
    }

    
    // VIEW SINGLE POST — GET /posts/{id}
  
    @GetMapping("/posts/{id}")
    public String viewPost(@PathVariable Long id, Model model) {
        Post post = postService.getPublishedPostById(id);
        model.addAttribute("post",     post);
        model.addAttribute("comments", commentService.getCommentsByPostId(id));
        model.addAttribute("newComment", new Comment());
        model.addAttribute("readOnly", demoMode.isReadOnly());
        return "posts/view";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE FORM — GET /posts/new
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/posts/new")
    public String showCreateForm(Model model) {
        demoMode.requireWritable();
        model.addAttribute("post",     new Post());
        model.addAttribute("tags",     postService.getAllTags());
        model.addAttribute("isEdit",   false);
        model.addAttribute("readOnly", demoMode.isReadOnly());
        return "posts/form";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE — POST /posts
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/posts")
    public String createPost(
            @Valid @ModelAttribute("post") Post post,
            BindingResult result,
            @RequestParam(defaultValue = "") String tagsCsv,
            RedirectAttributes ra,
            Model model) {

        demoMode.requireWritable();

        if (result.hasErrors()) {
            model.addAttribute("tags",   postService.getAllTags());
            model.addAttribute("isEdit", false);
            model.addAttribute("readOnly", demoMode.isReadOnly());
            return "posts/form";
        }

        postService.createPost(post, tagsCsv);
        ra.addFlashAttribute("successMessage", "Post created successfully!");
        return "redirect:/";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // EDIT FORM — GET /posts/{id}/edit
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/posts/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        demoMode.requireWritable();
        Post post = postService.getPostById(id);
        model.addAttribute("post",     post);
        model.addAttribute("tags",     postService.getAllTags());
        model.addAttribute("tagsCsv",  postService.tagsToString(post));
        model.addAttribute("isEdit",   true);
        model.addAttribute("readOnly", demoMode.isReadOnly());
        return "posts/form";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UPDATE — POST /posts/{id}/update
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/posts/{id}/update")
    public String updatePost(
            @PathVariable Long id,
            @Valid @ModelAttribute("post") Post post,
            BindingResult result,
            @RequestParam(defaultValue = "") String tagsCsv,
            RedirectAttributes ra,
            Model model) {

        demoMode.requireWritable();

        if (result.hasErrors()) {
            model.addAttribute("tags",   postService.getAllTags());
            model.addAttribute("isEdit", true);
            model.addAttribute("readOnly", demoMode.isReadOnly());
            return "posts/form";
        }

        Post updatedPost = postService.updatePost(id, post, tagsCsv);
        ra.addFlashAttribute("successMessage", "Post updated successfully!");
        return updatedPost.isPublished() ? "redirect:/posts/" + id : "redirect:/";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE — POST /posts/{id}/delete
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/posts/{id}/delete")
    public String deletePost(@PathVariable Long id, RedirectAttributes ra) {
        demoMode.requireWritable();
        postService.deletePost(id);
        ra.addFlashAttribute("successMessage", "Post deleted successfully!");
        return "redirect:/";
    }
}

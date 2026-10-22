package com.blog.controller;

import com.blog.config.DemoMode;
import com.blog.entity.Comment;
import com.blog.entity.Post;
import com.blog.service.CommentService;
import com.blog.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final PostService    postService;
    private final DemoMode       demoMode;

    // ─────────────────────────────────────────────────────────────────────────
    // ADD COMMENT — POST /posts/{postId}/comments
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/posts/{postId}/comments")
    public String addComment(
            @PathVariable Long postId,
            @Valid @ModelAttribute("newComment") Comment comment,
            BindingResult result,
            RedirectAttributes ra,
            Model model) {

        demoMode.requireWritable();

        if (result.hasErrors()) {
            Post post = postService.getPublishedPostById(postId);
            model.addAttribute("post",     post);
            model.addAttribute("comments", commentService.getCommentsByPostId(postId));
            model.addAttribute("readOnly", demoMode.isReadOnly());
            return "posts/view";
        }

        Post post = postService.getPublishedPostById(postId);
        commentService.addComment(comment, post);
        ra.addFlashAttribute("successMessage", "Comment added!");
        return "redirect:/posts/" + postId;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // EDIT COMMENT FORM — GET /posts/{postId}/comments/{commentId}/edit
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/posts/{postId}/comments/{commentId}/edit")
    public String showEditCommentForm(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            Model model) {
        demoMode.requireWritable();
        model.addAttribute("commentForm", commentService.getCommentByIdAndPostId(postId, commentId));
        model.addAttribute("postId",  postId);
        return "comments/edit";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UPDATE COMMENT — POST /posts/{postId}/comments/{commentId}/update
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/posts/{postId}/comments/{commentId}/update")
    public String updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @ModelAttribute("commentForm") Comment comment,
            BindingResult result,
            RedirectAttributes ra,
            Model model) {

        demoMode.requireWritable();

        if (result.hasErrors()) {
            model.addAttribute("postId", postId);
            return "comments/edit";
        }

        commentService.updateComment(postId, commentId, comment);
        ra.addFlashAttribute("successMessage", "Comment updated!");
        return "redirect:/posts/" + postId;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE COMMENT IN APP — POST /posts/{postId}/comments/{commentId}/delete
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/posts/{postId}/comments/{commentId}/delete")
    public String deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            RedirectAttributes ra) {

        demoMode.requireWritable();
        commentService.deleteComment(postId, commentId);
        ra.addFlashAttribute("successMessage", "Comment deleted!");
        return "redirect:/posts/" + postId;
    }
}

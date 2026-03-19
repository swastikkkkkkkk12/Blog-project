package com.blog.service;

import com.blog.entity.Comment;
import com.blog.entity.Post;
import com.blog.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;

    @Transactional(readOnly = true)
    public List<Comment> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId);
    }

    @Transactional(readOnly = true)
    public Comment getCommentById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + id));
    }

    public Comment addComment(Comment comment, Post post) {
        comment.setPost(post);
        return commentRepository.save(comment);
    }

    public Comment updateComment(Long id, Comment updatedComment) {
        Comment existing = getCommentById(id);
        existing.setName(updatedComment.getName());
        existing.setEmail(updatedComment.getEmail());
        existing.setComment(updatedComment.getComment());
        return commentRepository.save(existing);
    }

    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }
}

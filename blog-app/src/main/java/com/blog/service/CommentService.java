package com.blog.service;

import com.blog.entity.Comment;
import com.blog.entity.Post;
import com.blog.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
    }

    @Transactional(readOnly = true)
    public Comment getCommentByIdAndPostId(Long postId, Long commentId) {
        return commentRepository.findByIdAndPostId(commentId, postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
    }

    public Comment addComment(Comment comment, Post post) {
        comment.setPost(post);
        return commentRepository.save(comment);
    }

    public Comment updateComment(Long postId, Long id, Comment updatedComment) {
        Comment existing = getCommentByIdAndPostId(postId, id);
        existing.setName(updatedComment.getName());
        existing.setEmail(updatedComment.getEmail());
        existing.setComment(updatedComment.getComment());
        return commentRepository.save(existing);
    }

    public void deleteComment(Long postId, Long id) {
        commentRepository.delete(getCommentByIdAndPostId(postId, id));
    }
}

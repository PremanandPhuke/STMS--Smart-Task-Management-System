package com.stms.task.service;

import com.stms.task.dto.CommentDtos.CommentRequest;
import com.stms.task.dto.CommentDtos.CommentResponse;
import com.stms.task.entity.Comment;
import com.stms.task.entity.Role;
import com.stms.task.entity.Task;
import com.stms.task.entity.User;
import com.stms.task.exception.ResourceNotFoundException;
import com.stms.task.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskService taskService;

    @Transactional(readOnly = true)
    public List<CommentResponse> list(Long taskId, User user) {
        taskService.getViewable(taskId, user);
        return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId).stream().map(CommentService::toResponse).toList();
    }

    @Transactional
    public CommentResponse add(Long taskId, CommentRequest req, User user) {
        Task task = taskService.getViewable(taskId, user);
        Comment comment = new Comment();
        comment.setTask(task);
        comment.setAuthor(user);
        comment.setContent(req.content().trim());
        return toResponse(commentRepository.save(comment));
    }

    @Transactional
    public void delete(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment " + commentId + " not found"));
        if (user.getRole() != Role.ADMIN && !comment.getAuthor().getId().equals(user.getId())) {
            throw new AccessDeniedException("You can only delete your own comments");
        }
        commentRepository.delete(comment);
    }

    private static CommentResponse toResponse(Comment c) {
        return new CommentResponse(c.getId(), c.getContent(), c.getAuthor().getId(), c.getAuthor().getName(), c.getCreatedAt());
    }
}

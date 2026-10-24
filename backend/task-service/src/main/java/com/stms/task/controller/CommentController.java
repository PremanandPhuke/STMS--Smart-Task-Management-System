package com.stms.task.controller;

import com.stms.task.dto.CommentDtos.CommentRequest;
import com.stms.task.dto.CommentDtos.CommentResponse;
import com.stms.task.security.AppUserDetails;
import com.stms.task.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/api/tasks/{taskId}/comments")
    public List<CommentResponse> list(@PathVariable Long taskId, @AuthenticationPrincipal AppUserDetails p) {
        return commentService.list(taskId, p.getUser());
    }

    @PostMapping("/api/tasks/{taskId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse add(@PathVariable Long taskId, @Valid @RequestBody CommentRequest req,
                               @AuthenticationPrincipal AppUserDetails p) {
        return commentService.add(taskId, req, p.getUser());
    }

    @DeleteMapping("/api/comments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails p) {
        commentService.delete(id, p.getUser());
    }
}

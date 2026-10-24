package com.stms.task.controller;

import com.stms.task.dto.ProjectDtos.ProjectRequest;
import com.stms.task.dto.ProjectDtos.ProjectResponse;
import com.stms.task.security.AppUserDetails;
import com.stms.task.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public List<ProjectResponse> list(@AuthenticationPrincipal AppUserDetails p) {
        return projectService.list(p.getUser());
    }

    @GetMapping("/{id}")
    public ProjectResponse get(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails p) {
        return projectService.get(id, p.getUser());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(@Valid @RequestBody ProjectRequest req, @AuthenticationPrincipal AppUserDetails p) {
        return projectService.create(req, p.getUser());
    }

    @PutMapping("/{id}")
    public ProjectResponse update(@PathVariable Long id, @Valid @RequestBody ProjectRequest req,
                                  @AuthenticationPrincipal AppUserDetails p) {
        return projectService.update(id, req, p.getUser());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails p) {
        projectService.delete(id, p.getUser());
    }
}

package com.stms.task.service;

import com.stms.task.dto.ProjectDtos.ProjectRequest;
import com.stms.task.dto.ProjectDtos.ProjectResponse;
import com.stms.task.entity.Project;
import com.stms.task.entity.Role;
import com.stms.task.entity.User;
import com.stms.task.exception.ResourceNotFoundException;
import com.stms.task.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public List<ProjectResponse> list(User user) {
        List<Project> projects = user.getRole() == Role.ADMIN
                ? projectRepository.findAllByOrderByCreatedAtDesc()
                : projectRepository.findByOwnerIdOrderByCreatedAtDesc(user.getId());
        return projects.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse get(Long id, User user) {
        return toResponse(getManageable(id, user));
    }

    @Transactional
    public ProjectResponse create(ProjectRequest req, User user) {
        Project project = projectRepository.save(Project.builder()
                .name(req.name().trim())
                .description(req.description())
                .owner(user)
                .build());
        return toResponse(project);
    }

    @Transactional
    public ProjectResponse update(Long id, ProjectRequest req, User user) {
        Project project = getManageable(id, user);
        project.setName(req.name().trim());
        project.setDescription(req.description());
        return toResponse(project);
    }

    @Transactional
    public void delete(Long id, User user) {
        projectRepository.delete(getManageable(id, user));
    }

    /** Loads a project and makes sure the user is its owner (or an admin). */
    @Transactional(readOnly = true)
    public Project getManageable(Long id, User user) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project " + id + " not found"));
        boolean allowed = user.getRole() == Role.ADMIN || project.getOwner().getId().equals(user.getId());
        if (!allowed) {
            throw new AccessDeniedException("Not your project");
        }
        return project;
    }

    private ProjectResponse toResponse(Project p) {
        return new ProjectResponse(p.getId(), p.getName(), p.getDescription(),
                p.getOwner().getId(), p.getOwner().getName(), p.getCreatedAt());
    }
}

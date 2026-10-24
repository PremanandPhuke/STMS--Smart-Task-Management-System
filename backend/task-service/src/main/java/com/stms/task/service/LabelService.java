package com.stms.task.service;

import com.stms.task.dto.LabelDtos.LabelRequest;
import com.stms.task.dto.LabelDtos.LabelResponse;
import com.stms.task.entity.Label;
import com.stms.task.entity.Role;
import com.stms.task.entity.User;
import com.stms.task.exception.ConflictException;
import com.stms.task.exception.ResourceNotFoundException;
import com.stms.task.repository.LabelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LabelService {

    private final LabelRepository labelRepository;

    @Transactional(readOnly = true)
    public List<LabelResponse> list(User user) {
        return labelRepository.findByOwnerIdOrderByNameAsc(user.getId()).stream().map(LabelService::toResponse).toList();
    }

    @Transactional
    public LabelResponse create(LabelRequest req, User user) {
        String name = req.name().trim();
        if (labelRepository.existsByOwnerIdAndNameIgnoreCase(user.getId(), name)) {
            throw new ConflictException("You already have a label called \"" + name + "\"");
        }
        Label label = labelRepository.save(Label.builder()
                .name(name)
                .color(req.color() != null ? req.color() : "#0f766e")
                .owner(user)
                .build());
        return toResponse(label);
    }

    @Transactional
    public void delete(Long id, User user) {
        Label label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label " + id + " not found"));
        if (user.getRole() != Role.ADMIN && !label.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("Not your label");
        }
        labelRepository.delete(label);
    }

    static LabelResponse toResponse(Label l) {
        return new LabelResponse(l.getId(), l.getName(), l.getColor());
    }
}

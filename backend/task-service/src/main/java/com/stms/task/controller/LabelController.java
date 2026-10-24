package com.stms.task.controller;

import com.stms.task.dto.LabelDtos.LabelRequest;
import com.stms.task.dto.LabelDtos.LabelResponse;
import com.stms.task.security.AppUserDetails;
import com.stms.task.service.LabelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/labels")
@RequiredArgsConstructor
public class LabelController {

    private final LabelService labelService;

    @GetMapping
    public List<LabelResponse> list(@AuthenticationPrincipal AppUserDetails p) {
        return labelService.list(p.getUser());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LabelResponse create(@Valid @RequestBody LabelRequest req, @AuthenticationPrincipal AppUserDetails p) {
        return labelService.create(req, p.getUser());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails p) {
        labelService.delete(id, p.getUser());
    }
}

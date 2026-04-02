package com.gpp.bankmanagement.controller;

import com.gpp.bankmanagement.dto.MessageResponse;
import com.gpp.bankmanagement.dto.ProjectionStatusResponse;
import com.gpp.bankmanagement.service.ProjectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projections")
public class ProjectionController {

    private final ProjectionService projectionService;

    public ProjectionController(ProjectionService projectionService) {
        this.projectionService = projectionService;
    }

    @PostMapping("/rebuild")
    public ResponseEntity<MessageResponse> rebuild() {
        projectionService.rebuild();
        return ResponseEntity.accepted().body(new MessageResponse("Projection rebuild initiated."));
    }

    @GetMapping("/status")
    public ProjectionStatusResponse status() {
        return projectionService.getStatus();
    }
}
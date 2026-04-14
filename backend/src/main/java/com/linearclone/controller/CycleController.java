package com.linearclone.controller;

import com.linearclone.dto.response.ApiResponse;
import com.linearclone.service.impl.CycleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cycles")
@RequiredArgsConstructor
@Tag(name = "Cycles", description = "Sprint/cycle management")
public class CycleController {

    private final CycleService cycleService;

    @PostMapping("/team/{teamId}")
    @Operation(summary = "Create a new cycle for a team")
    public ResponseEntity<ApiResponse.CycleResponse> createCycle(
            @PathVariable UUID teamId,
            @Valid @RequestBody CreateCycleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                cycleService.createCycle(teamId, request.name, request.description,
                        request.startDate, request.endDate));
    }

    @GetMapping("/team/{teamId}")
    @Operation(summary = "Get all cycles for a team")
    public ResponseEntity<List<ApiResponse.CycleResponse>> getTeamCycles(@PathVariable UUID teamId) {
        return ResponseEntity.ok(cycleService.getTeamCycles(teamId));
    }

    @GetMapping("/team/{teamId}/active")
    @Operation(summary = "Get the currently active cycle for a team")
    public ResponseEntity<ApiResponse.CycleResponse> getActiveCycle(@PathVariable UUID teamId) {
        return ResponseEntity.ok(cycleService.getActiveCycle(teamId));
    }

    @PostMapping("/{cycleId}/start")
    @Operation(summary = "Start a draft cycle")
    public ResponseEntity<ApiResponse.CycleResponse> startCycle(@PathVariable UUID cycleId) {
        return ResponseEntity.ok(cycleService.startCycle(cycleId));
    }

    @PostMapping("/{cycleId}/complete")
    @Operation(summary = "Complete an active cycle")
    public ResponseEntity<ApiResponse.CycleResponse> completeCycle(@PathVariable UUID cycleId) {
        return ResponseEntity.ok(cycleService.completeCycle(cycleId));
    }

    @PostMapping("/{cycleId}/issues/{issueId}")
    @Operation(summary = "Add an issue to a cycle")
    public ResponseEntity<Void> addIssue(@PathVariable UUID cycleId, @PathVariable UUID issueId) {
        cycleService.addIssueToCycle(cycleId, issueId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/issues/{issueId}")
    @Operation(summary = "Remove an issue from its current cycle")
    public ResponseEntity<Void> removeIssue(@PathVariable UUID issueId) {
        cycleService.removeIssueFromCycle(issueId);
        return ResponseEntity.noContent().build();
    }

    @Data
    public static class CreateCycleRequest {
        @NotBlank private String name;
        private String description;
        @NotNull private LocalDate startDate;
        @NotNull private LocalDate endDate;
    }
}

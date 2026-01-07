package com.linearclone.controller;

import com.linearclone.dto.request.WorkspaceRequest;
import com.linearclone.dto.response.ApiResponse;
import com.linearclone.service.impl.WorkspaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/workspaces")
@RequiredArgsConstructor
@Tag(name = "Workspaces", description = "Manage workspaces and members")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    @Operation(summary = "Create a new workspace")
    public ResponseEntity<ApiResponse.WorkspaceResponse> create(
            @Valid @RequestBody WorkspaceRequest.Create request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workspaceService.createWorkspace(request));
    }

    @GetMapping
    @Operation(summary = "Get all workspaces for the current user")
    public ResponseEntity<List<ApiResponse.WorkspaceResponse>> getMyWorkspaces() {
        return ResponseEntity.ok(workspaceService.getMyWorkspaces());
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get a workspace by slug")
    public ResponseEntity<ApiResponse.WorkspaceResponse> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(workspaceService.getWorkspaceBySlug(slug));
    }

    @PatchMapping("/{workspaceId}")
    @Operation(summary = "Update workspace details")
    public ResponseEntity<ApiResponse.WorkspaceResponse> update(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody WorkspaceRequest.Update request) {
        return ResponseEntity.ok(workspaceService.updateWorkspace(workspaceId, request));
    }

    @PostMapping("/{workspaceId}/members")
    @Operation(summary = "Invite a user to the workspace")
    public ResponseEntity<Void> inviteMember(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody WorkspaceRequest.InviteMember request) {
        workspaceService.inviteMember(workspaceId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{workspaceId}/members/{userId}")
    @Operation(summary = "Remove a member from workspace")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID workspaceId,
            @PathVariable UUID userId) {
        workspaceService.removeMember(workspaceId, userId);
        return ResponseEntity.noContent().build();
    }
}

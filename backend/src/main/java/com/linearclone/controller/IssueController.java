package com.linearclone.controller;

import com.linearclone.dto.request.IssueRequest;
import com.linearclone.dto.response.ApiResponse;
import com.linearclone.entity.Issue;
import com.linearclone.service.impl.IssueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/issues")
@RequiredArgsConstructor
@Tag(name = "Issues", description = "Create, read, update, and delete issues")
public class IssueController {

    private final IssueService issueService;

    @PostMapping
    @Operation(summary = "Create a new issue")
    public ResponseEntity<ApiResponse.IssueResponse> createIssue(
            @Valid @RequestBody IssueRequest.Create request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(issueService.createIssue(request));
    }

    @GetMapping("/team/{teamId}")
    @Operation(summary = "Get paginated issues for a team")
    public ResponseEntity<ApiResponse.PagedResponse<ApiResponse.IssueResponse>> getTeamIssues(
            @PathVariable UUID teamId,
            @PageableDefault(size = 25, sort = "createdAt") Pageable pageable) {
        Page<ApiResponse.IssueResponse> page = issueService.getTeamIssues(teamId, pageable);
        return ResponseEntity.ok(ApiResponse.PagedResponse.from(page));
    }

    @GetMapping("/team/{teamId}/filter")
    @Operation(summary = "Filter issues by status, priority, assignee")
    public ResponseEntity<List<ApiResponse.IssueResponse>> filterTeamIssues(
            @PathVariable UUID teamId,
            @RequestParam(required = false) Issue.IssueStatus status,
            @RequestParam(required = false) Issue.IssuePriority priority,
            @RequestParam(required = false) UUID assigneeId) {
        return ResponseEntity.ok(
                issueService.getTeamIssuesFiltered(teamId, status, priority, assigneeId));
    }

    @GetMapping("/team/{teamId}/search")
    @Operation(summary = "Search issues by title")
    public ResponseEntity<List<ApiResponse.IssueResponse>> searchIssues(
            @PathVariable UUID teamId,
            @RequestParam String q) {
        return ResponseEntity.ok(issueService.searchIssues(teamId, q));
    }

    @GetMapping("/{issueId}")
    @Operation(summary = "Get a single issue by ID")
    public ResponseEntity<ApiResponse.IssueResponse> getIssue(@PathVariable UUID issueId) {
        return ResponseEntity.ok(issueService.getIssue(issueId));
    }

    @PatchMapping("/{issueId}")
    @Operation(summary = "Update an issue (partial update)")
    public ResponseEntity<ApiResponse.IssueResponse> updateIssue(
            @PathVariable UUID issueId,
            @Valid @RequestBody IssueRequest.Update request) {
        return ResponseEntity.ok(issueService.updateIssue(issueId, request));
    }

    @DeleteMapping("/{issueId}")
    @Operation(summary = "Delete an issue")
    public ResponseEntity<Void> deleteIssue(@PathVariable UUID issueId) {
        issueService.deleteIssue(issueId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{issueId}/sub-issues")
    @Operation(summary = "Get sub-issues of a parent issue")
    public ResponseEntity<List<ApiResponse.IssueResponse>> getSubIssues(
            @PathVariable UUID issueId) {
        return ResponseEntity.ok(issueService.getSubIssues(issueId));
    }
}

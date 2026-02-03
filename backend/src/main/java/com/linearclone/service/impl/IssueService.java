package com.linearclone.service.impl;

import com.linearclone.dto.request.IssueRequest;
import com.linearclone.dto.response.ApiResponse;
import com.linearclone.entity.*;
import com.linearclone.exception.BadRequestException;
import com.linearclone.exception.ForbiddenException;
import com.linearclone.exception.ResourceNotFoundException;
import com.linearclone.repository.*;
import com.linearclone.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IssueService {

    private final IssueRepository issueRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final CycleRepository cycleRepository;
    private final LabelRepository labelRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Transactional
    public ApiResponse.IssueResponse createIssue(IssueRequest.Create request) {
        User currentUser = getCurrentUser();
        Team team = getTeamAndVerifyAccess(request.getTeamId(), currentUser);

        // Get next sequence number
        int nextSeq = issueRepository.findMaxSequenceByTeamId(team.getId()) + 1;

        Issue.IssueBuilder builder = Issue.builder()
                .team(team)
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : Issue.IssueStatus.BACKLOG)
                .priority(request.getPriority() != null ? request.getPriority() : Issue.IssuePriority.NONE)
                .sequenceNumber(nextSeq)
                .createdBy(currentUser)
                .estimate(request.getEstimate())
                .dueDate(request.getDueDate());

        // Optional relations
        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.getAssigneeId()));
            builder.assignee(assignee);
        }

        if (request.getProjectId() != null) {
            Project project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project", request.getProjectId()));
            builder.project(project);
        }

        if (request.getCycleId() != null) {
            Cycle cycle = cycleRepository.findById(request.getCycleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cycle", request.getCycleId()));
            builder.cycle(cycle);
        }

        if (request.getParentId() != null) {
            Issue parent = issueRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Issue", request.getParentId()));
            builder.parent(parent);
        }

        Issue issue = builder.build();

        // Labels
        if (request.getLabelIds() != null && !request.getLabelIds().isEmpty()) {
            Set<Label> labels = new HashSet<>(labelRepository.findAllById(request.getLabelIds()));
            issue.setLabels(labels);
        }

        issue = issueRepository.save(issue);
        log.info("Issue created: {} by user {}", issue.getIdentifier(), currentUser.getEmail());
        return ApiResponse.IssueResponse.from(issue);
    }

    @Transactional(readOnly = true)
    public Page<ApiResponse.IssueResponse> getTeamIssues(UUID teamId, Pageable pageable) {
        User currentUser = getCurrentUser();
        getTeamAndVerifyAccess(teamId, currentUser);
        return issueRepository.findByTeamId(teamId, pageable)
                .map(ApiResponse.IssueResponse::from);
    }

    @Transactional(readOnly = true)
    public List<ApiResponse.IssueResponse> getTeamIssuesFiltered(
            UUID teamId,
            Issue.IssueStatus status,
            Issue.IssuePriority priority,
            UUID assigneeId) {

        User currentUser = getCurrentUser();
        getTeamAndVerifyAccess(teamId, currentUser);
        return issueRepository.findByTeamIdWithFilters(teamId, status, priority, assigneeId)
                .stream().map(ApiResponse.IssueResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ApiResponse.IssueResponse getIssue(UUID issueId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue", issueId));
        User currentUser = getCurrentUser();
        verifyTeamAccess(issue.getTeam().getId(), currentUser);
        return ApiResponse.IssueResponse.from(issue);
    }

    @Transactional
    public ApiResponse.IssueResponse updateIssue(UUID issueId, IssueRequest.Update request) {
        User currentUser = getCurrentUser();
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue", issueId));
        verifyTeamAccess(issue.getTeam().getId(), currentUser);

        List<IssueActivity> activities = new ArrayList<>();

        if (request.getTitle() != null) {
            issue.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            issue.setDescription(request.getDescription());
        }
        if (request.getStatus() != null && request.getStatus() != issue.getStatus()) {
            activities.add(buildActivity(issue, currentUser, "status_changed",
                    issue.getStatus().name(), request.getStatus().name()));
            issue.setStatus(request.getStatus());
            if (request.getStatus() == Issue.IssueStatus.DONE) {
                issue.setCompletedAt(LocalDateTime.now());
            } else if (request.getStatus() == Issue.IssueStatus.CANCELLED) {
                issue.setCancelledAt(LocalDateTime.now());
            }
        }
        if (request.getPriority() != null && request.getPriority() != issue.getPriority()) {
            activities.add(buildActivity(issue, currentUser, "priority_changed",
                    issue.getPriority().name(), request.getPriority().name()));
            issue.setPriority(request.getPriority());
        }
        if (request.getAssigneeId() != null) {
            String oldAssignee = issue.getAssignee() != null ? issue.getAssignee().getDisplayName() : "None";
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.getAssigneeId()));
            issue.setAssignee(assignee);
            activities.add(buildActivity(issue, currentUser, "assignee_changed", oldAssignee, assignee.getDisplayName()));
        }
        if (request.getProjectId() != null) {
            Project project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project", request.getProjectId()));
            issue.setProject(project);
        }
        if (request.getCycleId() != null) {
            Cycle cycle = cycleRepository.findById(request.getCycleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cycle", request.getCycleId()));
            issue.setCycle(cycle);
        }
        if (request.getEstimate() != null) {
            issue.setEstimate(request.getEstimate());
        }
        if (request.getDueDate() != null) {
            issue.setDueDate(request.getDueDate());
        }
        if (request.getLabelIds() != null) {
            Set<Label> labels = new HashSet<>(labelRepository.findAllById(request.getLabelIds()));
            issue.setLabels(labels);
        }
        if (request.getSortOrder() != null) {
            issue.setSortOrder(request.getSortOrder());
        }

        issue.getActivities().addAll(activities);
        issue = issueRepository.save(issue);
        return ApiResponse.IssueResponse.from(issue);
    }

    @Transactional
    public void deleteIssue(UUID issueId) {
        User currentUser = getCurrentUser();
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue", issueId));
        verifyTeamAccess(issue.getTeam().getId(), currentUser);
        issueRepository.delete(issue);
        log.info("Issue deleted: {} by user {}", issue.getIdentifier(), currentUser.getEmail());
    }

    @Transactional(readOnly = true)
    public List<ApiResponse.IssueResponse> searchIssues(UUID teamId, String query) {
        User currentUser = getCurrentUser();
        getTeamAndVerifyAccess(teamId, currentUser);
        return issueRepository.searchByTitle(teamId, query)
                .stream().map(ApiResponse.IssueResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ApiResponse.IssueResponse> getSubIssues(UUID parentId) {
        return issueRepository.findByParentId(parentId)
                .stream().map(ApiResponse.IssueResponse::from).toList();
    }

    // ---- Helpers ----

    private Team getTeamAndVerifyAccess(UUID teamId, User user) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", teamId));
        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, user.getId())) {
            throw new ForbiddenException("You are not a member of this team");
        }
        return team;
    }

    private void verifyTeamAccess(UUID teamId, User user) {
        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, user.getId())) {
            throw new ForbiddenException("You are not a member of this team");
        }
    }

    private IssueActivity buildActivity(Issue issue, User actor, String type, String oldVal, String newVal) {
        return IssueActivity.builder()
                .issue(issue)
                .actor(actor)
                .activityType(type)
                .oldValue(oldVal)
                .newValue(newVal)
                .build();
    }

    private User getCurrentUser() {
        CustomUserDetailsService.CustomUserDetails details =
                (CustomUserDetailsService.CustomUserDetails) SecurityContextHolder
                        .getContext().getAuthentication().getPrincipal();
        return details.user();
    }
}

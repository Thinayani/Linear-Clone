package com.linearclone.dto.response;

import com.linearclone.entity.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ApiResponse {

    @Data
    @Builder
    public static class Auth {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private long expiresIn;
        private UserSummary user;
    }

    @Data
    @Builder
    public static class UserSummary {
        private UUID id;
        private String email;
        private String displayName;
        private String avatarUrl;

        public static UserSummary from(User user) {
            if (user == null) return null;
            return UserSummary.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .displayName(user.getDisplayName())
                    .avatarUrl(user.getAvatarUrl())
                    .build();
        }
    }

    @Data
    @Builder
    public static class WorkspaceResponse {
        private UUID id;
        private String name;
        private String slug;
        private String description;
        private String logoUrl;
        private boolean isActive;
        private LocalDateTime createdAt;
        private int memberCount;
        private int teamCount;

        public static WorkspaceResponse from(Workspace w) {
            return WorkspaceResponse.builder()
                    .id(w.getId())
                    .name(w.getName())
                    .slug(w.getSlug())
                    .description(w.getDescription())
                    .logoUrl(w.getLogoUrl())
                    .isActive(w.isActive())
                    .createdAt(w.getCreatedAt())
                    .memberCount(w.getMembers().size())
                    .teamCount(w.getTeams().size())
                    .build();
        }
    }

    @Data
    @Builder
    public static class TeamResponse {
        private UUID id;
        private UUID workspaceId;
        private String name;
        private String identifier;
        private String description;
        private String color;
        private String icon;
        private boolean isActive;
        private LocalDateTime createdAt;
        private int memberCount;

        public static TeamResponse from(Team t) {
            return TeamResponse.builder()
                    .id(t.getId())
                    .workspaceId(t.getWorkspace().getId())
                    .name(t.getName())
                    .identifier(t.getIdentifier())
                    .description(t.getDescription())
                    .color(t.getColor())
                    .icon(t.getIcon())
                    .isActive(t.isActive())
                    .createdAt(t.getCreatedAt())
                    .memberCount(t.getMembers().size())
                    .build();
        }
    }

    @Data
    @Builder
    public static class IssueResponse {
        private UUID id;
        private String identifier;
        private int sequenceNumber;
        private String title;
        private String description;
        private Issue.IssueStatus status;
        private Issue.IssuePriority priority;
        private UserSummary assignee;
        private UserSummary createdBy;
        private UUID teamId;
        private String teamIdentifier;
        private UUID projectId;
        private String projectName;
        private UUID cycleId;
        private String cycleName;
        private UUID parentId;
        private Integer estimate;
        private LocalDate dueDate;
        private LocalDateTime completedAt;
        private double sortOrder;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<LabelResponse> labels;
        private int subIssueCount;
        private int commentCount;

        public static IssueResponse from(Issue issue) {
            return IssueResponse.builder()
                    .id(issue.getId())
                    .identifier(issue.getIdentifier())
                    .sequenceNumber(issue.getSequenceNumber())
                    .title(issue.getTitle())
                    .description(issue.getDescription())
                    .status(issue.getStatus())
                    .priority(issue.getPriority())
                    .assignee(UserSummary.from(issue.getAssignee()))
                    .createdBy(UserSummary.from(issue.getCreatedBy()))
                    .teamId(issue.getTeam().getId())
                    .teamIdentifier(issue.getTeam().getIdentifier())
                    .projectId(issue.getProject() != null ? issue.getProject().getId() : null)
                    .projectName(issue.getProject() != null ? issue.getProject().getName() : null)
                    .cycleId(issue.getCycle() != null ? issue.getCycle().getId() : null)
                    .cycleName(issue.getCycle() != null ? issue.getCycle().getName() : null)
                    .parentId(issue.getParent() != null ? issue.getParent().getId() : null)
                    .estimate(issue.getEstimate())
                    .dueDate(issue.getDueDate())
                    .completedAt(issue.getCompletedAt())
                    .sortOrder(issue.getSortOrder())
                    .createdAt(issue.getCreatedAt())
                    .updatedAt(issue.getUpdatedAt())
                    .labels(issue.getLabels().stream().map(LabelResponse::from).toList())
                    .subIssueCount(issue.getSubIssues().size())
                    .commentCount(issue.getComments().size())
                    .build();
        }
    }

    @Data
    @Builder
    public static class ProjectResponse {
        private UUID id;
        private UUID teamId;
        private String name;
        private String description;
        private Project.ProjectStatus status;
        private String color;
        private String icon;
        private LocalDate startDate;
        private LocalDate targetDate;
        private UserSummary lead;
        private UserSummary createdBy;
        private LocalDateTime createdAt;
        private int issueCount;

        public static ProjectResponse from(Project p) {
            return ProjectResponse.builder()
                    .id(p.getId())
                    .teamId(p.getTeam().getId())
                    .name(p.getName())
                    .description(p.getDescription())
                    .status(p.getStatus())
                    .color(p.getColor())
                    .icon(p.getIcon())
                    .startDate(p.getStartDate())
                    .targetDate(p.getTargetDate())
                    .lead(UserSummary.from(p.getLead()))
                    .createdBy(UserSummary.from(p.getCreatedBy()))
                    .createdAt(p.getCreatedAt())
                    .build();
        }
    }

    @Data
    @Builder
    public static class CycleResponse {
        private UUID id;
        private UUID teamId;
        private String name;
        private String description;
        private Cycle.CycleStatus status;
        private LocalDate startDate;
        private LocalDate endDate;
        private UserSummary createdBy;
        private LocalDateTime createdAt;
        private int issueCount;
        private int completedIssueCount;

        public static CycleResponse from(Cycle c) {
            long completed = c.getIssues().stream()
                    .filter(i -> i.getStatus() == Issue.IssueStatus.DONE).count();
            return CycleResponse.builder()
                    .id(c.getId())
                    .teamId(c.getTeam().getId())
                    .name(c.getName())
                    .description(c.getDescription())
                    .status(c.getStatus())
                    .startDate(c.getStartDate())
                    .endDate(c.getEndDate())
                    .createdBy(UserSummary.from(c.getCreatedBy()))
                    .createdAt(c.getCreatedAt())
                    .issueCount(c.getIssues().size())
                    .completedIssueCount((int) completed)
                    .build();
        }
    }

    @Data
    @Builder
    public static class LabelResponse {
        private UUID id;
        private String name;
        private String color;
        private String description;

        public static LabelResponse from(Label l) {
            return LabelResponse.builder()
                    .id(l.getId())
                    .name(l.getName())
                    .color(l.getColor())
                    .description(l.getDescription())
                    .build();
        }
    }

    @Data
    @Builder
    public static class CommentResponse {
        private UUID id;
        private UUID issueId;
        private UserSummary author;
        private UUID parentId;
        private String body;
        private boolean isEdited;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<CommentResponse> replies;

        public static CommentResponse from(Comment c) {
            return CommentResponse.builder()
                    .id(c.getId())
                    .issueId(c.getIssue().getId())
                    .author(UserSummary.from(c.getAuthor()))
                    .parentId(c.getParent() != null ? c.getParent().getId() : null)
                    .body(c.getBody())
                    .isEdited(c.isEdited())
                    .createdAt(c.getCreatedAt())
                    .updatedAt(c.getUpdatedAt())
                    .build();
        }
    }

    @Data
    @Builder
    public static class PagedResponse<T> {
        private List<T> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean last;

        public static <T> PagedResponse<T> from(org.springframework.data.domain.Page<T> page) {
            return PagedResponse.<T>builder()
                    .content(page.getContent())
                    .page(page.getNumber())
                    .size(page.getSize())
                    .totalElements(page.getTotalElements())
                    .totalPages(page.getTotalPages())
                    .last(page.isLast())
                    .build();
        }
    }
}

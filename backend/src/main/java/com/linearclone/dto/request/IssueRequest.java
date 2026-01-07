package com.linearclone.dto.request;

import com.linearclone.entity.Issue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public class IssueRequest {

    @Data
    public static class Create {
        @NotBlank @Size(max = 500)
        private String title;

        private String description;

        @NotNull
        private UUID teamId;

        private UUID projectId;
        private UUID cycleId;
        private UUID parentId;
        private UUID assigneeId;

        private Issue.IssueStatus status = Issue.IssueStatus.BACKLOG;
        private Issue.IssuePriority priority = Issue.IssuePriority.NONE;

        private Integer estimate;
        private LocalDate dueDate;
        private Set<UUID> labelIds;
    }

    @Data
    public static class Update {
        @Size(max = 500)
        private String title;

        private String description;
        private Issue.IssueStatus status;
        private Issue.IssuePriority priority;
        private UUID assigneeId;
        private UUID projectId;
        private UUID cycleId;
        private Integer estimate;
        private LocalDate dueDate;
        private Set<UUID> labelIds;
        private Double sortOrder;
    }
}

package com.linearclone.dto.request;

import com.linearclone.entity.Cycle;
import com.linearclone.entity.Project;
import com.linearclone.entity.WorkspaceMember;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

public class WorkspaceRequest {

    @Data
    public static class Create {
        @NotBlank @Size(min = 2, max = 255)
        private String name;

        @NotBlank @Size(min = 2, max = 100)
        @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must be lowercase letters, numbers, and hyphens only")
        private String slug;

        private String description;
    }

    @Data
    public static class Update {
        @Size(min = 2, max = 255)
        private String name;

        private String description;
        private String logoUrl;
    }

    @Data
    public static class InviteMember {
        @NotBlank
        private String email;

        private WorkspaceMember.WorkspaceRole role = WorkspaceMember.WorkspaceRole.MEMBER;
    }
}

class TeamRequest {

    @Data
    public static class Create {
        @NotBlank @Size(min = 2, max = 255)
        private String name;

        @NotBlank @Size(min = 2, max = 10)
        @Pattern(regexp = "^[A-Z0-9]+$", message = "Identifier must be uppercase letters and numbers")
        private String identifier;

        private String description;
        private String color;
        private String icon;
    }

    @Data
    public static class Update {
        @Size(min = 2, max = 255)
        private String name;

        private String description;
        private String color;
        private String icon;
    }
}

class ProjectRequest {

    @Data
    public static class Create {
        @NotBlank @Size(min = 1, max = 255)
        private String name;

        private String description;
        private String color;
        private String icon;
        private LocalDate startDate;
        private LocalDate targetDate;
        private UUID leadId;
    }

    @Data
    public static class Update {
        @Size(min = 1, max = 255)
        private String name;

        private String description;
        private Project.ProjectStatus status;
        private String color;
        private String icon;
        private LocalDate startDate;
        private LocalDate targetDate;
        private UUID leadId;
    }
}

class CycleRequest {

    @Data
    public static class Create {
        @NotBlank @Size(min = 1, max = 255)
        private String name;

        private String description;

        @NotNull
        private LocalDate startDate;

        @NotNull
        private LocalDate endDate;
    }

    @Data
    public static class Update {
        @Size(min = 1, max = 255)
        private String name;

        private String description;
        private Cycle.CycleStatus status;
        private LocalDate startDate;
        private LocalDate endDate;
    }
}

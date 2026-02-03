-- EXTENSIONS
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- WORKSPACES (top-level multi-tenant unit)

CREATE TABLE workspaces (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name            VARCHAR(255) NOT NULL,
    slug            VARCHAR(100) NOT NULL UNIQUE,
    description     TEXT,
    logo_url        VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE
);

-- USERS
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    display_name    VARCHAR(255) NOT NULL,
    avatar_url      VARCHAR(500),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    last_login_at   TIMESTAMP
);

-- WORKSPACE MEMBERS (many-to-many with roles)

CREATE TYPE workspace_role AS ENUM ('OWNER', 'ADMIN', 'MEMBER', 'GUEST');

CREATE TABLE workspace_members (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workspace_id    UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role            workspace_role NOT NULL DEFAULT 'MEMBER',
    joined_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(workspace_id, user_id)
);

-- TEAMS (groups within a workspace)

CREATE TABLE teams (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workspace_id    UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    identifier      VARCHAR(10) NOT NULL,   -- e.g. "ENG", "MKT"
    description     TEXT,
    color           VARCHAR(7),              -- hex color
    icon            VARCHAR(50),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE(workspace_id, identifier)
);


-- TEAM MEMBERS
CREATE TYPE team_role AS ENUM ('LEAD', 'MEMBER', 'VIEWER');

CREATE TABLE team_members (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    team_id         UUID NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role            team_role NOT NULL DEFAULT 'MEMBER',
    joined_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(team_id, user_id)
);


-- PROJECTS

CREATE TYPE project_status AS ENUM ('BACKLOG', 'PLANNED', 'IN_PROGRESS', 'PAUSED', 'COMPLETED', 'CANCELLED');

CREATE TABLE projects (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    team_id         UUID NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    status          project_status NOT NULL DEFAULT 'PLANNED',
    color           VARCHAR(7),
    icon            VARCHAR(50),
    start_date      DATE,
    target_date     DATE,
    created_by      UUID NOT NULL REFERENCES users(id),
    lead_id         UUID REFERENCES users(id),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);


-- CYCLES (Sprints)

CREATE TYPE cycle_status AS ENUM ('DRAFT', 'STARTED', 'COMPLETED');

CREATE TABLE cycles (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    team_id         UUID NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    status          cycle_status NOT NULL DEFAULT 'DRAFT',
    start_date      DATE NOT NULL,
    end_date        DATE NOT NULL,
    created_by      UUID NOT NULL REFERENCES users(id),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);


-- LABELS
CREATE TABLE labels (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    team_id         UUID NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,
    color           VARCHAR(7) NOT NULL,
    description     TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(team_id, name)
);

-- ISSUES
CREATE TYPE issue_status AS ENUM ('BACKLOG', 'TODO', 'IN_PROGRESS', 'IN_REVIEW', 'DONE', 'CANCELLED');
CREATE TYPE issue_priority AS ENUM ('URGENT', 'HIGH', 'MEDIUM', 'LOW', 'NONE');

CREATE TABLE issues (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    team_id         UUID NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    project_id      UUID REFERENCES projects(id) ON DELETE SET NULL,
    cycle_id        UUID REFERENCES cycles(id) ON DELETE SET NULL,
    parent_id       UUID REFERENCES issues(id) ON DELETE SET NULL,  -- sub-issues
    sequence_number INT NOT NULL,       -- ENG-001, ENG-002...
    title           VARCHAR(500) NOT NULL,
    description     TEXT,
    status          issue_status NOT NULL DEFAULT 'BACKLOG',
    priority        issue_priority NOT NULL DEFAULT 'NONE',
    assignee_id     UUID REFERENCES users(id) ON DELETE SET NULL,
    created_by      UUID NOT NULL REFERENCES users(id),
    estimate        INT,                -- story points
    due_date        DATE,
    completed_at    TIMESTAMP,
    cancelled_at    TIMESTAMP,
    sort_order      FLOAT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Issue sequence counter per team
CREATE TABLE team_issue_sequences (
    team_id         UUID PRIMARY KEY REFERENCES teams(id) ON DELETE CASCADE,
    last_sequence   INT NOT NULL DEFAULT 0
);

-- Issue <-> Label (many-to-many)
CREATE TABLE issue_labels (
    issue_id        UUID NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    label_id        UUID NOT NULL REFERENCES labels(id) ON DELETE CASCADE,
    PRIMARY KEY(issue_id, label_id)
);


-- COMMENTS

CREATE TABLE comments (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    issue_id        UUID NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    author_id       UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    parent_id       UUID REFERENCES comments(id) ON DELETE CASCADE,
    body            TEXT NOT NULL,
    is_edited       BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ISSUE ACTIVITY LOG (audit trail)

CREATE TABLE issue_activities (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    issue_id        UUID NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    actor_id        UUID NOT NULL REFERENCES users(id),
    activity_type   VARCHAR(100) NOT NULL,  -- e.g. "status_changed", "assignee_changed"
    old_value       TEXT,
    new_value       TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- REFRESH TOKENS

CREATE TABLE refresh_tokens (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token           VARCHAR(500) NOT NULL UNIQUE,
    expires_at      TIMESTAMP NOT NULL,
    revoked         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- INDEXES
CREATE INDEX idx_issues_team_id ON issues(team_id);
CREATE INDEX idx_issues_status ON issues(status);
CREATE INDEX idx_issues_assignee_id ON issues(assignee_id);
CREATE INDEX idx_issues_project_id ON issues(project_id);
CREATE INDEX idx_issues_cycle_id ON issues(cycle_id);
CREATE INDEX idx_issues_parent_id ON issues(parent_id);
CREATE INDEX idx_issues_created_at ON issues(created_at DESC);
CREATE INDEX idx_issues_title_trgm ON issues USING gin(title gin_trgm_ops);

CREATE INDEX idx_workspace_members_user_id ON workspace_members(user_id);
CREATE INDEX idx_team_members_user_id ON team_members(user_id);
CREATE INDEX idx_comments_issue_id ON comments(issue_id);
CREATE INDEX idx_issue_activities_issue_id ON issue_activities(issue_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);

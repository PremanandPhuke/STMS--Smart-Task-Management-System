-- Smart Task Management System - initial schema (PostgreSQL)

CREATE TABLE users (
    id            BIGSERIAL    PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'USER',
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE projects (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    description TEXT,
    owner_id    BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE tasks (
    id             BIGSERIAL    PRIMARY KEY,
    title          VARCHAR(200) NOT NULL,
    description    TEXT,
    status         VARCHAR(20)  NOT NULL DEFAULT 'TODO',
    priority       VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    recurrence     VARCHAR(20)  NOT NULL DEFAULT 'NONE',
    due_date       DATE,
    priority_score INTEGER      NOT NULL DEFAULT 0,
    reminder_sent  BOOLEAN      NOT NULL DEFAULT FALSE,
    project_id     BIGINT       NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    assignee_id    BIGINT       REFERENCES users (id) ON DELETE SET NULL,
    parent_task_id BIGINT       REFERENCES tasks (id) ON DELETE CASCADE,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    completed_at   TIMESTAMP
);

CREATE TABLE labels (
    id       BIGSERIAL   PRIMARY KEY,
    name     VARCHAR(50) NOT NULL,
    color    VARCHAR(20) NOT NULL DEFAULT '#0f766e',
    owner_id BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_label_owner_name UNIQUE (owner_id, name)
);

CREATE TABLE task_labels (
    task_id  BIGINT NOT NULL REFERENCES tasks (id)  ON DELETE CASCADE,
    label_id BIGINT NOT NULL REFERENCES labels (id) ON DELETE CASCADE,
    PRIMARY KEY (task_id, label_id)
);

CREATE TABLE comments (
    id         BIGSERIAL PRIMARY KEY,
    content    TEXT      NOT NULL,
    task_id    BIGINT    NOT NULL REFERENCES tasks (id) ON DELETE CASCADE,
    author_id  BIGINT    NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes for the most common queries (filters, joins, reminders)
CREATE INDEX idx_projects_owner      ON projects (owner_id);
CREATE INDEX idx_tasks_project       ON tasks (project_id);
CREATE INDEX idx_tasks_assignee      ON tasks (assignee_id);
CREATE INDEX idx_tasks_parent        ON tasks (parent_task_id);
CREATE INDEX idx_tasks_status_due    ON tasks (status, due_date);
CREATE INDEX idx_tasks_priority_score ON tasks (priority_score DESC);
CREATE INDEX idx_comments_task       ON comments (task_id);

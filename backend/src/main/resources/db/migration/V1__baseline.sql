-- Baseline Migration for ATS (Applicant Tracking System)
-- Creates all tables, constraints, and indexes

-- 1. Departments
CREATE TABLE IF NOT EXISTS departments (
    id BINARY(16) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_by BINARY(16),
    created_at DATETIME(6) NOT NULL,
    modified_by BINARY(16),
    last_modified_date DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_departments_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Users
CREATE TABLE IF NOT EXISTS users (
    id BINARY(16) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255),
    role VARCHAR(50) NOT NULL,
    department_id BINARY(16),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    account_locked BOOLEAN NOT NULL DEFAULT FALSE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    activation_token VARCHAR(255),
    activation_token_expires_at TIMESTAMP NULL,
    reset_token VARCHAR(255),
    reset_token_expires_at TIMESTAMP NULL,
    avatar_url VARCHAR(512),
    created_by BINARY(16),
    created_at DATETIME(6) NOT NULL,
    modified_by BINARY(16),
    last_modified_date DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email),
    INDEX idx_users_role (role),
    INDEX idx_users_dept (department_id),
    CONSTRAINT fk_users_department FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Refresh Tokens
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BINARY(16) NOT NULL,
    token VARCHAR(255) NOT NULL,
    user_id BINARY(16) NOT NULL,
    expiry_date DATETIME(6) NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_by BINARY(16),
    created_at DATETIME(6) NOT NULL,
    modified_by BINARY(16),
    last_modified_date DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_refresh_tokens_token (token),
    INDEX idx_refresh_tokens_user (user_id),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Jobs
CREATE TABLE IF NOT EXISTS jobs (
    id CHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    department_id BINARY(16),
    hiring_manager_id BINARY(16),
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    headcount INT NOT NULL DEFAULT 1,
    rejection_reason TEXT,
    created_by BINARY(16),
    created_at DATETIME(6) NOT NULL,
    modified_by BINARY(16),
    last_modified_date DATETIME(6),
    PRIMARY KEY (id),
    INDEX idx_jobs_status (status),
    INDEX idx_jobs_department (department_id),
    CONSTRAINT fk_jobs_department FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL,
    CONSTRAINT fk_jobs_hiring_manager FOREIGN KEY (hiring_manager_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Candidates
CREATE TABLE IF NOT EXISTS candidates (
    id BINARY(16) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50),
    current_company VARCHAR(255),
    source VARCHAR(100),
    location VARCHAR(255),
    experience_years INT,
    summary TEXT,
    created_by BINARY(16),
    created_at DATETIME(6) NOT NULL,
    modified_by BINARY(16),
    last_modified_date DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_candidates_email (email),
    INDEX idx_candidates_source (source)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Candidate Documents
CREATE TABLE IF NOT EXISTS candidate_documents (
    id BINARY(16) NOT NULL,
    candidate_id BINARY(16) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(1024) NOT NULL,
    file_type VARCHAR(50),
    file_size_bytes BIGINT,
    uploaded_at DATETIME(6) NOT NULL,
    created_by BINARY(16),
    created_at DATETIME(6) NOT NULL,
    modified_by BINARY(16),
    last_modified_date DATETIME(6),
    PRIMARY KEY (id),
    INDEX idx_candidate_docs_candidate (candidate_id),
    CONSTRAINT fk_candidate_docs_candidate FOREIGN KEY (candidate_id) REFERENCES candidates(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Applications
CREATE TABLE IF NOT EXISTS applications (
    id BINARY(16) NOT NULL,
    candidate_id BINARY(16) NOT NULL,
    job_id CHAR(36) NOT NULL,
    applied_at DATETIME(6) NOT NULL,
    stage VARCHAR(50) NOT NULL DEFAULT 'APPLIED',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_by BINARY(16),
    created_at DATETIME(6) NOT NULL,
    modified_by BINARY(16),
    last_modified_date DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_applications_candidate_job (candidate_id, job_id),
    INDEX idx_applications_stage (stage),
    INDEX idx_applications_status (status),
    CONSTRAINT fk_applications_candidate FOREIGN KEY (candidate_id) REFERENCES candidates(id) ON DELETE CASCADE,
    CONSTRAINT fk_applications_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Candidate Stage History
CREATE TABLE IF NOT EXISTS candidate_stage_history (
    id BINARY(16) NOT NULL,
    application_id BINARY(16) NOT NULL,
    from_stage VARCHAR(50),
    to_stage VARCHAR(50) NOT NULL,
    reason TEXT,
    created_by BINARY(16),
    created_at DATETIME(6) NOT NULL,
    modified_by BINARY(16),
    last_modified_date DATETIME(6),
    PRIMARY KEY (id),
    INDEX idx_csh_application (application_id),
    CONSTRAINT fk_csh_application FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Candidate Notes
CREATE TABLE IF NOT EXISTS candidate_notes (
    id BINARY(16) NOT NULL,
    application_id BINARY(16) NOT NULL,
    content TEXT NOT NULL,
    created_by BINARY(16),
    created_at DATETIME(6) NOT NULL,
    modified_by BINARY(16),
    last_modified_date DATETIME(6),
    PRIMARY KEY (id),
    INDEX idx_notes_application (application_id),
    CONSTRAINT fk_notes_application FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Scorecard Templates
CREATE TABLE IF NOT EXISTS scorecard_templates (
    id BINARY(16) NOT NULL,
    title VARCHAR(255) NOT NULL,
    job_id CHAR(36),
    created_by BINARY(16),
    created_at DATETIME(6) NOT NULL,
    modified_by BINARY(16),
    last_modified_date DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_scorecard_templates_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. Scorecard Criteria
CREATE TABLE IF NOT EXISTS scorecard_criteria (
    id BINARY(16) NOT NULL,
    template_id BINARY(16) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    weight DOUBLE NOT NULL DEFAULT 1.0,
    created_by BINARY(16),
    created_at DATETIME(6) NOT NULL,
    modified_by BINARY(16),
    last_modified_date DATETIME(6),
    PRIMARY KEY (id),
    INDEX idx_criteria_template (template_id),
    CONSTRAINT fk_criteria_template FOREIGN KEY (template_id) REFERENCES scorecard_templates(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. Interviews
CREATE TABLE IF NOT EXISTS interviews (
    id BINARY(16) NOT NULL,
    application_id BINARY(16) NOT NULL,
    template_id BINARY(16),
    scheduled_at DATETIME(6) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
    overall_score DOUBLE,
    created_by BINARY(16),
    created_at DATETIME(6) NOT NULL,
    modified_by BINARY(16),
    last_modified_date DATETIME(6),
    PRIMARY KEY (id),
    INDEX idx_interviews_app (application_id),
    INDEX idx_interviews_scheduled (scheduled_at),
    CONSTRAINT fk_interviews_application FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE,
    CONSTRAINT fk_interviews_template FOREIGN KEY (template_id) REFERENCES scorecard_templates(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. Interview Participants
CREATE TABLE IF NOT EXISTS interview_participants (
    interview_id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'INTERVIEWER',
    submitted_at DATETIME(6),
    PRIMARY KEY (interview_id, user_id),
    CONSTRAINT fk_ip_interview FOREIGN KEY (interview_id) REFERENCES interviews(id) ON DELETE CASCADE,
    CONSTRAINT fk_ip_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. Interview Scores
CREATE TABLE IF NOT EXISTS interview_scores (
    id BINARY(16) NOT NULL,
    interview_id BINARY(16) NOT NULL,
    criterion_id BINARY(16) NOT NULL,
    interviewer_id BINARY(16) NOT NULL,
    score DOUBLE NOT NULL,
    comment TEXT,
    created_by BINARY(16),
    created_at DATETIME(6) NOT NULL,
    modified_by BINARY(16),
    last_modified_date DATETIME(6),
    PRIMARY KEY (id),
    INDEX idx_scores_interview (interview_id),
    CONSTRAINT fk_scores_interview FOREIGN KEY (interview_id) REFERENCES interviews(id) ON DELETE CASCADE,
    CONSTRAINT fk_scores_criterion FOREIGN KEY (criterion_id) REFERENCES scorecard_criteria(id) ON DELETE CASCADE,
    CONSTRAINT fk_scores_interviewer FOREIGN KEY (interviewer_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15. Offers
CREATE TABLE IF NOT EXISTS offers (
    id BINARY(16) NOT NULL,
    application_id BINARY(16),
    salary DECIMAL(15, 2),
    position_title VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    start_date DATE,
    benefits TEXT,
    notes TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_by BINARY(16),
    created_at DATETIME(6) NOT NULL,
    modified_by BINARY(16),
    last_modified_date DATETIME(6),
    PRIMARY KEY (id),
    INDEX idx_offers_status (status),
    CONSTRAINT fk_offers_application FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 16. Offer Approvals
CREATE TABLE IF NOT EXISTS offer_approvals (
    id BINARY(16) NOT NULL,
    offer_id BINARY(16) NOT NULL,
    approved_by BINARY(16) NOT NULL,
    status VARCHAR(50) NOT NULL,
    comment TEXT,
    created_by BINARY(16),
    created_at DATETIME(6) NOT NULL,
    modified_by BINARY(16),
    last_modified_date DATETIME(6),
    PRIMARY KEY (id),
    INDEX idx_offer_approvals_offer (offer_id),
    CONSTRAINT fk_oa_offer FOREIGN KEY (offer_id) REFERENCES offers(id) ON DELETE CASCADE,
    CONSTRAINT fk_oa_approved_by FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 17. Onboarding Checklists
CREATE TABLE IF NOT EXISTS onboarding_checklists (
    id BINARY(16) NOT NULL,
    application_id BINARY(16),
    title VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'NOT_STARTED',
    created_by BINARY(16),
    created_at DATETIME(6) NOT NULL,
    modified_by BINARY(16),
    last_modified_date DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_oc_application FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 18. Onboarding Tasks
CREATE TABLE IF NOT EXISTS onboarding_tasks (
    id BINARY(16) NOT NULL,
    checklist_id BINARY(16) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INT NOT NULL DEFAULT 0,
    due_date DATE,
    assigned_to BINARY(16),
    created_by BINARY(16),
    created_at DATETIME(6) NOT NULL,
    modified_by BINARY(16),
    last_modified_date DATETIME(6),
    PRIMARY KEY (id),
    INDEX idx_ot_checklist (checklist_id),
    CONSTRAINT fk_ot_checklist FOREIGN KEY (checklist_id) REFERENCES onboarding_checklists(id) ON DELETE CASCADE,
    CONSTRAINT fk_ot_assigned_to FOREIGN KEY (assigned_to) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 19. Notifications
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BINARY(16) NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    reference_id BIGINT,
    created_at TIMESTAMP NULL,
    PRIMARY KEY (id),
    INDEX idx_notifications_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 20. Audit Logs
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BINARY(16),
    action VARCHAR(255) NOT NULL,
    entity_type VARCHAR(255) NOT NULL,
    entity_id VARCHAR(255),
    old_value TEXT,
    new_value TEXT,
    ip_address VARCHAR(255),
    user_agent VARCHAR(500),
    created_at TIMESTAMP NULL,
    PRIMARY KEY (id),
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_action (action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 21. System Configs
CREATE TABLE IF NOT EXISTS system_configs (
    config_key VARCHAR(100) NOT NULL,
    value TEXT NOT NULL,
    updated_by BINARY(16),
    updated_at TIMESTAMP NULL,
    PRIMARY KEY (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

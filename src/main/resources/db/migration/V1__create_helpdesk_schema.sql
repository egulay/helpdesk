CREATE TABLE issue_requester
(
    id        INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email     VARCHAR(320) NOT NULL,
    is_active BOOLEAN      NOT NULL DEFAULT TRUE,
    created   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_issue_requester_email UNIQUE (email),
    INDEX idx_issue_requester_created (created),
    INDEX idx_issue_requester_active_created (is_active, created)
);

CREATE TABLE issue_request
(
    id           INT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    requester_id INT      NOT NULL,
    request_body TEXT     NOT NULL,
    is_solved    BOOLEAN  NOT NULL DEFAULT FALSE,
    created      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    solved       DATETIME NULL,
    CONSTRAINT fk_issue_request_requester
        FOREIGN KEY (requester_id) REFERENCES issue_requester (id)
            ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_issue_request_requester_created (requester_id, created),
    INDEX idx_issue_request_solved_created (is_solved, created),
    INDEX idx_issue_request_solved_at (solved)
);

CREATE TABLE issue_response
(
    id            INT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    request_id    INT      NOT NULL,
    requester_id  INT      NOT NULL,
    response_body TEXT     NOT NULL,
    created       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_issue_response_requester
        FOREIGN KEY (requester_id) REFERENCES issue_requester (id)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_issue_response_request
        FOREIGN KEY (request_id) REFERENCES issue_request (id)
            ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_issue_response_request_created (request_id, created),
    INDEX idx_issue_response_requester_created (requester_id, created)
);

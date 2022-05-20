DROP
DATABASE IF EXISTS help_desk;

CREATE
DATABASE help_desk
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;
USE
help_desk;

DROP TABLE IF EXISTS issue_response;
DROP TABLE IF EXISTS issue_request;
DROP TABLE IF EXISTS issue_requester;

CREATE TABLE issue_requester
(
    id        INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email     VARCHAR(255) NOT NULL,
    is_active BOOLEAN  DEFAULT TRUE,
    created   DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE issue_request
(
    id           INT  NOT NULL AUTO_INCREMENT PRIMARY KEY,
    requester_id INT  NOT NULL,
    request_body TEXT NOT NULL,
    is_solved    BOOLEAN  DEFAULT FALSE,
    created      DATETIME DEFAULT CURRENT_TIMESTAMP,
    solved       DATETIME NULL,
    FOREIGN KEY (requester_id) REFERENCES issue_requester (id)
        ON DELETE CASCADE
);

CREATE TABLE issue_response
(
    id            INT  NOT NULL AUTO_INCREMENT PRIMARY KEY,
    request_id    INT  NOT NULL,
    requester_id  INT  NOT NULL,
    response_body TEXT NOT NULL,
    created       DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (requester_id) REFERENCES issue_requester (id)
        ON DELETE CASCADE,
    FOREIGN KEY (request_id) REFERENCES issue_request (id)
        ON DELETE CASCADE
);
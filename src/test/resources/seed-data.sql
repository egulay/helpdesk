SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE issue_response;
TRUNCATE TABLE issue_request;
TRUNCATE TABLE issue_requester;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO issue_requester (full_name, email)
VALUES ('Ludwig van Beethoven', 'ludwig@beethoven.net');

INSERT INTO issue_request (requester_id, request_body)
VALUES (1, 'Elise is not loving me anymore..!');

INSERT INTO issue_response (request_id, requester_id, response_body)
VALUES (1, 1, 'It is OK... I am not loving her anymore either :P');
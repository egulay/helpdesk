# Dummy Helpdesk API

## Used Technologies at a Glance
* [Java 11](https://openjdk.java.net/projects/jdk/11/)
* [Maven](https://maven.apache.org/)
* [Spring Cloud](https://spring.io/projects/spring-cloud)
* [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
* [MySQL](https://www.mysql.com/)
* [Hibernate ORM](https://hibernate.org/orm/)
* [Apache Commons Lang](https://commons.apache.org/proper/commons-lang/)
* [Google Protocol Buffers](https://developers.google.com/protocol-buffers/)
* [Maven Protocol Buffers Plugin](https://www.xolstice.org/protobuf-maven-plugin/)
* [JUnit](https://junit.org/junit5/)
* [Testcontainers](https://www.testcontainers.org/modules/databases/)
* [Lombok](https://projectlombok.org/)

## Installation & Execution
The solution requires up-to-date [Docker](https://www.docker.com/products/docker-desktop/) to execute all integration tests with maven surefire plugin.
### Base [Docker](https://www.docker.com/products/docker-desktop/) Images for Integration Tests
* #### Testcontainers version 0.3.3
```sh
   docker pull testcontainers/ryuk:0.3.3
```
* #### MySQL version 8.0
```sh
   docker pull mysql:8.0
```
### Building the Project
```sh
   mvn clean install
```
* Above command creates Java source files located in proto directory, execute tests and compile the solution.
#### Create Developer User T-SQL Script
```mysql
CREATE USER 'dev_user'@'localhost' IDENTIFIED BY '111';
GRANT ALL PRIVILEGES ON *.* TO 'dev_user'@'localhost';
FLUSH PRIVILEGES;
```
* The user info defined in above T-SQL also defined in [application.yml](https://github.com/egulay/helpdesk/blob/master/src/main/resources/application.yml).
#### DDL Script
```mysql
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
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE issue_response
(
    id            INT  NOT NULL AUTO_INCREMENT PRIMARY KEY,
    request_id    INT  NOT NULL,
    requester_id  INT  NOT NULL,
    response_body TEXT NOT NULL,
    created       DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (requester_id) REFERENCES issue_requester (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (request_id) REFERENCES issue_request (id)
        ON DELETE CASCADE ON UPDATE CASCADE
);
```
#### Seed Test Data T-SQL Script
```mysql
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
```
### Starting the API
```sh
   java -jar target/helpdesk-0.0.1-SNAPSHOT.jar
```
### Open API Documentation (in JSON)
```sh
   curl localhost:8888/api-docs
```
![Open API Screen Capture](open-api-sc.JPG)
### Example API Calls
#### JSON (HTTP1)
```sh
   curl --header "accept: application/json" localhost:8888/v1/issue_requesters/1
```
#### XML (HTTP1)
```sh
   curl --header "accept: application/xml" localhost:8888/v1/issue_requesters/1
```
#### gRPC / Binary (HTTP2)
```sh
   curl localhost:8888/v1/issue_requesters/1
```

### License
The MIT License (MIT)
Copyright © 2022

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.




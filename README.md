> [!WARNING]
> 🇹🇷 **Önemli Not**
>
> Bu proje eğitim amacıyla paylaşılmıştır.
>
> İçerisinde bilinçli olarak bırakılmış mantıksal ve mimari eksiklikler vardır.
>
> Doğrudan kopyalanıp ticari ürün olarak sunulması tavsiye edilmez. Böyle bir tercihin en büyük maliyeti teknik borç değil, ticari itibardır.

# Dummy Helpdesk API

Helpdesk is a Spring Boot application that exposes a helpdesk data model through REST endpoints, protobuf-based HTTP endpoints, and an embedded MCP server for AI clients. It uses MySQL for persistence, Vault for secrets, and either OpenAI or LM Studio for AI-assisted tools.

## What’s included

- REST API for issue requesters, requests, and responses
- Protobuf message definitions for typed payloads
- Embedded Spring AI MCP server that exposes backend operations as tools
- AI-backed summarization and response generation through OpenAI or LM Studio

## Tech stack

- Java 17
- Maven
- Spring Boot
- Spring Cloud Vault
- Spring Data JPA
- MySQL
- Hibernate ORM
- Google Protocol Buffers
- Spring AI / MCP
- OpenAI Java SDK
- JUnit 5
- Testcontainers
- Lombok

## Prerequisites

- Docker Desktop
- Java 17
- Maven
- `protoc` installed locally

The project uses the protobuf Maven plugin to generate Java classes from `.proto` files. The `pom.xml` points to a specific `protoc` path, so update that path if your local installation differs.

For integration tests, you may want to pre-pull the Docker images used by Testcontainers:

```bash
docker pull testcontainers/ryuk:0.12.0
docker pull mysql:8.0
```

## AI provider configuration

The active AI provider is selected in `src/main/resources/application.yml` with `helpdesk.ai.provider`:

- `lm-studio` for local testing
- `openai` for hosted usage

If you use LM Studio, run it locally and point it at `http://localhost:1234/v1` or update `helpdesk.ai.lm-studio.base-url`. The recommended model for MCP tests is `unsloth/Qwen3-Coder-30B-A3-B-Instruct-GGUF`.

If you use OpenAI, export `OPENAI_API_KEY` before running `build.sh` or `run.sh`; those scripts patch that value into Vault as `helpdesk.ai.openai.api-key`. The model stays in `application.yml`.

## Quick start

The fastest way to build and run everything is:

```bash
./build.sh
```

`build.sh`:

- starts MySQL
- creates the database schema
- seeds sample data
- starts Vault
- creates the required Vault secret
- patches the OpenAI API key into Vault at `helpdesk.ai.openai.api-key` if `OPENAI_API_KEY` is set
- generates Java sources from protobuf files
- runs the tests
- compiles the project

## Manual setup

### 1) Start MySQL 8

```bash
docker run -d --name mysql8 \
  -p 3306:3306 \
  -e MYSQL_DATABASE=help_desk \
  -e MYSQL_USER=help_user \
  -e MYSQL_PASSWORD=help_pass \
  -e MYSQL_ROOT_PASSWORD=root_pass \
  mysql:8.0
```

### 2) Create the schema

```mysql
DROP DATABASE IF EXISTS help_desk;

CREATE DATABASE help_desk
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE help_desk;

DROP TABLE IF EXISTS issue_response;
DROP TABLE IF EXISTS issue_request;
DROP TABLE IF EXISTS issue_requester;

CREATE TABLE issue_requester
(
    id         INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    full_name  VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL,
    is_active  BOOLEAN DEFAULT TRUE,
    created    DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE issue_request
(
    id           INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    requester_id  INT NOT NULL,
    request_body  TEXT NOT NULL,
    is_solved     BOOLEAN DEFAULT FALSE,
    created       DATETIME DEFAULT CURRENT_TIMESTAMP,
    solved        DATETIME NULL,
    FOREIGN KEY (requester_id) REFERENCES issue_requester (id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE issue_response
(
    id            INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    request_id    INT NOT NULL,
    requester_id  INT NOT NULL,
    response_body TEXT NOT NULL,
    created       DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (requester_id) REFERENCES issue_requester (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (request_id) REFERENCES issue_request (id)
        ON DELETE CASCADE ON UPDATE CASCADE
);
```

### 3) Seed sample data

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

### 4) Start Vault in dev mode

```bash
docker run -d --name vault \
  -p 8200:8200 \
  -e VAULT_DEV_ROOT_TOKEN_ID=root \
  -e VAULT_DEV_LISTEN_ADDRESS=0.0.0.0:8200 \
  hashicorp/vault:latest \
  server -dev -dev-root-token-id=root -dev-listen-address=0.0.0.0:8200
```

### 5) Set Vault environment variables

```bash
export VAULT_ADDR=http://localhost:8200
export VAULT_TOKEN=root
```

### 6) Store database settings in Vault

Spring reads database settings from `secret/helpdesk`.

```bash
docker exec \
  -e VAULT_ADDR=http://127.0.0.1:8200 \
  -e VAULT_TOKEN=root \
  vault \
  vault kv put secret/helpdesk \
    spring.datasource.url="jdbc:mysql://127.0.0.1:3306/help_desk?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
    spring.datasource.username="help_user" \
    spring.datasource.password="help_pass" \
    spring.datasource.driver-class-name="com.mysql.cj.jdbc.Driver" \
    spring.jpa.properties.hibernate.dialect="org.hibernate.dialect.MySQLDialect" \
    spring.datasource.hikari.auto-commit="false" \
    spring.datasource.hikari.transaction-isolation="TRANSACTION_READ_COMMITTED" \
    spring.datasource.hikari.minimum-idle="2" \
    spring.datasource.hikari.maximum-pool-size="10" \
    spring.datasource.hikari.pool-name="HikariPool"
```

### 7) Configure the AI provider

Set the provider in `src/main/resources/application.yml`:

```yaml
helpdesk:
  ai:
    provider: lm-studio
```

Use `openai` for hosted API usage or `lm-studio` for local testing.

### 8) Store the OpenAI API key in Vault

```bash
export OPENAI_API_KEY="your-openai-api-key"
```

If you want to patch Vault manually, store it only once under `helpdesk.ai.openai.api-key`:

```bash
docker exec \
  -e VAULT_ADDR=http://127.0.0.1:8200 \
  -e VAULT_TOKEN=root \
  vault \
  vault kv patch secret/helpdesk \
    helpdesk.ai.openai.api-key="${OPENAI_API_KEY}"
```

Check the secret:

```bash
docker exec -e VAULT_ADDR=http://127.0.0.1:8200 -e VAULT_TOKEN=root vault \
  vault kv get secret/helpdesk
```

### 9) Tell Spring Boot to read from Vault

```yaml
spring:
  application:
    name: helpdesk
  config:
    import: optional:vault://
  cloud:
    vault:
      enabled: true
      uri: ${VAULT_ADDR}
      token: ${VAULT_TOKEN}
      kv:
        enabled: true
        backend: secret
        application-name: ${spring.application.name}
```

### 10) Start the API

```bash
./run.sh
```

`run.sh`:

- ensures `secret/helpdesk` exists
- patches the OpenAI API key into Vault at `helpdesk.ai.openai.api-key` if `OPENAI_API_KEY` is available
- prints the Vault secret for verification
- starts Spring Boot with `mvn spring-boot:run`

## MCP support

The embedded MCP server runs in the same Spring Boot process as the REST and protobuf endpoints.

Tool flow:

```text
MCP Tool -> Existing Service Layer -> Repository -> Database
```

MCP tools expose JSON-friendly DTOs rather than JPA entities to keep responses simple and avoid serialization issues.

For AI-assisted MCP operations, the server uses the configured provider from `helpdesk.ai.provider`. OpenAI settings come from Vault; LM Studio can be used locally without an API key.

## Testing the embedded MCP server

Start the application:

```bash
./run.sh
```

Run the smoke test in another terminal:

```bash
./test-mcp.sh
```

The script verifies the SSE handshake and tool listing flow.

If you want to test the AI-backed MCP flow locally, use `lm-studio`, start LM Studio, and use the recommended model above.

## Claude Desktop as an MCP client

Start the application:

```bash
./run.sh
```

Then edit:

```text
~/Library/Application Support/Claude/claude_desktop_config.json
```

Example configuration:

```json
{
  "mcpServers": {
    "helpdesk": {
      "command": "/opt/homebrew/bin/npx",
      "args": [
        "-y",
        "mcp-remote@latest",
        "http://localhost:8888/sse"
      ]
    }
  }
}
```

Restart Claude Desktop after saving. The Helpdesk MCP tools will be discovered automatically.

## API docs and examples

OpenAPI docs:

```bash
curl localhost:8888/api-docs
```

Example requests:

```bash
curl --header "accept: application/json" localhost:8888/v1/issue_requesters/1
curl --header "accept: application/xml" localhost:8888/v1/issue_requesters/1
curl localhost:8888/v1/issue_requesters/1
```

## Screenshots

![Local MCP Servers Screen Capture](local-mcp-servers.png)
![Local MCP Test 0](local-mcp-test-0.png)
![Local MCP Test](local-mcp-test-2.png)
![Local MCP Test 2](local-mcp-test.png)
![Local MCP Test 3](local-mcp-test-3.png)
![Local MCP Test DB](local-mcp-test-db.png)
![Open API Screen Capture](open-api-sc.JPG)

## License

MIT License. Copyright © 2025.

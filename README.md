# Dummy Helpdesk API

## Technologies Used

- Java 17 – the programming language used to build the project
- Maven – a tool to build and manage the project
- Spring Boot – the main application framework
- Spring Cloud – helps connect different parts of the system
- Spring Cloud Vault – loads secrets and configuration from HashiCorp Vault
- Spring Data JPA – makes it easier to work with databases
- MySQL – the database used to store the data
- Hibernate ORM – connects Java objects with database tables
- Google Protocol Buffers – a way to exchange data in a fast and small format
- Protobuf Maven Plugin – generates Java code from .proto files
- Spring AI – provides MCP server support and tool registration
- Model Context Protocol (MCP) – exposes API capabilities as tools for AI clients
- OpenAI Java SDK – calls OpenAI models from the MCP assistant layer
- JUnit 5 – used for testing the code
- Testcontainers – runs temporary databases in Docker for tests
- Lombok – reduces boilerplate code in Java classes
- HashiCorp Vault – stores secrets like database credentials and OpenAI API keys safely

## Installation & Execution

You need the latest [Docker Desktop](https://www.docker.com/products/docker-desktop/) to run integration tests with the Maven Surefire plugin.

### Base [Docker](https://www.docker.com/products/docker-desktop/) Images for Integration Tests

* #### Testcontainers version 0.12.0

```sh
docker pull testcontainers/ryuk:0.12.0
```

* #### MySQL version 8.0

```sh
docker pull mysql:8.0
```

---

> **_NOTE:_** The Protobuf compiler is required to build Java classes from the ".proto" files in the
proto directory. The path to protoc is set in the pom.xml plugin configuration (see protocExecutable), for example:

```xml
<!-- Protobuf codegen (this script uses your protoc path) -->
<plugin>
    <groupId>org.xolstice.maven.plugins</groupId>
    <artifactId>protobuf-maven-plugin</artifactId>
    <version>${protobuf-maven-plugin.version}</version>
    <configuration>
        <protocExecutable>/opt/homebrew/Cellar/protobuf/32.0_1/bin/protoc</protocExecutable>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>compile</goal>
                <goal>test-compile</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Building the Project

```sh
./build.sh
```

* The build.sh script pulls and starts MySQL, executes the DDL and seeds some data, pulls and starts Vault, creates the necessary secret, patches OpenAI configuration into Vault if `OPENAI_API_KEY` is available, then generates Java sources from the proto directory, runs the tests, and compiles the project.

### Development Mode Guide

#### 1) Run MySQL 8 (Docker)

```bash
docker run -d --name mysql8 \
  -p 3306:3306 \
  -e MYSQL_DATABASE=help_desk \
  -e MYSQL_USER=help_user \
  -e MYSQL_PASSWORD=help_pass \
  -e MYSQL_ROOT_PASSWORD=root_pass \
  mysql:8.0
```

#### 2) Execute DDL Script

```mysql
DROP DATABASE IF EXISTS help_desk;

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

#### 3) Seed Test Data (SQL)

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

#### 4) Run Vault (dev mode, Docker)

```bash
docker run -d --name vault \
  -p 8200:8200 \
  -e VAULT_DEV_ROOT_TOKEN_ID=root \
  -e VAULT_DEV_LISTEN_ADDRESS=0.0.0.0:8200 \
  hashicorp/vault:latest \
  server -dev -dev-root-token-id=root -dev-listen-address=0.0.0.0:8200
```

#### 5) Set environment variables on your host terminal

```bash
export VAULT_ADDR=http://localhost:8200
export VAULT_TOKEN=root
```

#### 6) Save your DB settings into Vault (KV v2)

It stores the properties with the same keys Spring uses. Spring will read them directly from `secret/helpdesk`.

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

#### 7) Save OpenAI settings into Vault

The OpenAI API key should not be committed to Git. Store it in Vault instead.

```bash
export OPENAI_API_KEY="your-openai-api-key"
export OPENAI_MODEL="gpt-5.2"
```

Then patch the existing Vault secret:

```bash
docker exec \
  -e VAULT_ADDR=http://127.0.0.1:8200 \
  -e VAULT_TOKEN=root \
  vault \
  vault kv patch secret/helpdesk \
    helpdesk.ai.openai.api-key="${OPENAI_API_KEY}" \
    helpdesk.ai.openai.model="${OPENAI_MODEL}"
```

Check the secret:

```bash
docker exec -e VAULT_ADDR=http://127.0.0.1:8200 -e VAULT_TOKEN=root vault \
  vault kv get secret/helpdesk
```

#### 8) Tell Spring Boot to read from Vault

```yaml
spring:
  application:
    name: helpdesk
  config:
    import: optional:vault://   # reads config from Vault at startup
  cloud:
    vault:
      enabled: true
      uri: ${VAULT_ADDR}        # reads Vault address from environment variable
      token: ${VAULT_TOKEN}
      kv:
        enabled: true
        backend: secret
        application-name: ${spring.application.name}
```

#### 9) Start the API

```sh
./run.sh
```

The run.sh script:
- Checks if the Vault secret `secret/helpdesk` exists and creates it if missing.
- Patches OpenAI settings into Vault if `OPENAI_API_KEY` is available.
- Prints the Vault secret to confirm values.
- Runs the Spring Boot application with `mvn spring-boot:run`.


## MCP Support

This project includes an embedded MCP server inside the same Spring Boot application that already serves REST and gRPC-style endpoints. This makes the application a hybrid API:

```text
REST / JSON / XML API
gRPC-style binary API
MCP tool server
```

The MCP layer does not replace the existing REST or gRPC APIs. It adds a third adapter that allows AI clients and agents to call selected backend capabilities as tools.

### Why MCP Is Useful Here

Traditional REST and gRPC endpoints are designed for direct application-to-application calls. MCP exposes backend capabilities in a tool-oriented format so an AI client can:

- search helpdesk requesters,
- search issue requests,
- search issue responses,
- inspect ticket history,
- summarize a ticket,
- suggest a support response,
- combine multiple backend calls into a higher-level assistant workflow.

This is useful because the business logic remains in the existing service layer, while MCP simply exposes that logic in a model-friendly way.

### MCP Package Structure

```text
com.helpdesk.mcp.ai
  AiService
  HelpdeskAssistantTools
  OpenAiService

com.helpdesk.mcp.assistant
  HelpdeskAssistanceFacade
  HelpdeskContextBuilder
  HelpdeskPromptService
  HelpdeskTicketContext

com.helpdesk.mcp.config
  McpToolConfiguration
  OpenAiConfiguration

com.helpdesk.mcp.tool
  IssueRequesterTool
  IssueRequestTool
  IssueResponseTool

com.helpdesk.mcp.util
  McpDateParser
```

### MCP Data Tools

The classes under `com.helpdesk.mcp.tool` are thin adapters over the existing data services.

```text
IssueRequesterTool  -> IssueRequesterService
IssueRequestTool    -> IssueRequestService
IssueResponseTool   -> IssueResponseService
```

These classes should not contain business logic. Their responsibility is to expose existing service methods as MCP tools.

Examples of MCP data tool operations:

- find issue requester by id,
- find issue requester by email,
- find issue requests by requester id,
- find issue requests by created date range,
- find issue responses by request id,
- save or update issue records,
- mark issue request as solved.

This keeps the architecture clean:

```text
MCP Tool -> Existing Data Service -> Repository -> Database
```

### MCP Assistant Layer

The assistant package contains AI-oriented orchestration logic. It is separate from the CRUD/data tools.

```text
HelpdeskAssistantTools
  -> HelpdeskAssistanceFacade
      -> HelpdeskContextBuilder
      -> HelpdeskPromptService
      -> AiService
```

The assistant layer is used for higher-level AI operations that do not exist as normal CRUD endpoints.

Examples:

- summarize an issue request,
- suggest a professional support response,
- build a ticket context from request + requester + responses,
- apply a default system prompt,
- call OpenAI using the configured API key.

### Context Builder

`HelpdeskContextBuilder` prepares the data needed for AI workflows. For example, to summarize a ticket, the assistant needs:

- the issue request,
- the requester,
- all related responses.

Instead of putting that composition logic into the MCP tool class, the project keeps it in the context builder.

```text
HelpdeskContextBuilder
  -> IssueRequestService
  -> IssueRequesterService
  -> IssueResponseService
```

### Prompt Service

`HelpdeskPromptService` centralizes default and system prompts.

This keeps prompts out of data services and out of tool classes.

Typical prompt responsibilities:

- default system prompt,
- summarization prompt,
- response suggestion prompt,
- rules such as “do not invent missing information”.

This design allows prompt changes without modifying the data services.

### AI Service Abstraction

`AiService` is an interface for model calls.

`OpenAiService` is the current implementation using the OpenAI Java SDK.

```text
AiService
  -> OpenAiService
```

This makes the project easier to extend later. For example, future implementations could support:

- Azure OpenAI,
- Anthropic,
- local models,
- mock AI service for tests.

### OpenAI Configuration

`OpenAiConfiguration` creates the OpenAI client bean. The API key is loaded from Vault through Spring configuration, not hardcoded in source code.

Expected Vault properties:

```properties
helpdesk.ai.openai.api-key=your-openai-api-key
helpdesk.ai.openai.model=gpt-5.2
```

The OpenAI API key must not be committed to Git.

### Date Parsing

`McpDateParser` converts string date parameters received by MCP tools into Java `Date` values expected by the existing service layer.

The expected format is an ISO-8601 instant:

```text
2026-07-09T00:00:00Z
```

This is used by tool methods that query data by created or solved date ranges.

### MCP Configuration

`McpToolConfiguration` registers MCP tool classes with Spring AI.

It exposes both data tools and assistant tools:

```text
IssueRequesterTool
IssueRequestTool
IssueResponseTool
HelpdeskAssistantTools
```

This allows an MCP-compatible client to discover and call these tools.

### Design Rule

The project follows this separation:

```text
Simple data operation:
MCP Tool -> Existing Data Service

AI-assisted operation:
MCP Assistant Tool -> Assistant Facade -> Context Builder + Prompt Service + AiService
```

This prevents the MCP layer from duplicating business logic and keeps AI-specific behavior isolated.



## Testing the Embedded MCP Server

The project embeds a Spring AI MCP server using the SSE transport. This allows any MCP-compatible client to discover and invoke the registered tools.

### Verify the MCP Server

Start the application:

```bash
./run.sh
```

Open an SSE connection:

```bash
curl -N \
  -H "Accept: text/event-stream" \
  http://localhost:8888/sse
```

A successful response looks similar to:

```text
id:4453f025-ff9d-45cb-b3c5-134c7369eafd
event:endpoint
data:/mcp/message?sessionId=4453f025-ff9d-45cb-b3c5-134c7369eafd
```

The `sessionId` is then used for all subsequent MCP requests.

### Automated Test Script

A helper script (`test-mcp.sh`) can be used after `./run.sh` to verify the complete MCP handshake automatically.

The script performs:

1. Opens the SSE connection.
2. Waits for the generated session identifier.
3. Sends the `initialize` request.
4. Sends the `notifications/initialized` notification.
5. Executes `tools/list`.
6. Prints all responses received from the server.

Run it with:

```bash
./test-mcp.sh
```

### Expected Result

The output should contain the MCP protocol messages followed by the registered tool list.

Typical flow:

```text
SSE Connection
    ↓
initialize
    ↓
notifications/initialized
    ↓
tools/list
    ↓
tools/call
```

### Debugging

If the server starts but no tools are returned, enable debug logging:

```yaml
logging:
  level:
    org.springframework.ai.mcp: DEBUG
    io.modelcontextprotocol: DEBUG
```

Useful endpoints:

```text
GET  /sse
POST /mcp/message?sessionId=<session-id>
```

The embedded MCP server registers both CRUD-oriented data tools and higher-level AI assistant tools. Any MCP-compatible client (for example MCP Inspector, Claude Desktop with MCP support, or a custom Java client) can discover and invoke them.


## Open API Documentation (in JSON)

```sh
curl localhost:8888/api-docs
```

![Open API Screen Capture](open-api-sc.JPG)

### Example API Calls

#### JSON (HTTP/1.1)

```sh
curl --header "accept: application/json" localhost:8888/v1/issue_requesters/1
```

#### XML (HTTP/1.1)

```sh
curl --header "accept: application/xml" localhost:8888/v1/issue_requesters/1
```

#### gRPC / Binary (HTTP/2)

```sh
curl localhost:8888/v1/issue_requesters/1
```

### License

The MIT License (MIT)
Copyright © 2025

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

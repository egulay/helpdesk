# Dummy Helpdesk API

### Used Technologies
* [Spring Cloud](https://spring.io/projects/spring-cloud)
* [Google Protocol Buffers](https://developers.google.com/protocol-buffers/)
* [Test Containers (MySQL)](https://www.testcontainers.org/modules/databases/)

### Installation
Standard build requires up-to-date [Docker](https://www.docker.com/products/docker-desktop/) to execute all integration tests.

#### Maven - Clean Build included integration test executions
```sh
   mvn clean install
```
#### Maven - Class generation from proto files
```sh
   mvn protobuf:compile
```
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




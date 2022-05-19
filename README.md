# Dummy Helpdesk API

### Used Technologies
* [Spring Cloud](https://spring.io/projects/spring-cloud)
* [Google Protocol Buffers](https://developers.google.com/protocol-buffers/)
* [Test Containers (MySQL)](https://www.testcontainers.org/modules/databases/)

### Installation & Execution
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

### License
The MIT License (MIT)
Copyright © 2022

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.




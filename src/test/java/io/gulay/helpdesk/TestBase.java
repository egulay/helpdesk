package io.gulay.helpdesk;

import io.gulay.helpdesk.data.service.IssueRequestService;
import io.gulay.helpdesk.data.service.IssueRequesterService;
import io.gulay.helpdesk.data.service.IssueResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;


@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(
        classes = HelpdeskApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
@Testcontainers
public abstract class TestBase {
    private static final String IMAGE_VERSION = "mysql:8.0";

    @Container
    private static final MySQLContainer container =
            new MySQLContainer(IMAGE_VERSION)
                    .withUsername("test_user")
                    .withPassword("test_password")
                    .withDatabaseName("help_desk");

    @LocalServerPort
    public int port;

    @Autowired
    protected WebClient webClient;

    @Autowired
    protected WebClient.Builder webClientBuilder;

    @Autowired
    public IssueRequesterService issueRequesterService;

    @Autowired
    public IssueRequestService issueRequestService;

    @Autowired
    public IssueResponseService issueResponseService;


    @DynamicPropertySource
    public static void overrideContainerProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.datasource.url", container::getJdbcUrl);
        dynamicPropertyRegistry.add("spring.datasource.username", container::getUsername);
        dynamicPropertyRegistry.add("spring.datasource.password", container::getPassword);
    }
}
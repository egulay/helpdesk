package com.helpdesk;

import com.helpdesk.data.service.IssueRequestService;
import com.helpdesk.data.service.IssueRequesterService;
import com.helpdesk.data.service.IssueResponseService;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@SuppressWarnings("rawtypes")
@DirtiesContext
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = HelpdeskApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@Ignore
public abstract class TestBase {
    private static final MySQLContainer container;
    private static final String IMAGE_VERSION = "mysql:8.0";

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

    static {
        container = new MySQLContainer<>(IMAGE_VERSION)
                .withUsername("test_user")
                .withPassword("test_password")
                .withInitScript("ddl.sql")
                .withDatabaseName("help_desk");
        container.start();
    }

    @DynamicPropertySource
    public static void overrideContainerProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.datasource.url", container::getJdbcUrl);
        dynamicPropertyRegistry.add("spring.datasource.username", container::getUsername);
        dynamicPropertyRegistry.add("spring.datasource.password", container::getPassword);
    }
}


package com.angular.backend;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ActiveProfiles("test")
@Testcontainers
public abstract class AbstractMongoIntegrationTest {

    @ServiceConnection
    @Container
    public static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0.23-jammy")
            .withReuse(true);

}

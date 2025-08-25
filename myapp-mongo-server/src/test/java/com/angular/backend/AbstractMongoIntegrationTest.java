package com.angular.backend;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@ActiveProfiles("test")
@ContextConfiguration(initializers = MongoContainerInitializer.class)
public abstract class AbstractMongoIntegrationTest {
    // The container lifecycle and property setting are now handled by MongoContainerInitializer.
    // This ensures a single container is shared across all test classes, regardless of
    // their Spring context configuration.
}

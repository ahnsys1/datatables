package cz.example.airag.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AsyncConfiguration {

    @Bean(destroyMethod = "shutdown")
    ExecutorService documentIndexExecutor() {
        return Executors.newSingleThreadExecutor();
    }
}

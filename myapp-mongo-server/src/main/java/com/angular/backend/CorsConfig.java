package com.angular.backend;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        return new CorsConfigurationSource() {
            @Override
            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                CorsConfiguration configuration = new CorsConfiguration();
                List<String> allowedOrigins = Arrays.asList("http://localhost:4200", "http://localhost"); // Replace with your Angular app's origin or origins
                String origin = request.getHeader("Origin");
                if (allowedOrigins.contains(origin)) {
                    configuration.setAllowedOrigins(Arrays.asList(origin));
                } else {
                    return null; // Or handle the invalid origin as needed
                }
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Allowed methods
                configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With")); // Allowed headers
                configuration.setAllowCredentials(true); // Allow sending credentials (cookies, authorization headers)
                return configuration;
            }
        };
    }

}

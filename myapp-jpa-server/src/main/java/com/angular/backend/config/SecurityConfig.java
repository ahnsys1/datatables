package com.angular.backend.config;

import static org.springframework.security.config.Customizer.withDefaults;

import java.io.IOException;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults()) // Enable CORS with default settings
                .csrf(AbstractHttpConfigurer::disable)
                //.headers(headers -> headers.frameOptions(AbstractHttpConfigurer::disable))
                .authorizeHttpRequests((authz) -> authz
                .anyRequest().permitAll()
                );
        return http.build();
    }

    @Bean
    public FilterRegistrationBean<Filter> referrerPolicyFilter() {
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new Filter() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
                HttpServletResponse httpResp = (HttpServletResponse) response;
                // Set to 'no-referrer' or remove the header as needed
                httpResp.setHeader("Referrer-Policy", "no-referrer");
                chain.doFilter(request, response);
            }
        });
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }
}

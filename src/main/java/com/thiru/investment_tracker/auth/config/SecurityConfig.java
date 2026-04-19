package com.thiru.investment_tracker.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record SecurityConfig(
        String keySecret
) {}
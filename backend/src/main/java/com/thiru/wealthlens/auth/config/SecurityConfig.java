package com.thiru.wealthlens.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record SecurityConfig(
        String keySecret
) {}

package com.thiru.wealthlens;

import com.thiru.wealthlens.auth.config.SecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableMethodSecurity
@EnableTransactionManagement
@EnableConfigurationProperties(SecurityConfig.class)
@SpringBootApplication
public class WealthLensApplication {

	public static void main(String[] args) {
		SpringApplication.run(WealthLensApplication.class, args);
	}
}

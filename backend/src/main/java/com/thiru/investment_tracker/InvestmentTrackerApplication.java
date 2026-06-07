package com.thiru.investment_tracker;

import com.thiru.investment_tracker.auth.config.SecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableMethodSecurity
@EnableTransactionManagement
@EnableConfigurationProperties(SecurityConfig.class)
@SpringBootApplication
public class InvestmentTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(InvestmentTrackerApplication.class, args);
	}
}

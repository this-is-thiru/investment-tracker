package com.thiru.wealthlens;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableMethodSecurity
@EnableTransactionManagement
@ConfigurationPropertiesScan("com.thiru.wealthlens")
@SpringBootApplication
public class WealthLensApplication {

	public static void main(String[] args) {
		SpringApplication.run(WealthLensApplication.class, args);
	}
}

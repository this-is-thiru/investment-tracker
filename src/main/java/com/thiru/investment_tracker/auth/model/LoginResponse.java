package com.thiru.investment_tracker.auth.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(staticName = "from")
public class LoginResponse {
	private String accessToken;
	private final String tokenType = "Bearer";
}

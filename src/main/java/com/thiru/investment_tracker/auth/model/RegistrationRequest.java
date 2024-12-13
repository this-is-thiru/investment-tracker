package com.thiru.investment_tracker.auth.model;

import lombok.Data;

@Data
public class RegistrationRequest {
	private String email;
	private String password;
	private AuthHelper.Role role;
}

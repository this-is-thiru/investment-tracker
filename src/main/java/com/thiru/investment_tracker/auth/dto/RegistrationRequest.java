package com.thiru.investment_tracker.auth.dto;

import lombok.Data;

@Data
public class RegistrationRequest {
	private String email;
	private String password;
	private String newPassword;
	private AuthHelper.Role role;
}

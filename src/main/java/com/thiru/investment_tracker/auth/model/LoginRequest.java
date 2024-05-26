package com.thiru.investment_tracker.auth.model;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}

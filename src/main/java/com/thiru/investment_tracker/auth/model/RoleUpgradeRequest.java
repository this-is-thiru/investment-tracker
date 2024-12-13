package com.thiru.investment_tracker.auth.model;

import lombok.Data;

import java.util.List;

@Data
public class RoleUpgradeRequest {
    private String email;
    private List<AuthHelper.Role> role;
}

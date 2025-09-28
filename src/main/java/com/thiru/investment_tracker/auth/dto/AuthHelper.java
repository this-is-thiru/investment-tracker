package com.thiru.investment_tracker.auth.dto;

import com.thiru.investment_tracker.core.util.collection.TObjectMapper;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AuthHelper {
    private static final String ROLES = "roles";

    public enum Role {
        ROLE_SUPER_USER,
        ROLE_ADMIN,
        ROLE_MANAGER,
        ROLE_EDITOR,
        ROLE_AUTHOR,
        ROLE_MODERATOR,
        ROLE_USER,
        ROLE_GUEST
    }

    private static final Map<Role, List<Role>> roleHierarchy = new HashMap<>();

    static {
        roleHierarchy.put(Role.ROLE_SUPER_USER, List.of(Role.ROLE_ADMIN, Role.ROLE_MANAGER, Role.ROLE_EDITOR, Role.ROLE_AUTHOR, Role.ROLE_MODERATOR, Role.ROLE_USER, Role.ROLE_GUEST));
        roleHierarchy.put(Role.ROLE_ADMIN, List.of(Role.ROLE_MANAGER, Role.ROLE_EDITOR, Role.ROLE_AUTHOR, Role.ROLE_MODERATOR, Role.ROLE_USER, Role.ROLE_GUEST));
        roleHierarchy.put(Role.ROLE_MANAGER, List.of(Role.ROLE_EDITOR, Role.ROLE_AUTHOR, Role.ROLE_MODERATOR, Role.ROLE_USER, Role.ROLE_GUEST));
        roleHierarchy.put(Role.ROLE_EDITOR, List.of(Role.ROLE_AUTHOR, Role.ROLE_MODERATOR, Role.ROLE_USER, Role.ROLE_GUEST));
    }

    public static boolean canUpgradeRole(Role currentRole, Role newRole) {
        List<Role> hierarchy = roleHierarchy.get(currentRole);
        return hierarchy.contains(newRole);
    }

    public static String getRole(Role role) {
        return role.name();
    }

    public static List<GrantedAuthority> getAuthorities(Claims claims) {

        List<String> roles = TObjectMapper.readAsList(claims.get(ROLES), String.class);
        return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    public static void setRoles(Map<String, Object> claims, Collection<? extends GrantedAuthority> authorities) {

        List<String> roles = authorities.stream().map(GrantedAuthority::getAuthority).toList();
        claims.put(ROLES, roles);
    }
}

package com.thiru.investment_tracker.auth.dto;

import com.thiru.investment_tracker.util.collection.TObjectMapper;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AuthHelper {
    private static final String AUTHORITIES_KEY = "authorities";

    public enum Role {
        SUPER_USER,
        ADMIN,
        MANAGER,
        EDITOR,
        AUTHOR,
        MODERATOR,
        USER,
        GUEST,
        TEST_USER
    }

    private static final Map<Role, List<Role>> roleHierarchy = new HashMap<>();

    static {
        roleHierarchy.put(Role.SUPER_USER, List.of(Role.ADMIN, Role.MANAGER, Role.EDITOR, Role.AUTHOR, Role.MODERATOR, Role.USER, Role.GUEST));
        roleHierarchy.put(Role.ADMIN, List.of(Role.MANAGER, Role.EDITOR, Role.AUTHOR, Role.MODERATOR, Role.USER, Role.GUEST));
        roleHierarchy.put(Role.MANAGER, List.of(Role.EDITOR, Role.AUTHOR, Role.MODERATOR, Role.USER, Role.GUEST));
        roleHierarchy.put(Role.EDITOR, List.of(Role.AUTHOR, Role.MODERATOR, Role.USER, Role.GUEST));
    }

    public static boolean canUpgradeRole(Role currentRole, Role newRole) {
        List<Role> hierarchy = roleHierarchy.get(currentRole);
        return hierarchy.contains(newRole);
    }

    public static String getRole(Role role) {
        return role.name();
    }

    public static List<GrantedAuthority> getAuthorities(Claims claims) {

        List<String> roles = TObjectMapper.readAsList(claims.get(AUTHORITIES_KEY), String.class);
        return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    public static void setRoles(Map<String, Object> claims, Collection<? extends GrantedAuthority> authorities) {

        var userAuthorities = authorities.stream().map(GrantedAuthority::getAuthority).toList();
        claims.put(AUTHORITIES_KEY, userAuthorities);
    }
}

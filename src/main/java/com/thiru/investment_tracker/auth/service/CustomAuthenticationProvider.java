package com.thiru.investment_tracker.auth.service;

import com.thiru.investment_tracker.util.collection.TCollectionUtil;
import com.thiru.investment_tracker.util.collection.TStringUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;


@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;

    public CustomAuthenticationProvider(PasswordEncoder passwordEncoder, UserDetailsService userDetailsService) {
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (UserValidator.isRestrictedUser(userDetails)) {
            throw new BadCredentialsException("Username does not match path variable");
        }

        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        return new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

    private static boolean isDifferentUser(String username, HttpServletRequest request) {
        String email = extractUsernameFromPath(request);
        return email != null && !username.equals(email);
    }

    private static String extractUsernameFromPath(HttpServletRequest request) {
        String path = URLDecoder.decode(request.getRequestURI(), StandardCharsets.UTF_8);
        return extractUsernameFromPath(path);
    }

    private static String extractUsernameFromPath(String path) {
        if (!path.contains("/user/")) {
            return null;
        } else {
            List<String> argumentList = TStringUtil.splitSafeTrimmed(path, "/");
            return TCollectionUtil.getElementSafe(argumentList, argumentList.indexOf("user") + 1);
        }
    }
}
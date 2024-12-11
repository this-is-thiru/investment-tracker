package com.thiru.investment_tracker.auth.filter;

import com.thiru.investment_tracker.auth.service.AuthService;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@Log4j2
public class AuthFilter extends OncePerRequestFilter {

    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    private enum AuthType {
        BEARER,
        BASIC,
        INVALID
    }

    AuthFilter(final AuthService authService, PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) throws ServletException, IOException {
        try {
            final String authHeader = request.getHeader("Authorization");
            AuthType authType = getAuthType(authHeader);
            boolean authenticated = switch (authType) {
                case BEARER:
                    yield handleBearerAuthentication(request, response);
                case BASIC:
                    yield handleBasicAuthentication(request, response);
                default:
                    yield true;
            };
            if (!authenticated) {
                log.error("Authentication failed");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Error in authentication filter {}", e.getMessage());
            filterChain.doFilter(request, response);
        }
    }

    private AuthType getAuthType(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return AuthType.BEARER;
        } else if (authHeader != null && authHeader.startsWith("Basic ")) {
            return AuthType.BASIC;
        }
        return AuthType.INVALID;
    }

    private boolean handleBearerAuthentication(HttpServletRequest request, HttpServletResponse response) {
        final String authHeader = request.getHeader("Authorization");
        final String token = authHeader.substring(7);
        Claims claims = authService.extractAllClaims(token);

        if (claims != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            String username = claims.getSubject();
            final UserDetails userDetails = authService.getUserByUsername(username);
            if (authService.validateToken(claims, userDetails)) {
                final UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } else if (claims == null) {
            log.error("Token expired or invalid");
            // Handle unauthenticated requests
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        return true;
    }

    private boolean handleBasicAuthentication(HttpServletRequest request, HttpServletResponse response) throws Exception {
        final String authHeader = request.getHeader("Authorization");
        String base64Credentials = authHeader.substring("Basic".length()).trim();
        byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
        String credentials = new String(credDecoded, StandardCharsets.UTF_8);
        final String[] values = credentials.split(":", 2);
        String username = values[0];
        String password = values[1];

        final UserDetails userDetails = authService.getUserByUsername(username);
        if (passwordEncoder.matches(password, userDetails.getPassword())) {
            final UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            return true;
        } else {
            log.error("Incorrect username or password");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }
}

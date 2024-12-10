package com.thiru.investment_tracker.auth.filter;

import java.io.IOException;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import io.jsonwebtoken.Claims;

import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.thiru.investment_tracker.auth.service.AuthService;

@Component
@Log4j2
public class AuthFilter extends OncePerRequestFilter {

    private final AuthService authService;

    private enum AuthType {
        BEARER,
        BASIC,
        INVALID
    }

    AuthFilter(final AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) throws ServletException, IOException {
        try {
            final String authHeader = request.getHeader("Authorization");
            AuthType authType = getAuthType(authHeader);
            switch (authType) {
                case BEARER:
                    handleBearerAuthentication(request, response);
                    break;
                case BASIC:
//                    handleBasicAuthentication(request, response);
                    break;
                default:
                    break;
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

    private void handleBearerAuthentication(HttpServletRequest request, HttpServletResponse response) {
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
        }
    }
}

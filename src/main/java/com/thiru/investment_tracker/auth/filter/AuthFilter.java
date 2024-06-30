package com.thiru.investment_tracker.auth.filter;

import java.io.IOException;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import io.jsonwebtoken.Claims;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.thiru.investment_tracker.auth.service.AuthService;

@Component
public class AuthFilter extends OncePerRequestFilter {

	private final AuthService authService;

	AuthFilter(final AuthService authService) {
		this.authService = authService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, @Nonnull HttpServletResponse response,
			@Nonnull FilterChain filterChain) throws ServletException, IOException {
		final String authHeader = request.getHeader("Authorization");
		Claims claims = null;
		String username = null;
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			final String token = authHeader.substring(7);
			claims = authService.extractAllClaims(token);
			username = claims.getSubject();
		}

		if (claims != null && username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			final UserDetails userDetails = authService.getUserByUsername(username);
			if (authService.validateToken(claims, userDetails)) {
				final UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
						userDetails, null, userDetails.getAuthorities());
				authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authToken);
			}
		}
		filterChain.doFilter(request, response);
	}
}

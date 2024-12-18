package com.thiru.investment_tracker.auth.service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.thiru.investment_tracker.auth.model.LoginResponse;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.thiru.investment_tracker.auth.entity.UserDetail;
import com.thiru.investment_tracker.auth.model.RegistrationRequest;
import com.thiru.investment_tracker.auth.repository.UserDetailsRepository;

import lombok.AllArgsConstructor;

@Log4j2
@Service
@AllArgsConstructor
public class AuthService {

    private final UserDetailsRepository userDetailsRepo;
    private final PasswordEncoder passwordEncoder;

    public String addUser(RegistrationRequest request) {

        Optional<UserDetail> optionalUserDetails = userDetailsRepo.findById(request.getEmail());
        if (optionalUserDetails.isPresent()) {
            throw new IllegalArgumentException("User with email " + request.getEmail() + " already exists");
        }

        UserDetail userEntity = new UserDetail();
        userEntity.setEmail(request.getEmail());
        userEntity.setPassword(passwordEncoder.encode(request.getPassword()));
        userEntity.setRoles(request.getRole().name());
        userDetailsRepo.save(userEntity);
        return "user with username " + request.getEmail() + " added to system ";
    }


    /**
     * This needs to be re-visited
     * This method upgrades the role of a user in the system
     * @param request The object containing the email and the role to be upgraded
     * @return a success message
     * @throws IllegalArgumentException if the user already exists
     */
    public String upgradeRole(RegistrationRequest request) {

        Optional<UserDetail> optionalUserDetails = userDetailsRepo.findById(request.getEmail());
        if (optionalUserDetails.isPresent()) {
            throw new IllegalArgumentException("User with email " + request.getEmail() + " already exists");
        }

        UserDetail userEntity = new UserDetail();
        userEntity.setEmail(request.getEmail());
        userEntity.setPassword(passwordEncoder.encode(request.getPassword()));
        userEntity.setRoles(request.getRole().name());
        userDetailsRepo.save(userEntity);
        return "user with username " + request.getEmail() + " added to system";
    }

    /**
     * <a href="https://www.grc.com/passwords.htm">Token Website</a>
     */
    final private static String SECRET = "80DC002A54B59ACF5F198D0A8D644EEE992C04FFBCF947DAAA90AA7DFDDA2A05";

    public Claims extractAllClaims(String authToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(authToken).getBody();
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Invalid JWT token");
        }

        return null;
    }

    public Boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    public Boolean validateToken(Claims claims) {
        return !isTokenExpired(claims);
    }

    public LoginResponse generateToken(String username, Authentication authentication) {
        return createToken(username, authentication);
    }

    private LoginResponse createToken(String username, Authentication authentication) {
        int expirationTime = 60 * 30;
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
        Date expiration = new Date(System.currentTimeMillis() + 1000 * expirationTime);

        String token = Jwts.builder().setClaims(claims).setSubject(username).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expiration)
                .signWith(getSignKey(), SignatureAlgorithm.HS256).compact();
        return LoginResponse.from(token, expirationTime);
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
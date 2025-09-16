package com.thiru.investment_tracker.auth.service;

import com.thiru.investment_tracker.auth.entity.UserDetail;
import com.thiru.investment_tracker.auth.dto.AuthHelper;
import com.thiru.investment_tracker.auth.dto.LoginResponse;
import com.thiru.investment_tracker.auth.dto.RegistrationRequest;
import com.thiru.investment_tracker.auth.repository.UserDetailsRepository;
import io.jsonwebtoken.*;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.*;

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
     *
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
            return Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(authToken)
                    .getPayload();
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

    public String changePassword(String email, RegistrationRequest request) {

        if (!Objects.equals(email, request.getEmail())) {
            throw new IllegalArgumentException("Email mismatch");
        }

        if (Objects.equals(request.getPassword(), request.getNewPassword())) {
            throw new IllegalArgumentException("Old password cannot be same as new password");
        }

        Optional<UserDetail> optionalUserDetails = userDetailsRepo.findById(email);
        if (optionalUserDetails.isEmpty()) {
            throw new UsernameNotFoundException("User with email " + email + " not found");
        }

        UserDetail userEntity = optionalUserDetails.get();
        if (!passwordEncoder.matches(request.getPassword(), userEntity.getPassword())) {
            throw new IllegalArgumentException("Invalid old password");
        }
        userEntity.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userDetailsRepo.save(userEntity);
        return email + "'s password changed successfully";
    }

    public LoginResponse generateToken(String username, Authentication authentication) {
        return createToken(username, authentication);
    }

    private LoginResponse createToken(String username, Authentication authentication) {
        int expirationTime = 60 * 30;
        Map<String, Object> claims = new HashMap<>();
        AuthHelper.setRoles(claims, authentication.getAuthorities());
        Date expiration = new Date(System.currentTimeMillis() + 1000 * expirationTime);

        String token = Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(expiration)
                .signWith(getSignInKey())
                .compact();

        return LoginResponse.from(token, expirationTime);
    }

    private SecretKey getSignInKey() {
        byte[] bytes = Base64.getDecoder()
                .decode(SECRET);
        return new SecretKeySpec(bytes, "HmacSHA256");
    }
}